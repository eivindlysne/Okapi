package me.lysne.okapi.graphics

import org.joml.Vector3f


public data class Attenuation(
        var constant: Float = 1.0f,
        var linear: Float = 0.0f,
        var quadratic: Float = 1.0f)

public data class PointLight(
        val position: Vector3f,
        val intensity: Vector3f,
        val attenuation: Attenuation = Attenuation()) {
    constructor() : this(Vector3f(0f, 0f, 0f), Vector3f(1f, 1f, 1f))
}