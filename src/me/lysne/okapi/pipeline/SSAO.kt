package me.lysne.okapi.pipeline

import me.lysne.okapi.graphics.*
import me.lysne.okapi.window.Window
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL30
import java.util.Random

public class SSAO {

    private val r = Random()

    val ssaoFB: Framebuffer
    val blurFB: Framebuffer

    val ssaoShader: Shader
//    val ssaoBlurShader: Shader

    val noiseTexture: Int

    init {
        ssaoFB = Framebuffer(Framebuffer.Attachment.Color)
        blurFB = Framebuffer(Framebuffer.Attachment.Color)

        ssaoShader = Shader("genericLightPass.vert.glsl", "ssaoPass.frag.glsl")
        ssaoShader.registerUniforms(
                "gPositionDepth",
                "gNormal",
                "noiseTex",
                "samples",
                "projection")
        ssaoShader.setUniform("gPositionDepth", 0)
        ssaoShader.setUniform("gNormal", 1)
        ssaoShader.setUniform("noiseTex", 2)
        ssaoShader.setUniform("samples", generateKernel())
//
//        ssaoBlurShader = Shader("genericLightPass.vert.glsl", "ssaoBlurPass.frag.glsl")


        noiseTexture = generateNoiseTexture()
    }

    public fun render(window: Window, camera: Camera, gBuffer: GBuffer, screenMesh: TextureMesh) {

        GL11.glDisable(GL11.GL_BLEND)

        ssaoFB.bind()
            window.clear()
            ssaoShader.use()
            ssaoShader.setUniform("projection", camera.projectionMatrix)

            bindTextures(gBuffer)

            screenMesh.draw()

        ssaoFB.unbind()

        GL11.glEnable(GL11.GL_BLEND)
    }

    private fun bindTextures(gBuffer: GBuffer) {

        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, gBuffer.gPositionDepth)
        GL13.glActiveTexture(GL13.GL_TEXTURE1)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, gBuffer.gNormal)
        GL13.glActiveTexture(GL13.GL_TEXTURE2)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, noiseTexture)
    }

    private fun lerp(a: Float, b: Float, f: Float) : Float = a + f * (b - a)

    private fun generateKernel() : Array<Vector3f> {

        return Array(64, { i ->
            val sample = Vector3f(
                    r.nextFloat() * 2f - 1f,
                    r.nextFloat() * 2f - 1f,
                    r.nextFloat()
            ).normalize()
            sample.mul(r.nextFloat())
            var scale = i.toFloat() / 64f
            scale = lerp(0.1f, 1.0f, scale * scale)
            sample.mul(scale)
        })


    }

    private fun generateNoiseTexture() : Int {

        val ssaoNoise = Array(16, { i ->
            Vector3f(
                    r.nextFloat() * 2f - 1f,
                    r.nextFloat() * 2f - 1f,
                    0f
            )
        })
        val noiseBuffer = BufferUtils.createFloatBuffer(16 * 3)
        ssaoNoise.forEach { v ->
            noiseBuffer.put(v.x)
                       .put(v.y)
                       .put(v.z)
        }
        noiseBuffer.flip()

        val noiseTexture = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, noiseTexture)
        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL30.GL_RGB16F,
                4,
                4,
                0,
                GL11.GL_RGB,
                GL11.GL_FLOAT,
                noiseBuffer)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        return noiseTexture
    }

    public fun destroy() {
        ssaoFB.destroy()
        blurFB.destroy()
        ssaoShader.destroy()
        GL11.glDeleteTextures(noiseTexture)
    }

}