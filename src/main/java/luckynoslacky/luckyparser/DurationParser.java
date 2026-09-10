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
            "\\bhalf\\s+(?:a|an)\\s+"
                    + "(minutes?|min(?:s)?|hours?|h(?:r)?s?|days?|d(?:s)?|"
                    + "weeks?|months?|mo(?:s)?|years?|yr(?:s)?)\\b",
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
        String normalizedText = normalizeInput(durationText, inputText);

        return parseDurationParts(normalizedText, inputText);
    }

    /**
     * Validates and normalizes the raw duration input.
     *
     * @param durationText raw duration text supplied by the user
     * @param inputText original duration text used in error messages
     * @return normalized duration text
     * @throws LuckyNoInputException if the input is blank or contains a slash
     */
    private static String normalizeInput(
            String durationText,
            String inputText)
            throws LuckyNoInputException {
        if (durationText == null || durationText.isBlank()) {
            throw invalidDurationFormat(inputText);
        }
        String normalizedText = durationText.trim();
        if (normalizedText.contains("/")) {
            throw invalidDurationFormat(inputText);
        }
        return normalizeNaturalLanguage(normalizedText);
    }

    /**
     * Parses all duration components in normalized text.
     *
     * @param normalizedText normalized duration text
     * @param inputText original duration text used in error messages
     * @return parsed calendar and clock amounts
     * @throws LuckyNoInputException if a component or remainder is malformed
     */
    private static DurationPeriod parseDurationParts(
            String normalizedText,
            String inputText)
            throws LuckyNoInputException {
        Matcher matcher = COMPONENT_PATTERN.matcher(normalizedText);
        int cursor = 0;
        DurationAccumulator accumulator = new DurationAccumulator();

        while (cursor < normalizedText.length()) {
            cursor = skipWhitespace(
                    normalizedText,
                    cursor,
                    accumulator.hasParsedComponent(),
                    inputText);

            matcher.region(cursor, normalizedText.length());
            if (!matcher.lookingAt()) {
                break;
            }

            ParsedComponent component = parseNextComponent(
                    matcher,
                    accumulator.getPreviousUnitOrder(),
                    inputText);
            accumulator.add(component, inputText);
            cursor = component.endIndex();
        }

        rejectInvalidRemainder(
                normalizedText.substring(cursor),
                accumulator.hasParsedComponent(),
                inputText);

        return accumulator.toDurationPeriod();
    }

    /**
     * Skips whitespace between duration components.
     *
     * @param normalizedText normalized duration text
     * @param cursor current parsing position
     * @param parsedComponent whether a component has already been parsed
     * @param inputText original duration text used in error messages
     * @return next non-whitespace parsing position
     * @throws LuckyNoInputException if components are adjacent without spacing
     */
    private static int skipWhitespace(
            String normalizedText,
            int cursor,
            boolean parsedComponent,
            String inputText)
            throws LuckyNoInputException {
        int componentStart = cursor;
        while (cursor < normalizedText.length()
                && Character.isWhitespace(normalizedText.charAt(cursor))) {
            cursor++;
        }
        if (parsedComponent && cursor == componentStart) {
            throw invalidDurationFormat(inputText);
        }
        return cursor;
    }

    /**
     * Parses and validates the component at the matcher's current position.
     *
     * @param matcher matcher positioned at the next component
     * @param previousUnitOrder order of the preceding unit
     * @param inputText original duration text used in error messages
     * @return parsed duration component
     * @throws LuckyNoInputException if the component violates duration rules
     */
    private static ParsedComponent parseNextComponent(
            Matcher matcher,
            int previousUnitOrder,
            String inputText)
            throws LuckyNoInputException {
        BigDecimal amount = parseAmount(matcher.group(1), inputText);
        DurationPeriodUnit unit =
                DurationPeriodUnit.fromText(matcher.group(2), inputText);
        validateComponent(amount, unit, previousUnitOrder, inputText);

        return new ParsedComponent(amount, unit, matcher.end());
    }

    /**
     * Validates the value and ordering rules for one duration component.
     *
     * @param amount numeric amount in the component
     * @param unit unit represented by the component
     * @param previousUnitOrder order of the preceding unit
     * @param inputText original duration text used in error messages
     * @throws LuckyNoInputException if the component is invalid
     */
    private static void validateComponent(
            BigDecimal amount,
            DurationPeriodUnit unit,
            int previousUnitOrder,
            String inputText)
            throws LuckyNoInputException {
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
            throw invalidDurationFormat(inputText);
        }
        if (unit.order <= previousUnitOrder) {
            throw invalidDurationFormat(inputText);
        }
    }

    /**
     * Rejects malformed text remaining after the last parsed component.
     *
     * @param remainingText unparsed trailing text
     * @param parsedComponent whether at least one component was parsed
     * @param inputText original duration text used in error messages
     * @throws LuckyNoInputException if no component or a numeric remainder exists
     */
    private static void rejectInvalidRemainder(
            String remainingText,
            boolean parsedComponent,
            String inputText)
            throws LuckyNoInputException {
        String trimmedText = remainingText.trim();
        if (!parsedComponent || trimmedText.matches("-?\\d.*")) {
            throw invalidDurationFormat(inputText);
        }
    }

    /** Represents one duration component matched from the input. */
    private record ParsedComponent(
            BigDecimal amount,
            DurationPeriodUnit unit,
            int endIndex) {
    }

    /** Accumulates validated calendar and clock duration components. */
    private static final class DurationAccumulator {
        private int years;
        private int months;
        private int days;
        private Duration timeAmount = Duration.ZERO;
        private int previousUnitOrder = -1;
        private boolean parsedComponent;

        /**
         * Adds one validated component to the accumulated duration.
         *
         * @param component validated duration component
         * @param inputText original duration text used in error messages
         * @throws LuckyNoInputException if the component exceeds supported precision
         */
        private void add(
                ParsedComponent component,
                String inputText)
                throws LuckyNoInputException {
            try {
                switch (component.unit()) {
                    case YEAR -> years = component.amount().intValueExact();
                    case MONTH -> months = component.amount().intValueExact();
                    case WEEK -> days = Math.addExact(
                            days,
                            Math.multiplyExact(
                                    component.amount().intValueExact(), 7));
                    case DAY -> addDays(component.amount(), inputText);
                    case HOUR -> timeAmount = timeAmount.plus(
                            toDuration(
                                    component.amount(),
                                    NANOS_PER_HOUR,
                                    inputText));
                    case MINUTE -> timeAmount = timeAmount.plus(
                            toDuration(
                                    component.amount(),
                                    NANOS_PER_MINUTE,
                                    inputText));
                    default -> throw invalidDurationFormat(inputText);
                }
            } catch (ArithmeticException exception) {
                throw invalidDurationFormat(inputText);
            }

            previousUnitOrder = component.unit().order;
            parsedComponent = true;
        }

        /**
         * Adds a potentially fractional day to the accumulated duration.
         *
         * @param amount day amount to add
         * @param inputText original duration text used in error messages
         * @throws LuckyNoInputException if the fractional part exceeds precision
         */
        private void addDays(
                BigDecimal amount,
                String inputText)
                throws LuckyNoInputException {
            days = amount.setScale(0, RoundingMode.FLOOR).intValueExact();
            timeAmount = timeAmount.plus(
                    toDuration(
                            amount.subtract(BigDecimal.valueOf(days)),
                            NANOS_PER_DAY,
                            inputText));
        }

        private boolean hasParsedComponent() {
            return parsedComponent;
        }

        private int getPreviousUnitOrder() {
            return previousUnitOrder;
        }

        private DurationPeriod toDurationPeriod() {
            return new DurationPeriod(
                    Period.of(years, months, days),
                    timeAmount);
        }
    }

    /**
     * Normalizes the supported natural-language duration forms.
     *
     * @param durationText duration text supplied by the user
     * @return duration text in the numeric format understood by the parser
     * @throws LuckyNoInputException if a half-unit alias is unsupported
     */
    private static String normalizeNaturalLanguage(String durationText)
            throws LuckyNoInputException {
        String normalizedText = normalizeHalfUnits(durationText);
        normalizedText = normalizeNumberWords(normalizedText);
        return normalizeNumericMore(normalizedText);
    }

    /**
     * Converts supported half-unit phrases into numeric components.
     *
     * @param durationText duration text to normalize
     * @return text with half-unit phrases replaced
     * @throws LuckyNoInputException if a half-unit alias is unsupported
     */
    private static String normalizeHalfUnits(String durationText)
            throws LuckyNoInputException {
        Matcher matcher = HALF_UNIT_PATTERN.matcher(durationText);
        StringBuffer normalizedText = new StringBuffer();

        while (matcher.find()) {
            DurationPeriodUnit unit = DurationPeriodUnit.fromText(
                    matcher.group(1),
                    durationText);
            String replacement = switch (unit) {
                case MINUTE -> "0.5 minutes";
                case HOUR -> "0.5 hours";
                case DAY -> "0.5 days";
                case WEEK -> "3 days 12 hours";
                case MONTH -> "0.5 months";
                case YEAR -> "0.5 years";
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
    private static BigDecimal parseAmount(
            String amountText,
            String inputText)
            throws LuckyNoInputException {
        try {
            return new BigDecimal(amountText);
        } catch (NumberFormatException exception) {
            throw invalidDurationFormat(inputText);
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
            throw invalidDurationFormat(inputText);
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
        private static DurationPeriodUnit fromText(
                String unitText,
                String inputText)
                throws LuckyNoInputException {
            String normalizedUnit = unitText.toLowerCase(Locale.ROOT);

            return Arrays.stream(values())
                    .filter(unit -> unit.acceptedForms.contains(normalizedUnit))
                    .findFirst()
                    .orElseThrow(() -> invalidDurationFormat(inputText));
        }
    }

    /**
     * Creates an invalid-duration exception with the original input.
     *
     * @param durationText original duration text supplied by the user
     * @return invalid-duration exception
     */
    private static LuckyNoInputException invalidDurationFormat(
            String durationText) {
        return new LuckyNoInputException(
                LuckyNoMessages.invalidDurationMessage(durationText));
    }
}
