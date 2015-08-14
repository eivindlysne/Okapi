package me.lysne.okapi.graphics

import me.lysne.okapi.Config
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil

public class GBuffer(
        val width: Int = Config.WindowWidth,
        val height: Int = Config.WindowHeight) {

    private val fbo: Int

    val gPositionDepth: Int
    val gNormal: Int
    val gAlbedoSpec: Int
    val depthBuffer: Int

    init {
        fbo = GL30.glGenFramebuffers()
        gPositionDepth = GL11.glGenTextures()
        gNormal = GL11.glGenTextures()
        gAlbedoSpec = GL11.glGenTextures()
        depthBuffer = GL30.glGenRenderbuffers()

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)

        attachRenderTarget(
                gPositionDepth,
                GL30.GL_COLOR_ATTACHMENT0,
                GL30.GL_RGBA16F,
                GL11.GL_RGBA,
                GL11.GL_FLOAT)
        attachRenderTarget(
                gNormal,
                GL30.GL_COLOR_ATTACHMENT1,
                GL11.GL_RGB,
                GL11.GL_RGB,
                GL11.GL_FLOAT)
        attachRenderTarget(
                gAlbedoSpec,
                GL30.GL_COLOR_ATTACHMENT2,
                GL11.GL_RGBA,
                GL11.GL_RGBA,
                GL11.GL_FLOAT)

        val drawBuffers = BufferUtils.createIntBuffer(3)
        drawBuffers.put(GL30.GL_COLOR_ATTACHMENT0)
            .put(GL30.GL_COLOR_ATTACHMENT1)
            .put(GL30.GL_COLOR_ATTACHMENT2)

        drawBuffers.flip()
        GL20.glDrawBuffers(drawBuffers)

        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer)
        GL30.glRenderbufferStorage(
                GL30.GL_RENDERBUFFER,
                GL11.GL_DEPTH_COMPONENT,
                width,
                height)
        GL30.glFramebufferRenderbuffer(
                GL30.GL_FRAMEBUFFER,
                GL30.GL_DEPTH_ATTACHMENT,
                GL30.GL_RENDERBUFFER,
                depthBuffer)

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
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

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
        GL11.glDeleteTextures(gPositionDepth)
        GL11.glDeleteTextures(gNormal)
        GL11.glDeleteTextures(gAlbedoSpec)
        GL30.glDeleteRenderbuffers(depthBuffer)
    }

    public fun bind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)
    }

    public fun unbind() {
        GL11.glFlush()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    }
}