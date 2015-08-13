package me.lysne.okapi.world

import me.lysne.okapi.Config
import me.lysne.okapi.generation.createRegionMeshData
import me.lysne.okapi.graphics.*
import org.joml.Vector2f
import org.joml.Vector3f

val REGION_SIZE_X: Int = 32
val REGION_SIZE_Z: Int = 32

public class Region(val worldX: Int, val worldZ: Int, val world: World) {

    // Need lots of other values for diversity
    enum class Type(
            val color: Vector3f,
            val tileCoord: Vector2f,
            val magnitude: Float) {
        Field(Vector3f(.2f, 1f, .3f), Vector2f(0f, 2f), 1f),
        Desert(Vector3f(.76f, .74f, .5f), Vector2f(0f, 1f), 3f),
        Rocky(Vector3f(.34f, .37f, .39f), Vector2f(0f, 0f), 6f),
//        Water(Vector3f(.117f, .564f, 1f))
    }

    private val mesh: Mesh
    private val transform: Transform

    private val tree: Tree

    val type: Type

    private var debugMesh: DebugMesh? = null

    init {

        transform = Transform()
        transform.position.set(
                (worldX * REGION_SIZE_X.toFloat()) - REGION_SIZE_X / 2,
                0f,
                (worldZ * REGION_SIZE_Z.toFloat()) + REGION_SIZE_Z / 2)

        type = Type.values()[world.r.nextInt(Type.values().size())]

        // NOTE: Might not need to delay after all
        val data = createRegionMeshData(
                world.getRegion(worldX, worldZ - 1),
                world.getRegion(worldX, worldZ + 1),
                world.getRegion(worldX - 1, worldZ),
                world.getRegion(worldX + 1, worldZ),
                type,
                REGION_SIZE_X,
                REGION_SIZE_Z,
                -1f,
                type.color,
                transform)
        data.generateNormals()
        mesh = Mesh(data.vertices, data.indices)

        if (Config.Debug) {
            debugMesh = DebugMesh(data.vertices, data.indices)
        }

        world.pointLights.add(PointLight(
                Vector3f(1f, 1f, 1f),
                100f,
                Vector3f(worldX * REGION_SIZE_X.toFloat(), 4f, worldZ * REGION_SIZE_Z.toFloat()),
                Attenuation()))

        tree = Tree()
    }


    fun draw(shader: Shader) {

        shader.setUniform("transform.position", transform.position)
        shader.setUniform("transform.orientation", transform.orientation)
        shader.setUniform("transform.scale", transform.scale)

        mesh.draw()

        tree.draw()
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
        tree.destroy()
        debugMesh?.destroy()
    }
}