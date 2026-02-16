package ru.healthfamily.biobank.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Оборачивает DataSource для выполнения schema-enums.sql при первом подключении.
 * Enum-типы должны существовать до того, как Hibernate создаёт таблицы patients и samples.
 * Создаём DataSource сами (без инъекции), чтобы избежать циклической зависимости.
 */
@Configuration
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        HikariDataSource real = properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        return new SchemaInitDataSource(real);
    }
}
