package me.lysne.okapi.world

import me.lysne.okapi.generation.TreeGenerator
import me.lysne.okapi.graphics.Mesh
import org.joml.Vector3f

public class Tree {

//    val trunkMesh: Mesh
    val crownMesh: Mesh

    init {
//        trunkMesh = TreeGenerator.generateIcoSphere(Vector3f(.54f, .32f, .18f))
    //    crownMesh = TreeGenerator.generateIcosahedron(Vector3f(1f, .31f, 0f), 4f)
//        crownMesh = TreeGenerator.generateIcoSphere(Vector3f(.2f, .4f, 0f))
        crownMesh = TreeGenerator.generateConvexHull(10, 4f, Vector3f(.2f, .4f, 0f), Vector3f(1f,2.5f,1f))
    }

    public fun draw() {
//        trunkMesh.draw()
        crownMesh.draw()
    }

    public fun destroy() {
//        trunkMesh.destroy()
        crownMesh.destroy()
    }
}