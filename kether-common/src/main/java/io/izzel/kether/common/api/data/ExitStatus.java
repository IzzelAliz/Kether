package io.izzel.kether.common.api.data;

import io.izzel.kether.common.api.persistent.KetherSerializer;
import io.izzel.kether.common.util.Coerce;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ExitStatus {

    private static final ExitStatus PAUSED = new ExitStatus(true, false, 0);

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExitStatus that = (ExitStatus) o;
        return isRunning() == that.isRunning() &&
            isWaiting() == that.isWaiting() &&
            getStartTime() == that.getStartTime();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isRunning(), isWaiting(), getStartTime());
    }

    @Override
    public String toString() {
        return "ExitStatus{" +
            "running=" + running +
            ", waiting=" + waiting +
            ", startTime=" + startTime +
            '}';
    }

    public static ExitStatus success() {
        return new ExitStatus(false, false, 0);
    }

    public static ExitStatus paused() {
        return PAUSED;
    }

    public static ExitStatus cooldown(long timeout) {
        return new ExitStatus(true, true, System.currentTimeMillis() + timeout);
    }

    public static class Serializer implements KetherSerializer<ExitStatus> {

        @Override
        public Map<String, Object> serialize(ExitStatus instance) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("running", instance.running);
            map.put("waiting", instance.waiting);
            map.put("startTime", instance.startTime);
            return map;
        }

        @Override
        public Optional<ExitStatus> deserialize(Map<String, Object> map) {
            boolean running = Coerce.toBoolean(map.get("running"));
            boolean waiting = Coerce.toBoolean(map.get("waiting"));
            long startTime = Coerce.toLong(map.get("startTime"));
            return Optional.of(new ExitStatus(running, waiting, startTime));
        }
    }
}
