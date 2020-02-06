package io.izzel.kether.common.api;

public class ExitStatus {

    private final boolean running;
    private final boolean waiting;
    private final long startTime;

    public ExitStatus(boolean running, boolean waiting, long startTime) {
        this.running = running;
        this.waiting = waiting;
        this.startTime = startTime;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public long getStartTime() {
        return startTime;
    }

    public static ExitStatus success() {
        return new ExitStatus(false, false, 0);
    }

    public static ExitStatus paused() {
        return new ExitStatus(true, false, 0);
    }

    public static ExitStatus cooldown(long timeout) {
        return new ExitStatus(true, true, System.currentTimeMillis() + timeout);
    }

}
