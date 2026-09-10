package luckynoslacky.luckyparser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Period;
import java.util.Locale;
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
            "(-?\\d+(?:\\.\\d+)?)\\s+(minutes?|hours?|days?|months?|years?)\\b",
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
        if (durationText == null || durationText.isBlank()) {
            throw invalidFormat();
        }
        String normalizedText = durationText.trim();
        if (normalizedText.contains("/")) {
            throw invalidFormat();
        }

        Matcher matcher = COMPONENT_PATTERN.matcher(normalizedText);
        int cursor = 0;
        int previousUnitOrder = -1;
        int years = 0;
        int months = 0;
        int days = 0;
        Duration timeAmount = Duration.ZERO;
        boolean parsedComponent = false;

        while (cursor < normalizedText.length()) {
            while (cursor < normalizedText.length()
                    && Character.isWhitespace(normalizedText.charAt(cursor))) {
                cursor++;
            }
            matcher.region(cursor, normalizedText.length());
            if (!matcher.lookingAt()) {
                break;
            }

            BigDecimal amount = parseAmount(matcher.group(1));
            Unit unit = Unit.fromText(matcher.group(2));
            if (amount.signum() < 0) {
                throw negativeDuration();
            }
            if (unit.order <= previousUnitOrder || (!unit.allowsDecimal
                    && amount.scale() > 0)) {
                throw invalidFormat();
            }

            try {
                switch (unit) {
                    case YEAR -> years = amount.intValueExact();
                    case MONTH -> months = amount.intValueExact();
                    case DAY -> {
                        days = amount.setScale(0, RoundingMode.FLOOR).intValueExact();
                        timeAmount = timeAmount.plus(toDuration(
                                amount.subtract(BigDecimal.valueOf(days)), NANOS_PER_DAY));
                    }
                    case HOUR -> timeAmount = timeAmount.plus(
                            toDuration(amount, NANOS_PER_HOUR));
                    case MINUTE -> timeAmount = timeAmount.plus(
                            toDuration(amount, NANOS_PER_MINUTE));
                    default -> throw invalidFormat();
                }
            } catch (ArithmeticException exception) {
                throw invalidFormat();
            }

            previousUnitOrder = unit.order;
            parsedComponent = true;
            cursor = matcher.end();
        }

        String remainingText = normalizedText.substring(cursor).trim();
        if (!parsedComponent || remainingText.matches("-?\\d.*")) {
            throw invalidFormat();
        }
        return new DurationPeriod(Period.of(years, months, days), timeAmount);
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
    private static Duration toDuration(BigDecimal amount, long nanosPerUnit)
            throws LuckyNoInputException {
        try {
            long nanos = amount.multiply(BigDecimal.valueOf(nanosPerUnit))
                    .longValueExact();
            return Duration.ofNanos(nanos);
        } catch (ArithmeticException exception) {
            throw invalidFormat();
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

    /** Identifies the supported duration units in canonical order. */
    private enum Unit {
        YEAR(0, false),
        MONTH(1, false),
        DAY(2, true),
        HOUR(3, true),
        MINUTE(4, true);

        private final int order;
        private final boolean allowsDecimal;

        Unit(int order, boolean allowsDecimal) {
            this.order = order;
            this.allowsDecimal = allowsDecimal;
        }

        /**
         * Converts a parsed unit name into its unit definition.
         *
         * @param unitText unit name
         * @return matching unit
         */
        private static Unit fromText(String unitText) throws LuckyNoInputException {
            return switch (unitText.toLowerCase(Locale.ROOT)) {
                case "year", "years" -> YEAR;
                case "month", "months" -> MONTH;
                case "day", "days" -> DAY;
                case "hour", "hours" -> HOUR;
                case "minute", "minutes" -> MINUTE;
                default -> throw invalidFormat();
            };
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
}
