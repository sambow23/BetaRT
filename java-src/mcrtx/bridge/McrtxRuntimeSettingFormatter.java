package mcrtx.bridge;

final class McrtxRuntimeSettingFormatter {
    private McrtxRuntimeSettingFormatter() {
    }

    static String formatBoolean(boolean enabled) {
        return enabled ? "1" : "0";
    }

    static String formatThousandthsValue(int thousandthsValue) {
        int absoluteThousandthsValue = Math.abs(thousandthsValue);
        String formattedValue = Integer.toString(absoluteThousandthsValue / 1000)
            + "."
            + (absoluteThousandthsValue % 1000 < 100 ? "0" : "")
            + (absoluteThousandthsValue % 1000 < 10 ? "0" : "")
            + Integer.toString(absoluteThousandthsValue % 1000);
        if (thousandthsValue < 0) {
            return "-" + formattedValue;
        }
        return formattedValue;
    }

    static String formatUpscalerType(int type) {
        switch (normalizeUpscalerType(type)) {
            case McrtxRuntimeSettings.UPSCALER_TYPE_NONE:
                return "None";
            case McrtxRuntimeSettings.UPSCALER_TYPE_TAAU:
                return "TAAU";
            case McrtxRuntimeSettings.UPSCALER_TYPE_XESS:
                return "XeSS";
            case McrtxRuntimeSettings.UPSCALER_TYPE_DLSS:
            default:
                return "DLSS";
        }
    }

    static String formatDlssPreset(int preset) {
        switch (normalizeDlssPreset(preset)) {
            case McrtxRuntimeSettings.DLSS_PRESET_QUALITY:
                return "MaxQuality";
            case McrtxRuntimeSettings.DLSS_PRESET_BALANCED:
                return "Balanced";
            case McrtxRuntimeSettings.DLSS_PRESET_PERFORMANCE:
                return "MaxPerf";
            case McrtxRuntimeSettings.DLSS_PRESET_ULTRA_PERFORMANCE:
                return "UltraPerf";
            case McrtxRuntimeSettings.DLSS_PRESET_DLAA:
                return "FullResolution";
            case McrtxRuntimeSettings.DLSS_PRESET_AUTO:
            default:
                return "Auto";
        }
    }

    static String formatXessPreset(int preset) {
        switch (normalizeXessPreset(preset)) {
            case McrtxRuntimeSettings.XESS_PRESET_ULTRA_PERFORMANCE:
                return "UltraPerf";
            case McrtxRuntimeSettings.XESS_PRESET_PERFORMANCE:
                return "Performance";
            case McrtxRuntimeSettings.XESS_PRESET_QUALITY:
                return "Quality";
            case McrtxRuntimeSettings.XESS_PRESET_ULTRA_QUALITY:
                return "UltraQuality";
            case McrtxRuntimeSettings.XESS_PRESET_ULTRA_QUALITY_PLUS:
                return "UltraQualityPlus";
            case McrtxRuntimeSettings.XESS_PRESET_NATIVE_AA:
                return "NativeAA";
            case McrtxRuntimeSettings.XESS_PRESET_BALANCED:
            default:
                return "Balanced";
        }
    }

    static String formatTaauPreset(int preset) {
        switch (normalizeTaauPreset(preset)) {
            case McrtxRuntimeSettings.TAAU_PRESET_ULTRA_PERFORMANCE:
                return "UltraPerformance";
            case McrtxRuntimeSettings.TAAU_PRESET_PERFORMANCE:
                return "Performance";
            case McrtxRuntimeSettings.TAAU_PRESET_QUALITY:
                return "Quality";
            case McrtxRuntimeSettings.TAAU_PRESET_FULLSCREEN:
                return "Fullscreen";
            case McrtxRuntimeSettings.TAAU_PRESET_BALANCED:
            default:
                return "Balanced";
        }
    }

    static String formatRtQuality(int quality) {
        switch (normalizeRtQuality(quality)) {
            case McrtxRuntimeSettings.RT_QUALITY_POTATO:
                return "Potato";
            case McrtxRuntimeSettings.RT_QUALITY_LOW:
                return "Low";
            case McrtxRuntimeSettings.RT_QUALITY_MEDIUM:
                return "Medium";
            case McrtxRuntimeSettings.RT_QUALITY_ULTRA:
                return "Ultra";
            case McrtxRuntimeSettings.RT_QUALITY_HIGH:
            default:
                return "High";
        }
    }

    static String formatBlockOutlineStyle(int style) {
        switch (normalizeBlockOutlineStyle(style)) {
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_SUBTLE:
                return "Subtle";
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_GLOW:
                return "Glow";
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_RGB:
                return "RGB";
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_THIN:
                return "Thin";
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_SOLID:
                return "Solid";
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_BOLD:
            default:
                return "Bold";
        }
    }

    static String formatDisplacementFactorHundredths(int factorHundredths) {
        int normalizedFactorHundredths = normalizeDisplacementFactorHundredths(factorHundredths);
        return formatHundredthsValue(normalizedFactorHundredths);
    }

    static String formatHundredthsValue(int hundredthsValue) {
        int absoluteHundredthsValue = Math.abs(hundredthsValue);
        int wholeValue = absoluteHundredthsValue / 100;
        int fractionalValue = absoluteHundredthsValue % 100;
        String formattedValue = Integer.toString(wholeValue)
            + "."
            + (fractionalValue < 10 ? "0" : "")
            + Integer.toString(fractionalValue);
        if (hundredthsValue < 0) {
            return "-" + formattedValue;
        }
        return formattedValue;
    }

    private static int normalizeUpscalerType(int type) {
        if (type == McrtxRuntimeSettings.UPSCALER_TYPE_NONE
                || type == McrtxRuntimeSettings.UPSCALER_TYPE_DLSS
                || type == McrtxRuntimeSettings.UPSCALER_TYPE_TAAU
                || type == McrtxRuntimeSettings.UPSCALER_TYPE_XESS) {
            return type;
        }
        return McrtxRuntimeSettings.UPSCALER_TYPE_DLSS;
    }

    private static int normalizeDlssPreset(int preset) {
        if (preset >= McrtxRuntimeSettings.DLSS_PRESET_ULTRA_PERFORMANCE
                && preset <= McrtxRuntimeSettings.DLSS_PRESET_DLAA) {
            return preset;
        }
        return McrtxRuntimeSettings.DLSS_PRESET_AUTO;
    }

    private static int normalizeXessPreset(int preset) {
        if (preset >= McrtxRuntimeSettings.XESS_PRESET_ULTRA_PERFORMANCE
                && preset <= McrtxRuntimeSettings.XESS_PRESET_NATIVE_AA) {
            return preset;
        }
        return McrtxRuntimeSettings.XESS_PRESET_BALANCED;
    }

    private static int normalizeTaauPreset(int preset) {
        if (preset >= McrtxRuntimeSettings.TAAU_PRESET_ULTRA_PERFORMANCE
                && preset <= McrtxRuntimeSettings.TAAU_PRESET_FULLSCREEN) {
            return preset;
        }
        return McrtxRuntimeSettings.TAAU_PRESET_BALANCED;
    }

    private static int normalizeRtQuality(int quality) {
        if ((quality >= McrtxRuntimeSettings.RT_QUALITY_LOW && quality <= McrtxRuntimeSettings.RT_QUALITY_ULTRA)
                || quality == McrtxRuntimeSettings.RT_QUALITY_POTATO) {
            return quality;
        }
        return McrtxRuntimeSettings.RT_QUALITY_HIGH;
    }

    private static int normalizeBlockOutlineStyle(int style) {
        if (style >= McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_SUBTLE
                && style <= McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_THIN) {
            return style;
        }
        return McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_BOLD;
    }

    private static int normalizeDisplacementFactorHundredths(int factorHundredths) {
        if (factorHundredths < McrtxRuntimeSettings.MIN_DISPLACEMENT_FACTOR_HUNDREDTHS) {
            return McrtxRuntimeSettings.MIN_DISPLACEMENT_FACTOR_HUNDREDTHS;
        }
        if (factorHundredths > McrtxRuntimeSettings.MAX_DISPLACEMENT_FACTOR_HUNDREDTHS) {
            return McrtxRuntimeSettings.MAX_DISPLACEMENT_FACTOR_HUNDREDTHS;
        }
        return factorHundredths;
    }
}
