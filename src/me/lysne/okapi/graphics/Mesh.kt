package me.lysne.okapi.graphics

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

public class Mesh(val vertices: Array<Vertex>, val indices: ShortArray) {

    private enum class Attributes(val loc: Int, val size: Int) {
        POSITION(0, 3),
        TEXCOORD(1, 2),
        COLOR(2, 3),
        NORMAL(3, 3),
        TANGENT(4, 3),
    }

    val vao: Int
    val vbo: Int
    val ibo: Int

    private var drawCount = 0

    init {
        vao = GL30.glGenVertexArrays()
        vbo = GL15.glGenBuffers()
        ibo = GL15.glGenBuffers()

        drawCount = indices.size()
        val vertexBuffer = BufferUtils.createFloatBuffer(vertices.size() * VERTEX_SIZE)
        val indexBuffer = BufferUtils.createShortBuffer(indices.size())

        // NOTE: Calling reverse here might be costly
        vertices./*reverse().*/forEach {
            vertexBuffer.put(it.position.x).put(it.position.y).put(it.position.z)
            vertexBuffer.put(it.texCoord.x).put(it.texCoord.y)
            vertexBuffer.put(it.color.x).put(it.color.y).put(it.color.z)
        }
        indices./*reverse().*/forEach {  indexBuffer.put(it) }
        vertexBuffer.flip()
        indexBuffer.flip()

        GL30.glBindVertexArray(vao)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW)

        GL20.glEnableVertexAttribArray(Attributes.POSITION.loc)
        GL20.glEnableVertexAttribArray(Attributes.TEXCOORD.loc)
        GL20.glEnableVertexAttribArray(Attributes.COLOR.loc)
        GL20.glVertexAttribPointer(
                Attributes.POSITION.loc,
                Attributes.POSITION.size,
                GL11.GL_FLOAT,
                false,
                VERTEX_SIZE * FLOAT_SIZE,
                0L)
        GL20.glVertexAttribPointer(
                Attributes.TEXCOORD.loc,
                Attributes.TEXCOORD.size,
                GL11.GL_FLOAT,
                false,
                VERTEX_SIZE * FLOAT_SIZE,
                (Attributes.POSITION.size * FLOAT_SIZE).toLong())
        GL20.glVertexAttribPointer(
                Attributes.COLOR.loc,
                Attributes.COLOR.size,
                GL11.GL_FLOAT,
                false,
                VERTEX_SIZE * FLOAT_SIZE,
                ((Attributes.POSITION.size + Attributes.TEXCOORD.size)
                  * FLOAT_SIZE).toLong())

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