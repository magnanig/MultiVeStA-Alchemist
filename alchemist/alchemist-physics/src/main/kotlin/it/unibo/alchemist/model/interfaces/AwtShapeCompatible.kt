package it.unibo.alchemist.model.interfaces

import java.awt.Shape

/**
 * Anything which can be represented as a {@link java.awt.Shape}.
 */
interface AwtShapeCompatible {

    /**
     * @return a copy of itself in form of a {@link java.awt.Shape}.
     */
    fun asAwtShape(): Shape
}