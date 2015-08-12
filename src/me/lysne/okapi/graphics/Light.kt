package me.lysne.okapi.graphics

import org.joml.Vector3f


public data class Attenuation(
        var constant: Float = 1.0f,
        var linear: Float = 0.0f,
        var quadratic: Float = 1.0f)


open class Light(
        val color: Vector3f = Vector3f(1f, 1f, 1f),
        val intensity: Float = 1f)


public class PointLight(
        color: Vector3f,
        intensity: Float,
        val position: Vector3f,
        val attenuation: Attenuation = Attenuation()) : Light(color, intensity) {

    val range: Float

    init {
        val i = intensity * Math.max(color.x, Math.max(color.y, color.z))
        var r = - attenuation.linear +
                Math.sqrt(attenuation.linear * attenuation.linear -
                          4.0 * attenuation.quadratic *
                          (attenuation.constant - 256.0 * i))
        r /= 2.0 * attenuation.quadratic
        range = r.toFloat()
    }
}

