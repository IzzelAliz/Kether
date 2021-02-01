package io.izzel.kether.common.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public interface QuestRegistry {

    void registerAction(String namespace, String id, QuestActionParser parser);

    void registerAction(String id, QuestActionParser parser);

    void registerStringProcessor(String id, BiFunction<QuestContext.Frame, String, String> processor);

    Collection<String> getRegisteredActions(String namespace);

    Collection<String> getRegisteredActions();

    Optional<QuestActionParser> getParser(String id, List<String> namespace);

    Optional<QuestActionParser> getParser(String id);

    Optional<BiFunction<QuestContext.Frame, String, String>> getStringProcessor(String id);
}
