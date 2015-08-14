package me.lysne.okapi.pipeline

import me.lysne.okapi.graphics.Framebuffer
import me.lysne.okapi.graphics.GBuffer
import me.lysne.okapi.graphics.Shader
import me.lysne.okapi.graphics.TextureMesh
import me.lysne.okapi.window.Window
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13

public class Lighting {

    val lightingFB: Framebuffer

    private val ambientShader: Shader

    init {
        lightingFB = Framebuffer(Framebuffer.Attachment.Color)
        ambientShader = Shader("genericLightPass.vert.glsl", "ambientPass.frag.glsl")
        ambientShader.registerUniforms(
                "gPositionDepth",
                "gNormal",
                "gAlbedoSpec",
                "ssaoTex")
//        ambientShader.setUniform("gPositionDepth", 0)
//        ambientShader.setUniform("gNormal", 1)
        ambientShader.setUniform("gAlbedoSpec", 2)
        ambientShader.setUniform("ssaoTex", 3)
    }

    public fun render(window: Window, gBuffer: GBuffer, ssaoFB: Framebuffer, screenMesh: TextureMesh) {

        lightingFB.bind()
            window.clear()
            ambientShader.use()
            GL13.glActiveTexture(GL13.GL_TEXTURE0)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, gBuffer.gPositionDepth)
            GL13.glActiveTexture(GL13.GL_TEXTURE1)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, gBuffer.gNormal)
            GL13.glActiveTexture(GL13.GL_TEXTURE2)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, gBuffer.gAlbedoSpec)
            ssaoFB.bindTexture(3, Framebuffer.Attachment.Color)

            screenMesh.draw()
        lightingFB.unbind()
    }

    public fun destroy() {
        lightingFB.destroy()
        ambientShader.destroy()
    }
}