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
import java.util.List;
import java.util.Locale;
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

    private static final List<String> WEEKDAY_NAMES = List.of(
            "monday", "tuesday", "wednesday", "thursday",
            "friday", "saturday", "sunday");
    private static final List<String> MONTH_NAMES = List.of(
            "january", "february", "march", "april", "may", "june",
            "july", "august", "september", "october", "november",
            "december");
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
     * Represents a parsed value together with whether its date was omitted.
     *
     * @param value parsed date and time
     * @param timeOnly true when the input contained no date
     */
    public record ParsedDateTime(LocalDateTime value, boolean timeOnly) {
    }

    /**
     * Parses a value used as an event start.
     *
     * @param input date and time text to parse
     * @return parsed date and time
     * @throws LuckyNoInputException if the input is not a supported date/time
     */
    public ParsedDateTime parseStartDateTime(String input)
            throws LuckyNoInputException {
        return parse(input, LocalTime.MIN);
    }

    /**
     * Parses a value used as an event end or deadline.
     *
     * @param input date and time text to parse
     * @return parsed date and time
     * @throws LuckyNoInputException if the input is not a supported date/time
     */
    public ParsedDateTime parseEndDateTime(String input)
            throws LuckyNoInputException {
        return parse(input, LocalTime.of(23, 59));
    }

    /**
     * Parses an event end time relative to its start date and time.
     *
     * <p>An explicitly supplied end date is preserved. When the end input
     * contains only a time, the reference date is used unless that time has
     * already passed, in which case the next date is used.</p>
     *
     * @param input event end date/time text
     * @param referenceDateTime event start date and time
     * @return parsed event end date and time
     * @throws LuckyNoInputException if the input is invalid
     */
    public ParsedDateTime parseEndDateTime(
            String input,
            LocalDateTime referenceDateTime)
            throws LuckyNoInputException {
        if (referenceDateTime == null) {
            throw new IllegalArgumentException(
                    "Reference date and time cannot be null.");
        }

        ParsedDateTime parsed = parse(input, LocalTime.of(23, 59));
        if (!parsed.timeOnly()) {
            return parsed;
        }

        LocalTime endTime = parsed.value().toLocalTime();
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
     * Formats a value for the CSV storage format.
     *
     * @param value date and time to format, or null for an empty field
     * @return formatted storage value
     */
    public static String formatForStorage(LocalDateTime value) {
        return value == null ? "" : CSV_FORMATTER.format(value);
    }

    /**
     * Parses a value from the CSV storage format.
     *
     * @param value stored date and time, or blank for no date and time
     * @return parsed date and time, or null for a blank value
     */
    public static LocalDateTime parseFromStorage(String value) {
        return value == null || value.isBlank()
                ? null
                : LocalDateTime.parse(value, CSV_FORMATTER);
    }

    /**
     * Parses input and applies the default time used when only a date is given.
     *
     * @param input date/time text to parse
     * @param dateOnlyDefault time to use for date-only input
     * @return parsed date and time
     * @throws LuckyNoInputException if the input is invalid
     */
    private ParsedDateTime parse(String input, LocalTime dateOnlyDefault)
            throws LuckyNoInputException {
        String normalized = normalize(input);
        if (normalized.isEmpty()) {
            throw invalidDateTime();
        }

        LocalDateTime current = now();
        LocalDate today = current.toLocalDate();

        // A bare four-digit value is treated as a year first. Users can write
        // an unambiguous compact time with a separator, such as 20:30.
        if (YEAR_ONLY_PATTERN.matcher(normalized).matches()) {
            LocalDate date = parseDate(normalized, today);
            return new ParsedDateTime(LocalDateTime.of(date, dateOnlyDefault), false);
        }

        LocalTime time = parseTime(normalized);
        if (time != null) {
            LocalDate resolvedDate = time.isBefore(current.toLocalTime())
                    ? today.plusDays(1)
                    : today;
            return new ParsedDateTime(LocalDateTime.of(resolvedDate, time), true);
        }

        int split = findDateTimeSplit(normalized);
        if (split > 0) {
            String dateText = normalized.substring(0, split).trim();
            String timeText = normalized.substring(split).trim();
            LocalTime dateTime = parseTime(timeText);
            if (dateTime != null) {
                LocalDate date = parseDate(dateText, today);
                return new ParsedDateTime(LocalDateTime.of(date, dateTime), false);
            }
        }

        LocalDate date = parseDate(normalized, today);
        return new ParsedDateTime(LocalDateTime.of(date, dateOnlyDefault), false);
    }

    /**
     * Finds the boundary between a date expression and its time expression.
     *
     * @param value normalized date/time expression
     * @return index at which the time expression starts, or -1 if not found
     */
    private static int findDateTimeSplit(String value) {
        for (int split = value.length() - 1; split > 0; split--) {
            if (Character.isWhitespace(value.charAt(split - 1))) {
                String suffix = value.substring(split).trim();
                if (parseTime(suffix) != null) {
                    if (isValidDateWithYear(value.substring(0, split), suffix)) {
                        continue;
                    }
                    return split;
                }
            }
        }
        return -1;
    }

    /**
     * Parses a time in separated, meridiem, or compact HHMM notation.
     *
     * @param value time text to parse
     * @return parsed time, or null if the text is not a valid time
     */
    private static LocalTime parseTime(String value) {
        String normalized = value.trim()
                .replaceAll("(?i)a\\.m\\.", "am")
                .replaceAll("(?i)p\\.m\\.", "pm");
        Matcher matcher = TIME_PATTERN.matcher(normalized);
        if (!matcher.matches()) {
            return parseCompactTime(normalized);
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
     * Parses a four-digit time in HHMM notation.
     *
     * @param value compact time text
     * @return parsed time, or null if the text is not a valid HHMM value
     */
    private static LocalTime parseCompactTime(String value) {
        if (!value.matches("\\d{4}")) {
            return null;
        }

        int hour = Integer.parseInt(value.substring(0, 2));
        int minute = Integer.parseInt(value.substring(2, 4));
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
    private static boolean isValidDateWithYear(String dateText, String yearText) {
        String normalizedDate = normalizeDate(dateText).replaceFirst(
                "^(monday|tuesday|wednesday|thursday|friday|saturday|sunday"
                        + "|mon|tue|wed|thu|fri|sat|sun)\\s+",
                "");
        String candidate = normalizedDate + " " + yearText;
        return parseWithKnownFormat(candidate) != null;
    }

    /**
     * Parses a date expression and resolves relative terms against today.
     *
     * @param input date text to parse
     * @param today current date used for relative resolution
     * @return resolved date
     * @throws LuckyNoInputException if the date is invalid
     */
    private LocalDate parseDate(String input, LocalDate today)
            throws LuckyNoInputException {
        String value = normalizeDate(input);
        Prefix prefix = extractPrefix(value);
        value = prefix.remainder();

        DayOfWeek weekday = parseWeekday(value);
        if (weekday != null) {
            return resolveWeekday(weekday, today, prefix);
        }

        if (prefix.modifier().equals("none")) {
            switch (value) {
                case "today":
                    return today;
                case "tomorrow": // Fallthrough
                case "tmr":
                    return today.plusDays(1);
                case "yesterday": // Fallthrough
                case "ytd":
                    return today.minusDays(1);
                default:
                    break;
            }
        }

        value = value.replaceFirst(
                "^(monday|tuesday|wednesday|thursday|friday|saturday|sunday"
                        + "|mon|tue|wed|thu|fri|sat|sun)\\s+",
                "");

        if (value.equals("month")) {
            return resolveMonth(today, prefix);
        }
        if (value.equals("year")) {
            return resolveYear(today, prefix);
        }

        Matcher yearMatcher = YEAR_ONLY_PATTERN.matcher(value);
        if (yearMatcher.matches()) {
            return LocalDate.of(Integer.parseInt(yearMatcher.group(1)), 1, 1);
        }

        LocalDate formattedDate = parseWithKnownFormat(value);
        if (formattedDate != null) {
            return formattedDate;
        }

        Matcher monthDayMatcher = MONTH_DAY_PATTERN.matcher(value);
        if (monthDayMatcher.matches()) {
            return resolveMonthDay(
                    monthDayMatcher.group(1),
                    Integer.parseInt(monthDayMatcher.group(2)),
                    monthDayMatcher.group(3),
                    today,
                    prefix);
        }

        Matcher dayMonthMatcher = DAY_MONTH_PATTERN.matcher(value);
        if (dayMonthMatcher.matches()) {
            return resolveMonthDay(
                    dayMonthMatcher.group(2),
                    Integer.parseInt(dayMonthMatcher.group(1)),
                    dayMonthMatcher.group(3),
                    today,
                    prefix);
        }

        Matcher dayMatcher = DAY_ONLY_PATTERN.matcher(value);
        if (dayMatcher.matches()) {
            return resolveDayOfMonth(
                    Integer.parseInt(dayMatcher.group(1)), today, prefix);
        }

        int month = monthNumber(value);
        if (month > 0) {
            return resolveMonth(month, today, prefix);
        }

        throw invalidDateTime();
    }

    /**
     * Attempts each explicitly supported absolute date formatter.
     *
     * @param value normalized date text
     * @return parsed date, or null if no formatter accepts the value
     */
    private static LocalDate parseWithKnownFormat(String value) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDate parsed = LocalDate.parse(value, formatter);
                // Year zero is supported by java.time, but is not accepted by
                // the chatbot and can therefore be reinterpreted as HHMM.
                return parsed.getYear() > 0 ? parsed : null;
            } catch (DateTimeParseException exception) {
                // Try the next explicitly supported format.
            }
        }
        return null;
    }

    /**
     * Resolves a weekday and its relative prefix into a calendar date.
     *
     * @param weekday requested day of the week
     * @param today current date
     * @param prefix relative-date prefix
     * @return resolved weekday date
     */
    private static LocalDate resolveWeekday(
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
    private static LocalDate resolveMonth(LocalDate today, Prefix prefix) {
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
     * @param month month number
     * @param today current date
     * @param prefix relative-date prefix
     * @return first day of the resolved month
     */
    private static LocalDate resolveMonth(
            int month, LocalDate today, Prefix prefix) {
        int yearOffset = switch (prefix.modifier()) {
            case "next", "following" -> prefix.count();
            case "coming" -> 1;
            default -> 0;
        };
        LocalDate candidate = LocalDate.of(today.getYear(), month, 1)
                .plusYears(yearOffset);
        if (prefix.modifier().equals("none") && candidate.isBefore(today)) {
            candidate = candidate.plusYears(1);
        }
        return candidate;
    }

    /**
     * Resolves a relative reference to a year.
     *
     * @param today current date
     * @param prefix relative-date prefix
     * @return first day of the resolved year
     */
    private static LocalDate resolveYear(LocalDate today, Prefix prefix) {
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
     * @param day day of month
     * @param yearText explicit year, or null when omitted
     * @param today current date
     * @param prefix relative-date prefix
     * @return resolved date
     * @throws LuckyNoInputException if the date is invalid
     */
    private static LocalDate resolveMonthDay(
            String monthText,
            int day,
            String yearText,
            LocalDate today,
            Prefix prefix) throws LuckyNoInputException {
        int month = monthNumber(monthText);
        if (month < 1 || day < 1 || day > 31) {
            throw invalidDateTime();
        }

        if (yearText != null) {
            try {
                return LocalDate.of(Integer.parseInt(yearText), month, day);
            } catch (DateTimeException exception) {
                throw invalidDateTime();
            }
        }

        LocalDate candidate;
        try {
            candidate = LocalDate.of(today.getYear(), month, day);
        } catch (DateTimeException exception) {
            throw invalidDateTime();
        }

        int prefixCount = prefix.count();
        boolean strictNext = prefixCount > 0;
        if ((strictNext && !candidate.isAfter(today))
                || (!strictNext && candidate.isBefore(today))) {
            candidate = candidate.plusYears(1);
        }
        if (prefixCount > 1) {
            candidate = candidate.plusYears(prefixCount - 1L);
        }
        return candidate;
    }

    /**
     * Resolves a day-of-month expression to its next applicable month.
     *
     * @param day day of month
     * @param today current date
     * @param prefix relative-date prefix
     * @return resolved date
     * @throws LuckyNoInputException if the day cannot occur in nearby months
     */
    private static LocalDate resolveDayOfMonth(
            int day, LocalDate today, Prefix prefix) throws LuckyNoInputException {
        if (day < 1 || day > 31) {
            throw invalidDateTime();
        }

        LocalDate candidate = firstValidDay(today.withDayOfMonth(1), day);
        int prefixCount = prefix.count();
        boolean strictNext = prefixCount > 0;
        if ((strictNext && !candidate.isAfter(today))
                || (!strictNext && candidate.isBefore(today))) {
            candidate = firstValidDay(candidate.plusMonths(1), day);
        }
        for (int i = 1; i < prefixCount; i++) {
            candidate = firstValidDay(candidate.plusMonths(1), day);
        }
        return candidate;
    }

    /**
     * Finds the first nearby month that contains the requested day.
     *
     * @param monthStart first day of the month to try
     * @param day day of month
     * @return date using the requested day
     * @throws LuckyNoInputException if the day is absent from the next three months
     */
    private static LocalDate firstValidDay(LocalDate monthStart, int day)
            throws LuckyNoInputException {
        if (day > monthStart.lengthOfMonth()) {
            LocalDate nextMonth = monthStart.plusMonths(1);
            if (day > nextMonth.lengthOfMonth()) {
                LocalDate followingMonth = nextMonth.plusMonths(1);
                if (day > followingMonth.lengthOfMonth()) {
                    throw invalidDateTime();
                }
                return followingMonth.withDayOfMonth(day);
            }
            return nextMonth.withDayOfMonth(day);
        }
        return monthStart.withDayOfMonth(day);
    }

    /**
     * Normalizes general date/time input before parsing.
     *
     * @param input raw date/time text
     * @return trimmed text with equivalent separators and spacing normalized
     */
    private static String normalize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim()
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
     * @param input raw date text
     * @return normalized lower-case date text
     */
    private static String normalizeDate(String input) {
        return input.toLowerCase(Locale.ENGLISH)
                .replaceAll("(?i)\\b(\\d{1,2})(st|nd|rd|th)\\b", "$1")
                .replaceAll("(?i)\\bthe\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Extracts a relative-date modifier and its repetition count.
     *
     * @param value normalized date text
     * @return parsed prefix and remaining date expression
     */
    private static Prefix extractPrefix(String value) {
        int count = 0;
        String remainder = value;

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
     * Converts a weekday name or three-letter abbreviation to a day value.
     *
     * @param value weekday text
     * @return matching day, or null if the value is not a weekday
     */
    private static DayOfWeek parseWeekday(String value) {
        for (int i = 0; i < WEEKDAY_NAMES.size(); i++) {
            String fullName = WEEKDAY_NAMES.get(i);
            if (value.equals(fullName) || value.equals(fullName.substring(0, 3))) {
                return DayOfWeek.of(i + 1);
            }
        }
        return null;
    }

    /**
     * Converts a month name or three-letter abbreviation to a month number.
     *
     * @param value month text
     * @return month number from 1 to 12, or -1 if not a month
     */
    private static int monthNumber(String value) {
        String normalized = value.toLowerCase(Locale.ENGLISH);
        for (int i = 0; i < MONTH_NAMES.size(); i++) {
            String fullName = MONTH_NAMES.get(i);
            if (normalized.equals(fullName)
                    || normalized.equals(fullName.substring(0, 3))) {
                return i + 1;
            }
        }
        return -1;
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
