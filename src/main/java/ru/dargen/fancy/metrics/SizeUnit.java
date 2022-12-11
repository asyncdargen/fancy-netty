package ru.dargen.fancy.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SizeUnit {

    BYTES("b", 0x1L),
    KILOBYTES("kb", 0x400L),
    MEGABYTES("mb", 0x100000L),
    GIGABYTES("gb", 0x40000000L),
    TERABYTES("tb", 0x10000000000L);

    private final String name;
    private final long size;

}
