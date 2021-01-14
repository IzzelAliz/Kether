package io.izzel.kether.common.loader;

import io.izzel.kether.common.api.Quest;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;

import java.util.logging.Logger;

public interface QuestLoader {

    <C extends QuestContext> Quest load(QuestService<C> service, Logger logger, String id, byte[] bytes);
}
