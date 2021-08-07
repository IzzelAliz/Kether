package io.izzel.kether.common.loader.types;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.loader.ArgType;
import io.izzel.kether.common.loader.QuestReader;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("UnstableApiUsage")
public class ArgTypes {

    public static final ArgType<Integer> INT = new CoerceType<>(ArgTypes::asInteger, "integer");

    public static final ArgType<Long> LONG = new CoerceType<>(ArgTypes::asLong, "long");

    public static final ArgType<Double> DOUBLE = new CoerceType<>(ArgTypes::asDouble, "double");

    public static final ArgType<Boolean> BOOLEAN = new CoerceType<>(ArgTypes::asBoolean, "boolean");

    public static final ArgType<Duration> DURATION = new DurationType();

    public static final ArgType<ParsedAction<?>> ACTION = QuestReader::nextAction;

    public static <T> ArgType<List<T>> listOf(ArgType<T> argType) {
        return new ListType<>(argType);
    }

    private static final Pattern listPattern = Pattern.compile("^([(\\[{]?)(.+?)([)\\]}]?)$");

    private static final String[] listPairings = {"([{", ")]}"};

    private static boolean toBoolean( Object obj) {
        if (obj == null) {
            return false;
        }
        return (obj instanceof Boolean) ? (Boolean) obj : obj.toString().trim().matches("^(1|true|yes)$");
    }

    private static Optional<Boolean> asBoolean( Object obj) {
        if (obj instanceof Boolean) {
            return Optional.of((Boolean) obj);
        } else if (obj instanceof Byte) {
            return Optional.of((Byte) obj != 0);
        }
        return Optional.of(toBoolean(obj));
    }

    private static Optional<Integer> asInteger(Object obj) {
        if (obj == null) {
            return Optional.empty();
        }
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).intValue());
        }
        try {
            return Optional.of(Integer.valueOf(obj.toString()));
        } catch (NumberFormatException | NullPointerException ignored) {
        }
        String strObj = sanitiseNumber(obj);
        Integer iParsed = Ints.tryParse(strObj);
        if (iParsed == null) {
            Double dParsed = Doubles.tryParse(strObj);
            return dParsed == null ? Optional.empty() : Optional.of(dParsed.intValue());
        }
        return Optional.of(iParsed);
    }

    private static Optional<Long> asLong( Object obj) {
        if (obj == null) {
            return Optional.empty();
        }
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).longValue());
        }
        try {
            return Optional.of(Long.parseLong(sanitiseNumber(obj)));
        } catch (NumberFormatException | NullPointerException ignored) {
        }
        return Optional.empty();
    }

    private static Optional<Double> asDouble( Object obj) {
        if (obj == null) {
            return Optional.empty();
        }
        if (obj instanceof Number) {
            return Optional.of(((Number) obj).doubleValue());
        }
        try {
            return Optional.of(Double.valueOf(obj.toString()));
        } catch (NumberFormatException | NullPointerException ignored) {
        }
        String strObj = sanitiseNumber(obj);
        Double dParsed = Doubles.tryParse(strObj);
        return dParsed == null ? Optional.empty() : Optional.of(dParsed);
    }

    private static boolean listBracketsMatch(Matcher candidate) {
        return candidate.matches() && listPairings[0].indexOf(candidate.group(1)) == listPairings[1].indexOf(candidate.group(3));
    }

    private static String sanitiseNumber(Object obj) {
        String string = obj.toString().trim();
        if (string.length() < 1) {
            return "0";
        }
        Matcher candidate = listPattern.matcher(string);
        if (listBracketsMatch(candidate)) {
            string = candidate.group(2).trim();
        }
        int decimal = string.indexOf('.');
        int comma = string.indexOf(',', decimal);
        if (decimal > -1 && comma > -1) {
            return sanitiseNumber(string.substring(0, comma));
        }
        if (string.indexOf('-', 1) != -1) {
            return "0";
        }
        return string.replace(",", "").split(" ")[0];
    }
}
