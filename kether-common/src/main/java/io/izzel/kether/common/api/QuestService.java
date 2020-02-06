package io.izzel.kether.common.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

public interface QuestService<CTX extends QuestContext> {

    Collection<String> getRegisteredActions();

    Optional<QuestActionParser> getParserById(String id);

    QuestRegistry getRegistry();

    void startQuest(CTX context);

    void terminateQuest(CTX context);

    Map<String, CTX> getRunningQuests();

    List<CTX> getRunningQuests(String playerIdentifier);

    Executor getExecutor();

    String getLocalizedText(String node, Object... params);

    QuestStorage getStorage();

}
