package me.lysne.okapi

import me.lysne.okapi.graphics.*
import me.lysne.okapi.window.Input
import me.lysne.okapi.window.Window
import me.lysne.okapi.window.getTime
import me.lysne.okapi.world.World
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.Sys
import org.lwjgl.stb.STBPerlin
import java.io.File




public class Okapi {

    // Unique objects
    private val window: Window
    private val input: Input
    private val camera: Camera
    private val skybox: Skybox
    private val world: World
//    private val floorMesh: Mesh

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
        window = Window(Config.WIDTH, Config.HEIGHT, Config.TITLE)
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
        fpsText = Text(Vector2f(10f, 10f), Vector3f(0f, 1f, 0f), "FPS:  0 ")

        whiteTexture = Texture("textures/white.png", Texture.Filter.NEAREST, Texture.WrapMode.CLAMP_TO_EDGE)
        rockTexture = Texture("textures/crack.png", Texture.Filter.LINEAR, Texture.WrapMode.CLAMP_TO_EDGE)
        mossTexture = Texture("textures/moss.png", Texture.Filter.LINEAR, Texture.WrapMode.CLAMP_TO_EDGE)

        if (Config.DEBUG) {
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

                if (Config.DEBUG)
                    fpsText.setNew("FPS: $frames")
//                    println(frames)

                frames = 0
                frameCounter = 0.0
            }

            while (unprocessedTime > Config.FRAME_TIME) {

                window.poll()

                update(Config.FRAME_TIME)

                render = true

                unprocessedTime -= Config.FRAME_TIME
            }

            if (render) {
                render(unprocessedTime / Config.FRAME_TIME)
                frames++
            } // Else sleep?
        }

        destroy()
    }

    private fun update(delta: Double) {

        // TODO: Input handling should be somewhere else
        camera.offsetOrientation(
                -input.mousedx * 0.2,
                -input.mousedy * 0.2)

        input.centerCursor()

        val velocity = 0.2f
        val direction = Vector3f(0f, 0f, 0f)

        if (input.keyDown(Input.Key.FORWARD))  direction.z -= 1f
        if (input.keyDown(Input.Key.BACKWARD)) direction.z += 1f
        if (input.keyDown(Input.Key.LEFT))     direction.x -= 1f
        if (input.keyDown(Input.Key.RIGHT))    direction.x += 1f
        if (direction.length() > 0) direction.normalize()

        direction.rotate(camera.transform.orientation)
        if (!Config.FLYING) direction.y = 0.0f
        direction.mul(velocity)
        camera.transform.position.add(direction)

        // TODO: Working bounce
//        if (direction.x != 0f || direction.y != 0f) {
//            val f = 0.6 * Math.sin(4 * getTime())
//            camera.transform.position.y = f.toFloat()
//        }

        camera.update()
        skybox.update(camera)

        world.update()
    }

    private fun render(alpha: Double) {
        window.clear()

        skybox.draw(camera.viewProjectionMatrix)

        basicShader.use()

        basicShader.setUniform("viewProjection", camera.viewProjectionMatrix)

        mossTexture.bind(0)
        mossTexture.bind(1)

//        floorMesh.draw()

        // Draw World
        world.draw(basicShader)

        if (Config.DEBUG)
            renderDebug()

        // Draw Ortho (Text and UI)
        textShader.use()
        textShader.setUniform("viewProjection", camera.orthoMatrix)
        fontTexture.bind(0)
        fpsText.draw()


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

        if (Config.DEBUG) {
            debugShader?.destroy()
        }
    }
}

fun main(args: Array<String>) {
    System.setProperty("org.lwjgl.librarypath", File("lib/lwjgl/native").getAbsolutePath())
    val okapi = Okapi()
    okapi.run()
}