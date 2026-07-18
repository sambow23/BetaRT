package mcrtx.bridge;

final class McrtxRuntimeSettingFormatter {
    private McrtxRuntimeSettingFormatter() {
    }

    static String formatBoolean(boolean enabled) {
        return enabled ? "1" : "0";
    }

    static String formatHundredthsValue(int hundredthsValue) {
        int absoluteHundredthsValue = Math.abs(hundredthsValue);
        int wholeValue = absoluteHundredthsValue / 100;
        int fractionalValue = absoluteHundredthsValue % 100;
        String formattedValue = Integer.toString(wholeValue)
                + "."
                + (fractionalValue < 10 ? "0" : "")
                + Integer.toString(fractionalValue);
        return hundredthsValue < 0 ? "-" + formattedValue : formattedValue;
    }

    static String formatThousandthsValue(int thousandthsValue) {
        int absoluteThousandthsValue = Math.abs(thousandthsValue);
        int fractionalValue = absoluteThousandthsValue % 1000;
        String formattedValue = Integer.toString(absoluteThousandthsValue / 1000)
                + "."
                + (fractionalValue < 100 ? "0" : "")
                + (fractionalValue < 10 ? "0" : "")
                + Integer.toString(fractionalValue);
        return thousandthsValue < 0 ? "-" + formattedValue : formattedValue;
    }
}
