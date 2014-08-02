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
        this.watch = new StopWatch();
        this.benchmark = benchmark;
        this.numCheckpoints = 1;
    }

    public void setNumCheckpoints(int numCheckpoints) {
        this.numCheckpoints = numCheckpoints;
    }

    public void setDuration(long maxDuration, TimeUnit timeUnit) {
        this.duration = TimeUnit.MILLISECONDS.convert(maxDuration, timeUnit);
    }

    protected void startBenchmark() {
        this.benchmarkThread = new Thread(this.benchmark);
        this.benchmark.init();
        this.benchmarkThread.start();
    }

    public void measure() {
        this.running = true;

        this.startBenchmark();
        this.watch.start();

        long rate = this.duration / this.numCheckpoints;
        long timeNextCheckpoint = System.currentTimeMillis();

        measurement:
        for (int crrCP = 0; crrCP < this.numCheckpoints; crrCP++) {
            timeNextCheckpoint += rate;
            while (System.currentTimeMillis() < timeNextCheckpoint) {
                // TODO wait using small sleep value to be accurate?
                if (!this.benchmark.isRunning()) {
                    LOG.info("benchmark finished in time");

                    break measurement;
                }
            }

            // print duration and progress
            LOG.info("progress after "
                    + this.watch.getDuration(TimeUnit.MILLISECONDS) + "ms: "
                    + this.benchmark.getProgress());
        }
        this.watch.stop();

        // print total progress
        LOG.info("total progress in run: " + this.benchmark.getProgress());

        this.stop();
    }

    public void stop() {
        if (this.running) {
            this.running = false;

            // TODO stop watch if running

            this.benchmark.stop();
            try {
                this.benchmarkThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
