package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.ExitStatus;
import io.izzel.kether.common.api.KetherSerializer;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestRegistry;
import io.izzel.kether.common.api.QuestService;

public class KetherTypes {

    public static <C extends QuestContext> void registerInternals(QuestRegistry registry, QuestService<C> service) {
        registry.registerAction("await", AwaitAction.parser(service));
        registry.registerAction("await_all", AwaitAllAction.parser(service));
        registry.registerAction("await_any", AwaitAnyAction.parser(service));
        registry.registerAction("call", CallAction.parser());
        registry.registerAction("data", DataAction.parser());
        registry.registerAction("if", IfAction.parser(service));
        registry.registerAction("goto", GotoAction.parser());
        registry.registerAction("while", WhileAction.parser(service));
        registry.registerAction("repeat", RepeatAction.parser(service));
        registry.registerAction("exit", ExitAction.parser());
        registry.registerAction("require", RequireAction.parser(service));
        registry.registerAction("all", AllAction.parser(service));
        registry.registerAction("any", AnyAction.parser(service));
        registry.registerAction("not", NotAction.parser(service));

        registry.registerPersistentDataType("exit_status", ExitStatus.class, KetherSerializer.gson(ExitStatus.class));
    }

}
