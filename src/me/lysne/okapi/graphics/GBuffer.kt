package me.lysne.okapi.graphics

import me.lysne.okapi.Config
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil

public class GBuffer(
        val width: Int = Config.WindowWidth,
        val height: Int = Config.WindowHeight) {

    private val fbo: Int

    /*private*/ val diffuseColor: Int
    /*private*/ val specularColor: Int
    /*private*/ val worldNormals: Int
    /*private*/ val depthBuffer: Int

    init {
        fbo = GL30.glGenFramebuffers()
        diffuseColor = GL11.glGenTextures()
        specularColor = GL11.glGenTextures()
        worldNormals = GL11.glGenTextures()
        depthBuffer = GL11.glGenTextures()

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)

        attachRenderTarget(
                diffuseColor,
                GL30.GL_COLOR_ATTACHMENT0,
                GL11.GL_RGBA8,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE)
        attachRenderTarget(
                specularColor,
                GL30.GL_COLOR_ATTACHMENT1,
                GL11.GL_RGBA8,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE)
        attachRenderTarget(
                worldNormals,
                GL30.GL_COLOR_ATTACHMENT2,
                GL11.GL_RGB10_A2,
                GL11.GL_RGBA,
                GL11.GL_FLOAT)
        attachRenderTarget(
                depthBuffer,
                GL30.GL_DEPTH_ATTACHMENT,
                GL14.GL_DEPTH_COMPONENT24,
                GL11.GL_DEPTH_COMPONENT,
                GL11.GL_FLOAT)

        val drawBuffers = BufferUtils.createIntBuffer(4)
        drawBuffers.put(GL30.GL_COLOR_ATTACHMENT0)
            .put(GL30.GL_COLOR_ATTACHMENT1)
            .put(GL30.GL_COLOR_ATTACHMENT2)
 //           .put(GL30.GL_DEPTH_ATTACHMENT) // FIXME: Causing error
        drawBuffers.flip()
        println(GL11.glGetError())
        GL20.glDrawBuffers(drawBuffers)
        println(GL11.glGetError())

        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) !=
                GL30.GL_FRAMEBUFFER_COMPLETE) {
            error("Failed to create Gbuffer")
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    }

    private fun attachRenderTarget(
            handle: Int,
            attachment: Int,
            internalFormat: Int,
            format: Int,
            type: Int) {

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)

        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                internalFormat,
                width,
                height,
                0,
                format,
                type,
                MemoryUtil.NULL)

        GL30.glFramebufferTexture2D(
                GL30.GL_FRAMEBUFFER,
                attachment,
                GL11.GL_TEXTURE_2D,
                handle,
                0)
    }

    public fun destroy() {
        GL30.glDeleteFramebuffers(fbo)
        GL11.glDeleteTextures(diffuseColor)
        GL11.glDeleteTextures(specularColor)
        GL11.glDeleteTextures(worldNormals)
        GL11.glDeleteTextures(depthBuffer)
    }

    public fun bind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)
    }

    public fun unbind() {
        GL11.glFlush()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    }
}