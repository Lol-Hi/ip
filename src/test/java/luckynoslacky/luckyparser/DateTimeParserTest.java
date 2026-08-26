package luckynoslacky.luckyparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import luckynoslacky.luckyexception.LuckyNoInputException;
import luckynoslacky.luckyui.LuckyNoMessages;

/** Tests supported date/time formats, relative resolution, and invalid input. */
class DateTimeParserTest {
    private static final Clock TEST_CLOCK = Clock.fixed(
            Instant.parse("2026-08-25T10:00:00Z"), ZoneId.of("UTC"));
    private final DateTimeParser parser =
            new DateTimeParser(TEST_CLOCK);

    @Test
    void parseStartDateTime_supportedDateFormats_returnsExpectedDateTime() throws Exception {
        LocalDateTime expected = LocalDateTime.of(2030, 10, 15, 0, 0);

        for (String input : new String[] {
                "2030-10-15",
                "2030/10/15",
                "15/10/2030",
                "15-10-2030",
                "15 Oct 2030",
                "15 October 2030",
                "Oct 15 2030",
                "October 15 2030",
                "Tue Oct 15 2030",
                "Tuesday, October 15 2030"}) {
            assertEquals(expected, parser.parseStartDateTime(input).value(), input);
        }
    }

    @Test
    void parseStartDateTime_supportedTimeFormats_returnsExpectedDateTime() throws Exception {
        for (String input : new String[] {
                "14:15", "14:15:30", "2pm", "2 pm", "2:15pm",
                "2:15 pm", "2.15pm", "2.15 pm"}) {
            LocalDateTime expected = input.contains("30")
                    ? LocalDateTime.of(2026, 8, 25, 14, 15, 30)
                    : input.contains(":15") || input.contains(".15")
                    ? LocalDateTime.of(2026, 8, 25, 14, 15)
                    : LocalDateTime.of(2026, 8, 25, 14, 0);
            assertEquals(expected, parser.parseStartDateTime(input).value(), input);
            assertTrue(parser.parseStartDateTime(input).timeOnly(), input);
        }
    }

    @Test
    void parseStartDateTime_dateAndTime_returnsExpectedDateTime() throws Exception {
        assertEquals(LocalDateTime.of(2030, 10, 15, 14, 15),
                parser.parseStartDateTime("2030-10-15 14:15").value());
        assertEquals(LocalDateTime.of(2030, 10, 15, 14, 15),
                parser.parseStartDateTime("2030-10-15T2.15pm").value());
        assertFalse(parser.parseStartDateTime("2030-10-15 14:15").timeOnly());
    }

    @Test
    void parseStartDateTime_yearZeroDate_reinterpretsAsCompactMidnight() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 25, 0, 0),
                parser.parseStartDateTime("25 Aug 0000").value());
        assertEquals(LocalDateTime.of(2026, 8, 25, 0, 0),
                parser.parseEndDateTime("25 Aug 0000").value());
    }

    @Test
    void parseStartDateTime_fourDigitValues_returnsYears() throws Exception {
        assertEquals(LocalDateTime.of(2030, 8, 25, 0, 0),
                parser.parseStartDateTime("25 Aug 2030").value());
        assertEquals(LocalDateTime.of(2025, 8, 25, 0, 0),
                parser.parseStartDateTime("25 Aug 2025").value());
        assertEquals(LocalDateTime.of(2030, 1, 1, 0, 0),
                parser.parseStartDateTime("2030").value());
    }

    @Test
    void parseDateTime_dateOnlyValues_useStartAndEndDefaults() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 25, 0, 0),
                parser.parseStartDateTime("2026-08-25").value());
        assertEquals(LocalDateTime.of(2026, 8, 25, 23, 59),
                parser.parseEndDateTime("2026-08-25").value());
    }

    @Test
    void parseEndDateTime_endTimeAfterStart_usesReferenceDate() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 8, 25, 14, 0);

        DateTimeParser.ParsedDateTime end =
                parser.parseEndDateTime("4pm", start);

        assertEquals(LocalDateTime.of(2026, 8, 25, 16, 0), end.value());
        assertTrue(end.timeOnly());
    }

    @Test
    void parseEndDateTime_endTimeBeforeStart_usesNextDate() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 8, 25, 23, 0);

        DateTimeParser.ParsedDateTime end =
                parser.parseEndDateTime("1am", start);

        assertEquals(LocalDateTime.of(2026, 8, 26, 1, 0), end.value());
        assertTrue(end.timeOnly());
    }

    @Test
    void parseEndDateTime_explicitEndDate_ignoresReferenceDate() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 8, 25, 23, 0);

        DateTimeParser.ParsedDateTime end =
                parser.parseEndDateTime("26 Aug 2030 1am", start);

        assertEquals(LocalDateTime.of(2030, 8, 26, 1, 0), end.value());
        assertFalse(end.timeOnly());
    }

    @Test
    void resolvesTimeOnlyValuesToTodayOrTomorrow() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 25, 14, 0),
                parser.parseStartDateTime("2pm").value());
        assertEquals(LocalDateTime.of(2026, 8, 26, 9, 0),
                parser.parseStartDateTime("9am").value());
    }

    @Test
    void parseStartDateTime_namedRelativeDates_returnsExpectedDates() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 25, 0, 0),
                parser.parseStartDateTime("today").value());
        assertEquals(LocalDateTime.of(2026, 8, 26, 0, 0),
                parser.parseStartDateTime("tomorrow").value());
        assertEquals(LocalDateTime.of(2026, 8, 26, 0, 0),
                parser.parseStartDateTime("tmr").value());
        assertEquals(LocalDateTime.of(2026, 8, 24, 0, 0),
                parser.parseStartDateTime("yesterday").value());
        assertEquals(LocalDateTime.of(2026, 8, 24, 0, 0),
                parser.parseStartDateTime("ytd").value());
        assertEquals(LocalDateTime.of(2026, 8, 25, 23, 59),
                parser.parseEndDateTime("today").value());
        assertEquals(LocalDateTime.of(2026, 8, 26, 14, 0),
                parser.parseStartDateTime("tomorrow 2pm").value());
    }

    @Test
    void parseStartDateTime_relativeDayOfMonthForms_returnsExpectedDates() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 30, 0, 0),
                parser.parseStartDateTime("30th").value());
        assertEquals(LocalDateTime.of(2026, 9, 15, 0, 0),
                parser.parseStartDateTime("the 15th").value());
        assertEquals(LocalDateTime.of(2026, 9, 15, 0, 0),
                parser.parseStartDateTime("next 15th").value());
        assertEquals(LocalDateTime.of(2026, 10, 15, 0, 0),
                parser.parseStartDateTime("next next 15th").value());
        assertEquals(LocalDateTime.of(2026, 10, 15, 0, 0),
                parser.parseStartDateTime("the following 15th").value());
    }

    @Test
    void parseStartDateTime_relativeWeekdayForms_returnsExpectedDates() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 31, 0, 0),
                parser.parseStartDateTime("Monday").value());
        assertEquals(LocalDateTime.of(2026, 8, 24, 0, 0),
                parser.parseStartDateTime("this Monday").value());
        assertEquals(LocalDateTime.of(2026, 8, 25, 0, 0),
                parser.parseStartDateTime("this Tuesday").value());
        assertEquals(LocalDateTime.of(2026, 8, 30, 0, 0),
                parser.parseStartDateTime("this Sunday").value());
        assertEquals(LocalDateTime.of(2026, 9, 2, 0, 0),
                parser.parseStartDateTime("next Wednesday").value());
        assertEquals(LocalDateTime.of(2026, 9, 7, 0, 0),
                parser.parseStartDateTime("next next Monday").value());
        assertEquals(LocalDateTime.of(2026, 9, 9, 0, 0),
                parser.parseStartDateTime("the following Wednesday").value());
        assertEquals(LocalDateTime.of(2026, 8, 26, 0, 0),
                parser.parseStartDateTime("this coming Wednesday").value());
        assertEquals(LocalDateTime.of(2026, 9, 1, 0, 0),
                parser.parseStartDateTime("the coming Tuesday").value());
    }

    @Test
    void parseStartDateTime_relativeMonthAndYearForms_returnsExpectedDates() throws Exception {
        assertEquals(LocalDateTime.of(2027, 6, 6, 0, 0),
                parser.parseStartDateTime("June 6th").value());
        assertEquals(LocalDateTime.of(2026, 9, 1, 0, 0),
                parser.parseStartDateTime("next month").value());
        assertEquals(LocalDateTime.of(2026, 10, 1, 0, 0),
                parser.parseStartDateTime("next next month").value());
        assertEquals(LocalDateTime.of(2026, 10, 1, 0, 0),
                parser.parseStartDateTime("the following month").value());
        assertEquals(LocalDateTime.of(2026, 8, 1, 0, 0),
                parser.parseStartDateTime("this month").value());
        assertEquals(LocalDateTime.of(2027, 1, 1, 0, 0),
                parser.parseStartDateTime("next year").value());
        assertEquals(LocalDateTime.of(2028, 1, 1, 0, 0),
                parser.parseStartDateTime("next next year").value());
        assertEquals(LocalDateTime.of(2028, 1, 1, 0, 0),
                parser.parseStartDateTime("the following year").value());
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0),
                parser.parseStartDateTime("this year").value());
    }

    @Test
    void parseStartDateTime_invalidDates_throwsInputException() {
        assertInvalid("the 32nd");
        assertInvalid("2030-01-32");
        assertInvalid("2030-13-01");
        assertInvalid("April 31st");
        assertInvalid("June 31st");
        assertInvalid("February 30th");
        assertInvalid("2030-04-31");
    }

    @Test
    void parseStartDateTime_invalidTimes_throwsInputException() {
        assertInvalid("24:00");
        assertInvalid("25:00");
        assertInvalid("12:60");
        assertInvalid("2:60pm");
        assertInvalid("13pm");
    }

    @Test
    void parseStartDateTime_blankNullOrPlaintext_throwsInputException() {
        assertInvalid("");
        assertInvalid("   ");
        assertInvalid(null);
        assertInvalid("this is completely random plaintext");
    }

    @Test
    void formatForStorage_validDateTime_roundTripsThroughStorageParser() {
        LocalDateTime value = LocalDateTime.of(2030, 10, 15, 14, 15);
        String stored = DateTimeParser.formatForStorage(value);

        assertEquals("2030-10-15 14:15", stored);
        assertEquals(value, DateTimeParser.parseFromStorage(stored));
        assertEquals("", DateTimeParser.formatForStorage(null));
        assertEquals(null, DateTimeParser.parseFromStorage(""));
    }

    @Test
    void now_fixedClock_returnsFixedTime() {
        assertEquals(LocalDateTime.of(2026, 8, 25, 10, 0), parser.now());
    }

    @Test
    void DateTimeParser_nullClockOrReference_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new DateTimeParser(null));
        assertThrows(IllegalArgumentException.class,
                () -> parser.parseEndDateTime("4pm", null));
    }

    private void assertInvalid(String input) {
        LuckyNoInputException exception = assertThrows(LuckyNoInputException.class,
                () -> parser.parseStartDateTime(input));
        assertEquals(LuckyNoMessages.invalidDateTimeMessage(), exception.getMessage());
    }
}
