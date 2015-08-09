package me.lysne.okapi.graphics

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

public class Camera(
        val projectionType: Camera.ProjectionType,
        position: Vector3f) {

    enum class ProjectionType {
        PERSPECTIVE,
        INFINITE_PERSPECTIVE,
        ORTHOGRAPHIC
    }

    private val WORLD_UP = Vector3f(0f, 1f, 0f)
    private val maxPitch = Math.toRadians(60.0).toFloat()
    private var currentPitch = 0f

    // NOTE: Just cache this here temporary
    // FIXME: Hardcoded values
    val orthoMatrix = Matrix4f().ortho(0f, 960f, 0f, 540f, 1f, -1f)

    var projectionMatrix = Matrix4f()
        private set
    var viewMatrix = Matrix4f()
        private set
    var viewProjectionMatrix = Matrix4f()
        private set

    val transform: Transform
    val fov = Math.toRadians(70.0).toFloat()
    val zNear = 0.01f
    val zFar = 256f
    val ascpectRatio = 16f / 9f

    init {
        transform = Transform()
        transform.position.set(position)

        when (projectionType) {
            ProjectionType.PERSPECTIVE -> projectionMatrix.perspective(
                    fov, ascpectRatio, zNear, zFar
            )
            ProjectionType.INFINITE_PERSPECTIVE -> println("Not supported")
            ProjectionType.ORTHOGRAPHIC -> println("Not Supported")
        }
    }

    public fun lookAt(position: Vector3f, up: Vector3f) {

    }

    public fun offsetOrientation(yawDegrees: Double, pitchDegrees: Double) {

        val yaw = Math.toRadians(yawDegrees).toFloat()
        var pitch = Math.toRadians(pitchDegrees).toFloat()

        if (currentPitch + pitch > maxPitch) {
            pitch = maxPitch - currentPitch
        } else if (currentPitch + pitch < -maxPitch) {
            pitch = -maxPitch - currentPitch
        }
        currentPitch += pitch

        val yawRot = Quaternionf().rotateAxis(yaw, WORLD_UP)

        val right = Vector3f(1f, 0f, 0f).rotate(transform.orientation)
        val pitchRot = Quaternionf().rotateAxis(pitch, right)

        yawRot.mul(pitchRot).mul(transform.orientation, transform.orientation)
    }

    public fun update() {

        // Recalculate view
        viewMatrix.identity()
        val orientationConj = Quaternionf(transform.orientation).conjugate()
        val positionNegated = Vector3f(transform.position).negate()

        viewMatrix.scale(Vector3f(1f, 1f, 1f).div(transform.scale))
            .mul(Matrix4f().identity().rotation(orientationConj))
            .mul(Matrix4f().identity().translate(positionNegated))

        // Recalculate Projection?

        // Combine
        projectionMatrix.mul(viewMatrix, viewProjectionMatrix)
    }
}