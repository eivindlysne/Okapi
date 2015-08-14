package me.lysne.okapi.pipeline

import me.lysne.okapi.graphics.Camera
import me.lysne.okapi.graphics.GBuffer
import me.lysne.okapi.graphics.Shader
import me.lysne.okapi.graphics.Texture
import me.lysne.okapi.window.Window
import me.lysne.okapi.world.World
import org.lwjgl.opengl.GL11

public class Geometry {

    val gBuffer: GBuffer

    val shader: Shader

    init {
        gBuffer = GBuffer()

        shader = Shader("geometryPass.vert.glsl", "geometryPass.frag.glsl")
        shader.registerUniforms(
                "model",
                "view",
                "projection",
                "diffuseTex"
        )
        //shader.setUniform("diffuseTex", 0)
    }

    public fun render(window: Window, camera: Camera, texture: Texture, world: World) {

        GL11.glDisable(GL11.GL_BLEND)

        gBuffer.bind()

            window.clear()

            shader.use()
            shader.setUniform("view", camera.viewMatrix)
            shader.setUniform("projection", camera.projectionMatrix)

            texture.bind(0)

            world.drawGeometry(shader)

        gBuffer.unbind()

        GL11.glEnable(GL11.GL_BLEND)
    }

    public fun destroy() {
        gBuffer.destroy()
        shader.destroy()
    }
}