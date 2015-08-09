package me.lysne.okapi

import me.lysne.okapi.graphics.*
import me.lysne.okapi.window.Input
import me.lysne.okapi.window.Window
import me.lysne.okapi.window.getTime
import me.lysne.okapi.world.REGION_SIZE_X
import me.lysne.okapi.world.REGION_SIZE_Z
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

    // Shaders
    private val basicShader: Shader
    private val textShader: Shader

    // Textures
    private val whiteTexture: Texture
    private val rockTexture: Texture
    private val mossTexture: Texture
    private val fontTexture: Texture

    // UI
    private val fpsText: Text

    // Debug stuff
    private var debugShader: Shader? = null
//    private var debugMesh: DebugMesh? = null


    init  {
        window = Window(Config.WindowWidth, Config.WindowHeight, Config.WindowTitle)
        input = Input(window)
        camera = Camera(Camera.ProjectionType.PERSPECTIVE, Vector3f(0f, 0f, 0f))

        skybox = Skybox("neg_z.png", "pos_z.png",
                        "pos_y.png", "neg_y.png",
                        "neg_x.png", "pos_x.png")

        world = World()

        basicShader = Test.getBasicShader()

        textShader = Shader("text_vert.glsl", "text_frag.glsl")
        textShader.registerUniforms(arrayOf("viewProjection", "font"))
        textShader.setUniform("font", 0)
        fontTexture = Texture("fonts/font.png", Texture.Filter.LINEAR, Texture.WrapMode.CLAMP_TO_EDGE)
        fpsText = Text("FPS:  0 ", Vector2f(10f, 10f))

        whiteTexture = Texture("textures/white.png", Texture.Filter.NEAREST, Texture.WrapMode.CLAMP_TO_EDGE)
        rockTexture = Texture("textures/crack.png", Texture.Filter.LINEAR, Texture.WrapMode.CLAMP_TO_EDGE)
        mossTexture = Texture("textures/moss2.png", Texture.Filter.LINEAR, Texture.WrapMode.CLAMP_TO_EDGE)

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
        window.clear()

        skybox.draw(camera.viewProjectionMatrix)

        basicShader.use()

        basicShader.setUniform("viewProjection", camera.viewProjectionMatrix)

        mossTexture.bind(0)
        mossTexture.bind(1)

        // Draw World
        world.draw(basicShader)

        if (Config.DebugRender)
            renderDebug()

        // Draw Ortho (Text and UI)
        textShader.use()
        textShader.setUniform("viewProjection", camera.orthoMatrix)
        fontTexture.bind(0)
        fpsText.draw()
        world.drawText()


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

        basicShader.destroy()
        textShader.destroy()

        rockTexture.destroy()
        mossTexture.destroy()
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