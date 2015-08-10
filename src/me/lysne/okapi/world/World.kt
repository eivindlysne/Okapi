package me.lysne.okapi.world

import me.lysne.okapi.graphics.Camera
import me.lysne.okapi.graphics.PointLight
import me.lysne.okapi.graphics.Shader
import me.lysne.okapi.graphics.Text
import me.lysne.okapi.window.getTime
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import java.util.HashMap
import java.util.Random

public class World {

    private val regions: MutableMap<Pair<Int, Int>, Region>
    private val coordsText: Text

    val r = Random()

    val ambientLight = Vector4f(1f, 1f, 0f, .2f)
    val light: PointLight

    var currentRegion: Region

    init {
        regions = HashMap<Pair<Int, Int>, Region>()

        // North row
        createNewRegion(-1, -1)
        createNewRegion( 0, -1)
        createNewRegion( 1, -1)

        // Middle row
        createNewRegion(-1,  0)
        currentRegion = createNewRegion( 0,  0) // Always start at center
        createNewRegion( 1,  0)

        // South row
        createNewRegion(-1,  1)
        createNewRegion( 0,  1)
        createNewRegion( 1,  1)

        light = PointLight(Vector3f(0f, 2f, 0f), Vector3f(10f, 10f, 10f))
        coordsText = Text("Coords: (0,0)      ", Vector2f(10f, 35f))
    }

    public fun update(camera: Camera) {

        // TODO: World should have its own transformation
        // TODO: Hardcoded region offset
        val camPosition = Vector3f().set(camera.transform.position)
                .add(REGION_SIZE_X / 2f, 0f, REGION_SIZE_Z / 2f)

        val camWorldX = Math.floorDiv(camPosition.x.toInt(), REGION_SIZE_X)
        val camWorldZ = Math.floorDiv(camPosition.z.toInt(), REGION_SIZE_Z)

        var crossedRegionBoundary = (
                currentRegion.worldX != camWorldX ||
                currentRegion.worldZ != camWorldZ)

        if (crossedRegionBoundary) {

            coordsText.setNew("Coords: ($camWorldX,$camWorldZ)")

            var newRegion = getRegion(camWorldX, camWorldZ)
            if (newRegion == null) newRegion = createNewRegion(camWorldX, camWorldZ)

            // Create missing neighbors
            for (z in -1..1) {
                for (x in -1..1) {
                    var nx = newRegion.worldX + x
                    val nz = newRegion.worldZ + z
                    if (getRegion(nx, nz) == null) {
                        createNewRegion(nx, nz)
                    }
                }
            }

            currentRegion = newRegion
        }

        light.position.x = 4.0f + 8.0f * Math.cos(getTime()).toFloat()
        light.position.z = 4.0f + 8.0f * Math.sin(getTime()).toFloat()


        for (region in regions.values()) {
            region.update()
        }
    }

    public fun draw(shader: Shader) {

        shader.setUniform("pointLight.position", light.position)
        shader.setUniform("pointLight.intensity", light.intensity)

        for (region in regions.values()) {
            region.draw(shader)
        }
    }

    // FIXME: Maybe not the best place for this
    public fun drawText() {
        coordsText.draw()
    }

    public fun drawDebug(shader: Shader?) {
        for (region in regions.values()) {
            region.drawDebugMesh(shader)
        }
    }

    public fun destroy() {
        for (region in regions.values()) {
            region.destroy()
        }
        coordsText.destroy()
    }

    public fun createNewRegion(x: Int, z: Int) : Region {

        val region = Region(x, z, this)
        regions[Pair(x, z)] = region
        return region
    }

    public fun getRegion(x: Int, z: Int): Region? {
        return regions.get(Pair(x, z))
    }
}