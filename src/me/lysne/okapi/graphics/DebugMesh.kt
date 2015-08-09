package me.lysne.okapi.graphics

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

/**
 * Not Horribly optimized
 */
public class DebugMesh(val vertices: Array<Vertex>, val indices: ShortArray) {

    val vao: Int
    val vbo: Int
    val ibo: Int

    private var drawCount = 0

    private var numVertices = 0
    private var numTriangles = 0

    init {
        vao = GL30.glGenVertexArrays()
        vbo = GL15.glGenBuffers()
        ibo = GL15.glGenBuffers()

        drawCount = (vertices.size() / 4) * 10
        numVertices = vertices.size()
        numTriangles = indices.size() / 3

        val vertexBuffer = BufferUtils.createFloatBuffer(vertices.size() * 3)
        val indexBuffer = BufferUtils.createShortBuffer((vertices.size() / 4) * 10)

        vertices.forEach {
            vertexBuffer.put(it.position.x).put(it.position.y+0.08f).put(it.position.z)
        }
        for (i in 0..vertices.size() - 1 step 4) {
            val i0 = i.toShort()
            indexBuffer
                .put(i0).put((i0 + 1).toShort())
                .put((i0 + 1).toShort()).put((i0 + 2).toShort())
                .put((i0 + 2).toShort()).put((i0 + 3).toShort())
                .put((i0 + 3).toShort()).put((i0).toShort())
                .put((i0).toShort()).put((i0 + 2).toShort())
        }
        vertexBuffer.flip()
        indexBuffer.flip()

        GL30.glBindVertexArray(vao)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW)

        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, FLOAT_SIZE * 3, 0L)

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW)

        GL30.glBindVertexArray(0)

        //GL11.glPointSize(2f)
    }

    public fun destroy() {
        GL30.glDeleteVertexArrays(vao)
        GL15.glDeleteBuffers(vbo)
        GL15.glDeleteBuffers(ibo)
    }

    public fun draw() {

        GL30.glBindVertexArray(vao)
        //GL11.glDrawElements(GL11.GL_POINTS, drawCount, GL11.GL_UNSIGNED_SHORT, 0L)
        GL11.glDrawElements(GL11.GL_LINES, drawCount, GL11.GL_UNSIGNED_SHORT, 0L)
        GL30.glBindVertexArray(0)
    }
}