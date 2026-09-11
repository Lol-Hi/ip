package luckynoslacky.luckyparser;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import luckynoslacky.luckyexception.LuckyNoInputException;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Parses the flexible date and time text accepted by the chatbot.
 *
 * <p>The parser deliberately keeps the accepted formats explicit rather than
 * relying on locale-dependent parsing. This makes the same input behave the
 * same way on different computers.</p>
 */
public final class DateTimeParser {
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "^(\\d{1,2})(?:(:|\\.)(\\d{2}))?(?::(\\d{2}))?\\s*(am|pm)?$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern DAY_ONLY_PATTERN = Pattern.compile(
            "^(\\d{1,2})$");
    private static final Pattern YEAR_ONLY_PATTERN = Pattern.compile(
            "^(\\d{4})$");
    private static final Pattern MONTH_DAY_PATTERN = Pattern.compile(
            "^([A-Za-z]+)\\s+(\\d{1,2})(?:\\s+(\\d{4}))?$");
    private static final Pattern DAY_MONTH_PATTERN = Pattern.compile(
            "^(\\d{1,2})\\s+([A-Za-z]+)(?:\\s+(\\d{4}))?$");

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            formatter("uuuu-MM-dd"),
            formatter("uuuu/MM/dd"),
            formatter("d/M/uuuu"),
            formatter("d-M-uuuu"),
            formatter("d MMM uuuu"),
            formatter("d MMMM uuuu"),
            formatter("MMM d uuuu"),
            formatter("MMMM d uuuu"));
    private static final DateTimeFormatter CSV_FORMATTER =
            formatter("uuuu-MM-dd HH:mm");

    /** Associates accepted weekday names with their java.time values. */
    private enum Weekday {
        MONDAY("monday", DayOfWeek.MONDAY),
        TUESDAY("tuesday", DayOfWeek.TUESDAY),
        WEDNESDAY("wednesday", DayOfWeek.WEDNESDAY),
        THURSDAY("thursday", DayOfWeek.THURSDAY),
        FRIDAY("friday", DayOfWeek.FRIDAY),
        SATURDAY("saturday", DayOfWeek.SATURDAY),
        SUNDAY("sunday", DayOfWeek.SUNDAY);

        private final String fullName;
        private final DayOfWeek dayOfWeek;

        Weekday(String fullName, DayOfWeek dayOfWeek) {
            this.fullName = fullName;
            this.dayOfWeek = dayOfWeek;
        }

        private boolean matches(String value) {
            return value.equals(fullName)
                    || value.equals(fullName.substring(0, 3));
        }
    }

    /** Associates accepted month names with their one-based month numbers. */
    private enum Month {
        JANUARY("january", 1),
        FEBRUARY("february", 2),
        MARCH("march", 3),
        APRIL("april", 4),
        MAY("may", 5),
        JUNE("june", 6),
        JULY("july", 7),
        AUGUST("august", 8),
        SEPTEMBER("september", 9),
        OCTOBER("october", 10),
        NOVEMBER("november", 11),
        DECEMBER("december", 12);

        private final String fullName;
        private final int number;

        Month(String fullName, int number) {
            this.fullName = fullName;
            this.number = number;
        }

        private boolean matches(String value) {
            return value.equals(fullName)
                    || value.equals(fullName.substring(0, 3));
        }
    }

    private final Clock clock;

    /** Creates a parser using the system's default time zone and clock. */
    public DateTimeParser() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates a parser using a supplied clock, which is useful for deterministic
     * tests.
     *
     * @param clock clock used to resolve relative dates
     */
    public DateTimeParser(Clock clock) {
        if (clock == null) {
            throw new IllegalArgumentException("Clock cannot be null.");
        }
        this.clock = clock;
    }

    /**
     * Represents a parsed date/time together with whether its date was omitted.
     *
     * @param dateTime parsed date and time
     * @param timeOnly true when the input contained no date
     */
    public record ParsedDateTime(LocalDateTime dateTime, boolean timeOnly) {
    }

    /**
     * Parses date/time text used as an event start.
     *
     * @param dateTimeText date and time text to parse
     * @return parsed date and time
     * @throws LuckyNoInputException if the input is not a supported date/time
     */
    public ParsedDateTime parseStartDateTime(String dateTimeText)
            throws LuckyNoInputException {
        return parse(dateTimeText, LocalTime.MIN);
    }

    /**
     * Parses date/time text used as an event end or deadline.
     *
     * @param dateTimeText date and time text to parse
     * @return parsed date and time
     * @throws LuckyNoInputException if the input is not a supported date/time
     */
    public ParsedDateTime parseEndDateTime(String dateTimeText)
            throws LuckyNoInputException {
        return parse(dateTimeText, LocalTime.of(23, 59));
    }

    /**
     * Parses an event end time relative to its start date and time.
     *
     * <p>An explicitly supplied end date is preserved. When the end input
     * contains only a time, the reference date is used unless that time has
     * already passed, in which case the next date is used.</p>
     *
     * @param dateTimeText event end date/time text
     * @param referenceDateTime event start date and time
     * @return parsed event end date and time
     * @throws LuckyNoInputException if the input is invalid
     */
    public ParsedDateTime parseEndDateTime(
            String dateTimeText,
            LocalDateTime referenceDateTime)
            throws LuckyNoInputException {
        if (referenceDateTime == null) {
            throw new IllegalArgumentException(
                    "Reference date and time cannot be null.");
        }

        ParsedDateTime parsed = parse(dateTimeText, LocalTime.of(23, 59));
        if (!parsed.timeOnly()) {
            return parsed;
        }

        LocalTime endTime = parsed.dateTime().toLocalTime();
        LocalDate endDate = referenceDateTime.toLocalDate();
        if (endTime.isBefore(referenceDateTime.toLocalTime())) {
            endDate = endDate.plusDays(1);
        }

        return new ParsedDateTime(
                LocalDateTime.of(endDate, endTime),
                true);
    }

    /**
     * Returns the current local date and time for this parser's clock.
     *
     * @return current local date and time
     */
    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    /**
     * Formats a date/time for the CSV storage format.
     *
     * @param dateTime date and time to format, or null for an empty field
     * @return formatted storage text
     */
    public static String formatForStorage(LocalDateTime dateTime) {
        return dateTime == null ? "" : CSV_FORMATTER.format(dateTime);
    }

    /**
     * Parses stored date/time text from the CSV storage format.
     *
     * @param storedDateTimeText stored date and time, or blank for no date
     *                           and time
     * @return parsed date and time, or null for blank stored text
     */
    public static LocalDateTime parseFromStorage(String storedDateTimeText) {
        return storedDateTimeText == null || storedDateTimeText.isBlank()
                ? null
                : LocalDateTime.parse(storedDateTimeText, CSV_FORMATTER);
    }

    /**
     * Parses date/time text and applies the default time used when only a date
     * is given.
     *
     * <p>The parsing hierarchy is intentionally layered: this method parses
     * the input shape, resolution methods apply calendar rules, and lookup
     * methods find matching formats or named date parts.</p>
     *
     * @param dateTimeText date/time text to parse
     * @param dateOnlyDefault time to use for date-only input
     * @return parsed date and time
     * @throws LuckyNoInputException if the input is invalid
     */
    private ParsedDateTime parse(String dateTimeText, LocalTime dateOnlyDefault)
            throws LuckyNoInputException {
        String normalizedDateTimeText = normalizeDateTimeText(dateTimeText);
        if (normalizedDateTimeText.isEmpty()) {
            throw invalidDateTime();
        }

        LocalDateTime currentDateTime = now();
        LocalDate today = currentDateTime.toLocalDate();

        ParsedDateTime yearOnly = parseYearOnly(
                normalizedDateTimeText, today, dateOnlyDefault);
        if (yearOnly != null) {
            return yearOnly;
        }

        ParsedDateTime timeOnly = parseTimeOnly(
                normalizedDateTimeText, currentDateTime, today);
        if (timeOnly != null) {
            return timeOnly;
        }

        ParsedDateTime dateAndTime = parseDateTime(
                normalizedDateTimeText, today);
        if (dateAndTime != null) {
            return dateAndTime;
        }

        return parseDateOnly(normalizedDateTimeText, today, dateOnlyDefault);
    }

    /**
     * Parses an input containing only a four-digit year.
     *
     * @param normalizedDateTimeText normalized input text
     * @param today current date used for resolving relative expressions
     * @param dateOnlyDefault default time for date-only input
     * @return parsed year-only input, or null when the input is not year-only
     * @throws LuckyNoInputException if the year cannot be resolved
     */
    private ParsedDateTime parseYearOnly(
            String normalizedDateTimeText,
            LocalDate today,
            LocalTime dateOnlyDefault)
            throws LuckyNoInputException {
        if (!YEAR_ONLY_PATTERN.matcher(normalizedDateTimeText).matches()) {
            return null;
        }

        LocalDate date = resolveDateText(normalizedDateTimeText, today);
        return new ParsedDateTime(LocalDateTime.of(date, dateOnlyDefault), false);
    }

    /**
     * Parses an input containing only a time and resolves it to today or tomorrow.
     *
     * @param normalizedDateTimeText normalized input text
     * @param currentDateTime current date and time
     * @param today current date
     * @return parsed time-only input, or null when the input is not time-only
     */
    private ParsedDateTime parseTimeOnly(
            String normalizedDateTimeText,
            LocalDateTime currentDateTime,
            LocalDate today) {
        LocalTime parsedTime = resolveClockTime(normalizedDateTimeText);
        if (parsedTime == null) {
            return null;
        }

        LocalDate resolvedDate = parsedTime.isBefore(currentDateTime.toLocalTime())
                ? today.plusDays(1)
                : today;
        return new ParsedDateTime(LocalDateTime.of(resolvedDate, parsedTime), true);
    }

    /**
     * Parses an input containing both a date and a time.
     *
     * @param normalizedDateTimeText normalized input text
     * @param today current date used for resolving incomplete dates
     * @return parsed date/time input, or null when no date/time split is found
     * @throws LuckyNoInputException if the date portion is invalid
     */
    private ParsedDateTime parseDateTime(
            String normalizedDateTimeText, LocalDate today)
            throws LuckyNoInputException {
        int dateTimeSplitIndex = findTimeBoundary(normalizedDateTimeText);
        if (dateTimeSplitIndex <= 0) {
            return null;
        }

        String dateText = normalizedDateTimeText
                .substring(0, dateTimeSplitIndex).trim();
        String timeText = normalizedDateTimeText
                .substring(dateTimeSplitIndex).trim();
        LocalTime parsedTime = resolveClockTime(timeText);
        if (parsedTime == null) {
            return null;
        }

        LocalDate date = resolveDateText(dateText, today);
        return new ParsedDateTime(LocalDateTime.of(date, parsedTime), false);
    }

    /**
     * Parses an input containing a date without an explicit time.
     *
     * @param normalizedDateTimeText normalized input text
     * @param today current date used for resolving incomplete dates
     * @param dateOnlyDefault default time for the parsed date
     * @return parsed date-only input
     * @throws LuckyNoInputException if the date is invalid
     */
    private ParsedDateTime parseDateOnly(
            String normalizedDateTimeText,
            LocalDate today,
            LocalTime dateOnlyDefault)
            throws LuckyNoInputException {
        LocalDate date = resolveDateText(normalizedDateTimeText, today);
        return new ParsedDateTime(LocalDateTime.of(date, dateOnlyDefault), false);
    }

    /**
     * Finds the boundary between a date expression and its time expression.
     *
     * @param normalizedDateTimeText normalized date/time expression
     * @return index at which the time expression starts, or -1 if not found
     */
    private static int findTimeBoundary(String normalizedDateTimeText) {
        for (int splitIndex = normalizedDateTimeText.length() - 1;
                splitIndex > 0; splitIndex--) {
            if (Character.isWhitespace(normalizedDateTimeText.charAt(splitIndex - 1))) {
                String timeText = normalizedDateTimeText.substring(splitIndex).trim();
                if (resolveClockTime(timeText) != null) {
                    if (isValidDateYearPair(
                            normalizedDateTimeText.substring(0, splitIndex), timeText)) {
                        continue;
                    }
                    return splitIndex;
                }
            }
        }
        return -1;
    }

    /**
     * Resolves a clock expression in separated, meridiem, or compact HHMM
     * notation.
     *
     * @param timeText time text to parse
     * @return parsed time, or null if the text is not a valid time
     */
    private static LocalTime resolveClockTime(String timeText) {
        String normalizedTimeText = timeText.trim()
                .replaceAll("(?i)a\\.m\\.", "am")
                .replaceAll("(?i)p\\.m\\.", "pm");
        Matcher matcher = TIME_PATTERN.matcher(normalizedTimeText);
        if (!matcher.matches()) {
            return readCompactTime(normalizedTimeText);
        }

        int hour = Integer.parseInt(matcher.group(1));
        String minuteText = matcher.group(3);
        String secondText = matcher.group(4);
        String meridiem = matcher.group(5);

        if (minuteText == null && meridiem == null) {
            return null;
        }

        int minute = minuteText == null ? 0 : Integer.parseInt(minuteText);
        int second = secondText == null ? 0 : Integer.parseInt(secondText);
        if (meridiem != null) {
            if (hour < 1 || hour > 12) {
                return null;
            }
            if (meridiem.equalsIgnoreCase("pm") && hour < 12) {
                hour += 12;
            } else if (meridiem.equalsIgnoreCase("am") && hour == 12) {
                hour = 0;
            }
        } else if (hour > 23) {
            return null;
        }

        try {
            return LocalTime.of(hour, minute, second);
        } catch (DateTimeException exception) {
            return null;
        }
    }

    /**
     * Reads a four-digit time in HHMM notation.
     *
     * @param compactTimeText compact time text
     * @return parsed time, or null if the text is not a valid HHMM value
     */
    private static LocalTime readCompactTime(String compactTimeText) {
        if (!compactTimeText.matches("\\d{4}")) {
            return null;
        }

        int hour = Integer.parseInt(compactTimeText.substring(0, 2));
        int minute = Integer.parseInt(compactTimeText.substring(2, 4));
        if (hour > 23 || minute > 59) {
            return null;
        }

        return LocalTime.of(hour, minute);
    }

    /**
     * Checks whether a candidate date and year form a valid date.
     *
     * @param dateText date portion of the candidate
     * @param yearText year or compact-time portion of the candidate
     * @return true if the candidate can be parsed as a date
     */
    private static boolean isValidDateYearPair(String dateText, String yearText) {
        String normalizedDateText = removeWeekdayPrefix(
                normalizeDateText(dateText));
        String candidateDateText = normalizedDateText + " " + yearText;
        return findKnownFormat(candidateDateText) != null;
    }

    /**
     * Resolves a date expression and its relative terms against today.
     *
     * @param dateText date text to parse
     * @param today current date used for relative resolution
     * @return resolved date
     * @throws LuckyNoInputException if the date is invalid
     */
    private LocalDate resolveDateText(String dateText, LocalDate today)
            throws LuckyNoInputException {
        String normalizedDateText = normalizeDateText(dateText);
        Prefix prefix = extractRelativePrefix(normalizedDateText);
        String dateExpression = prefix.remainder();

        LocalDate relativeDate = tryRelativeDate(dateExpression, today, prefix);
        if (relativeDate != null) {
            return relativeDate;
        }

        String dateWithoutWeekday = removeWeekdayPrefix(dateExpression);
        return tryCalendarDate(dateWithoutWeekday, today, prefix);
    }

    /**
     * Attempts to resolve a date expression that uses a relative keyword.
     *
     * @param dateExpression date expression after prefix extraction
     * @param today current date used for relative resolution
     * @param prefix relative-date prefix
     * @return resolved relative date, or null when the expression is absolute
     */
    private static LocalDate tryRelativeDate(
            String dateExpression, LocalDate today, Prefix prefix) {
        DayOfWeek weekday = findWeekday(dateExpression);
        if (weekday != null) {
            return calculateWeekdayDate(weekday, today, prefix);
        }

        if (!prefix.modifier().equals("none")) {
            return null;
        }

        return switch (dateExpression) {
            case "today" -> today;
            case "tomorrow", "tmr" -> today.plusDays(1);
            case "yesterday", "ytd" -> today.minusDays(1);
            default -> null;
        };
    }

    /**
     * Attempts to resolve a calendar date expression that is not a weekday.
     *
     * @param dateExpression date expression without a weekday prefix
     * @param today current date used for resolving incomplete dates
     * @param prefix relative-date prefix
     * @return resolved date
     * @throws LuckyNoInputException if the expression is invalid
     */
    private static LocalDate tryCalendarDate(
            String dateExpression, LocalDate today, Prefix prefix)
            throws LuckyNoInputException {
        if (dateExpression.equals("month")) {
            return resolveRelativeMonth(today, prefix);
        }
        if (dateExpression.equals("year")) {
            return resolveRelativeYear(today, prefix);
        }

        Matcher yearMatcher = YEAR_ONLY_PATTERN.matcher(dateExpression);
        if (yearMatcher.matches()) {
            return LocalDate.of(Integer.parseInt(yearMatcher.group(1)), 1, 1);
        }

        LocalDate formattedDate = findKnownFormat(dateExpression);
        if (formattedDate != null) {
            return formattedDate;
        }

        Matcher monthDayMatcher = MONTH_DAY_PATTERN.matcher(dateExpression);
        if (monthDayMatcher.matches()) {
            return resolveMonthDay(
                    monthDayMatcher.group(1),
                    Integer.parseInt(monthDayMatcher.group(2)),
                    monthDayMatcher.group(3),
                    today,
                    prefix);
        }

        Matcher dayMonthMatcher = DAY_MONTH_PATTERN.matcher(dateExpression);
        if (dayMonthMatcher.matches()) {
            return resolveMonthDay(
                    dayMonthMatcher.group(2),
                    Integer.parseInt(dayMonthMatcher.group(1)),
                    dayMonthMatcher.group(3),
                    today,
                    prefix);
        }

        Matcher dayMatcher = DAY_ONLY_PATTERN.matcher(dateExpression);
        if (dayMatcher.matches()) {
            return resolveDayOfMonth(
                    Integer.parseInt(dayMatcher.group(1)), today, prefix);
        }

        int monthNumber = findMonthNumber(dateExpression);
        if (monthNumber > 0) {
            return resolveNamedMonth(monthNumber, today, prefix);
        }

        throw invalidDateTime();
    }

    /**
     * Removes an optional weekday prefix from a date expression.
     *
     * @param dateExpression date expression that may begin with a weekday
     * @return expression without the weekday prefix
     */
    private static String removeWeekdayPrefix(String dateExpression) {
        return dateExpression.replaceFirst(
                "^(monday|tuesday|wednesday|thursday|friday|saturday|sunday"
                        + "|mon|tue|wed|thu|fri|sat|sun)\\s+",
                "");
    }

    /**
     * Finds the first explicitly supported absolute date format that matches.
     *
     * @param normalizedDateText normalized date text
     * @return parsed date, or null if no formatter accepts the text
     */
    private static LocalDate findKnownFormat(String normalizedDateText) {
        return DATE_FORMATTERS.stream()
                .map(formatter -> applyFormatter(normalizedDateText, formatter))
                .flatMap(Optional::stream)
                .filter(date -> date.getYear() > 0)
                .findFirst()
                .orElse(null);
    }

    /**
     * Applies one formatter to the normalized date text.
     *
     * @param normalizedDateText normalized date text to parse
     * @param formatter formatter to apply
     * @return parsed date, or an empty Optional if parsing fails
     */
    private static Optional<LocalDate> applyFormatter(
            String normalizedDateText, DateTimeFormatter formatter) {
        try {
            return Optional.of(LocalDate.parse(normalizedDateText, formatter));
        } catch (DateTimeParseException exception) {
            return Optional.empty();
        }
    }

    /**
     * Calculates a calendar date from a weekday and its relative prefix.
     *
     * @param weekday requested day of the week
     * @param today current date
     * @param prefix relative-date prefix
     * @return resolved weekday date
     */
    private static LocalDate calculateWeekdayDate(
            DayOfWeek weekday, LocalDate today, Prefix prefix) {
        LocalDate currentWeekStart = today.with(DayOfWeek.MONDAY);
        int weekdayOffset = weekday.getValue() % 7;

        if (prefix.modifier().equals("this")) {
            int daysFromMonday = (weekday.getValue()
                    - DayOfWeek.MONDAY.getValue() + 7) % 7;
            return currentWeekStart.plusDays(daysFromMonday);
        }

        if (prefix.modifier().equals("next")
                || prefix.modifier().equals("following")) {
            return currentWeekStart
                    .plusDays(6)
                    .plusWeeks(prefix.count() - 1L)
                    .plusDays(weekdayOffset);
        }

        int daysUntil = (weekday.getValue() - today.getDayOfWeek().getValue() + 7) % 7;
        if (daysUntil == 0) {
            daysUntil = 7;
        }
        return today.plusDays(daysUntil);
    }

    /**
     * Resolves a relative reference to a month.
     *
     * @param today current date
     * @param prefix relative-date prefix
     * @return first day of the resolved month
     */
    private static LocalDate resolveRelativeMonth(
            LocalDate today, Prefix prefix) {
        int monthOffset = switch (prefix.modifier()) {
            case "next", "following" -> prefix.count();
            case "coming" -> 1;
            default -> 0;
        };
        return today.withDayOfMonth(1).plusMonths(monthOffset);
    }

    /**
     * Resolves a named month to its next applicable year.
     *
     * @param monthNumber month number
     * @param today current date
     * @param prefix relative-date prefix
     * @return first day of the resolved month
     */
    private static LocalDate resolveNamedMonth(
            int monthNumber, LocalDate today, Prefix prefix) {
        int yearOffset = switch (prefix.modifier()) {
            case "next", "following" -> prefix.count();
            case "coming" -> 1;
            default -> 0;
        };
        LocalDate candidateDate = LocalDate.of(today.getYear(), monthNumber, 1)
                .plusYears(yearOffset);
        if (prefix.modifier().equals("none") && candidateDate.isBefore(today)) {
            candidateDate = candidateDate.plusYears(1);
        }
        return candidateDate;
    }

    /**
     * Resolves a relative reference to a year.
     *
     * @param today current date
     * @param prefix relative-date prefix
     * @return first day of the resolved year
     */
    private static LocalDate resolveRelativeYear(
            LocalDate today, Prefix prefix) {
        int yearOffset = switch (prefix.modifier()) {
            case "next", "following" -> prefix.count();
            case "coming" -> 1;
            default -> 0;
        };
        return LocalDate.of(today.getYear() + yearOffset, 1, 1);
    }

    /**
     * Resolves a month/day expression with an optional year and prefix.
     *
     * @param monthText month name or abbreviation
     * @param dayOfMonth day of month
     * @param yearText explicit year, or null when omitted
     * @param today current date
     * @param prefix relative-date prefix
     * @return resolved date
     * @throws LuckyNoInputException if the date is invalid
     */
    private static LocalDate resolveMonthDay(
            String monthText,
            int dayOfMonth,
            String yearText,
            LocalDate today,
            Prefix prefix) throws LuckyNoInputException {
        int monthNumber = findMonthNumber(monthText);
        if (monthNumber < 1 || dayOfMonth < 1 || dayOfMonth > 31) {
            throw invalidDateTime();
        }

        if (yearText != null) {
            try {
                return LocalDate.of(
                        Integer.parseInt(yearText), monthNumber, dayOfMonth);
            } catch (DateTimeException exception) {
                throw invalidDateTime();
            }
        }

        LocalDate candidateDate;
        try {
            candidateDate = LocalDate.of(today.getYear(), monthNumber, dayOfMonth);
        } catch (DateTimeException exception) {
            throw invalidDateTime();
        }

        int prefixCount = prefix.count();
        boolean strictNext = prefixCount > 0;
        if ((strictNext && !candidateDate.isAfter(today))
                || (!strictNext && candidateDate.isBefore(today))) {
            candidateDate = candidateDate.plusYears(1);
        }
        if (prefixCount > 1) {
            candidateDate = candidateDate.plusYears(prefixCount - 1L);
        }
        return candidateDate;
    }

    /**
     * Resolves a day-of-month expression to its next applicable month.
     *
     * @param dayOfMonth day of month
     * @param today current date
     * @param prefix relative-date prefix
     * @return resolved date
     * @throws LuckyNoInputException if the day cannot occur in nearby months
     */
    private static LocalDate resolveDayOfMonth(
            int dayOfMonth, LocalDate today, Prefix prefix)
            throws LuckyNoInputException {
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw invalidDateTime();
        }

        LocalDate candidateDate = findValidDay(
                today.withDayOfMonth(1), dayOfMonth);
        int prefixCount = prefix.count();
        boolean strictNext = prefixCount > 0;
        if ((strictNext && !candidateDate.isAfter(today))
                || (!strictNext && candidateDate.isBefore(today))) {
            candidateDate = findValidDay(
                    candidateDate.plusMonths(1), dayOfMonth);
        }
        for (int i = 1; i < prefixCount; i++) {
            candidateDate = findValidDay(
                    candidateDate.plusMonths(1), dayOfMonth);
        }
        return candidateDate;
    }

    /**
     * Finds the first nearby month that contains the requested day.
     *
     * @param monthStart first day of the month to try
     * @param dayOfMonth day of month
     * @return date using the requested day of the month
     * @throws LuckyNoInputException if the day is absent from the next three months
     */
    private static LocalDate findValidDay(
            LocalDate monthStart, int dayOfMonth)
            throws LuckyNoInputException {
        for (int monthOffset = 0; monthOffset < 3; monthOffset++) {
            LocalDate candidateMonth = monthStart.plusMonths(monthOffset);
            if (dayOfMonth <= candidateMonth.lengthOfMonth()) {
                return candidateMonth.withDayOfMonth(dayOfMonth);
            }
        }

        throw invalidDateTime();
    }

    /**
     * Normalizes general date/time input before parsing.
     *
     * @param rawDateTimeText raw date/time text
     * @return trimmed text with equivalent separators and spacing normalized
     */
    private static String normalizeDateTimeText(String rawDateTimeText) {
        if (rawDateTimeText == null) {
            return "";
        }
        return rawDateTimeText.trim()
                .replaceAll("(?<=\\d)T(?=\\d)", " ")
                .replaceAll("(?i)a\\.m\\.", "am")
                .replaceAll("(?i)p\\.m\\.", "pm")
                .replaceAll(",", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Normalizes date text, including ordinal suffixes and the word "the".
     *
     * @param rawDateText raw date text
     * @return normalized lower-case date text
     */
    private static String normalizeDateText(String rawDateText) {
        return rawDateText.toLowerCase(Locale.ENGLISH)
                .replaceAll("(?i)\\b(\\d{1,2})(st|nd|rd|th)\\b", "$1")
                .replaceAll("(?i)\\bthe\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Extracts a relative-date modifier and its repetition count.
     *
     * @param normalizedDateText normalized date text
     * @return parsed prefix and remaining date expression
     */
    private static Prefix extractRelativePrefix(String normalizedDateText) {
        int count = 0;
        String remainder = normalizedDateText;

        if (remainder.startsWith("this coming ")) {
            return new Prefix("coming", 1,
                    remainder.substring("this coming ".length()).trim());
        }
        if (remainder.startsWith("coming ")) {
            return new Prefix("coming", 1,
                    remainder.substring("coming ".length()).trim());
        }
        if (remainder.startsWith("this ")) {
            return new Prefix("this", 0,
                    remainder.substring("this ".length()).trim());
        }
        while (remainder.startsWith("next ")) {
            count++;
            remainder = remainder.substring("next ".length()).trim();
        }
        if (remainder.startsWith("following ")) {
            count = Math.max(2, count + 1);
            remainder = remainder.substring("following ".length()).trim();
            return new Prefix("following", count, remainder);
        }
        return new Prefix(count == 0 ? "none" : "next", count, remainder);
    }

    /**
     * Finds the day value represented by a weekday name or abbreviation.
     *
     * @param weekdayText weekday text
     * @return matching day, or null if the text is not a weekday
     */
    private static DayOfWeek findWeekday(String weekdayText) {
        return Arrays.stream(Weekday.values())
                .filter(weekday -> weekday.matches(weekdayText))
                .map(weekday -> weekday.dayOfWeek)
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds the month number represented by a month name or abbreviation.
     *
     * @param monthText month text
     * @return month number from 1 to 12, or -1 if not a month
     */
    private static int findMonthNumber(String monthText) {
        String normalized = monthText.toLowerCase(Locale.ENGLISH);
        return Arrays.stream(Month.values())
                .filter(month -> month.matches(normalized))
                .mapToInt(month -> month.number)
                .findFirst()
                .orElse(-1);
    }

    /**
     * Creates a strict, case-insensitive English date formatter.
     *
     * @param pattern formatter pattern
     * @return configured formatter
     */
    private static DateTimeFormatter formatter(String pattern) {
        return new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern(pattern)
                .toFormatter(Locale.ENGLISH)
                .withResolverStyle(ResolverStyle.STRICT);
    }

    /**
     * Creates the standard exception for invalid date/time input.
     *
     * @return invalid date/time exception
     */
    private static LuckyNoInputException invalidDateTime() {
        return new LuckyNoInputException(LuckyNoMessages.invalidDateTimeMessage());
    }

    private record Prefix(String modifier, int count, String remainder) {
    }
}
