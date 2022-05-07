package ru.dargen.fancy.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SizeUnit {

    BYTES("b", 1),
    KILOBYTES("kb", 1024),
    MEGABYTES("mb", 1024 * 1024),
    GIGABYTES("gb", 1024 * 1024 * 1024),
    TERABYTES("tb", 1024L * 1024 * 1024 * 1024);

    private final String name;
    private final long size;

}
