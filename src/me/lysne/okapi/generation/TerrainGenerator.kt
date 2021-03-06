package me.lysne.okapi.generation

import me.lysne.okapi.graphics.Mesh
import me.lysne.okapi.graphics.TextureMesh
import me.lysne.okapi.graphics.Transform
import me.lysne.okapi.graphics.Vertex
import me.lysne.okapi.world.Region
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.stb.STBPerlin


public class MeshData(val vertices: Array<Vertex>, val indices: ShortArray) {
    public fun generateNormals() {

        for (i in 0..indices.size()-1 step 3) {

            val v0 = vertices[indices[i].toInt()]
            val v1 = vertices[indices[i + 1].toInt()]
            val v2 = vertices[indices[i + 2].toInt()]

            val a = Vector3f(v1.position).sub(v0.position)
            val b = Vector3f(v2.position).sub(v1.position)

            val normal = Vector3f(a).cross(b).normalize()

            v0.normal.add(normal)
            v1.normal.add(normal)
            v2.normal.add(normal)
        }
        vertices.forEach { v -> v.normal.normalize() }
    }

    public fun toMesh() : Mesh {
        return Mesh(vertices, indices)
    }
}


public fun createRegionMeshData(
        north: Region?, south: Region?, west: Region?, east: Region?,
        type: Region.Type,
        xUnits: Int,
        zUnits: Int,
        yPlane: Float,
        color: Vector3f,
        transform: Transform) : MeshData {


    val density = 0.1f

    val t = Vector3f()
    transform.position.mul(density, t)

    val vertices: Array<Vertex> = Array(xUnits * zUnits * 4, { Vertex() })
    val indices: ShortArray = ShortArray(xUnits * zUnits * 6)

    val tileHeight = 1f / 3f
    val u1 = type.tileCoord.x * tileHeight
    val u2 = (type.tileCoord.x + 1) * tileHeight
    val v1 = type.tileCoord.y * tileHeight
    val v2 = (type.tileCoord.y + 1) * tileHeight

    var vx = 0; var ix = 0
    for (i in 0..zUnits-1) {

        for (j in 0..xUnits-1) {

            // FIXME: Corners may fail
            var magnitude0 = type.magnitude
            var magnitude1 = type.magnitude
            var magnitude2 = type.magnitude
            var magnitude3 = type.magnitude

            if (i == 0 && west != null) {
                magnitude0 = west.type.magnitude
                magnitude3 = west.type.magnitude
            } else if (i == zUnits-1 && east != null) {
                magnitude1 = east.type.magnitude
                magnitude2 = east.type.magnitude
            }
            if (j == xUnits-1 && north != null) {
                magnitude2 = north.type.magnitude
                magnitude3 = north.type.magnitude
            } else if (j == 0 && south != null) {
                magnitude0 = south.type.magnitude
                magnitude1 = south.type.magnitude
            }

            val x0 = i.toFloat(); val x1 = x0 + 1f
            val z0 = -j.toFloat(); val z1 = z0 - 1f

            val x0Mod = x0 * density + t.x
            val x1Mod = x1 * density + t.x
            val z0Mod = z0 * density + t.z
            val z1Mod = z1 * density + t.z

            // Calculate height with perlin noise
            val y0 = STBPerlin.stb_perlin_noise3(x0Mod, yPlane, z0Mod, 0, 0, 0) * magnitude0
            val y1 = STBPerlin.stb_perlin_noise3(x1Mod, yPlane, z0Mod, 0, 0, 0) * magnitude1
            val y2 = STBPerlin.stb_perlin_noise3(x1Mod, yPlane, z1Mod, 0, 0, 0) * magnitude2
            val y3 = STBPerlin.stb_perlin_noise3(x0Mod, yPlane, z1Mod, 0, 0, 0) * magnitude3

            // Creatte vertices
            vertices[vx] = Vertex(Vector3f(x0, yPlane - y0, z0), Vector2f(u1, v1), color)
            vertices[vx+1] = Vertex(Vector3f(x1, yPlane - y1, z0), Vector2f(u2, v1), color)
            vertices[vx+2] = Vertex(Vector3f(x1, yPlane - y2, z1), Vector2f(u2, v2), color)
            vertices[vx+3] = Vertex(Vector3f(x0, yPlane - y3, z1), Vector2f(u1, v2), color)

            // Add indices
            indices[ix++] = (vx).toShort()
            indices[ix++] = (vx + 1).toShort()
            indices[ix++] = (vx + 2).toShort()
            indices[ix++] = (vx + 2).toShort()
            indices[ix++] = (vx + 3).toShort()
            indices[ix++] = (vx + 0).toShort()
            vx += 4
        }
    }
    return MeshData(vertices, indices)
}

public fun createTiledPlane(xUnits: Int, zUnits: Int, plane: Float, color: Vector3f = Vector3f(1f, 1f, 1f)) : Mesh {
    val vertices: Array<Vertex> = Array(xUnits * zUnits * 4, {x -> Vertex() })
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

    val vertices = Array((xUnits + 1) * (zUnits + 1), {x -> Vertex() })
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

    val vertices: Array<Vertex> = Array(xUnits * zUnits * 4, {x -> Vertex() })
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

public fun createTextureMesh(x0: Float, y0: Float, x1: Float, y1: Float) : TextureMesh {
    return TextureMesh(arrayOf(
            Vertex(Vector3f(x0, y0, 0f), Vector2f(0f, 0f)),
            Vertex(Vector3f(x1, y0, 0f), Vector2f(1f, 0f)),
            Vertex(Vector3f(x1, y1, 0f), Vector2f(1f, 1f)),
            Vertex(Vector3f(x0, y1, 0f), Vector2f(0f, 1f))))
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