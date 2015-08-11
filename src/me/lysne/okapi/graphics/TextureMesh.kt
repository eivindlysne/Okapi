package me.lysne.okapi.graphics

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

public class TextureMesh(val vertices: Array<Vertex>) {

    val indices = shortArrayOf(0, 1, 2, 0, 2, 3)
    val drawCount = indices.size()

    val vao: Int
    val vbo: Int
    val ibo: Int


    init {
        vao = GL30.glGenVertexArrays()
        vbo = GL15.glGenBuffers()
        ibo = GL15.glGenBuffers()

        val vertexBuffer = BufferUtils.createFloatBuffer(vertices.size() * TEXTURE_VERTEX_SIZE)
        val indexBuffer = BufferUtils.createShortBuffer(indices.size())

        vertices.forEach { v ->
            vertexBuffer.put(v.position.x).put(v.position.y).put(v.position.z)
            vertexBuffer.put(v.texCoord.x).put(v.texCoord.y) }
        indices.forEach { i -> indexBuffer.put(i) }
        vertexBuffer.flip()
        indexBuffer.flip()

        GL30.glBindVertexArray(vao)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW)

        GL20.glEnableVertexAttribArray(0)
        GL20.glEnableVertexAttribArray(1)
        GL20.glVertexAttribPointer(
                0,
                3,
                GL11.GL_FLOAT,
                false,
                TEXTURE_VERTEX_SIZE * FLOAT_SIZE,
                0L)
        GL20.glVertexAttribPointer(
                1,
                2,
                GL11.GL_FLOAT,
                false,
                TEXTURE_VERTEX_SIZE * FLOAT_SIZE,
                (3 * FLOAT_SIZE).toLong())

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW)

        GL30.glBindVertexArray(0)
    }

    public fun destroy() {
        GL30.glDeleteVertexArrays(vao)
        GL15.glDeleteBuffers(vbo)
        GL15.glDeleteBuffers(ibo)
    }

    public fun draw() {
        GL30.glBindVertexArray(vao)
        GL11.glDrawElements(GL11.GL_TRIANGLES, drawCount, GL11.GL_UNSIGNED_SHORT, 0L)
        GL30.glBindVertexArray(0)
    }
}