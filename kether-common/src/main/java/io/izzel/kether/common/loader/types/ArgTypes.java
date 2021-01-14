package io.izzel.kether.common.loader.types;

import io.izzel.kether.common.loader.ArgType;
import io.izzel.kether.common.util.Coerce;

import java.time.Duration;

public class ArgTypes {

    public static final ArgType<Integer> INT = new NumberType<>(Coerce::asInteger, "integer");

    public static final ArgType<Long> LONG = new NumberType<>(Coerce::asLong, "long");

    public static final ArgType<Double> DOUBLE = new NumberType<>(Coerce::asDouble, "double");

    public static final ArgType<Duration> DURATION = new DurationType();
}
