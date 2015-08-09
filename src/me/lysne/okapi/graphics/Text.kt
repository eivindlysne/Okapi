package me.lysne.okapi.graphics

import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.stb.STBEasyFont
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer

public class Text(string: String,
                  val position: Vector2f,
                  val color: Vector3f = Vector3f(1f, 1f, 1f),
                  val charSize: Float = 32f) {

    private var drawCount = 0

    private val textBuffer: StringBuilder
    private val vertexBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer

    val vao: Int
    val vbo: Int
    val ibo: Int


    init {
        vao = GL30.glGenVertexArrays()
        vbo = GL15.glGenBuffers()
        ibo = GL15.glGenBuffers()

        drawCount = string.length() * 6

        textBuffer = StringBuilder(string)

        vertexBuffer = BufferUtils.createFloatBuffer(string.length() * 4 * VERTEX_SIZE)
        indexBuffer = BufferUtils.createShortBuffer(string.length() * 6)

        generateVertexData(charSize)

        GL30.glBindVertexArray(vao)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_DYNAMIC_DRAW)

        GL20.glEnableVertexAttribArray(0)
        GL20.glEnableVertexAttribArray(1)
        GL20.glEnableVertexAttribArray(2)
        GL20.glVertexAttribPointer(
                0,
                3,
                GL11.GL_FLOAT,
                false,
                VERTEX_SIZE * FLOAT_SIZE,
                0L)
        GL20.glVertexAttribPointer(
                1,
                2,
                GL11.GL_FLOAT,
                false,
                VERTEX_SIZE * FLOAT_SIZE,
                (3 * FLOAT_SIZE).toLong())
        GL20.glVertexAttribPointer(
                2,
                3,
                GL11.GL_FLOAT,
                false,
                VERTEX_SIZE * FLOAT_SIZE,
                ((3 + 2) * FLOAT_SIZE).toLong())

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_DYNAMIC_DRAW)

        GL30.glBindVertexArray(0)
    }


    public fun destroy() {
        GL30.glDeleteVertexArrays(vao)
        GL15.glDeleteBuffers(vbo)
        GL15.glDeleteBuffers(ibo)
    }

    /**
     * Not very optimal.. new string can not be bigger
     * than the original string.
     */
    public fun setNew(newString: String) {

        textBuffer.setLength(0) // NOTE: Hack for reusing the buffer
        textBuffer.append(newString)

        vertexBuffer.clear()
        indexBuffer.clear()

        drawCount = newString.length() * 6
        vertexBuffer.limit(newString.length() * 4 * VERTEX_SIZE)
        indexBuffer.limit(drawCount)

        generateVertexData(charSize)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, vertexBuffer)

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0L, indexBuffer)
    }

    public fun draw() {

        GL30.glBindVertexArray(vao)
        GL11.glDrawElements(GL11.GL_TRIANGLES, drawCount, GL11.GL_UNSIGNED_SHORT, 0L)
        GL30.glBindVertexArray(0)
    }

    private fun generateVertexData(charSize: Float) {

        val step = 1f / 16f

        for (i in textBuffer.toString().indices) {
            val charCode = textBuffer[i].toInt()
            // TODO: Cache uv's for alphabet?
            val uvX = (charCode % 16) / 16f
            val uvY = (charCode / 16) / 16f
            val x = position.x + (i * (charSize / 2.5f))

            vertexBuffer.put(x).put(position.y).put(0f)
                    .put(uvX).put(1f - (uvY + step))
                    .put(color.x).put(color.y).put(color.z)

            vertexBuffer.put(x + charSize).put(position.y).put(0f)
                    .put(uvX + step).put(1f - (uvY + step))
                    .put(color.x).put(color.y).put(color.z)

            vertexBuffer.put(x + charSize).put(position.y + charSize).put(0f)
                    .put(uvX + step).put(1f - uvY)
                    .put(color.x).put(color.y).put(color.z)

            vertexBuffer.put(x).put(position.y + charSize).put(0f)
                    .put(uvX).put(1f - uvY)
                    .put(color.x).put(color.y).put(color.z)

            val ixp = i * 4
            indexBuffer.put(ixp.toShort())
                    .put((ixp + 1).toShort())
                    .put((ixp + 2).toShort())
                    .put(ixp.toShort())
                    .put((ixp + 2).toShort())
                    .put((ixp + 3).toShort())
        }

        vertexBuffer.flip()
        indexBuffer.flip()
    }
}