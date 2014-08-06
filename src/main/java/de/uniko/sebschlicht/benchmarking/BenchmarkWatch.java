package de.uniko.sebschlicht.benchmarking;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class BenchmarkWatch {

    protected static Logger LOG = Logger.getLogger(BenchmarkWatch.class);

    protected static DateFormat DATE_FORMATTER = new SimpleDateFormat(
            "YYYY-MM-dd HH:mm:ss");

    // TODO log progress at checkpoints
    // TODO stop watch when duration reached

    protected StopWatch watch;

    protected Benchmarkable benchmark;

    protected Thread benchmarkThread;

    protected long numCheckpoints;

    protected long duration;

    protected boolean running;

    public BenchmarkWatch(
            Benchmarkable benchmark) {
        watch = new StopWatch();
        this.benchmark = benchmark;
        numCheckpoints = 1;
    }

    public void setNumCheckpoints(int numCheckpoints) {
        this.numCheckpoints = numCheckpoints;
    }

    public void setDuration(long maxDuration, TimeUnit timeUnit) {
        duration = TimeUnit.MILLISECONDS.convert(maxDuration, timeUnit);
    }

    protected void startBenchmark() {
        benchmarkThread = new Thread(benchmark);
        benchmark.init();
        benchmarkThread.start();
    }

    public void measure() {
        running = true;

        startBenchmark();
        watch.start();

        //TODO find better solution: makes benchmark inaccurate (+/- 5ms)
        while (!benchmark.isRunning()) {
            // wait for benchmark to be ready
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long rate = duration / numCheckpoints;
        long timeNextCheckpoint = System.currentTimeMillis();

        measurement:
        for (int crrCP = 0; crrCP < numCheckpoints; crrCP++) {
            timeNextCheckpoint += rate;
            while (System.currentTimeMillis() < timeNextCheckpoint) {
                // TODO wait using small sleep value to be accurate?
                if (!benchmark.isRunning()) {
                    LOG.info("benchmark finished in time");

                    break measurement;
                }
            }

            // print duration and progress
            LOG.info("progress after "
                    + watch.getDuration(TimeUnit.MILLISECONDS) + "ms: "
                    + benchmark.getProgress());
        }
        watch.stop();

        // print total progress
        LOG.info("total progress in run: " + benchmark.getProgress());

        stop();
    }

    public void stop() {
        if (running) {
            running = false;

            // TODO stop watch if running

            benchmark.stop();
            try {
                benchmarkThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
