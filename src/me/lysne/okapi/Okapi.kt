package me.lysne.okapi

import me.lysne.okapi.graphics.*
import me.lysne.okapi.window.Input
import me.lysne.okapi.window.Window
import me.lysne.okapi.window.getTime
import me.lysne.okapi.world.World
import org.joml.Vector2f
import org.joml.Vector3f
import java.io.File


public class Okapi {

    // Unique objects
    private val window: Window
    private val input: Input
    private val camera: Camera
    private val skybox: Skybox
    private val world: World

    // Framebuffers
    private val screenMesh: TextureMesh
    private val smallMesh: TextureMesh
    private val geometryFB: Framebuffer

    // Shaders
    private val defaultShader: Shader
    private val textureShader: Shader
    private val textShader: Shader

    // Textures
    private val whiteTexture: Texture
    private val terrainTexture: Texture
    private val fontTexture: Texture

    // UI
    private val fpsText: Text

    // Debug stuff
    private var debugShader: Shader? = null

    init  {
        window = Window(Config.WindowWidth, Config.WindowHeight, Config.WindowTitle)
        input = Input(window)
        camera = Camera(Camera.ProjectionType.PERSPECTIVE, Vector3f(0f, 0f, 0f))

        skybox = Skybox("m_negz.png", "m_posz.png",
                        "m_posy.png", "m_negy.png",
                        "m_negx.png", "m_posx.png")

        world = World()

        screenMesh = createFullscreenTextureMesh()
        smallMesh = createSmallTextureMesh()
        geometryFB = Framebuffer(Framebuffer.Attachment.ColorAndDepth)

        defaultShader = Shader("basic_vert.glsl", "basic_frag.glsl")
        defaultShader.registerUniforms(arrayOf(
                "diffuse0", "diffuse1",
                "viewProjection",
                "cameraPosition",
                "transform.position", "transform.orientation", "transform.scale",
                "pointLight.position", "pointLight.intensity",
                "pointLight.attenuation.constant", "pointLight.attenuation.linear", "pointLight.attenuation.quadratic"))
        defaultShader.setUniform("diffuse0", 0)

        textureShader = Shader("texturePass_vert.glsl", "texturePass_frag.glsl")
        textureShader.registerUniforms(arrayOf("scaleFactor", "texture0"))
        textureShader.setUniform("texture0", 0)

        textShader = Shader("text_vert.glsl", "text_frag.glsl")
        textShader.registerUniforms(arrayOf("viewProjection", "font"))
        textShader.setUniform("font", 0)
        fpsText = Text("FPS:  0 ", Vector2f(10f, 10f))

        whiteTexture = Texture("textures/white.png")
        terrainTexture = Texture("textures/terrain.png")
        fontTexture = Texture("fonts/font.png")

        if (Config.DebugRender) {
            debugShader = Shader("debug_vert.glsl", "debug_frag.glsl")
            debugShader?.registerUniforms(arrayOf(
                    "viewProjection",
                    "transform.position", "transform.orientation", "transform.scale"))
        }
    }

    public fun run() {

        window.show()

        var lastTime = getTime()
        var frameCounter = 0.0
        var unprocessedTime = 0.0
        var frames = 0

        while (!window.shouldClose()) {

            var render = false

            var startTime = getTime()
            var elapsedTime = startTime - lastTime
            lastTime = startTime

            unprocessedTime += elapsedTime
            frameCounter += elapsedTime

            if (frameCounter >= 1.0) {

                if (Config.Debug)
                    fpsText.setNew("FPS: $frames")

                frames = 0
                frameCounter = 0.0
            }

            while (unprocessedTime > Config.FrameTime) {

                window.poll()

                update(Config.FrameTime)

                render = true

                unprocessedTime -= Config.FrameTime
            }

            if (render) {
                render(unprocessedTime / Config.FrameTime)
                frames++
            } // Else sleep?
        }

        destroy()
    }

    private fun update(delta: Double) {

        camera.update(input)
        skybox.update(camera)

        world.update(camera)
    }

    private fun render(alpha: Double) {

        geometryFB.bind()
        window.clear()

        skybox.draw(camera.viewProjectionMatrix)

        defaultShader.use()

        defaultShader.setUniform("viewProjection", camera.viewProjectionMatrix)
        defaultShader.setUniform("cameraPosition", camera.transform.position)

        terrainTexture.bind(0)

        // Draw World
        world.draw(defaultShader)

        geometryFB.unbind()
        window.clear()

        if (Config.DebugRender)
            renderDebug()

        // Draw Ortho (Text and UI)
        textShader.use()
        textShader.setUniform("viewProjection", camera.orthoMatrix)
        fontTexture.bind(0)
        fpsText.draw()
        world.drawText()

        textureShader.use()
        geometryFB.bindTexture(0, Framebuffer.Attachment.Color)
        screenMesh.draw()
        //smallMesh.draw()

        window.swap()
    }

    private fun renderDebug() {

        debugShader?.use()
        debugShader?.setUniform("viewProjection", camera.viewProjectionMatrix)

        world.drawDebug(debugShader)
    }

    private fun destroy() {
        skybox.destroy()
        window.destroy()
        world.destroy()

        screenMesh.destroy()
        smallMesh.destroy()
        geometryFB.destroy()

        defaultShader.destroy()
        textureShader.destroy()
        textShader.destroy()

        whiteTexture.destroy()
        terrainTexture.destroy()
        fontTexture.destroy()

        fpsText.destroy()

        if (Config.DebugRender) {
            debugShader?.destroy()
        }
    }
}

fun main(args: Array<String>) {
    System.setProperty("org.lwjgl.librarypath", File("lib/lwjgl/native").getAbsolutePath())
    val okapi = Okapi()
    okapi.run()
}