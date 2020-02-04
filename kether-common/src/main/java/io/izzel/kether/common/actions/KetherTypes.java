package io.izzel.kether.common.actions;

import com.google.common.collect.ImmutableList;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestRegistry;
import io.izzel.kether.common.api.QuestService;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KetherTypes {

    public static <C extends QuestContext> void registerInternals(QuestRegistry registry, QuestService<C> service) {
        registry.registerAction("async", AsyncAction.parser(service));
        registry.registerAction("await", AwaitAction.parser(service));
    }

    static Function<List<String>, List<String>> completeAction(QuestService<?> service) {
        return params -> {
            if (params.size() == 0) return ImmutableList.of();
            else if (params.size() == 1) {
                return service.getRegisteredActions().stream()
                    .filter(it -> it.startsWith(params.get(0)))
                    .collect(Collectors.toList());
            } else {
                Optional<QuestActionParser> optional = service.getParserById(params.get(0));
                if (optional.isPresent()) {
                    return optional.get().complete(params.subList(1, params.size()));
                } else {
                    return ImmutableList.of();
                }
            }
        };
    }

}
