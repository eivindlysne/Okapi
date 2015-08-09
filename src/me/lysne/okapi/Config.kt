package me.lysne.okapi

public object Config {

    // Flags
    val Debug: Boolean = true
    val DebugRender: Boolean = false
    val Flying: Boolean = true

    // Window
    val WindowWidth: Int = 960
    val WindowHeight: Int = WindowWidth / 16 * 9
    val WindowTitle: String = "Okapi"

    // Timing (fixed timestep)
    val FrameRate: Int = 60
    val FrameTime: Double = 1.0 / FrameRate

    // Input
    val MouseSensitivity: Double = 0.2
}