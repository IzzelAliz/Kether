package io.izzel.kether.common.api;

import java.util.List;
import java.util.function.Function;

public interface QuestActionParser {

    <T> QuestAction<T> resolve(QuestResolver resolver);

    /**
     * @param params a mutable list
     * @return params
     */
    List<String> complete(List<String> params);

    @SuppressWarnings("unchecked")
    static <T> QuestActionParser of(
        Function<QuestResolver, QuestAction<T>> resolveFunction,
        Function<List<String>, List<String>> completeFunction
    ) {
        return new QuestActionParser() {
            @Override
            public <AT> QuestAction<AT> resolve(QuestResolver resolver) {
                return (QuestAction<AT>) resolveFunction.apply(resolver);
            }

            @Override
            public List<String> complete(List<String> params) {
                return completeFunction.apply(params);
            }
        };
    }

}
