package io.izzel.kether.common.api;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class KetherCompleters {

    private KetherCompleters() {
        throw new UnsupportedOperationException();
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
                    return service.getRegisteredActions().stream()
                        .filter(it -> it.startsWith(remove))
                        .collect(Collectors.toList());
                } else {
                    Optional<QuestActionParser> optional = service.getParser(remove);
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
