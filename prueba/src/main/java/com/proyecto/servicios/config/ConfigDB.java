package com.proyecto.servicios.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "com.proyecto.servicios.repositorys.sf",
                "com.proyecto.servicios.repositorys.gestopago"
        },
        transactionManagerRef = "sfTransactionManager",
        entityManagerFactoryRef = "sfEntityManagerFactory"
)
public class ConfigDB {

    private static final String PERSISTENCE_UNIT = "sfDatasource";
    private static final int MAXIMUM_POOL_SIZE = 10;
    private static final int MINIMUM_IDLE = 2;
    private static final long MAX_LIFETIME_MS = 1_800_000L;
    private static final long CONNECTION_TIMEOUT_MS = 5_000L;
    private static final long VALIDATION_TIMEOUT_MS = 5_000L;
    private static final int QUERY_TIMEOUT_MS = 600_000;

    private final Environment env;

    public ConfigDB(Environment env) {
        this.env = env;
    }

    @Bean(name = "sfDatasource")
    public DataSource sfDatasource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(env.getRequiredProperty("spring.datasource.url"));
        config.setUsername(env.getRequiredProperty("spring.datasource.username"));
        config.setPassword(env.getRequiredProperty("spring.datasource.password"));
        config.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
        config.setMinimumIdle(MINIMUM_IDLE);
        config.setMaxLifetime(MAX_LIFETIME_MS);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setValidationTimeout(VALIDATION_TIMEOUT_MS);
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName(PERSISTENCE_UNIT);
        return new HikariDataSource(config);
    }

    @Bean(name = "sfEntityManagerFactory")
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean sfEntityManagerFactory(
            @Qualifier("sfDatasource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(
                "com.proyecto.servicios.entity.sf",
                "com.proyecto.servicios.entity.gestopago"
        );
        em.setPersistenceUnitName(PERSISTENCE_UNIT);
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setJpaPropertyMap(jpaProperties());
        return em;
    }

    @Bean(name = "sfTransactionManager")
    public PlatformTransactionManager sfTransactionManager(
            @Qualifier("sfEntityManagerFactory") EntityManagerFactory sfEntityManagerFactory) {
        return new JpaTransactionManager(sfEntityManagerFactory);
    }

    private Map<String, Object> jpaProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.show_sql", false);
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("jakarta.persistence.query.timeout", QUERY_TIMEOUT_MS);
        return properties;
    }
}
