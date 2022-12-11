package ru.dargen.fancy.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.dargen.fancy.Fancy;

import java.util.concurrent.atomic.LongAdder;

@AllArgsConstructor
@NoArgsConstructor
public class MetricsImpl implements Metrics {

    @Getter
    private Metrics parent;

    @Getter
    private final long startTime = System.currentTimeMillis();

    private final LongAdder
            outPackets = new LongAdder(),
            inPackets = new LongAdder(),
            outBytes = new LongAdder(),
            inBytes = new LongAdder();

    public long getOutBytes() {
        return outBytes.longValue();
    }

    public long getInBytes() {
        return inBytes.longValue();
    }

    public long getIOBytes() {
        return getOutBytes() + getInBytes();
    }

    public long getOutPackets() {
        return outPackets.longValue();
    }

    public long getInPackets() {
        return inPackets.longValue();
    }

    public long getIOPackets() {
        return getInPackets() + getOutPackets();
    }

    public long getOutAveragePPS() {
        return getOutPackets() / (getRunningTime() / 1000);
    }

    public long getInAveragePPS() {
        return getInPackets() / (getRunningTime() / 1000);
    }

    public long getIOAveragePPS() {
        return getIOPackets() / (getRunningTime() / 1000);
    }

    public long getOutAverageBytesTraffic() {
        return getOutBytes() / (getRunningTime() / 1000);
    }

    public long getInAverageBytesTraffic() {
        return getInBytes() / (getRunningTime() / 1000);
    }

    public long getIOAverageBytesTraffic() {
        return getIOBytes() / (getRunningTime() / 1000);
    }

    public double getOutAverageTraffic(SizeUnit unit) {
        return getOutAverageBytesTraffic() / (double) unit.getSize();
    }

    public double getInAverageTraffic(SizeUnit unit) {
        return getInAverageBytesTraffic() / (double) unit.getSize();
    }

    public double getIOAverageTraffic(SizeUnit unit) {
        return getIOAverageBytesTraffic() / (double) unit.getSize();
    }

    public long getRunningTime() {
        return System.currentTimeMillis() - getStartTime();
    }

    public void incrementOutPackets() {
        outPackets.increment();
        if (parent != null) parent.incrementOutPackets();
    }

    public void incrementInPackets() {
        inPackets.increment();
        if (parent != null) parent.incrementInPackets();
    }

    public void incrementOutPackets(long bytes) {
        incrementOutPackets();
        incrementOutBytes(bytes);
    }

    public void incrementInPackets(long bytes) {
        incrementInPackets();
        incrementInBytes(bytes);
    }

    public void incrementOutBytes(long bytes) {
        outBytes.add(bytes);
        if (parent != null) parent.incrementOutBytes(bytes);
    }

    public void incrementInBytes(long bytes) {
        inBytes.add(bytes);
        if (parent != null) parent.incrementInBytes(bytes);
    }

    public Metrics fork() {
        return Fancy.createMetrics(this);
    }

}
