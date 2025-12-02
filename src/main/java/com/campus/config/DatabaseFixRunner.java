package com.campus.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseFixRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DatabaseFixRunner: Checking if database schema needs update...");
        try {
            // 检查当前类型
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT data_type, udt_name FROM information_schema.columns WHERE table_name = 'event_history' AND column_name = 'participants'"
            );
            
            if (columns.isEmpty()) {
                System.out.println("DatabaseFixRunner: 'participants' column not found.");
                return;
            }
            
            String dataType = (String) columns.get(0).get("data_type");
            String udtName = (String) columns.get(0).get("udt_name");
            
            System.out.println("DatabaseFixRunner: Current type: " + dataType + ", UDT: " + udtName);
            
            // PostgreSQL 中 bigint[] 的 data_type 是 ARRAY，udt_name 是 _int8
            if ("ARRAY".equalsIgnoreCase(dataType) || "_int8".equalsIgnoreCase(udtName)) {
                 System.out.println("DatabaseFixRunner: Converting 'participants' column to TEXT...");
                 // 先删除默认值（如果有）
                 jdbcTemplate.execute("ALTER TABLE event_history ALTER COLUMN participants DROP DEFAULT");
                 // 修改类型
                 jdbcTemplate.execute("ALTER TABLE event_history ALTER COLUMN participants TYPE TEXT USING participants::text");
                 // 恢复默认值 NULL
                 jdbcTemplate.execute("ALTER TABLE event_history ALTER COLUMN participants SET DEFAULT NULL");
                 System.out.println("DatabaseFixRunner: Successfully converted 'participants' column to TEXT.");
            } else {
                 System.out.println("DatabaseFixRunner: No need to update 'participants' column.");
            }

        } catch (Exception e) {
            System.err.println("DatabaseFixRunner: Error checking/updating database schema: " + e.getMessage());
            // 尝试强制执行一次，以防万一上面的检查逻辑有漏洞（虽然不太可能）
            try {
                jdbcTemplate.execute("ALTER TABLE event_history ALTER COLUMN participants TYPE TEXT USING participants::text");
                System.out.println("DatabaseFixRunner: Force update executed.");
            } catch (Exception ex) {
                System.err.println("DatabaseFixRunner: Force update failed: " + ex.getMessage());
            }
        }
    }
}
