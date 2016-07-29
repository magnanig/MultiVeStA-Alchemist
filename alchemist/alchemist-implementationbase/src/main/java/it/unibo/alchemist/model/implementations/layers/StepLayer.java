package it.unibo.alchemist.model.implementations.layers;

import it.unibo.alchemist.model.interfaces.Layer;
import it.unibo.alchemist.model.interfaces.Position;

/**
 * Implements a {@link Layer} with a discontinue spatial distribution: the plane is divided 
 * in two parts, both with a constant concentration but with a different in value.
 * @param <T> the type describing the concentration in this {@link Layer}.
 *
 */
public class StepLayer<T> implements Layer<T> {

    private final double maxx;
    private final double maxy;
    private final T highValue;
    private final T lowValue;

    /**
     * Initialize a {@link StepLayer}.
     * @param mx the x value above which the concentration in layer is at {@link maxValue} 
     * @param my the y value above which the concentration in layer is at {@link maxValue}
     * @param minValue 
     * @param maxValue
     */
    public StepLayer(final double mx, final double my, final T maxValue, final T minValue) {
        maxx = mx;
        maxy = my;
        highValue = maxValue;
        lowValue = minValue;
    }

    /**
     * Initialize a {@link StepLayer} where concentration is at {@link maxValue}
     * in first quadrant (for positive values of x and y).
     * @param maxValue
     * @param minValue
     */
    public StepLayer(final T maxValue, final T minValue) {
        this(0, 0, maxValue, minValue);
    }

    @Override
    public T getValue(final Position p) {
        if (p.getCoordinate(0) > maxx && p.getCoordinate(1) > maxy) {
            return highValue;
        } else {
            return lowValue;
        }
    }

}
