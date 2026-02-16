package ru.healthfamily.biobank.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Обёртка DataSource, выполняющая schema-enums.sql при первом подключении.
 * Гарантирует, что enum-типы PostgreSQL созданы до того, как Hibernate начнёт schema export.
 */
public class SchemaInitDataSource extends org.springframework.jdbc.datasource.DelegatingDataSource {

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final DataSource target;

    public SchemaInitDataSource(DataSource targetDataSource) {
        super(targetDataSource);
        this.target = targetDataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        ensureSchemaEnums();
        return super.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        ensureSchemaEnums();
        return super.getConnection(username, password);
    }

    private void ensureSchemaEnums() {
        if (initialized.compareAndSet(false, true)) {
            try {
                var populator = new ResourceDatabasePopulator();
                populator.setScripts(new ClassPathResource("schema-enums.sql"));
                populator.setSeparator("//");
                populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
                populator.execute(target);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to run schema-enums.sql", e);
            }
        }
    }
}
