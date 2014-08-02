package de.uniko.sebschlicht.benchmarking;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class StopWatch {

    protected static Logger LOG = Logger.getLogger(StopWatch.class);

    protected static DateFormat DATE_FORMATTER = new SimpleDateFormat(
            "YYYY-MM-dd HH:mm:ss");

    protected long start;

    protected long finish;

    /**
     * duration in nano seconds
     */
    protected long duration;

    /**
     * durations of all runs
     */
    protected List<Long> durations;

    public StopWatch() {
        this.durations = new LinkedList<Long>();
    }

    protected void printStartTime(Date startDate) {
        LOG.info("started at " + DATE_FORMATTER.format(startDate));
    }

    public void start() {
        this.finish = 0;

        Date startDate = new Date(System.currentTimeMillis());
        this.start = System.nanoTime();
        this.printStartTime(startDate);
    }

    /**
     * @return VM timestamp of start (nanoseconds)
     */
    public long getStartTime() {
        return this.start;
    }

    public long getDuration() {
        if (this.finish == 0) {
            return System.nanoTime() - this.start;
        } else {
            return this.duration;
        }
    }

    public long getDuration(TimeUnit timeUnit) {
        return timeUnit.convert(this.getDuration(), TimeUnit.NANOSECONDS);
    }

    protected void printStopTime(Date finishDate) {
        LOG.info("finished at " + DATE_FORMATTER.format(finishDate)
                + ", total duration: "
                + this.getDuration(TimeUnit.MILLISECONDS) + "ms");
    }

    public void stop() {
        this.finish = System.nanoTime();

        Date finishDate = new Date(System.currentTimeMillis());

        this.duration = this.finish - this.start;
        this.durations.add(this.duration);

        this.printStopTime(finishDate);
    }

    /**
     * @return VM timestamp of finish (nanoseconds)
     */
    public long getFinishTime() {
        return this.finish;
    }

    public long getAvgDuration(TimeUnit timeUnit) {
        if (this.durations.size() < 2) {
            return this.getDuration(timeUnit);
        }

        long totalDuration = 0;
        for (long duration : this.durations) {
            totalDuration += duration;
        }
        return timeUnit.convert(totalDuration / this.durations.size(),
                TimeUnit.NANOSECONDS);
    }
}
