package me.lysne.okapi.graphics

import me.lysne.okapi.Config
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL14
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil

public class Framebuffer(
        val attachment: Framebuffer.Attachment,
        val width: Int = Config.WindowWidth,
        val height: Int = Config.WindowHeight) {

    enum class Attachment {
        Color,
        Depth,
        ColorAndDepth,
    }

    private val fbo: Int
    private val colorTexture: Int
    private val depthTexture: Int
//    private val renderBuffer: Int

    init {
        fbo = GL30.glGenFramebuffers()

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)

//        renderBuffer = GL30.glGenRenderbuffers()
//        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, renderBuffer)
//        GL30.glRenderbufferStorage(
//                GL30.GL_RENDERBUFFER,
//                GL11.GL_DEPTH_COMPONENT,
//                width,
//                height)
//
//        GL30.glFramebufferRenderbuffer(
//                GL30.GL_FRAMEBUFFER,
//                GL30.GL_DEPTH_ATTACHMENT,
//                GL30.GL_RENDERBUFFER,
//                renderBuffer)


        if (attachment == Attachment.Color || attachment == Attachment.ColorAndDepth) {

            colorTexture = GL11.glGenTextures()
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)

            GL11.glTexImage2D(
                    GL11.GL_TEXTURE_2D,
                    0,
                    GL11.GL_RGB,
                    width,
                    height,
                    0,
                    GL11.GL_RGB,
                    GL11.GL_UNSIGNED_BYTE,
                    MemoryUtil.NULL)

            GL30.glFramebufferTexture2D(
                    GL30.GL_FRAMEBUFFER,
                    GL30.GL_COLOR_ATTACHMENT0,
                    GL11.GL_TEXTURE_2D,
                    colorTexture,
                    0)

        } else colorTexture = -1

        if (attachment == Attachment.Depth || attachment == Attachment.ColorAndDepth) {

            depthTexture = GL11.glGenTextures()
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)

            GL11.glTexImage2D(
                    GL11.GL_TEXTURE_2D,
                    0,
                    GL14.GL_DEPTH_COMPONENT24,
                    width,
                    height,
                    0,
                    GL11.GL_DEPTH_COMPONENT,
                    GL11.GL_FLOAT,
                    MemoryUtil.NULL)

            GL30.glFramebufferTexture2D(
                    GL30.GL_FRAMEBUFFER,
                    GL30.GL_DEPTH_ATTACHMENT,
                    GL11.GL_TEXTURE_2D,
                    depthTexture,
                    0)

        } else depthTexture = -1

        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) !=
            GL30.GL_FRAMEBUFFER_COMPLETE) {
            destroy()
            error("Failed to create framebuffer")
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    }

    public fun destroy() {
        GL30.glDeleteFramebuffers(fbo)

        if (attachment == Attachment.Color || attachment == Attachment.ColorAndDepth)
            GL11.glDeleteTextures(colorTexture)
        if (attachment == Attachment.Depth || attachment == Attachment.ColorAndDepth)
            GL11.glDeleteTextures(depthTexture)
//            GL30.glDeleteRenderbuffers(renderBuffer)
    }

    public fun bind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo)
    }

    public fun unbind() {
        GL11.glFlush() // NOTE: Might not need
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    }

    public fun bindTexture(unit: Int = 0, attachment: Attachment) {

        if (unit < 0 || unit > 31) error("Invalid texture unit")

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit)

        if (attachment == Attachment.Color)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture)
        else if (attachment == Attachment.Depth)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture)
        else
            error("Cant bind both you idiot")
    }
}