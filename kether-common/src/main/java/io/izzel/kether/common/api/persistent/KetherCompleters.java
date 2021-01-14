package io.izzel.kether.common.api.persistent;

import com.google.common.collect.ImmutableList;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class KetherCompleters {

    private KetherCompleters() {
        throw new UnsupportedOperationException();
    }

    public static Function<List<String>, List<String>> list(QuestService<?> service) {
        return seq(
            firstParsing(
                constant("["),
                constant("begin")
            ),
            some(
                action(service)
            ),
            firstParsing(
                constant("]"),
                constant("end")
            )
        );
    }

    public static <E extends Enum<E>> Function<List<String>, List<String>> enumValue(Class<E> cl) {
        return params -> {
            String remove = params.remove(0);
            List<String> list = new ArrayList<>();
            for (E constant : cl.getEnumConstants()) {
                if (constant.name().startsWith(remove)) {
                    list.add(constant.name());
                }
            }
            return list;
        };
    }

    public static Function<List<String>, List<String>> some(Function<List<String>, List<String>> function) {
        return params -> {
            List<String> ret = new ArrayList<>(), last = new LinkedList<>(params);
            try {
                while (!params.isEmpty()) {
                    ret = function.apply(params);
                    last = new LinkedList<>(params);
                }
                return ret;
            } catch (Exception e) {
                params.clear();
                params.addAll(last);
                return ret;
            }
        };
    }

    @SafeVarargs
    public static Function<List<String>, List<String>> seq(Function<List<String>, List<String>>... functions) {
        return params -> {
            for (Function<List<String>, List<String>> function : functions) {
                List<String> ret = function.apply(params);
                if (params.isEmpty()) return ret;
            }
            return ImmutableList.of();
        };
    }

    public static Function<List<String>, List<String>> optional(Function<List<String>, List<String>> function) {
        return params -> {
            ImmutableList<String> copy = ImmutableList.copyOf(params);
            try {
                return function.apply(params);
            } catch (Exception e) {
                params.clear();
                params.addAll(copy);
                return ImmutableList.of();
            }
        };
    }

    @SafeVarargs
    public static Function<List<String>, List<String>> firstParsing(Function<List<String>, List<String>>... functions) {
        return params -> {
            ImmutableList<String> copy = ImmutableList.copyOf(params);
            for (Function<List<String>, List<String>> function : functions) {
                try {
                    return function.apply(params);
                } catch (Exception e) {
                    params.clear();
                    params.addAll(copy);
                }
            }
            throw new IllegalArgumentException();
        };
    }

    public static Function<List<String>, List<String>> constant(String value) {
        return params -> {
            if (!params.isEmpty()) {
                String remove = params.remove(0);
                if (value.startsWith(remove)) return ImmutableList.of(value);
                else throw new IllegalArgumentException();
            }
            return ImmutableList.of();
        };
    }

    public static Function<List<String>, List<String>> consume() {
        return params -> {
            if (!params.isEmpty()) params.remove(0);
            return ImmutableList.of();
        };
    }

    public static Function<List<String>, List<String>> action(QuestService<?> service) {
        return params -> {
            if (params.isEmpty()) {
                return ImmutableList.of();
            } else {
                String remove = params.remove(0);
                if (params.isEmpty()) {
                    return service.getRegistry().getRegisteredActions().stream()
                        .filter(it -> it.startsWith(remove))
                        .collect(Collectors.toList());
                } else {
                    Optional<QuestActionParser> optional = service.getRegistry().getParser(remove);
                    if (optional.isPresent()) {
                        return optional.get().complete(params);
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
            }
        };
    }
}
