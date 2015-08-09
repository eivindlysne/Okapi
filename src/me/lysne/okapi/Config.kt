package me.lysne.okapi

public object Config {

    val DEBUG: Boolean = true
    val FLYING: Boolean = true


    val WIDTH: Int = 960
    val HEIGHT: Int = WIDTH / 16 * 9
    val TITLE: String = "Okapi"

    val FRAME_RATE: Int = 60
    val FRAME_TIME: Double = 1.0 / FRAME_RATE
}