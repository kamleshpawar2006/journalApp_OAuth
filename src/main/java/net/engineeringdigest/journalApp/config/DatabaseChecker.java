package net.engineeringdigest.journalApp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j
@Component
public class DatabaseChecker {

    private final DataSource dataSource;

    public DatabaseChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void testConnection() {
        try (Connection connection = dataSource.getConnection()) {
            log.info("Database connected successfully!");
        } catch (Exception e) {
            log.error("Database connection failed!");
            e.printStackTrace();
        }
    }
}

