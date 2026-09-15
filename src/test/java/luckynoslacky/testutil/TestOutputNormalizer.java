package luckynoslacky.testutil;

/**
 * Normalizes platform-dependent portions of process output for integration
 * tests.
 */
public final class TestOutputNormalizer {
    private TestOutputNormalizer() {
    }

    /**
     * Normalizes line endings and localized date/time names while preserving
     * all other output content.
     *
     * @param output process output to normalize
     * @return normalized output
     */
    public static String normalize(String output) {
        String normalized = output.replace("\r\n", "\n")
                .replace('\r', '\n');
        normalized = normalized.replaceAll(
                "(?i)\\b(?:mon|tue|wed|thu|fri|sat|sun)\\b", "DAY");
        normalized = normalized.replaceAll(
                "(?i)\\b(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b",
                "MONTH");
        normalized = normalized.replaceAll(
                "(?i)(?<![a-z])(?:am|pm)\\b", "MERIDIEM");
        return normalized.endsWith("\n")
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
    }
}
