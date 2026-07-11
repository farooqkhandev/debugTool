package com.quadlogixs.debugtool.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DynamicTypeLevel(val label: String, val scale: Float) {
    XS("XS", 0.85f),
    S("S", 0.92f),
    M("M (default)", 1.0f),
    L("L", 1.12f),
    XL("XL", 1.25f),
    XXL("XXL", 1.4f),
}

data class ScreenSizePreset(
    val id: String,
    val displayName: String,
    val widthDp: Int,
    val heightDp: Int,
    val manufacturer: String,
) {
    val sizeLabel: String
        get() = "${widthDp}×${heightDp} dp"
}

data class MockLocationPreset(
    val id: String,
    val label: String,
    val latitude: Double,
    val longitude: Double,
)

object DebugUiToolsStore {
    val screenSizeManufacturerOrder: List<String> = listOf(
        "Samsung",
        "OPPO",
        "VIVO",
        "Motorola",
        "Infinix",
        "Google",
        "Xiaomi",
        "OnePlus",
        "Realme",
        "Tecno",
        "Tablets",
    )

    val screenSizePresets: List<ScreenSizePreset> = listOf(
        // Samsung
        ScreenSizePreset("samsung_s25_ultra", "Galaxy S25 Ultra (6.9\")", 384, 824, "Samsung"),
        ScreenSizePreset("samsung_s25_plus", "Galaxy S25+ (6.7\")", 412, 915, "Samsung"),
        ScreenSizePreset("samsung_s25", "Galaxy S25 (6.2\")", 360, 780, "Samsung"),
        ScreenSizePreset("samsung_s24_ultra", "Galaxy S24 Ultra (6.8\")", 384, 824, "Samsung"),
        ScreenSizePreset("samsung_s24_plus", "Galaxy S24+ (6.7\")", 412, 915, "Samsung"),
        ScreenSizePreset("samsung_s24", "Galaxy S24 (6.2\")", 360, 780, "Samsung"),
        ScreenSizePreset("samsung_a55", "Galaxy A55 (6.6\")", 412, 915, "Samsung"),
        ScreenSizePreset("samsung_a35", "Galaxy A35 (6.6\")", 412, 915, "Samsung"),
        ScreenSizePreset("samsung_a15", "Galaxy A15 (6.5\")", 360, 800, "Samsung"),
        ScreenSizePreset("samsung_z_fold6_cover", "Galaxy Z Fold 6 — Cover (6.3\")", 374, 904, "Samsung"),
        ScreenSizePreset("samsung_z_fold6_inner", "Galaxy Z Fold 6 — Inner (7.6\")", 690, 829, "Samsung"),
        ScreenSizePreset("samsung_z_flip6", "Galaxy Z Flip 6 (6.7\")", 384, 892, "Samsung"),

        // OPPO
        ScreenSizePreset("oppo_find_x7_ultra", "Find X7 Ultra (6.82\")", 412, 915, "OPPO"),
        ScreenSizePreset("oppo_find_x7", "Find X7 (6.78\")", 412, 915, "OPPO"),
        ScreenSizePreset("oppo_reno12_pro", "Reno 12 Pro (6.7\")", 412, 915, "OPPO"),
        ScreenSizePreset("oppo_reno12", "Reno 12 (6.7\")", 412, 915, "OPPO"),
        ScreenSizePreset("oppo_a79", "A79 5G (6.67\")", 360, 800, "OPPO"),
        ScreenSizePreset("oppo_a58", "A58 (6.72\")", 360, 800, "OPPO"),

        // VIVO
        ScreenSizePreset("vivo_x100_ultra", "X100 Ultra (6.78\")", 412, 915, "VIVO"),
        ScreenSizePreset("vivo_x100_pro", "X100 Pro (6.78\")", 412, 915, "VIVO"),
        ScreenSizePreset("vivo_v30_pro", "V30 Pro (6.78\")", 412, 915, "VIVO"),
        ScreenSizePreset("vivo_v30", "V30 (6.78\")", 412, 915, "VIVO"),
        ScreenSizePreset("vivo_y100", "Y100 5G (6.67\")", 360, 800, "VIVO"),
        ScreenSizePreset("vivo_y28", "Y28 (6.67\")", 360, 800, "VIVO"),

        // Motorola
        ScreenSizePreset("motorola_edge50_ultra", "Edge 50 Ultra (6.7\")", 412, 915, "Motorola"),
        ScreenSizePreset("motorola_edge50_pro", "Edge 50 Pro (6.7\")", 412, 915, "Motorola"),
        ScreenSizePreset("motorola_edge50", "Edge 50 (6.7\")", 412, 915, "Motorola"),
        ScreenSizePreset("motorola_moto_g84", "Moto G84 (6.5\")", 412, 915, "Motorola"),
        ScreenSizePreset("motorola_moto_g54", "Moto G54 (6.5\")", 412, 891, "Motorola"),
        ScreenSizePreset("motorola_razr50_ultra", "Razr 50 Ultra (6.9\")", 384, 892, "Motorola"),

        // Infinix
        ScreenSizePreset("infinix_note40_pro", "Note 40 Pro (6.78\")", 412, 915, "Infinix"),
        ScreenSizePreset("infinix_note40", "Note 40 (6.78\")", 412, 915, "Infinix"),
        ScreenSizePreset("infinix_hot40_pro", "Hot 40 Pro (6.78\")", 360, 800, "Infinix"),
        ScreenSizePreset("infinix_hot40", "Hot 40 (6.6\")", 360, 800, "Infinix"),
        ScreenSizePreset("infinix_zero40", "Zero 40 (6.78\")", 412, 915, "Infinix"),
        ScreenSizePreset("infinix_smart8", "Smart 8 (6.6\")", 360, 800, "Infinix"),

        // Google
        ScreenSizePreset("google_pixel9_pro_xl", "Pixel 9 Pro XL (6.8\")", 412, 892, "Google"),
        ScreenSizePreset("google_pixel9_pro", "Pixel 9 Pro (6.3\")", 412, 915, "Google"),
        ScreenSizePreset("google_pixel9", "Pixel 9 (6.3\")", 412, 915, "Google"),
        ScreenSizePreset("google_pixel8_pro", "Pixel 8 Pro (6.7\")", 412, 892, "Google"),
        ScreenSizePreset("google_pixel8a", "Pixel 8a (6.1\")", 393, 851, "Google"),

        // Xiaomi
        ScreenSizePreset("xiaomi_14_ultra", "14 Ultra (6.73\")", 412, 915, "Xiaomi"),
        ScreenSizePreset("xiaomi_14", "14 (6.36\")", 393, 851, "Xiaomi"),
        ScreenSizePreset("xiaomi_redmi_note13_pro", "Redmi Note 13 Pro (6.67\")", 412, 915, "Xiaomi"),
        ScreenSizePreset("xiaomi_redmi_13c", "Redmi 13C (6.74\")", 360, 800, "Xiaomi"),
        ScreenSizePreset("xiaomi_poco_x6_pro", "POCO X6 Pro (6.67\")", 412, 915, "Xiaomi"),

        // OnePlus
        ScreenSizePreset("oneplus_12", "OnePlus 12 (6.82\")", 412, 915, "OnePlus"),
        ScreenSizePreset("oneplus_12r", "OnePlus 12R (6.78\")", 412, 915, "OnePlus"),
        ScreenSizePreset("oneplus_nord4", "Nord 4 (6.74\")", 412, 915, "OnePlus"),
        ScreenSizePreset("oneplus_nord_ce4", "Nord CE 4 (6.7\")", 412, 915, "OnePlus"),

        // Realme
        ScreenSizePreset("realme_gt6", "GT 6 (6.78\")", 412, 915, "Realme"),
        ScreenSizePreset("realme_12_pro_plus", "12 Pro+ (6.7\")", 412, 915, "Realme"),
        ScreenSizePreset("realme_12", "Realme 12 (6.72\")", 360, 800, "Realme"),
        ScreenSizePreset("realme_c67", "C67 (6.72\")", 360, 800, "Realme"),

        // Tecno
        ScreenSizePreset("tecno_camon30_premier", "Camon 30 Premier (6.77\")", 412, 915, "Tecno"),
        ScreenSizePreset("tecno_camon30", "Camon 30 (6.78\")", 412, 915, "Tecno"),
        ScreenSizePreset("tecno_spark30_pro", "Spark 30 Pro (6.78\")", 360, 800, "Tecno"),
        ScreenSizePreset("tecno_pova6", "Pova 6 (6.78\")", 412, 915, "Tecno"),

        // Tablets
        ScreenSizePreset("tablet_7", "Tablet 7\" (WUXGA)", 600, 960, "Tablets"),
        ScreenSizePreset("tablet_8", "Tablet 8\" (HD)", 640, 1024, "Tablets"),
        ScreenSizePreset("tablet_10", "Tablet 10\" (FHD)", 800, 1280, "Tablets"),
        ScreenSizePreset("tablet_12", "Tablet 12\" (WUXGA+)", 900, 1440, "Tablets"),
    )

    val locationPresets: List<MockLocationPreset> = listOf(
        MockLocationPreset("karachi", "Karachi, Pakistan", 24.8607, 67.0011),
        MockLocationPreset("lahore", "Lahore, Pakistan", 31.5204, 74.3587),
        MockLocationPreset("islamabad", "Islamabad, Pakistan", 33.6844, 73.0479),
        MockLocationPreset("dubai", "Dubai, UAE", 25.2048, 55.2708),
        MockLocationPreset("london", "London, UK", 51.5074, -0.1278),
    )

    private val _dynamicTypeLevel = MutableStateFlow(DynamicTypeLevel.M)
    val dynamicTypeLevel: StateFlow<DynamicTypeLevel> = _dynamicTypeLevel.asStateFlow()

    private val _animationSpeedScale = MutableStateFlow(1f)
    val animationSpeedScale: StateFlow<Float> = _animationSpeedScale.asStateFlow()

    private val _gridOverlayEnabled = MutableStateFlow(false)
    val gridOverlayEnabled: StateFlow<Boolean> = _gridOverlayEnabled.asStateFlow()

    private val _screenSizePreset = MutableStateFlow<ScreenSizePreset?>(null)
    val screenSizePreset: StateFlow<ScreenSizePreset?> = _screenSizePreset.asStateFlow()

    private val _mockLocationEnabled = MutableStateFlow(false)
    val mockLocationEnabled: StateFlow<Boolean> = _mockLocationEnabled.asStateFlow()

    private val _mockLocation = MutableStateFlow(locationPresets.first())
    val mockLocation: StateFlow<MockLocationPreset> = _mockLocation.asStateFlow()

    fun setDynamicTypeLevel(level: DynamicTypeLevel) {
        _dynamicTypeLevel.value = level
    }

    fun setAnimationSpeedScale(scale: Float) {
        _animationSpeedScale.value = scale.coerceIn(0.1f, 2f)
    }

    fun setGridOverlayEnabled(enabled: Boolean) {
        _gridOverlayEnabled.value = enabled
    }

    fun toggleGridOverlay() {
        _gridOverlayEnabled.value = !_gridOverlayEnabled.value
    }

    fun setScreenSizePreset(preset: ScreenSizePreset?) {
        _screenSizePreset.value = preset
    }

    fun setCustomScreenSize(
        widthDp: Int,
        heightDp: Int,
        label: String = "Custom",
    ): ScreenSizePreset? {
        val width = widthDp.coerceIn(MIN_SCREEN_WIDTH_DP, MAX_SCREEN_WIDTH_DP)
        val height = heightDp.coerceIn(MIN_SCREEN_HEIGHT_DP, MAX_SCREEN_HEIGHT_DP)
        if (width <= 0 || height <= 0) return null

        val preset = ScreenSizePreset(
            id = "custom_${width}x$height",
            displayName = "$label (${width}×${height} dp)",
            widthDp = width,
            heightDp = height,
            manufacturer = "Custom",
        )
        _screenSizePreset.value = preset
        return preset
    }

    fun isCustomScreenPreset(preset: ScreenSizePreset?): Boolean =
        preset?.id?.startsWith("custom_") == true

    fun setMockLocationEnabled(enabled: Boolean) {
        _mockLocationEnabled.value = enabled
    }

    fun setMockLocation(preset: MockLocationPreset) {
        _mockLocation.value = preset
    }

    fun setCustomMockLocation(latitude: Double, longitude: Double, label: String = "Custom") {
        _mockLocation.value = MockLocationPreset("custom", label, latitude, longitude)
    }

    fun debugAnimDurationMillis(baseMs: Int = DEFAULT_ANIM_DURATION_MS): Int {
        val speed = _animationSpeedScale.value.coerceIn(0.1f, 2f)
        return (baseMs / speed).toInt().coerceAtLeast(16)
    }

    const val DEFAULT_ANIM_DURATION_MS = 300
    const val MIN_SCREEN_WIDTH_DP = 240
    const val MAX_SCREEN_WIDTH_DP = 1200
    const val MIN_SCREEN_HEIGHT_DP = 400
    const val MAX_SCREEN_HEIGHT_DP = 1600
}
