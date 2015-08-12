package me.lysne.okapi.graphics

import me.lysne.okapi.loadImage
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.*
import java.io.File

public class Skybox(
        front: String,
        back: String,
        top: String,
        bot: String,
        left: String,
        right: String
) {
    private val SKYBOX_TEXTURE_PATH = "res/textures/skybox/"

    private val vao: Int
    private val vbo: Int
    private val tex: Int
    private val shader: Shader
    private val transform: Transform

    init {
        vao = GL30.glGenVertexArrays()
        vbo = GL15.glGenBuffers()
        tex = GL11.glGenTextures()

        shader = Shader("skybox.vert.glsl", "skybox.frag.glsl")
        shader.registerUniforms(
                "skybox",
                "transform.position",
                "transform.orientation",
                "transform.scale",
                "viewProjection")
        shader.setUniform("skybox", 0)

        transform = Transform()

        initTexture(arrayOf(front, back, top, bot, left, right))
        initBuffer()
    }

    public fun destroy() {
        GL30.glDeleteVertexArrays(vao)
        GL15.glDeleteBuffers(vbo)
        GL11.glDeleteTextures(tex)
        shader.destroy()
    }

    public fun update(camera: Camera) {
        transform.position.set(camera.transform.position)
    }

    public fun draw(viewProjection: Matrix4f) {

        GL11.glDepthMask(false)

        shader.use()
        shader.setUniform("transform.position", transform.position)
        shader.setUniform("transform.orientation", transform.orientation)
        shader.setUniform("transform.scale", transform.scale)
        shader.setUniform("viewProjection", viewProjection)

        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, tex)

        GL30.glBindVertexArray(vao)
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 36)

        GL11.glDepthMask(true)
    }

    private fun initTexture(sides: Array<String>) {

        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, tex)
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)

        val targets = arrayOf(
            GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z,
            GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
            GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
            GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
            GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
            GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X
        )

        for (i in 0..(targets.size() - 1)) {
            val image = loadImage(File(SKYBOX_TEXTURE_PATH + sides[i]), true)
            GL11.glTexImage2D(
                    targets[i],
                    0,
                    GL11.GL_RGBA,
                    image.width,
                    image.height,
                    0,
                    GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE,
                    image.data
            )
            image.free()
        }
    }

    private fun initBuffer() {

        val p = 10.0f

        val vertices = arrayOf(
                Vector3f(-p, -p,  p), Vector3f(-p,  p,  p), Vector3f( p,  p,  p),
                Vector3f( p,  p,  p), Vector3f( p, -p,  p), Vector3f(-p, -p,  p),

                Vector3f( p,  p, -p), Vector3f(-p,  p, -p), Vector3f(-p, -p, -p),
                Vector3f(-p, -p, -p), Vector3f( p, -p, -p), Vector3f( p,  p, -p),


                Vector3f(-p,  p, -p), Vector3f( p,  p, -p), Vector3f( p,  p,  p),
                Vector3f( p,  p,  p), Vector3f(-p,  p,  p), Vector3f(-p,  p, -p),

                Vector3f( p, -p,  p), Vector3f( p, -p, -p), Vector3f(-p, -p, -p),
                Vector3f(-p, -p, -p), Vector3f(-p, -p,  p), Vector3f( p, -p,  p),


                Vector3f(-p,  p, -p), Vector3f(-p,  p,  p), Vector3f(-p, -p,  p),
                Vector3f(-p, -p,  p), Vector3f(-p, -p, -p), Vector3f(-p,  p, -p),

                Vector3f( p, -p, -p), Vector3f( p, -p,  p), Vector3f( p,  p,  p),
                Vector3f( p,  p,  p), Vector3f( p,  p, -p), Vector3f( p, -p, -p)
        )
        val buffer = BufferUtils.createFloatBuffer(vertices.size() * 3)
        for (v in vertices) {
            buffer.put(v.x)
            buffer.put(v.y)
            buffer.put(v.z)
        }
        buffer.flip()

        GL30.glBindVertexArray(vao)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW)

        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0)
    }
}