package de.uniko.sebschlicht.benchmarking;

/**
 * benchmarkable unit a watch can take measurements for
 * 
 * @author sebschlicht
 * 
 */
public interface Benchmarkable extends Runnable {

    /**
     * Initialize the benchmark.<br>
     * Execution time should be excluded from measurement.
     */
    void init();

    /**
     * @return true - if the benchmark has not been finished yet<br>
     *         false - otherwise
     */
    boolean isRunning();

    /**
     * @return current benchmark progress
     */
    long getProgress();

    /**
     * Stops the benchmark.
     */
    void stop();
}
