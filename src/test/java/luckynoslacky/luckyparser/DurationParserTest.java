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

    /** Verifies decimal calendar units use the dedicated user message. */
    @Test
    void parse_decimalCalendarUnits_throwsDedicatedInputException() {
        LuckyNoInputException monthException = assertThrows(
                LuckyNoInputException.class, () -> DurationParser.parse(
                        "1.5 months"));
        LuckyNoInputException yearException = assertThrows(
                LuckyNoInputException.class, () -> DurationParser.parse(
                        "1.5 years"));

        assertEquals(
                "Paiseh bro... i cannot settle decimal values for years and months yet...",
                monthException.getMessage());
        assertEquals(monthException.getMessage(), yearException.getMessage());
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

    /** Verifies abbreviated units map to their canonical duration units. */
    @Test
    void parse_abbreviatedUnits_returnsExpectedAmounts()
            throws LuckyNoInputException {
        assertEquals(new DurationPeriod(Period.ofYears(1), Duration.ZERO),
                DurationParser.parse("1yr"));
        assertEquals(new DurationPeriod(Period.ofMonths(1), Duration.ZERO),
                DurationParser.parse("1 mos"));
        assertEquals(new DurationPeriod(Period.ofMonths(1), Duration.ZERO),
                DurationParser.parse("1mo"));
        assertEquals(new DurationPeriod(Period.ofDays(1), Duration.ZERO),
                DurationParser.parse("1ds"));
        assertEquals(new DurationPeriod(Period.ofDays(1), Duration.ZERO),
                DurationParser.parse("1d"));
        assertEquals(new DurationPeriod(Period.ZERO, Duration.ofHours(1)),
                DurationParser.parse("1hrs"));
        assertEquals(new DurationPeriod(Period.ZERO, Duration.ofHours(1)),
                DurationParser.parse("1hr"));
        assertEquals(new DurationPeriod(Period.ZERO, Duration.ofHours(1)),
                DurationParser.parse("1h"));
        assertEquals(new DurationPeriod(Period.ZERO, Duration.ofMinutes(1)),
                DurationParser.parse("1 mins"));
        assertEquals(new DurationPeriod(Period.ZERO, Duration.ofMinutes(1)),
                DurationParser.parse("1min"));
    }

    /** Verifies abbreviated units accept optional whitespace and case changes. */
    @Test
    void parse_abbreviatedUnitsWithWhitespaceAndCase_returnsExpectedAmounts()
            throws LuckyNoInputException {
        assertEquals(new DurationPeriod(Period.ofMonths(1), Duration.ZERO),
                DurationParser.parse("1   MO"));
        assertEquals(new DurationPeriod(Period.ZERO, Duration.ofHours(1)),
                DurationParser.parse("1 H"));
        assertEquals(new DurationPeriod(Period.ZERO, Duration.ofHours(1)),
                DurationParser.parse("1 hr"));
        assertEquals(new DurationPeriod(Period.ZERO, Duration.ofMinutes(90)),
                DurationParser.parse("1.5 h"));
        assertEquals(new DurationPeriod(Period.ofDays(1), Duration.ofHours(12)),
                DurationParser.parse("1.5 d"));
    }

    /** Verifies abbreviated components can be combined in canonical order. */
    @Test
    void parse_combinedAbbreviatedUnits_returnsExpectedAmounts()
            throws LuckyNoInputException {
        assertEquals(
                new DurationPeriod(Period.of(1, 2, 3), Duration.ofHours(4)
                        .plusMinutes(30)),
                DurationParser.parse("1yr 2mos 3ds 4hrs 30mins"));
    }

    /** Verifies natural-language components become duration amounts. */
    @Test
    void parse_naturalLanguageComponents_returnsExpectedAmounts()
            throws LuckyNoInputException {
        assertEquals(new DurationPeriod(Period.ofDays(7), Duration.ZERO),
                DurationParser.parse("a week"));
        assertEquals(new DurationPeriod(Period.ofDays(7), Duration.ZERO),
                DurationParser.parse("one more week"));
        assertEquals(new DurationPeriod(Period.ofDays(7), Duration.ZERO),
                DurationParser.parse("1 more week"));
        assertEquals(new DurationPeriod(Period.ZERO, Duration.ofMinutes(30)),
                DurationParser.parse("half an hour"));
        assertEquals(new DurationPeriod(Period.ZERO, Duration.ofHours(12)),
                DurationParser.parse("half a day"));
        assertEquals(new DurationPeriod(Period.ofDays(3), Duration.ofHours(12)),
                DurationParser.parse("half a week"));
    }

    /** Verifies natural-language components combine in canonical order. */
    @Test
    void parse_combinedNaturalLanguageComponents_returnsExpectedAmounts()
            throws LuckyNoInputException {
        assertEquals(
                new DurationPeriod(Period.ofMonths(1).plusDays(14),
                        Duration.ofHours(2)),
                DurationParser.parse("one month two weeks 2 hours"));
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
                DurationParser.parse("1.5mo"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("1.5yr"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("1.5 weeks"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("half"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("one and a half hours"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("1 hour 2 hours"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("2 days 1 month"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("1m"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("1h30min"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("2 fortnights"));
        assertThrows(LuckyNoInputException.class, () ->
                DurationParser.parse("1 hour /to tomorrow"));
    }

    /** Verifies invalid duration errors include the original duration text. */
    @Test
    void parse_invalidDuration_includesOriginalInputInErrorMessage() {
        LuckyNoInputException exception = assertThrows(
                LuckyNoInputException.class, () -> DurationParser.parse("1h30min"));

        assertEquals(
                "Eh can you be more specific anot, what do you mean by \"1h30min\" sia?",
                exception.getMessage());
    }
}
