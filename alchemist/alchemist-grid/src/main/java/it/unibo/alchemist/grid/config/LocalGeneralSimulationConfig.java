package it.unibo.alchemist.grid.config;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * Local {@link GeneralSimulationConfig} that contains all informations in local memory.
 *
 * @param <T> the concentration type
 */
public class LocalGeneralSimulationConfig<T> extends LightInfoGeneralSimulationConfig<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 3974035069237901864L;
    private final Map<String, String> dependencies;

    /**
     * 
     * @param loader Simulation's loader
     * @param endStep Simulation's end step
     * @param endTime Simulation's end time
     */
    public LocalGeneralSimulationConfig(final Loader loader, final long endStep, final Time endTime) {
        super(loader, endStep, endTime);
        //TODO assicurati file in classpath, chiedi conferme
        this.dependencies = new HashMap<>();
        for (final String file : loader.getDependencies()) {
            try {
                final URL dependency = LocalGeneralSimulationConfig.class.getResource(file);
                if (dependency != null) {
                    dependencies.put(file, FileUtils.readFileToString(new File(dependency.toURI()), StandardCharsets.UTF_8));
                } else {
                    throw new IllegalArgumentException("Dependency non exixts: " + file);
                }
            } catch (IOException e) {
                //TODO pre controllo nel loader per l'esistenza???
                throw new IllegalArgumentException("Dependency non exixts: " + file);
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Failed to get resource URI: " + file);
            }
        }
    }

    @Override
    public Map<String, String> getDependencies() {
        return this.dependencies;
    }

}
