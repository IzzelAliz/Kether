package io.izzel.kether.common.api.storage;

import io.izzel.kether.common.api.persistent.KetherSerializer;
import io.izzel.kether.common.api.QuestService;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;

import java.util.LinkedHashMap;
import java.util.Map;

final class KetherRepresenter extends Representer {

    public KetherRepresenter(QuestService<?> service) {
        for (Map.Entry<String, KetherSerializer<?>> entry : service.getRegistry().getIdSerializers().entrySet()) {
            Class<?> cl = service.getRegistry().getSerializedClass(entry.getKey()).get();
            RepresentKetherType represent = new RepresentKetherType(entry.getKey(), entry.getValue());
            this.multiRepresenters.put(cl, represent);
        }
    }

    private class RepresentKetherType extends RepresentMap {

        private final String id;
        private final KetherSerializer<?> serializer;

        private RepresentKetherType(String id, KetherSerializer<?> serializer) {
            this.id = id;
            this.serializer = serializer;
        }

        @Override
        public Node representData(Object data) {
            Map<String, Object> serialize = ((KetherSerializer) serializer).serialize(data);
            Map<String, Object> values = new LinkedHashMap<>(serialize.size() + 1);
            values.putAll(serialize);
            values.put("_type", id);
            return super.representData(values);
        }
    }
}
