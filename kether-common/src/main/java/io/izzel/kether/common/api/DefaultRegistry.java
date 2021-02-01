package io.izzel.kether.common.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class DefaultRegistry implements QuestRegistry {

    private final Map<String, Map<String, QuestActionParser>> parsers = new HashMap<>();
    private final Map<String, BiFunction<QuestContext.Frame, String, String>> processors = new HashMap<>();

    @Override
    public void registerAction(String namespace, String id, QuestActionParser parser) {
        parsers.computeIfAbsent(namespace, i -> new HashMap<>()).put(id, parser);
    }

    @Override
    public void registerAction(String id, QuestActionParser parser) {
        registerAction("kether", id, parser);
    }

    @Override
    public void registerStringProcessor(String id, BiFunction<QuestContext.Frame, String, String> processor) {
        processors.put(id, processor);
    }

    @Override
    public Collection<String> getRegisteredActions(String namespace) {
        Map<String, QuestActionParser> map = parsers.get(namespace);
        return map == null ? Collections.emptyList() : Collections.unmodifiableCollection(map.keySet());
    }

    @Override
    public Collection<String> getRegisteredActions() {
        return getRegisteredActions("kether");
    }

    private Optional<QuestActionParser> getParser(String namespace, String id) {
        Map<String, QuestActionParser> map = parsers.get(namespace);
        return map == null ? Optional.empty() : Optional.ofNullable(map.get(id));
    }

    @Override
    public Optional<QuestActionParser> getParser(String id, List<String> namespace) {
        String[] domain = id.split(":");
        if (domain.length == 2) {
            return getParser(domain[0], domain[1]);
        } else {
            for (String name : namespace) {
                Optional<QuestActionParser> optional = getParser(name, id);
                if (optional.isPresent()) {
                    return optional;
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<QuestActionParser> getParser(String id) {
        return getParser("kether", id);
    }

    @Override
    public Optional<BiFunction<QuestContext.Frame, String, String>> getStringProcessor(String id) {
        return Optional.ofNullable(processors.get(id));
    }
}
