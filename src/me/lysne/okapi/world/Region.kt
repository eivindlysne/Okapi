package me.lysne.okapi.world

import me.lysne.okapi.Config
import me.lysne.okapi.graphics.*
import org.joml.Vector3f

private val REGION_SIZE_X: Int = 32
private val REGION_SIZE_Z: Int = 32

public class Region(val worldX: Int, val worldZ: Int, val world: World) {

    private val mesh: Mesh
    private val transform: Transform

    private var debugMesh: DebugMesh? = null

    init {

        transform = Transform()
        transform.position.set(
                -(worldX * REGION_SIZE_X.toFloat()) - REGION_SIZE_X / 2,
                0f,
                -(worldZ * REGION_SIZE_Z.toFloat()) - REGION_SIZE_Z / 2)

        val rgb = Vector3f(
                Math.random().toFloat(),
                Math.random().toFloat(),
                Math.random().toFloat())

        // NOTE: Might not need to delay after all
        val data = createRegionMeshData(REGION_SIZE_X, REGION_SIZE_Z, -1f, rgb, transform)
        mesh = Mesh(data.vertices, data.indices)

        if (Config.DEBUG) {
            debugMesh = DebugMesh(data.vertices, data.indices)
        }
    }


    fun draw(shader: Shader) {

        shader.setUniform("transform.position", transform.position)
        shader.setUniform("transform.orientation", transform.orientation)
        shader.setUniform("transform.scale", transform.scale)

        mesh.draw()
    }

    fun drawDebugMesh(shader: Shader?) {

        shader?.setUniform("transform.position", transform.position)
        shader?.setUniform("transform.orientation", transform.orientation)
        shader?.setUniform("transform.scale", transform.scale)

        debugMesh?.draw()
    }

    fun update() {

    }

    fun destroy() {
        mesh.destroy()
        debugMesh?.destroy()
    }
}