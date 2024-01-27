package ru.dargen.fancy.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SizeUnit {

    BYTES("b"),
    KILOBYTES("kb"),
    MEGABYTES("mb"),
    GIGABYTES("gb"),
    TERABYTES("tb");

    private final String name;
    private final long size = (long) Math.pow(1024, ordinal());

}
