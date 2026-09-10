package luckynoslacky.luckyparser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Period;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import luckynoslacky.luckyexception.LuckyNoInputException;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Parses the decimal and combined duration formats supported by the snooze
 * command.
 */
public final class DurationParser {
    private static final Pattern COMPONENT_PATTERN = Pattern.compile(
            "(-?\\d+(?:\\.\\d+)?)\\s*"
                    + "(minutes?|min(?:s)?|hours?|h(?:r)?s?|days?|d(?:s)?|"
                    + "weeks?|months?|mo(?:s)?|years?|yr(?:s)?)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern HALF_UNIT_PATTERN = Pattern.compile(
            "\\bhalf\\s+(?:a|an)\\s+(minutes?|hours?|days?|weeks?|"
                    + "months?|years?)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern WORD_NUMBER_PATTERN = Pattern.compile(
            "\\b(a|an|one|two|three|four|five|six|seven|eight|nine|ten)"
                    + "(?:\\s+more)?\\s+(minutes?|hours?|days?|weeks?|"
                    + "months?|years?)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMERIC_MORE_PATTERN = Pattern.compile(
            "\\b(-?\\d+(?:\\.\\d+)?)\\s+more\\s+(minutes?|hours?|days?|"
                    + "weeks?|months?|years?)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final long NANOS_PER_MINUTE = Duration.ofMinutes(1).toNanos();
    private static final long NANOS_PER_HOUR = Duration.ofHours(1).toNanos();
    private static final long NANOS_PER_DAY = Duration.ofDays(1).toNanos();

    private DurationParser() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Parses a non-negative duration and ignores trailing commentary.
     *
     * @param durationText duration text to parse
     * @return parsed calendar and clock amounts
     * @throws LuckyNoInputException if the duration is malformed or negative
     */
    public static DurationPeriod parse(String durationText)
            throws LuckyNoInputException {
        String inputText = String.valueOf(durationText);
        if (durationText == null || durationText.isBlank()) {
            throw invalidFormat(inputText);
        }
        String normalizedText = durationText.trim();
        if (normalizedText.contains("/")) {
            throw invalidFormat(inputText);
        }
        normalizedText = normalizeNaturalLanguage(normalizedText);

        Matcher matcher = COMPONENT_PATTERN.matcher(normalizedText);
        int cursor = 0;
        int previousUnitOrder = -1;
        int years = 0;
        int months = 0;
        int days = 0;
        Duration timeAmount = Duration.ZERO;
        boolean parsedComponent = false;

        while (cursor < normalizedText.length()) {
            int componentStart = cursor;
            while (cursor < normalizedText.length()
                    && Character.isWhitespace(normalizedText.charAt(cursor))) {
                cursor++;
            }
            if (parsedComponent && cursor == componentStart) {
                throw invalidFormat(inputText);
            }
            matcher.region(cursor, normalizedText.length());
            if (!matcher.lookingAt()) {
                break;
            }

            BigDecimal amount = parseAmount(matcher.group(1));
            DurationPeriodUnit unit =
                    DurationPeriodUnit.fromText(matcher.group(2));
            if (amount.signum() < 0) {
                throw negativeDuration();
            }
            if (amount.scale() > 0
                    && (unit == DurationPeriodUnit.MONTH
                    || unit == DurationPeriodUnit.YEAR)) {
                throw new LuckyNoInputException(
                        LuckyNoMessages.decimalCalendarDurationMessage());
            }
            if (!unit.allowsDecimal && amount.scale() > 0) {
                throw invalidFormat(inputText);
            }
            if (unit.order <= previousUnitOrder) {
                throw invalidFormat(inputText);
            }

            try {
                switch (unit) {
                    case YEAR -> years = amount.intValueExact();
                    case MONTH -> months = amount.intValueExact();
                    case WEEK -> days = Math.addExact(
                            days, Math.multiplyExact(amount.intValueExact(), 7));
                    case DAY -> {
                        days = amount.setScale(0, RoundingMode.FLOOR).intValueExact();
                        timeAmount = timeAmount.plus(toDuration(
                                amount.subtract(BigDecimal.valueOf(days)),
                                NANOS_PER_DAY, inputText));
                    }
                    case HOUR -> timeAmount = timeAmount.plus(
                            toDuration(amount, NANOS_PER_HOUR, inputText));
                    case MINUTE -> timeAmount = timeAmount.plus(
                            toDuration(amount, NANOS_PER_MINUTE, inputText));
                    default -> throw invalidFormat(inputText);
                }
            } catch (ArithmeticException exception) {
                throw invalidFormat(inputText);
            }

            previousUnitOrder = unit.order;
            parsedComponent = true;
            cursor = matcher.end();
        }

        String remainingText = normalizedText.substring(cursor).trim();
        if (!parsedComponent || remainingText.matches("-?\\d.*")) {
            throw invalidFormat(inputText);
        }
        return new DurationPeriod(Period.of(years, months, days), timeAmount);
    }

    /**
     * Normalizes the supported natural-language duration forms.
     *
     * @param durationText duration text supplied by the user
     * @return duration text in the numeric format understood by the parser
     */
    private static String normalizeNaturalLanguage(String durationText) {
        String normalizedText = normalizeHalfUnits(durationText);
        normalizedText = normalizeNumberWords(normalizedText);
        return normalizeNumericMore(normalizedText);
    }

    /**
     * Converts supported half-unit phrases into numeric components.
     *
     * @param durationText duration text to normalize
     * @return text with half-unit phrases replaced
     */
    private static String normalizeHalfUnits(String durationText) {
        Matcher matcher = HALF_UNIT_PATTERN.matcher(durationText);
        StringBuffer normalizedText = new StringBuffer();

        while (matcher.find()) {
            String replacement = switch (matcher.group(1).toLowerCase(Locale.ROOT)) {
                case "minute", "minutes" -> "0.5 minutes";
                case "hour", "hours" -> "0.5 hours";
                case "day", "days" -> "0.5 days";
                case "week", "weeks" -> "3 days 12 hours";
                case "month", "months" -> "0.5 months";
                case "year", "years" -> "0.5 years";
                default -> matcher.group(0);
            };
            matcher.appendReplacement(
                    normalizedText, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(normalizedText);
        return normalizedText.toString();
    }

    /**
     * Converts supported number-word components into numeric components.
     *
     * @param durationText duration text to normalize
     * @return text with number-word components replaced
     */
    private static String normalizeNumberWords(String durationText) {
        Matcher matcher = WORD_NUMBER_PATTERN.matcher(durationText);
        StringBuffer normalizedText = new StringBuffer();

        while (matcher.find()) {
            String number = numberWordValue(matcher.group(1));
            String replacement = number + " " + matcher.group(2);
            matcher.appendReplacement(
                    normalizedText, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(normalizedText);
        return normalizedText.toString();
    }

    /**
     * Removes the optional filler word from numeric components.
     *
     * @param durationText duration text to normalize
     * @return text with numeric filler words removed
     */
    private static String normalizeNumericMore(String durationText) {
        Matcher matcher = NUMERIC_MORE_PATTERN.matcher(durationText);
        StringBuffer normalizedText = new StringBuffer();

        while (matcher.find()) {
            String replacement = matcher.group(1) + " " + matcher.group(2);
            matcher.appendReplacement(
                    normalizedText, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(normalizedText);
        return normalizedText.toString();
    }

    /**
     * Converts a supported number word or article into its numeric value.
     *
     * @param numberWord number word or article
     * @return numeric value represented by the word
     */
    private static String numberWordValue(String numberWord) {
        return switch (numberWord.toLowerCase(Locale.ROOT)) {
            case "a", "an", "one" -> "1";
            case "two" -> "2";
            case "three" -> "3";
            case "four" -> "4";
            case "five" -> "5";
            case "six" -> "6";
            case "seven" -> "7";
            case "eight" -> "8";
            case "nine" -> "9";
            case "ten" -> "10";
            default -> numberWord;
        };
    }

    /**
     * Converts a parsed numeric component into a decimal amount.
     *
     * @param amountText numeric component text
     * @return parsed numeric amount
     * @throws LuckyNoInputException if the number is malformed
     */
    private static BigDecimal parseAmount(String amountText)
            throws LuckyNoInputException {
        try {
            return new BigDecimal(amountText);
        } catch (NumberFormatException exception) {
            throw invalidFormat();
        }
    }

    /**
     * Converts a fixed-length decimal unit into nanosecond precision.
     *
     * @param amount decimal unit amount
     * @param nanosPerUnit nanoseconds represented by one unit
     * @return converted duration
     * @throws LuckyNoInputException if the amount exceeds supported precision
     */
    private static Duration toDuration(
            BigDecimal amount, long nanosPerUnit, String inputText)
            throws LuckyNoInputException {
        try {
            long nanos = amount.multiply(BigDecimal.valueOf(nanosPerUnit))
                    .longValueExact();
            return Duration.ofNanos(nanos);
        } catch (ArithmeticException exception) {
            throw invalidFormat(inputText);
        }
    }

    /**
     * Creates the dedicated negative-duration exception.
     *
     * @return negative-duration exception
     */
    private static LuckyNoInputException negativeDuration() {
        return new LuckyNoInputException(
                "Siao ah time where got negative one");
    }

    /** Identifies supported duration units and their accepted forms. */
    private enum DurationPeriodUnit {
        YEAR(0, false, "year", "years", "yr", "yrs"),
        MONTH(1, false, "month", "months", "mo", "mos"),
        WEEK(2, false, "week", "weeks"),
        DAY(3, true, "day", "days", "d", "ds"),
        HOUR(4, true, "hour", "hours", "h", "hs", "hr", "hrs"),
        MINUTE(5, true, "minute", "minutes", "min", "mins");

        private final int order;
        private final boolean allowsDecimal;
        private final Set<String> acceptedForms;

        DurationPeriodUnit(
                int order,
                boolean allowsDecimal,
                String... acceptedForms) {
            this.order = order;
            this.allowsDecimal = allowsDecimal;
            this.acceptedForms = Set.of(acceptedForms);
        }

        /**
         * Converts a parsed unit name into its unit definition.
         *
         * @param unitText unit name
         * @return matching duration period unit
         * @throws LuckyNoInputException if the unit is unsupported
         */
        private static DurationPeriodUnit fromText(String unitText)
                throws LuckyNoInputException {
            String normalizedUnit = unitText.toLowerCase(Locale.ROOT);

            return Arrays.stream(values())
                    .filter(unit -> unit.acceptedForms.contains(normalizedUnit))
                    .findFirst()
                    .orElseThrow(DurationParser::invalidFormat);
        }
    }

    /**
     * Creates the standard invalid-format exception for a duration.
     *
     * @return invalid duration exception
     */
    private static LuckyNoInputException invalidFormat() {
        return new LuckyNoInputException(
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.SNOOZE,
                        "<taskNumber> [/by <duration>]",
                        "<taskNumber> [/to <end date/time>]"));
    }

    /**
     * Creates an invalid-duration exception that includes the original input.
     *
     * @param durationText original duration text supplied by the user
     * @return invalid-duration exception
     */
    private static LuckyNoInputException invalidFormat(String durationText) {
        return new LuckyNoInputException(
                LuckyNoMessages.invalidDurationMessage(durationText));
    }
}
