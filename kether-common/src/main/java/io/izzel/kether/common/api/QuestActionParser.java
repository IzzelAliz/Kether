package io.izzel.kether.common.api;

import io.izzel.kether.common.loader.QuestReader;

import java.util.List;
import java.util.function.Function;

public interface QuestActionParser {

    <T> QuestAction<T> resolve(QuestReader resolver);

    /**
     * @param params a mutable list
     * @return params
     */
    List<String> complete(List<String> params);

    @SuppressWarnings("unchecked")
    static <T> QuestActionParser of(
        Function<QuestReader, QuestAction<T>> resolveFunction,
        Function<List<String>, List<String>> completeFunction
    ) {
        return new QuestActionParser() {
            @Override
            public <AT> QuestAction<AT> resolve(QuestReader resolver) {
                return (QuestAction<AT>) resolveFunction.apply(resolver);
            }

            @Override
            public List<String> complete(List<String> params) {
                return completeFunction.apply(params);
            }
        };
    }

}
