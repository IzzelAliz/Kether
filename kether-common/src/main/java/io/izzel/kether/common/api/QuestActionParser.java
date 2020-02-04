package io.izzel.kether.common.api;

import java.util.List;
import java.util.function.Function;

public interface QuestActionParser {

    <T, C extends QuestContext> QuestAction<T, C> resolve(QuestResolver<C> resolver);

    List<String> complete(List<String> params);

    @SuppressWarnings("unchecked")
    static <T, C extends QuestContext> QuestActionParser of(
        Function<QuestResolver<C>, QuestAction<T,C>> resolveFunction,
        Function<List<String>, List<String>> completeFunction
    ) {
        return new QuestActionParser() {
            @Override
            public <AT, AC extends QuestContext> QuestAction<AT, AC> resolve(QuestResolver<AC> resolver) {
                return (QuestAction<AT, AC>) resolveFunction.apply((QuestResolver<C>) resolver);
            }

            @Override
            public List<String> complete(List<String> params) {
                return completeFunction.apply(params);
            }
        };
    }

}
