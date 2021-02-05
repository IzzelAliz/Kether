package io.izzel.kether.common.persistent.storage;

import io.izzel.kether.common.persistent.PersistentQuestService;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MariaDbStorage extends AbstractSqlStorage {

    public MariaDbStorage(PersistentQuestService<?> service, Supplier<DataSource> dataSourceSupplier) {
        super(service, dataSourceSupplier);
    }

    @Override
    protected Map.Entry<String, Consumer<PreparedStatement>> fetchContextsStatement(String playerIdentifier) {
        return null;
    }

    @Override
    protected List<Map.Entry<String, Consumer<PreparedStatement>>> updateContextStatement(String playerIdentifier, String questId, String str) {
        return null;
    }
}
