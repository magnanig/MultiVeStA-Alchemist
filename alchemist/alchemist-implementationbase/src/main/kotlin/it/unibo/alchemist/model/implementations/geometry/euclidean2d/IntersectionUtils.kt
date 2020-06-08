/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.geometry.euclidean2d

import it.unibo.alchemist.model.implementations.geometry.euclidean2d.CircleSegmentIntersectionType.PAIR
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.LinesIntersectionType.LINE
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.SegmentsIntersectionType.SEGMENT
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Segment2D
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.util.Optional

/**
 * In euclidean geometry, the intersection of two lines can be an [EMPTY] set, a [POINT],
 * or a [LINE] (in other words, infinite points).
 */
enum class LinesIntersectionType {
    EMPTY,
    POINT,
    LINE
}

/**
 * Describes the result of the intersection between two lines in an euclidean space.
 *
 * @param type
 *              the type of intersection.
 * @param point
 *              the intersection point (if present).
 */
data class LinesIntersection<P : Vector2D<P>>(
    val type: LinesIntersectionType,
    val point: Optional<P> = Optional.empty()
) {
    constructor(point: P) : this(LinesIntersectionType.POINT, Optional.of(point))

    companion object {
        /**
         * Creates an instance of [LinesIntersection] whose type is [LinesIntersectionType.EMPTY].
         */
        fun <P : Vector2D<P>> empty() =
            LinesIntersection<P>(
                LinesIntersectionType.EMPTY
            )

        /**
         * Creates an instance of [LinesIntersection] whose type is [LinesIntersectionType.LINE].
         */
        fun <P : Vector2D<P>> line() =
            LinesIntersection<P>(
                LinesIntersectionType.LINE
            )
    }
}

/**
 * Finds the intersection of two lines represented by segments.
 * Degenerate segments (of zero
 * length) are not supported.
 */
fun <P : Vector2D<P>> linesIntersection(s1: Segment2D<P>, s2: Segment2D<P>): LinesIntersection<P> {
    require(!s1.isDegenerate && !s2.isDegenerate) { "degenerate segments are not lines" }
    val m1 = s1.slope
    val q1 = s1.intercept
    val m2 = s2.slope
    val q2 = s2.intercept
    return when {
        coincide(m1, m2, q1, q2, s1, s2) -> LinesIntersection.line()
        areParallel(m1, m2) -> LinesIntersection.empty()
        else -> {
            val intersection = when {
                s1.yAxisAligned -> s1.first.newFrom(s1.first.x, m2 * s1.first.x + q2)
                s2.yAxisAligned -> s1.first.newFrom(s2.first.x, m1 * s2.first.x + q1)
                else -> {
                    val x = (q2 - q1) / (m1 - m2)
                    val y = m1 * x + q1
                    s1.first.newFrom(x, y)
                }
            }
            LinesIntersection(intersection)
        }
    }
}

private fun coincide(m1: Double, m2: Double, q1: Double, q2: Double, s1: Segment2D<*>, s2: Segment2D<*>) =
    when {
        !areParallel(m1, m2) -> false
        s1.yAxisAligned && s2.yAxisAligned -> fuzzyEquals(s1.first.x, s2.first.x)
        else -> fuzzyEquals(q1, q2)
    }

private fun areParallel(m1: Double, m2: Double) =
    (m1.isInfinite() && m2.isInfinite()) || (m1.isFinite() && m2.isFinite() && fuzzyEquals(m1, m2))

/**
 * In euclidean geometry, the intersection of two segments can be an [EMPTY] set, a [POINT], or a
 * [SEGMENT] (in other words, infinite points).
 */
enum class SegmentsIntersectionType {
    EMPTY,
    POINT,

    /**
     * Note that two segments may be collinear, overlapping and share a single point (e.g. an
     * endpoint). In this case the intersection type is [POINT].
     */
    SEGMENT
}

/**
 * Describes the result of the intersection between two segments in an euclidean space.
 *
 * @param type
 *              the type of intersection.
 * @param point
 *              the intersection point (if present).
 */
data class SegmentsIntersection<P : Vector2D<P>>(
    val type: SegmentsIntersectionType,
    val point: Optional<P> = Optional.empty()
) {
    constructor(point: P) : this(SegmentsIntersectionType.POINT, Optional.of(point))

    companion object {
        /**
         * Creates an instance of [SegmentsIntersection] whose type is [SegmentsIntersectionType.EMPTY].
         */
        fun <P : Vector2D<P>> empty() =
            SegmentsIntersection<P>(
                SegmentsIntersectionType.EMPTY
            )

        /**
         * Creates an instance of [SegmentsIntersection] whose type is [SegmentsIntersectionType.SEGMENT].
         */
        fun <P : Vector2D<P>> segment() =
            SegmentsIntersection<P>(
                SegmentsIntersectionType.SEGMENT
            )
    }
}

/**
 * Finds the intersection point of two given segments. This method is able to deal with degenerate
 * and collinear segments.
 */
fun <P : Vector2D<P>> Segment2D<P>.intersectSegment(other: Segment2D<P>): SegmentsIntersection<P> {
    if (isDegenerate || other.isDegenerate) {
        val degenerate = takeIf { it.isDegenerate } ?: other
        val otherSegment = other.takeIf { degenerate == this } ?: this
        return when {
            otherSegment.contains(degenerate.first) -> SegmentsIntersection(degenerate.first)
            else -> SegmentsIntersection.empty()
        }
    }
    val intersection = linesIntersection(this, other)
    return when {
        intersection.type == LinesIntersectionType.POINT && bothContain(this, other, intersection.point.get()) ->
            SegmentsIntersection(intersection.point.get())
        intersection.type == LinesIntersectionType.LINE && !disjoint(this, other) -> {
            val sharedEndPoint = sharedEndPoint(this, other)
            when {
                sharedEndPoint != null -> SegmentsIntersection(sharedEndPoint)
                /*
                 * Overlapping.
                 */
                else -> SegmentsIntersection.segment()
            }
        }
        else -> SegmentsIntersection.empty()
    }
}

private fun <P : Vector2D<P>> bothContain(s1: Segment2D<P>, s2: Segment2D<P>, point: P) =
    s1.contains(point) && s2.contains(point)

/*
 * Returns false if the segments share one or more points.
 */
private fun <P : Vector2D<P>> disjoint(s1: Segment2D<P>, s2: Segment2D<P>) =
    !(s1.contains(s2.first) || s1.contains(s2.second) || s2.contains(s1.first) || s2.contains(s1.second))

/*
 * Returns the end point shared by the two segments, or null if they share no endpoint OR
 * if they share more than one point (i.e. they overlap).
 */
private fun <P : Vector2D<P>> sharedEndPoint(s1: Segment2D<P>, s2: Segment2D<P>): P? {
    val fuzzyEquals: (P, P) -> Boolean = { first, second ->
        fuzzyEquals(first.x, second.x) && fuzzyEquals(first.y, second.y)
    }
    return when {
        fuzzyEquals(s1.first, s2.first) && !s1.contains(s2.second) -> s1.first
        fuzzyEquals(s1.first, s2.second) && !s1.contains(s2.first) -> s1.first
        fuzzyEquals(s1.second, s2.first) && !s1.contains(s2.second) -> s1.second
        fuzzyEquals(s1.second, s2.second) && !s1.contains(s1.first) -> s1.second
        else -> null
    }
}

/**
 * In euclidean geometry, the intersection between a segment and a circle can be an [EMPTY]
 * set, a [POINT] or a [PAIR] of points.
 */
enum class CircleSegmentIntersectionType {
    EMPTY,
    POINT,
    PAIR
}

/**
 * Describes the result of the intersection between a circle and a segment in an euclidean space.
 *
 * @param type
 *              the type of intersection.
 * @param point1
 *              the first point of intersection (if present).
 * @param point2
 *              the second point of intersection (if present).
 */
data class CircleSegmentIntersection<P : Vector2D<P>>(
    val type: CircleSegmentIntersectionType,
    val point1: P? = null,
    val point2: P? = null
) {

    constructor(point: P) : this(CircleSegmentIntersectionType.POINT, point)

    companion object {
        /**
         * Creates an instance of [CircleSegmentIntersection] whose type is [CircleSegmentIntersectionType.EMPTY].
         */
        fun <P : Vector2D<P>> empty() =
            CircleSegmentIntersection<P>(
                CircleSegmentIntersectionType.EMPTY
            )

        /**
         * Creates an appropriate instance of [CircleSegmentIntersection], taking care that, in case the resulting
         * instance has type [CircleSegmentIntersectionType.POINT], such point is stored in [point1].
         */
        fun <P : Vector2D<P>> create(point1: P?, point2: P?): CircleSegmentIntersection<P> =
            when {
                point1 == null && point2 == null -> empty()
                point1 == null -> CircleSegmentIntersection(
                    point2!!
                ) // Necessarily not null
                point2 == null -> CircleSegmentIntersection(
                    point1
                )
                else -> CircleSegmentIntersection(
                    CircleSegmentIntersectionType.PAIR,
                    point1,
                    point2
                )
            }
    }
}
