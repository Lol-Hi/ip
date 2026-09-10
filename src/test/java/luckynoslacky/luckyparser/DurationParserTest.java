package luckynoslacky.luckyparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;

import org.junit.jupiter.api.Test;

import luckynoslacky.luckyexception.LuckyNoInputException;

/**
 * Tests the duration formats accepted by the snooze command.
 */
class DurationParserTest {
    /** Verifies decimal fixed-length units become clock amounts. */
    @Test
    void parse_decimalFixedLengthUnits_returnsClockAmounts()
            throws LuckyNoInputException {
        assertEquals(new DurationPeriod(Period.ZERO, Duration.ofMinutes(90)),
                DurationParser.parse("1.5 hours"));
        assertEquals(new DurationPeriod(Period.ZERO, Duration.ofMinutes(1)),
                DurationParser.parse("1.0 minutes"));
        assertEquals(new DurationPeriod(Period.ofDays(1), Duration.ofHours(12)),
                DurationParser.parse("1.5 days"));
    }

    /** Verifies integer month and year units become calendar amounts. */
    @Test
    void parse_integerCalendarUnits_returnsCalendarAmounts()
            throws LuckyNoInputException {
        assertEquals(new DurationPeriod(Period.ofMonths(2), Duration.ZERO),
                DurationParser.parse("2 months"));
        assertEquals(new DurationPeriod(Period.ofYears(3), Duration.ZERO),
                DurationParser.parse("3 years"));
    }

    /** Verifies components are parsed in canonical order and combined. */
    @Test
    void parse_combinedUnits_returnsCalendarAndClockAmounts()
            throws LuckyNoInputException {
        assertEquals(
                new DurationPeriod(Period.of(1, 2, 3), Duration.ofHours(4)
                        .plusMinutes(30)),
                DurationParser.parse("1 year 2 months 3 days 4.5 hours"));
    }

    /** Verifies calendar amounts are applied before clock amounts. */
    @Test
    void addTo_monthEndAndLeapYearValues_usesCalendarFirst()
            throws LuckyNoInputException {
        DurationPeriod monthAndDay = DurationParser.parse("1 month 1 day");
        DurationPeriod monthAndHalfDay = DurationParser.parse("1 month 1.5 days");

        assertEquals(LocalDateTime.of(2026, 3, 1, 12, 0),
                monthAndDay.addTo(LocalDateTime.of(2026, 1, 31, 12, 0)));
        assertEquals(LocalDateTime.of(2028, 3, 1, 12, 0),
                monthAndDay.addTo(LocalDateTime.of(2028, 1, 31, 12, 0)));
        assertEquals(LocalDateTime.of(2026, 3, 2, 0, 0),
                monthAndHalfDay.addTo(LocalDateTime.of(2026, 1, 31, 12, 0)));
    }

    /** Verifies zero and trailing commentary are accepted. */
    @Test
    void parse_zeroAndTrailingText_returnsExpectedAmount()
            throws LuckyNoInputException {
        assertEquals(new DurationPeriod(Period.ZERO, Duration.ZERO),
                DurationParser.parse("0 hours 0 minutes"));
        assertEquals(new DurationPeriod(Period.ZERO, Duration.ofMinutes(90)),
                DurationParser.parse("  1   HoUr 30 minutes please"));
    }

    /** Verifies negative values receive the dedicated error. */
    @Test
    void parse_negativeComponent_throwsDedicatedInputException() {
        LuckyNoInputException exception = assertThrows(
                LuckyNoInputException.class, () -> DurationParser.parse(
                        "1 hour -2 minutes"));

        assertEquals("Siao ah time where got negative one", exception.getMessage());
    }

    /** Verifies unsupported, repeated, and incorrectly ordered components fail. */
    @Test
    void parse_unsupportedComponents_throwsInputException() {
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("1.5 months"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("1.5 years"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("1 hour 2 hours"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("2 days 1 month"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("1.5h"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("2 fortnights"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("1 hour /to tomorrow"));
    }
}
