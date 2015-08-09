package me.lysne.okapi.graphics

import org.joml.Vector2f
import org.joml.Vector3f

public class Vertex(
        var position: Vector3f,
        var texCoord: Vector2f,
        var color: Vector3f) {

    constructor(position: Vector3f, texCoord: Vector2f)
    : this(position, texCoord, Vector3f(1f, 1f, 1f))

    constructor()
    : this(Vector3f(), Vector2f(), Vector3f())
}

public val VERTEX_SIZE: Int = 8

public val FLOAT_SIZE: Int = 4