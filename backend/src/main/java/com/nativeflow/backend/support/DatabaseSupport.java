package com.nativeflow.backend.support;

import com.nativeflow.backend.config.DatabaseProperties;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class DatabaseSupport {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSupport.class);

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DatabaseProperties databaseProperties;

    public DatabaseSupport(
            JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            DatabaseProperties databaseProperties
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.databaseProperties = databaseProperties;
    }

    public Integer queryForInt(String sql) {
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public List<Map<String, Object>> queryForList(String sql, Map<String, ?> params) {
        return namedParameterJdbcTemplate.queryForList(sql, params);
    }

    public boolean tableExists(String tableName) {
        Integer count = namedParameterJdbcTemplate.queryForObject(
                """
                        select count(*)
                        from information_schema.tables
                        where table_schema = 'public'
                          and table_name = :tableName
                        """,
                Map.of("tableName", tableName),
                Integer.class
        );

        return count != null && count > 0;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logConnectionStatus() {
        Integer ping = queryForInt("select 1");
        log.info(
                "Supabase DB connected: host={}, port={}, database={}, poolMode={}, ping={}",
                databaseProperties.host(),
                databaseProperties.port(),
                databaseProperties.database(),
                databaseProperties.poolMode(),
                ping
        );
        log.info(
                "Core tables ready: users={}, series={}, learning_items={}, review_schedules={}",
                tableExists("users"),
                tableExists("series"),
                tableExists("learning_items"),
                tableExists("review_schedules")
        );
    }
}
