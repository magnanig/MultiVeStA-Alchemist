package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.influencesphere.FieldOfView2D
import it.unibo.alchemist.model.influencesphere.InfluenceSphere2D
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DShape

/**
 * The bidimensional representation of a pedestrian.
 */
interface Pedestrian2D<T> : Pedestrian<T> {

    /**
     * The default shape of any pedestrian in the Euclidean world.
     *
     * @param env
     *          the environment appointed to create the shape.
     */
    fun shape(env: EuclideanPhysics2DEnvironment<T>): Euclidean2DShape = env.shapeFactory.circle(0.5)

    /**
     * The spheres of influence a pedestrian in the Euclidean world is equipped with.
     *
     * @param env
     *          the environment where the pedestrian is.
     */
    fun sensorySpheres(env: EuclideanPhysics2DEnvironment<T>): List<InfluenceSphere2D<T>> = listOf(
        FieldOfView2D(env, this, 100.0, Math.PI * 2 / 3)
    )
}