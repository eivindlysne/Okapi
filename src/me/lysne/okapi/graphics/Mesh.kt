package me.lysne.okapi.graphics

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

public class Mesh(val vertices: Array<Vertex>, val indices: ShortArray) {

    private enum class Attributes(val loc: Int, val size: Int) {
        Position(0, 3),
        TexCoord(1, 2),
        Color(2, 3),
        Normal(3, 3),
        Tangent(4, 3),
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

        vertices.forEach { v ->
            vertexBuffer.put(v.position.x).put(v.position.y).put(v.position.z)
            vertexBuffer.put(v.texCoord.x).put(v.texCoord.y)
            vertexBuffer.put(v.color.x).put(v.color.y).put(v.color.z)
            vertexBuffer.put(v.normal.x).put(v.normal.y).put(v.normal.z) }
        indices.forEach { i -> indexBuffer.put(i) }
        vertexBuffer.flip()
        indexBuffer.flip()

        GL30.glBindVertexArray(vao)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW)

        GL20.glEnableVertexAttribArray(Attributes.Position.loc)
        GL20.glEnableVertexAttribArray(Attributes.TexCoord.loc)
        GL20.glEnableVertexAttribArray(Attributes.Color.loc)
        GL20.glEnableVertexAttribArray(Attributes.Normal.loc)
        GL20.glVertexAttribPointer(
                Attributes.Position.loc,
                Attributes.Position.size,
                GL11.GL_FLOAT,
                false,
                VERTEX_SIZE * FLOAT_SIZE,
                0L)
        GL20.glVertexAttribPointer(
                Attributes.TexCoord.loc,
                Attributes.TexCoord.size,
                GL11.GL_FLOAT,
                false,
                VERTEX_SIZE * FLOAT_SIZE,
                (Attributes.Position.size * FLOAT_SIZE).toLong())
        GL20.glVertexAttribPointer(
                Attributes.Color.loc,
                Attributes.Color.size,
                GL11.GL_FLOAT,
                false,
                VERTEX_SIZE * FLOAT_SIZE,
                ((Attributes.Position.size +
                  Attributes.TexCoord.size) * FLOAT_SIZE).toLong())
        GL20.glVertexAttribPointer(
                Attributes.Normal.loc,
                Attributes.Normal.size,
                GL11.GL_FLOAT,
                false,
                VERTEX_SIZE * FLOAT_SIZE,
                ((Attributes.Position.size +
                  Attributes.TexCoord.size +
                  Attributes.Color.size) * FLOAT_SIZE).toLong())

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