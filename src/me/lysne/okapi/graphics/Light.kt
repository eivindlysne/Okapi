package me.lysne.okapi.graphics

import org.joml.Vector3f

public data class PointLight(val position: Vector3f, val intensity: Vector3f) {

    constructor() : this(Vector3f(0f, 0f, 0f), Vector3f(1f, 1f, 1f))
}