package io.izzel.kether.common.api.storage;

import io.izzel.kether.common.api.persistent.KetherSerializer;
import io.izzel.kether.common.api.QuestService;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

final class KetherConstructor extends SafeConstructor {

    private final QuestService<?> service;

    public KetherConstructor(QuestService<?> service) {
        this.service = service;
        this.yamlConstructors.put(Tag.MAP, new ConstructKetherType());
    }

    private class ConstructKetherType extends ConstructYamlMap {

        @Override
        public Object construct(Node node) {
            if (node.isTwoStepsConstruction()) {
                throw new YAMLException("Unexpected recursive mapping structure. Node: " + node);
            }
            Map<?, ?> raw = (Map<?, ?>) super.construct(node);

            if (raw.containsKey("_type")) {
                Map<String, Object> typed = new LinkedHashMap<String, Object>(raw.size());
                for (Map.Entry<?, ?> entry : raw.entrySet()) {
                    typed.put(entry.getKey().toString(), entry.getValue());
                }

                String type = raw.get("_type").toString();
                Optional<KetherSerializer<Object>> optional = service.getRegistry().getPersistentDataSerializer(type);
                if (optional.isPresent()) {
                    return optional.get().deserialize(typed).orElse(null);
                }
            }
            return raw;
        }
    }
}
