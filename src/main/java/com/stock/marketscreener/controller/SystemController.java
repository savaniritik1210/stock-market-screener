package com.stock.marketscreener.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final DataSource dataSource;

    public SystemController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/db/health")
    public Map<String, Object> dbHealth() {
        Map<String, Object> result = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(3);
            DatabaseMetaData metaData = connection.getMetaData();
            result.put("connected", valid);
            result.put("status", valid ? "ok" : "unreachable");
            result.put("database", metaData.getDatabaseProductName());
            result.put("databaseVersion", metaData.getDatabaseProductVersion());
            result.put("url", metaData.getURL());
        } catch (Exception ex) {
            result.put("connected", false);
            result.put("status", "error");
            result.put("error", ex.getMessage());
        }
        return result;
    }
}
