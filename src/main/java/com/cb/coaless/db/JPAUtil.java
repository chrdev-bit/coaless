package com.cb.coaless.db;

import com.cb.coaless.model.Task;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;

import java.util.HashMap;
import java.util.Map;


public class JPAUtil {

    private static SessionFactory sessionFactory;

    public static synchronized void setUpSession() {
        if (sessionFactory == null) {

            try {

                HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"); // in-memory H2
                //hikariConfig.setJdbcUrl("jdbc:h2:file:./data/testdb;AUTO_SERVER=TRUE");
                hikariConfig.setUsername("sa");
                hikariConfig.setPassword("");
                hikariConfig.setMaximumPoolSize(10);
                hikariConfig.setPoolName("H2HikariPool");

                HikariDataSource dataSource = new HikariDataSource(hikariConfig);

                Map<String, Object> props = new HashMap<>();
                props.put(Environment.DATASOURCE, dataSource);
                props.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");
                props.put(Environment.HBM2DDL_AUTO, "update");   //create/update tables automatically
                props.put(Environment.SHOW_SQL, true);
                props.put(Environment.FORMAT_SQL, true);
                props.put("hibernate.archive.autodetection", "class"); //auto-detect @Entity classes

                StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                        .applySetting(Environment.DATASOURCE, dataSource)
                        .applySetting(Environment.DIALECT, "org.hibernate.dialect.H2Dialect")
                        .applySetting(Environment.HBM2DDL_AUTO, "update")
                        .applySetting(Environment.SHOW_SQL, true)
                        .build();

                sessionFactory = new MetadataSources(registry)
                        .addAnnotatedClass(Task.class)
                        .buildMetadata()
                        .buildSessionFactory();

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to initialize EMF", e);
            }
        }

    }

    public static EntityManager createEntityManager() {
        if(sessionFactory==null){
            setUpSession();
        }
        return sessionFactory.createEntityManager();
    }


}