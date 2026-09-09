package luckynoslacky.luckyparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;

import org.junit.jupiter.api.Test;

import luckynoslacky.luckyexception.LuckyNoInputException;

/**
 * Tests the duration formats accepted by the snooze command.
 */
class DurationParserTest {
    /** Verifies that minute, hour, and day values become durations. */
    @Test
    void parse_integerShortUnits_returnsDuration() throws LuckyNoInputException {
        assertEquals(Duration.ofMinutes(2), DurationParser.parse("2 minutes"));
        assertEquals(Duration.ofHours(3), DurationParser.parse("3 hours"));
        assertEquals(Duration.ofDays(4), DurationParser.parse("4 days"));
    }

    /** Verifies that month and year values become periods. */
    @Test
    void parse_integerLongUnits_returnsPeriod() throws LuckyNoInputException {
        assertEquals(Period.ofMonths(2), DurationParser.parse("2 months"));
        assertEquals(Period.ofYears(3), DurationParser.parse("3 years"));
    }

    /** Verifies case-insensitive parsing with repeated whitespace and comments. */
    @Test
    void parse_caseWhitespaceAndTrailingText_ignoresTrailingText()
            throws LuckyNoInputException {
        TemporalAmount amount = DurationParser.parse("  2   HoUrS please");

        assertEquals(Duration.ofHours(2), amount);
    }

    /** Verifies that a zero duration is accepted as a no-op. */
    @Test
    void parse_zeroDuration_returnsZeroAmount() throws LuckyNoInputException {
        assertEquals(Duration.ZERO, DurationParser.parse("0 hours"));
    }

    /** Verifies that negative durations receive the dedicated error. */
    @Test
    void parse_negativeDuration_throwsInputException() {
        LuckyNoInputException exception = assertThrows(
                LuckyNoInputException.class, () -> DurationParser.parse("-2 hours"));

        assertEquals("Siao ah time where got negative one", exception.getMessage());
    }

    /** Verifies that decimal, abbreviated, and unknown durations are rejected. */
    @Test
    void parse_unsupportedDurationFormats_throwsInputException() {
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("1.5 hours"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("1hr"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("2 fortnights"));
    }
}
