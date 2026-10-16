package dev.linkops.server.config;

import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 把 Neon 的 postgresql://user:pass@host/db 连接串转成 JDBC 数据源。 */
@Configuration
public class DataSourceConfig {

    @Bean
    DataSource dataSource(@Value("${DATABASE_URL}") String databaseUrl,
                          @Value("${app.db.use-direct:false}") boolean useDirect,
                          @Value("${spring.datasource.hikari.maximum-pool-size:5}") int poolSize) {
        PgUrl pg = PgUrl.parse(databaseUrl, useDirect);
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(pg.jdbcUrl());
        ds.setUsername(pg.user());
        ds.setPassword(pg.password());
        ds.setMaximumPoolSize(poolSize);
        return ds;
    }

    record PgUrl(String jdbcUrl, String user, String password) {
        static PgUrl parse(String raw, boolean useDirect) {
            URI u = URI.create(raw.trim().replaceFirst("^postgres(ql)?://", "postgresql://"));
            String[] userInfo = u.getRawUserInfo().split(":", 2);
            String host = useDirect ? u.getHost().replace("-pooler.", ".") : u.getHost();
            String jdbc = "jdbc:postgresql://" + host
                    + (u.getPort() > 0 ? ":" + u.getPort() : "")
                    + u.getRawPath()
                    + (u.getRawQuery() != null ? "?" + u.getRawQuery() : "");
            return new PgUrl(jdbc, decode(userInfo[0]), userInfo.length > 1 ? decode(userInfo[1]) : "");
        }

        private static String decode(String s) {
            return java.net.URLDecoder.decode(s, java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
