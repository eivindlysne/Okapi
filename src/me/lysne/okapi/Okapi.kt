package me.lysne.okapi

import me.lysne.okapi.generation.createTextureMesh
import me.lysne.okapi.graphics.*
import me.lysne.okapi.window.Input
import me.lysne.okapi.window.Window
import me.lysne.okapi.window.getTime
import me.lysne.okapi.world.World
import org.joml.Matrix4f
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
    private val lightFramebuffer: Framebuffer
    private val gBuffer: GBuffer

    // Shaders
    private val defaultShader: Shader
    private val geometryPassShader: Shader
    private val texturePassShader: Shader
    private val pointLightShader: Shader
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

        gBuffer = GBuffer()
        screenMesh = createTextureMesh(-1f, -1f, 1f, 1f)
        smallMesh = createTextureMesh(1f - (1f / 2f), 1f - (1f / 2f), 1f, 1f)
        lightFramebuffer = Framebuffer(Framebuffer.Attachment.ColorAndDepth)

        defaultShader = Shader("basic.vert.glsl", "basic.frag.glsl")
        defaultShader.registerUniforms(
                "diffuse0", "diffuse1",
                "viewProjection",
                "cameraPosition",
                "transform.position", "transform.orientation", "transform.scale",
                "pointLight.position", "pointLight.intensity",
                "pointLight.attenuation.constant", "pointLight.attenuation.linear", "pointLight.attenuation.quadratic")
        defaultShader.setUniform("diffuse0", 0)

        geometryPassShader = Shader("geometryPass.vert.glsl", "geometryPass.frag.glsl")
        geometryPassShader.registerUniforms(
                "diffuse0",
                "viewProjection",
                "transform.position", "transform.orientation", "transform.scale")
        geometryPassShader.setUniform("diffuse0", 0)

        texturePassShader = Shader("texturePass.vert.glsl", "texturePass.frag.glsl")
        texturePassShader.registerUniforms("scaleFactor", "texture0")
        texturePassShader.setUniform("texture0", 0)

        pointLightShader = Shader("genericLightPass.vert.glsl", "pointLightPass.frag.glsl")
        pointLightShader.registerUniforms(
                "diffuse", "specular", "normal", "depth",
                "invViewProjection",
                "light.color", "light.intensity", "light.position", "light.range",
                "light.attenuation.constant", "light.attenuation.linear", "light.attenuation.quadratic")
        pointLightShader.setUniform("diffuse", 0)
//        pointLightShader.setUniform("specular", 1)
        pointLightShader.setUniform("normal", 2)
        pointLightShader.setUniform("depth", 3)

        textShader = Shader("text.vert.glsl", "text.frag.glsl")
        textShader.registerUniforms("viewProjection", "font")
        textShader.setUniform("font", 0)
        fpsText = Text("FPS:  0 ", Vector2f(10f, 10f))

        whiteTexture = Texture("textures/white.png")
        terrainTexture = Texture("textures/terrain.png")
        fontTexture = Texture("fonts/font.png")

        if (Config.DebugRender) {
            debugShader = Shader("debug.vert.glsl", "debug.frag.glsl")
            debugShader?.registerUniforms(
                    "viewProjection",
                    "transform.position", "transform.orientation", "transform.scale")
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

        // Geometry pass
        gBuffer.bind()
            window.clear()

//            skybox.draw(camera.viewProjectionMatrix)

            geometryPassShader.use()
            geometryPassShader.setUniform("viewProjection", camera.viewProjectionMatrix)

            whiteTexture.bind(0)

            // Draw World
            world.drawGeometry(geometryPassShader)
        gBuffer.unbind()

        // TODO: Temp lighting
//        window.clear()
//
//        skybox.draw(camera.viewProjectionMatrix)
//
//        defaultShader.use()
//
//        defaultShader.setUniform("viewProjection", camera.viewProjectionMatrix)
//        defaultShader.setUniform("cameraPosition", camera.transform.position)
//
//        terrainTexture.bind(0)
//
//        world.draw(defaultShader)

        // Debug pass
        if (Config.DebugRender)
            renderDebug()

        // Lighting
        lightFramebuffer.bind()
            window.clear()
            pointLightShader.use()
            pointLightShader.setUniform("invViewProjection", Matrix4f(camera.viewProjectionMatrix).invert())
            gBuffer.bindTextures()
            world.drawLights(pointLightShader, screenMesh)
        lightFramebuffer.unbind()

        // Texture pass
        window.clear()
        texturePassShader.use()
        lightFramebuffer.bindTexture(0, Framebuffer.Attachment.Color)
        screenMesh.draw()

        // Skybox pass use stencil buffer?
        // FIXME: Not working
//        skybox.draw(camera.viewProjectionMatrix)


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

        screenMesh.destroy()
        smallMesh.destroy()
        lightFramebuffer.destroy()
        gBuffer.destroy()

        defaultShader.destroy()
        geometryPassShader.destroy()
        texturePassShader.destroy()
        pointLightShader.destroy()
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
//    TreeGenerator.timeGenerateConvexHull(80)
//    TreeGenerator.timeGenerateIcoSphere(1)
}