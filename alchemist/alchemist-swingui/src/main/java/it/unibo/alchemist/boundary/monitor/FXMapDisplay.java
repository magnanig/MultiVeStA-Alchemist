package it.unibo.alchemist.boundary.monitor;

import it.unibo.alchemist.boundary.interfaces.FX2DOutputMonitor;
import it.unibo.alchemist.boundary.monitor.generic.AbstractFXDisplay;
import it.unibo.alchemist.model.interfaces.Concentration;
import it.unibo.alchemist.model.interfaces.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation of a monitor that graphically represents a simulation on a 2D map.
 *
 * @param <T> The type which describes the {@link Concentration} of a molecule
 */
public class FXMapDisplay<T> extends AbstractFXDisplay<T> implements FX2DOutputMonitor<T> {
    /**
     * Default logger for this class. It hides {@link AbstractFXDisplay}'s one.
     */
    private static final Logger L = LoggerFactory.getLogger(FXMapDisplay.class);
    // TODO
    // TODO
    // TODO
    // TODO

    /**
     * Default constructor. The number of steps is set to default ({@value #DEFAULT_NUMBER_OF_STEPS}).
     */
    public FXMapDisplay() {
        super();
        // TODO
    }

    /**
     * Main constructor. It lets the developer specify the number of steps.
     *
     * @param step the number of steps
     * @see #setStep(int)
     */
    public FXMapDisplay(final int step) {
        super(step);
        // TODO
    }

    @Override
    public void zoomTo(final Position center, final double zoomLevel) {
        assert center.getDimensions() == 2;
        getWormhole().zoomOnPoint(getWormhole().getViewPoint(center), zoomLevel);
    }
}
