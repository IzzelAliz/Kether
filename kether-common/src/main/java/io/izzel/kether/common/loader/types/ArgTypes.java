package io.izzel.kether.common.loader.types;

import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.loader.ArgType;
import io.izzel.kether.common.loader.QuestReader;
import io.izzel.kether.common.util.Coerce;

import java.time.Duration;
import java.util.List;

public class ArgTypes {

    public static final ArgType<Integer> INT = new NumberType<>(Coerce::asInteger, "integer");

    public static final ArgType<Long> LONG = new NumberType<>(Coerce::asLong, "long");

    public static final ArgType<Double> DOUBLE = new NumberType<>(Coerce::asDouble, "double");

    public static final ArgType<Duration> DURATION = new DurationType();

    public static final ArgType<ParsedAction<?>> ACTION = QuestReader::nextAction;

    public static <T> ArgType<List<T>> listOf(ArgType<T> argType) {
        return new ListType<>(argType);
    }
}
