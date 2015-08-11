package me.lysne.okapi.graphics

import me.lysne.okapi.Config
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.*
import org.lwjgl.system.MemoryUtil

public class GBuffer(
        val width: Int = Config.WindowWidth,
        val height: Int = Config.WindowHeight) {

    private val fbo: Int

    private val diffuse: Int
    private val specular: Int
    private val normals: Int
    private val depth: Int

    init {
        fbo = GL30.glGenFramebuffers()
        diffuse = GL11.glGenTextures()
        specular = GL11.glGenTextures()
        normals = GL11.glGenTextures()
        depth = GL11.glGenTextures()

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)

        attachRenderTarget(
                diffuse,
                GL30.GL_COLOR_ATTACHMENT0,
                GL11.GL_RGBA8,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE)
        attachRenderTarget(
                specular,
                GL30.GL_COLOR_ATTACHMENT1,
                GL11.GL_RGBA8,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE)
        attachRenderTarget(
                normals,
                GL30.GL_COLOR_ATTACHMENT2,
                GL11.GL_RGBA8,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE)
        attachRenderTarget(
                depth,
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
        GL20.glDrawBuffers(drawBuffers)

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
        GL11.glDeleteTextures(diffuse)
        GL11.glDeleteTextures(specular)
        GL11.glDeleteTextures(normals)
        GL11.glDeleteTextures(depth)
    }

    public fun bind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)
    }

    public fun unbind() {
        GL11.glFlush()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    }

    public fun bindTextures() {
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, diffuse)
        GL13.glActiveTexture(GL13.GL_TEXTURE1)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, specular)
        GL13.glActiveTexture(GL13.GL_TEXTURE2)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, normals)
        GL13.glActiveTexture(GL13.GL_TEXTURE3)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depth)
    }
}