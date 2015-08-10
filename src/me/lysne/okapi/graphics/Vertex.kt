package me.lysne.okapi.graphics

import org.joml.Vector2f
import org.joml.Vector3f

public class Vertex(
        var position: Vector3f,
        var texCoord: Vector2f,
        var color: Vector3f,
        var normal: Vector3f) {

    constructor(position: Vector3f, texCoord: Vector2f)
    : this(position, texCoord, Vector3f(1f, 1f, 1f), Vector3f(0f, 0f, 0f))

    constructor(position: Vector3f, texCoord: Vector2f, color: Vector3f)
    : this(position, texCoord, color, Vector3f(0f, 0f, 0f))

    // Just for `arrayOf`-functions
    constructor()
    : this(Vector3f(), Vector2f(), Vector3f(), Vector3f())
}

public val VERTEX_SIZE: Int = 11
public val TEXT_VERTEX_SIZE: Int = 8
public val FLOAT_SIZE: Int = 4