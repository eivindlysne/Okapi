package me.lysne.okapi.world

import me.lysne.okapi.graphics.Shader
import java.util.*

public class World {

    private val regions: MutableMap<Pair<Int, Int>, Region>


    init {
        regions = HashMap<Pair<Int, Int>, Region>()

        // North row
        createNewRegion(-1, -1)
        createNewRegion( 0, -1)
        createNewRegion( 1, -1)

        // Middle row
        createNewRegion(-1,  0)
        createNewRegion( 0,  0)
        createNewRegion( 1,  0)

        // South row
        createNewRegion(-1,  1)
        createNewRegion( 0,  1)
        createNewRegion( 1,  1)
    }

    public fun update() {
        for (region in regions.values()) {
            region.update()
        }
    }

    public fun draw(shader: Shader) {
        for (region in regions.values()) {
            region.draw(shader)
        }
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
    }

    public fun createNewRegion(x: Int, z: Int) {

        regions[Pair(x, z)] = Region(x, z, this)
    }

    public fun getRegion(x: Int, z: Int): Region? {

        return regions.get(Pair(x, z))
    }
}