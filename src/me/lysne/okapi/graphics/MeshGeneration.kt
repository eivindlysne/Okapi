package me.lysne.okapi.graphics

import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.stb.STBPerlin


public data class MeshData(val vertices: Array<Vertex>, val indices: ShortArray)


public fun createRegionMeshData(
        xUnits: Int,
        zUnits: Int,
        yPlane: Float,
        color: Vector3f = Vector3f(1f, 1f, 1f),
        transform: Transform) : MeshData {

    val density = 0.1f
    val magnitude = 2f
    val t = Vector3f()
    transform.position.mul(density ,t)

    val vertices: Array<Vertex> = Array(xUnits * zUnits * 4, { Vertex() })
    val indices: ShortArray = ShortArray(xUnits * zUnits * 6)

    var vx = 0; var ix = 0
    for (i in 0..zUnits-1) {
        for (j in 0..xUnits-1) {
            val x1 = i.toFloat(); val x2 = x1 + 1f
            val z1 = j.toFloat(); val z2 = z1 + 1f
            val y1 = STBPerlin.stb_perlin_noise3(x1 * density + t.x, yPlane, z1 * density + t.z, 0, 0, 0) * magnitude
            val y2 = STBPerlin.stb_perlin_noise3(x2 * density + t.x, yPlane, z1 * density + t.z, 0, 0, 0) * magnitude
            val y3 = STBPerlin.stb_perlin_noise3(x2 * density + t.x, yPlane, z2 * density + t.z, 0, 0, 0) * magnitude
            val y4 = STBPerlin.stb_perlin_noise3(x1 * density + t.x, yPlane, z2 * density + t.z, 0, 0, 0) * magnitude
            vertices[vx] = Vertex(Vector3f(x1, yPlane - y1, z1), Vector2f(0f, 0f), color)
            vertices[vx+1] = Vertex(Vector3f(x2, yPlane - y2, z1), Vector2f(1f, 0f), color)
            vertices[vx+2] = Vertex(Vector3f(x2, yPlane - y3, z2), Vector2f(1f, 1f), color)
            vertices[vx+3] = Vertex(Vector3f(x1, yPlane - y4, z2), Vector2f(0f, 1f), color)

            indices[ix++] = (vx).toShort()
            indices[ix++] = (vx + 1).toShort()
            indices[ix++] = (vx + 2).toShort()
            indices[ix++] = (vx).toShort()
            indices[ix++] = (vx + 2).toShort()
            indices[ix++] = (vx + 3).toShort()
            vx += 4
        }
    }
    return MeshData(vertices, indices)
}

public fun createTiledPlane(xUnits: Int, zUnits: Int, plane: Float, color: Vector3f = Vector3f(1f, 1f, 1f)) : Mesh {
    val vertices: Array<Vertex> = Array(xUnits * zUnits * 4, {x -> Vertex()})
    val indices = ShortArray(xUnits * zUnits * 6)

    var vx = 0; var ix = 0
    for (i in 0..zUnits -1) {
        for (j in 0..xUnits -1) {
            val x1 = i.toFloat(); val x2 = x1 + 1f
            val z1 = j.toFloat(); val z2 = z1 + 1f
            vertices[vx] = Vertex(Vector3f(x1, plane, z1), Vector2f(0f, 0f), color)
            vertices[vx+1] = Vertex(Vector3f(x2, plane, z1), Vector2f(1f, 0f), color)
            vertices[vx+2] = Vertex(Vector3f(x2, plane, z2), Vector2f(1f, 1f), color)
            vertices[vx+3] = Vertex(Vector3f(x1, plane, z2), Vector2f(0f, 1f), color)
            indices[ix++] = vx.toShort()
            indices[ix++] = (vx + 2).toShort()
            indices[ix++] = (vx + 3).toShort()
            indices[ix++] = (vx + 1).toShort()
            indices[ix++] = (vx + 2).toShort()
            indices[ix++] = vx.toShort()
            vx += 4
        }
    }

    return Mesh(vertices, indices)
}

public fun createPerlinPlane(xUnits: Int, zUnits: Int, plane: Float) : Mesh {

     val offset = .5f

    val vertices = Array((xUnits + 1) * (zUnits + 1), {x -> Vertex()})
    val indices = ShortArray(xUnits * zUnits * 6)

    var idx = 0
    for (i in 0..zUnits) {

        val v = (1f / zUnits) * i

        for (j in 0..xUnits) {

            val pos = i * (xUnits + 1) + j

            val u = (1f / xUnits) * j

            val plane0 = STBPerlin.stb_perlin_noise3(
                    j * offset,
                    plane,
                    i * offset,
                    0, 0, 0) * 0.5f

            vertices[pos] = Vertex(
                    Vector3f(j * offset, plane + plane0, i * offset),
                    Vector2f(u, v))

            if (i != 0 && j != 0) {
                indices[idx++] = (pos - 1).toShort()
                indices[idx++] = (pos - xUnits - 1).toShort()
                indices[idx++] = (pos - xUnits - 2).toShort()
                indices[idx++] = (pos).toShort()
                indices[idx++] = (pos - xUnits - 1).toShort()
                indices[idx++] = (pos - 1).toShort()
            }
        }
    }

    return Mesh(vertices, indices)
}

public fun createTiledPerlinPlane(xUnits: Int, zUnits: Int, plane: Float, color: Vector3f = Vector3f(1f, 1f, 1f)) : Mesh {

    val offset = 0.3f
    val smooth = 2.0f

    val vertices: Array<Vertex> = Array(xUnits * zUnits * 4, {x -> Vertex()})
    val indices = ShortArray(xUnits * zUnits * 6)

    var vx = 0; var ix = 0
    for (i in 0..zUnits-1) {
        for (j in 0..xUnits-1) {
            val x1 = i.toFloat(); val x2 = x1 + 1f
            val z1 = j.toFloat(); val z2 = z1 + 1f
            val y1 = STBPerlin.stb_perlin_noise3(x1 * offset, plane, z1 * offset, 0, 0, 0) * smooth
            val y2 = STBPerlin.stb_perlin_noise3(x2 * offset, plane, z1 * offset, 0, 0, 0) * smooth
            val y3 = STBPerlin.stb_perlin_noise3(x2 * offset, plane, z2 * offset, 0, 0, 0) * smooth
            val y4 = STBPerlin.stb_perlin_noise3(x1 * offset, plane, z2 * offset, 0, 0, 0) * smooth
            vertices[vx] = Vertex(Vector3f(x1, plane - y1, z1), Vector2f(0f, 0f), color)
            vertices[vx+1] = Vertex(Vector3f(x2, plane - y2, z1), Vector2f(1f, 0f), color)
            vertices[vx+2] = Vertex(Vector3f(x2, plane - y3, z2), Vector2f(1f, 1f), color)
            vertices[vx+3] = Vertex(Vector3f(x1, plane - y4, z2), Vector2f(0f, 1f), color)

            indices[ix++] = (vx).toShort()
            indices[ix++] = (vx + 1).toShort()
            indices[ix++] = (vx + 2).toShort()
            indices[ix++] = (vx).toShort()
            indices[ix++] = (vx + 2).toShort()
            indices[ix++] = (vx + 3).toShort()
            vx += 4
        }
    }

    return Mesh(vertices, indices)
}


public fun createTestMesh() : Mesh {
    val vertices = arrayOf(
            Vertex(Vector3f(-0.5f, -1.0f, 0.0f), Vector2f(0.0f, 0.0f)),
            Vertex(Vector3f(0.5f, -1.0f, 0.0f), Vector2f(1.0f, 0.0f)),
            Vertex(Vector3f(0.5f, 0.0f, 0.0f), Vector2f(1.0f, 1.0f)),
            Vertex(Vector3f(-0.5f, 0.0f, 0.0f), Vector2f(0.0f, 1.0f)))
    val indices: ShortArray = shortArrayOf(0, 2, 3, 1, 2, 0)
    return Mesh(vertices, indices)
}