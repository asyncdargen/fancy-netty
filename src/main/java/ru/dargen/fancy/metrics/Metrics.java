package ru.dargen.fancy.metrics;

public interface Metrics {

    long getOutBytes();

    long getInBytes();

    long getIOBytes();

    long getOutPackets();

    long getInPackets();

    long getIOPackets();

    long getOutAveragePPS();

    long getInAveragePPS();

    long getIOAveragePPS();

    long getOutAverageBytesTraffic();

    long getInAverageBytesTraffic();

    long getIOAverageBytesTraffic();

    double getOutAverageTraffic(SizeUnit unit);

    double getInAverageTraffic(SizeUnit unit);

    double getIOAverageTraffic(SizeUnit unit);

    long getStartTime();

    long getRunningTime();

    void incrementOutPackets();

    void incrementInPackets();

    void incrementOutPackets(long bytes);

    void incrementInPackets(long bytes);

    void incrementOutBytes(long bytes);

    void incrementInBytes(long bytes);

    Metrics getParent();

    Metrics fork();

}
