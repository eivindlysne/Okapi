package me.lysne.okapi.generation

import com.github.quickhull3d.Point3d
import com.github.quickhull3d.QuickHull3D
import me.lysne.okapi.graphics.Mesh
import me.lysne.okapi.graphics.Vertex
import org.joml.Vector3f
import java.util.ArrayList
import java.util.HashMap
import java.util.Random

public object  TreeGenerator {

    private val r = Random()
    private val quickHull3d = QuickHull3D()

    public fun generateConvexHull(
            n: Int,
            yPlane: Float,
            color: Vector3f,
            weights: Vector3f) : Mesh {

        val points = Array(n, { Point3d() })
        points.forEach { p -> p.set(
                (2.0 * r.nextDouble() - 1.0) * weights.x,
                (2.0 * r.nextDouble() - 1.0) * weights.y,
                (2.0 * r.nextDouble() - 1.0) * weights.z) }

        quickHull3d.build(points)

        val hullPoints = quickHull3d.getVertices()
        val faces = quickHull3d.getFaces()

        val vertices = Array(hullPoints.size(), { Vertex() })
        hullPoints.forEachIndexed { i, p ->
            vertices[i].position.set(p.x.toFloat(), p.y.toFloat() + yPlane, p.z.toFloat())
            vertices[i].color.set(color)
        }

        val indicesList = ArrayList<Short>()

        for (face in faces) {
            for (index in face) {
                indicesList.add(index.toShort())
            }
        }

        val data = MeshData(vertices, indicesList.toShortArray())
        data.generateNormals()

        return Mesh(data.vertices, data.indices)
    }


    /**
     * Adapted from:
     * http://blog.andreaskahler.com/2009/06/creating-icosphere-mesh-in-code.html
     */

    private val middlePointIndexCache: MutableMap<Long, Short> = HashMap()

    private fun getMiddlePoint(p1: Short, p2: Short, vertices: ArrayList<Vertex>) : Short {

        val firstIsSmaller = p1 < p2
        val smallerIndex = if (firstIsSmaller) p1.toLong() else p2.toLong()
        val greaterIndex = if (firstIsSmaller) p2.toLong() else p1.toLong()
        val key = (smallerIndex shl 32) + greaterIndex

        if (middlePointIndexCache.containsKey(key)) {
            // NOTE: Will always have the value.. duh
            return middlePointIndexCache.getOrElse(key, { 0 })
        }

        val point1 = vertices[p1.toInt()].position
        val point2 = vertices[p2.toInt()].position
        val middle = Vector3f(
                (point1.x + point2.x) / 2f,
                (point1.y + point2.y) / 2f,
                (point1.z + point2.z) / 2f)

        val i = vertices.size().toShort()

        val v = Vertex()
        v.position.set(middle).normalize()
        vertices.add(v)

        middlePointIndexCache.put(key, i)
        return i
    }

    public fun generateIcoSphere(color: Vector3f,  n0: Int = 1) : Mesh {

        // Else normal generation will crash
        // 1 iteration should be plenty
        var n = if (n0 > 4) 4 else n0

        val t = ((1.0 + Math.sqrt(5.0)) / 2.0).toFloat()

        val vertices = ArrayList<Vertex>()

        for (i in 0..12-1)
            vertices.add(i, Vertex())

        vertices[0].position.set(-1f,  t,  0f).normalize()
        vertices[1].position.set( 1f,  t,  0f).normalize()
        vertices[2].position.set(-1f, -t,  0f).normalize()
        vertices[3].position.set( 1f, -t,  0f).normalize()

        vertices[4].position.set( 0f, -1f,  t).normalize() // Bottom
        vertices[5].position.set( 0f,  1f,  t).normalize() // Top
        vertices[6].position.set( 0f, -1f, -t).normalize()
        vertices[7].position.set( 0f,  1f, -t).normalize()

        vertices[8].position.set( t,  0f, -1f).normalize()
        vertices[9].position.set( t,  0f,  1f).normalize()
        vertices[10].position.set(-t,  0f, -1f).normalize()
        vertices[11].position.set(-t,  0f,  1f).normalize()

        var faces = ArrayList<Triple<Short, Short, Short>>()

        faces.add(Triple<Short, Short, Short>(0, 11, 5))
        faces.add(Triple<Short, Short, Short>(0, 5, 1))
        faces.add(Triple<Short, Short, Short>(0, 1, 7))
        faces.add(Triple<Short, Short, Short>(0, 7, 10))
        faces.add(Triple<Short, Short, Short>(0, 10, 11))

        faces.add(Triple<Short, Short, Short>(1, 5, 9))
        faces.add(Triple<Short, Short, Short>(5, 11, 4))
        faces.add(Triple<Short, Short, Short>(11, 10, 2))
        faces.add(Triple<Short, Short, Short>(10, 7, 6))
        faces.add(Triple<Short, Short, Short>(7, 1, 8))

        faces.add(Triple<Short, Short, Short>(3, 9, 4))
        faces.add(Triple<Short, Short, Short>(3, 4, 2))
        faces.add(Triple<Short, Short, Short>(3, 2, 6))
        faces.add(Triple<Short, Short, Short>(3, 6, 8))
        faces.add(Triple<Short, Short, Short>(3, 8, 9))

        faces.add(Triple<Short, Short, Short>(4, 9, 5))
        faces.add(Triple<Short, Short, Short>(2, 4, 11))
        faces.add(Triple<Short, Short, Short>(6, 2, 10))
        faces.add(Triple<Short, Short, Short>(8, 6, 7))
        faces.add(Triple<Short, Short, Short>(9, 8, 1))

        for (_ in 0..n-1) {
            val faces2 = ArrayList<Triple<Short, Short, Short>>()

            for (tri in faces) {

                val a = getMiddlePoint(tri.first, tri.second, vertices)
                val b = getMiddlePoint(tri.second, tri.third, vertices)
                val c = getMiddlePoint(tri.third, tri.first, vertices)

                faces2.add(Triple(tri.first, a, c))
                faces2.add(Triple(tri.second, b, a))
                faces2.add(Triple(tri.third, c, b))
                faces2.add(Triple(a, b, c))
            }

            faces.clear()
            faces.addAll(faces2)
        }

        val indices = ShortArray(faces.size() * 3)
        var i = 0
        faces.forEach { f ->
            indices[i++] = f.first
            indices[i++] = f.second
            indices[i++] = f.third
        }

        // Deform step
        vertices.forEach {
            v -> v.color.set(color)
            v.position.y += 3f
            v.position.add(
                    (2f * r.nextFloat() - 1f) * 0.2f,
                    0f,
                    (2f * r.nextFloat() - 1f) * 0.2f)
        }

        vertices[0].position.y += 0.5f
        vertices[1].position.y += 0.5f
        vertices[16].position.y += 1f


        val data = MeshData(vertices.toTypedArray(), indices)
        data.generateNormals()

        // NOTE: Can this cache persist?
        middlePointIndexCache.clear() // Clear for next time

        return data.toMesh()
    }

    public fun generateIcosahedron(color: Vector3f, yPlane: Float) : Mesh {

        val t = ((1.0 + Math.sqrt(5.0)) / 2.0).toFloat()

        val vertices = Array(12, { Vertex() })

        vertices[0].position.set(-1f,  t,  0f)
        vertices[1].position.set( 1f,  t,  0f)
        vertices[2].position.set(-1f, -t,  0f)
        vertices[3].position.set( 1f, -t,  0f)

        vertices[4].position.set( 0f, -1f,  t)
        vertices[5].position.set( 0f,  1f,  t)
        vertices[6].position.set( 0f, -1f, -t)
        vertices[7].position.set( 0f,  1f, -t)

        vertices[8].position.set( t,  0f, -1f)
        vertices[9].position.set( t,  0f,  1f)
        vertices[10].position.set(-t,  0f, -1f)
        vertices[11].position.set(-t,  0f,  1f)

        vertices.forEach { v ->
            v.color.set(color)
            v.position.y += yPlane
        }

        val indices = ShortArray(20 * 3) // 20 faces of three vertices

        indices[0]  = 0  ; indices[1]  = 11 ; indices[2]  = 5
        indices[3]  = 0  ; indices[4]  = 5  ; indices[5]  = 1
        indices[6]  = 0  ; indices[7]  = 1  ; indices[8]  = 7
        indices[9]  = 0  ; indices[10] = 7  ; indices[11] = 10
        indices[12] = 0  ; indices[13] = 10 ; indices[14] = 11

        indices[15] = 1  ; indices[16] = 5  ; indices[17] = 9
        indices[18] = 5  ; indices[19] = 11 ; indices[20] = 4
        indices[21] = 11 ; indices[22] = 10 ; indices[23] = 2
        indices[24] = 10 ; indices[25] = 7  ; indices[26] = 6
        indices[27] = 7  ; indices[28] = 1  ; indices[29] = 8

        indices[30] = 3  ; indices[31] = 9  ; indices[32] = 4
        indices[33] = 3  ; indices[34] = 4  ; indices[35] = 2
        indices[36] = 3  ; indices[37] = 2  ; indices[38] = 6
        indices[39] = 3  ; indices[40] = 6  ; indices[41] = 8
        indices[42] = 3  ; indices[43] = 8  ; indices[44] = 9

        indices[45] = 4  ; indices[46] = 9  ; indices[47] = 5
        indices[48] = 2  ; indices[49] = 4  ; indices[50] = 11
        indices[51] = 6  ; indices[52] = 2  ; indices[53] = 10
        indices[54] = 8  ; indices[55] = 6  ; indices[56] = 7
        indices[57] = 9  ; indices[58] = 8  ; indices[59] = 1

        val data = MeshData(vertices, indices)
        data.generateNormals()
        return data.toMesh()
    }


    public fun timeGenerateConvexHull(n: Int) {

        val timeBefore = System.nanoTime()

        val points = Array(n, { Point3d() })
        points.forEach { p -> p.set(
                2.0 * r.nextDouble() - 1.0,
                2.0 * r.nextDouble() - 1.0,
                2.0 * r.nextDouble() - 1.0) }

        quickHull3d.build(points)

        val hullPoints = quickHull3d.getVertices()
        val faces = quickHull3d.getFaces()

        val vertices = Array(hullPoints.size(), { Vertex() })
        hullPoints.forEachIndexed { i, p ->
            vertices[i].position.set(p.x.toFloat(), p.y.toFloat() + 3f, p.z.toFloat())
            vertices[i].color.set(1f, .51f, 0f)
        }

        val indicesList = ArrayList<Short>()

        for (face in faces) {
            for (index in face) {
                indicesList.add(index.toShort())
            }
        }

        val timeAfter = System.nanoTime()
        println("Quickhull time (ms): ${(timeAfter - timeBefore) / 1000000.0}")
    }

    public fun timeGenerateIcoSphere(n0: Int) {

        val timeBefore = System.nanoTime()

        var n = if (n0 > 4) 4 else n0 // Else normal generation will crash

        val t = ((1.0 + Math.sqrt(5.0)) / 2.0).toFloat()

        val vertices = ArrayList<Vertex>()

        for (i in 0..12-1)
            vertices.add(i, Vertex())

        vertices[0].position.set(-1f,  t,  0f).normalize()
        vertices[1].position.set( 1f,  t,  0f).normalize()
        vertices[2].position.set(-1f, -t,  0f).normalize()
        vertices[3].position.set( 1f, -t,  0f).normalize()

        vertices[4].position.set( 0f, -1f,  t).normalize()
        vertices[5].position.set( 0f,  1f,  t).normalize()
        vertices[6].position.set( 0f, -1f, -t).normalize()
        vertices[7].position.set( 0f,  1f, -t).normalize()

        vertices[8].position.set( t,  0f, -1f).normalize()
        vertices[9].position.set( t,  0f,  1f).normalize()
        vertices[10].position.set(-t,  0f, -1f).normalize()
        vertices[11].position.set(-t,  0f,  1f).normalize()

        var faces = ArrayList<Triple<Short, Short, Short>>()

        faces.add(Triple<Short, Short, Short>(0, 11, 5))
        faces.add(Triple<Short, Short, Short>(0, 5, 1))
        faces.add(Triple<Short, Short, Short>(0, 1, 7))
        faces.add(Triple<Short, Short, Short>(0, 7, 10))
        faces.add(Triple<Short, Short, Short>(0, 10, 11))

        faces.add(Triple<Short, Short, Short>(1, 5, 9))
        faces.add(Triple<Short, Short, Short>(5, 11, 4))
        faces.add(Triple<Short, Short, Short>(11, 10, 2))
        faces.add(Triple<Short, Short, Short>(10, 7, 6))
        faces.add(Triple<Short, Short, Short>(7, 1, 8))

        faces.add(Triple<Short, Short, Short>(3, 9, 4))
        faces.add(Triple<Short, Short, Short>(3, 4, 2))
        faces.add(Triple<Short, Short, Short>(3, 2, 6))
        faces.add(Triple<Short, Short, Short>(3, 6, 8))
        faces.add(Triple<Short, Short, Short>(3, 8, 9))

        faces.add(Triple<Short, Short, Short>(4, 9, 5))
        faces.add(Triple<Short, Short, Short>(2, 4, 11))
        faces.add(Triple<Short, Short, Short>(6, 2, 10))
        faces.add(Triple<Short, Short, Short>(8, 6, 7))
        faces.add(Triple<Short, Short, Short>(9, 8, 1))

        // TODO: Refine
        for (_ in 0..n-1) {
            val faces2 = ArrayList<Triple<Short, Short, Short>>()

            for (tri in faces) {

                val a = getMiddlePoint(tri.first, tri.second, vertices)
                val b = getMiddlePoint(tri.second, tri.third, vertices)
                val c = getMiddlePoint(tri.third, tri.first, vertices)

                faces2.add(Triple(tri.first, a, c))
                faces2.add(Triple(tri.second, b, a))
                faces2.add(Triple(tri.third, c, b))
                faces2.add(Triple(a, b, c))
            }

            faces.clear()
            faces.addAll(faces2)
        }

        val indices = ShortArray(faces.size() * 3)
        var i = 0
        faces.forEach { f ->
            indices[i++] = f.first
            indices[i++] = f.second
            indices[i++] = f.third
        }

        middlePointIndexCache.clear()

        val timeAfter = System.nanoTime()
        println("IcoSphere time (ms): ${(timeAfter - timeBefore) / 1000000.0}")
    }
}