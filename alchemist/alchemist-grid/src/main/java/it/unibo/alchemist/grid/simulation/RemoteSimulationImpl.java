package it.unibo.alchemist.grid.simulation;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.ignite.Ignition;

import it.unibo.alchemist.core.implementations.Engine;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.grid.config.GeneralSimulationConfig;
import it.unibo.alchemist.grid.config.SimulationConfig;
import it.unibo.alchemist.grid.util.WorkingDirectory;
import it.unibo.alchemist.loader.Loader;
import it.unibo.alchemist.loader.export.Exporter;
import it.unibo.alchemist.model.interfaces.Environment;

/**
 * {@link RemoteSimulation} implementation for Apache Ignite.
 *
 * @param <T>
 */
public class RemoteSimulationImpl<T> implements RemoteSimulation<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 8206545835842309336L;
    private final GeneralSimulationConfig<T> generalConfig;
    private final SimulationConfig config;
    private final UUID masterNodeId;
    /**
     * 
     * @param generalConfig General simulation config
     * @param config Simulation's specific configs
     * @param masterNodeId The node that started the computation
     */
    public RemoteSimulationImpl(final GeneralSimulationConfig<T> generalConfig, final SimulationConfig config, final UUID masterNodeId) {
        this.generalConfig = generalConfig;
        this.config = config;
        this.masterNodeId = masterNodeId;
    }



    @Override
    public RemoteResult call() {
        try (WorkingDirectory wd = new WorkingDirectory()) {
            wd.addToClasspath();
            ClassLoader cl = ClassLoader.getSystemClassLoader();

            URL[] urls = ((URLClassLoader)cl).getURLs();

            for(URL url: urls){
                System.out.println(url.getFile());
            }
            wd.writeFiles(this.generalConfig.getDependencies());
            final Loader loader = this.generalConfig.getLoader();
            final Environment<T> env = loader.getWith(this.config.getVariables());
            final Simulation<T> sim = new Engine<>(env, this.generalConfig.getEndStep(), this.generalConfig.getEndTime());
            final Map<String, Object> defaultVars = loader.getVariables().entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getDefault()));
            defaultVars.putAll(this.config.getVariables());
            final String header = this.config.getVariables().entrySet().stream()
                    .map(e -> e.getKey() + " = " + e.getValue())
                    .collect(Collectors.joining(", "));
            final String filename = this.masterNodeId.toString() + "_" + this.config.getVariables().entrySet().stream()
                    .map(e -> e.getKey() + '-' + e.getValue())
                    .collect(Collectors.joining("_")) + ".txt";
            final Exporter<T> exp = new Exporter<>(wd.getFileAbsolutePath(filename), 1, header, loader.getDataExtractors());
            sim.addOutputMonitor(exp);
            sim.play();
            sim.run();
            return new RemoteResultImpl(wd.getFileContent(filename), Ignition.ignite().cluster().localNode().id(), sim.getError(), config);
        } catch (SecurityException | IllegalArgumentException | ReflectiveOperationException | IOException e1) {
            throw new IllegalStateException(e1);
        }
    }
}
