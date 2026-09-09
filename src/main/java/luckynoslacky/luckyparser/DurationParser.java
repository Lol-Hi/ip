package luckynoslacky.luckyparser;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import luckynoslacky.luckyexception.LuckyNoInputException;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Parses the integer duration formats supported by the snooze command.
 */
public final class DurationParser {
    private static final Pattern DURATION_PATTERN = Pattern.compile(
            "^([+-]?\\d+)\\s+(minutes?|hours?|days?|months?|years?)\\b.*$",
            Pattern.CASE_INSENSITIVE);

    private DurationParser() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Parses a non-negative integer duration and ignores trailing commentary.
     *
     * @param durationText duration text to parse
     * @return parsed duration as a {@link Duration} or {@link Period}
     * @throws LuckyNoInputException if the duration is malformed or negative
     */
    public static TemporalAmount parse(String durationText)
            throws LuckyNoInputException {
        if (durationText == null || durationText.isBlank()) {
            throw invalidFormat();
        }

        Matcher matcher = DURATION_PATTERN.matcher(durationText.trim());
        if (!matcher.matches()) {
            throw invalidFormat();
        }

        long amount;
        try {
            amount = Long.parseLong(matcher.group(1));
        } catch (NumberFormatException exception) {
            throw invalidFormat();
        }

        if (amount < 0) {
            throw new LuckyNoInputException(
                    "Siao ah time where got negative one");
        }

        String unit = matcher.group(2).toLowerCase(Locale.ROOT);
        try {
            return switch (unit) {
                case "minute", "minutes" -> Duration.ofMinutes(amount);
                case "hour", "hours" -> Duration.ofHours(amount);
                case "day", "days" -> Duration.ofDays(amount);
                case "month", "months" -> Period.ofMonths(Math.toIntExact(amount));
                case "year", "years" -> Period.ofYears(Math.toIntExact(amount));
                default -> throw invalidFormat();
            };
        } catch (ArithmeticException exception) {
            throw invalidFormat();
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
