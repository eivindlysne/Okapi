package me.lysne.okapi.graphics

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

public class Transform {

    var position = Vector3f(0f, 0f, 0f)
    var orientation = Quaternionf(0f, 0f, 0f, 1f)
    var scale = Vector3f(1f, 1f, 1f)

    public fun mul(other: Transform): Transform {

        val final = Transform()
        val tempV = Vector3f()
        val tempQ = Quaternionf()

        // final position
        tempV.set(position).mul(other.scale).rotate(other.orientation).add(other.position)
        final.position.set(tempV)

        // final orientation
        tempQ.set(orientation).mul(other.orientation)
        final.orientation.set(tempQ)

        // final scale
        tempV.set(scale)
        tempV.rotate(other.orientation).mul(other.scale)
        final.scale.set(tempV)

        return final
    }

    public fun mulInPlace(other: Transform): Transform {

        position.mul(other.scale).rotate(other.orientation).add(other.scale)
        orientation.mul(other.orientation)
        scale.rotate(other.orientation).mul(other.scale)

        return this
    }

    public fun div(other: Transform): Transform {

        val final = Transform()
        val tempV = Vector3f()
        val tempQ = Quaternionf()

        val qConj = Quaternionf(other.orientation).conjugate()

        tempV.set(position).sub(other.position).rotate(qConj).div(other.scale)
        final.position.set(tempV)

        tempQ.set(orientation).mul(qConj)
        final.orientation.set(tempQ)

        tempV.set(scale).div(other.scale).rotate(qConj)
        final.scale.set(tempV)

        return final
    }

    public fun divInPlace(other: Transform): Transform {

        val qConj = Quaternionf(other.orientation).conjugate()

        position.sub(other.position).rotate(qConj).div(other.scale)
        orientation.mul(qConj)
        scale.div(other.scale).rotate(qConj)

        return this
    }

    public fun inverse(): Transform {

        val final = Transform()
        val tempV = Vector3f()

        val qConj = Quaternionf(orientation).conjugate()

        tempV.set(position).negate().rotate(qConj).div(scale)
        final.position.set(tempV)

        final.orientation.set(qConj)

        tempV.set(1f, 1f, 1f).div(scale).rotate(qConj)
        final.scale.set(tempV)

        return final
    }

    public fun transformPoint(point: Vector3f): Vector3f {

        val final = Vector3f(position)
        val conjugate = Quaternionf(orientation).conjugate()

        return final.sub(point).rotate(conjugate).div(scale)
    }

    public fun transformMatrix4f(): Matrix4f {

        val mPosition = Matrix4f().identity()
        val mOrientation = Matrix4f().identity()
        val mScale = Matrix4f().identity()

        return mPosition.translate(position)
                .mul(mOrientation.rotation(orientation))
                .mul(mScale.scale(scale))
    }
}