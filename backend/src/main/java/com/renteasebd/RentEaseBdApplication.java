package com.renteasebd;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RentEaseBdApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RentEaseBdApplication.class);
        Map<String, Object> derivedDatasource = deriveDatasourceFromDatabaseUrl();
        if (!derivedDatasource.isEmpty()) {
            application.setDefaultProperties(derivedDatasource);
        }
        application.run(args);
    }

    private static Map<String, Object> deriveDatasourceFromDatabaseUrl() {
        Map<String, Object> derived = new HashMap<>();
        if (hasText(System.getenv("SPRING_DATASOURCE_URL"))) {
            return derived;
        }

        String rawDatabaseUrl = System.getenv("DATABASE_URL");
        if (!hasText(rawDatabaseUrl)) {
            return derived;
        }

        try {
            URI uri = URI.create(rawDatabaseUrl.trim());
            String scheme = uri.getScheme();
            if (scheme == null) {
                return derived;
            }

            String normalizedScheme = scheme.toLowerCase();
            if (!"postgres".equals(normalizedScheme) && !"postgresql".equals(normalizedScheme)) {
                return derived;
            }

            String host = uri.getHost();
            String path = uri.getPath();
            if (!hasText(host) || !hasText(path) || "/".equals(path)) {
                return derived;
            }

            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String databaseName = path.startsWith("/") ? path.substring(1) : path;
            String query = uri.getRawQuery();

            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + databaseName;
            if (hasText(query)) {
                jdbcUrl = jdbcUrl + "?" + query;
            }
            derived.put("spring.datasource.url", jdbcUrl);

            String userInfo = uri.getRawUserInfo();
            if (hasText(userInfo)) {
                String[] parts = userInfo.split(":", 2);
                if (!hasText(System.getenv("SPRING_DATASOURCE_USERNAME")) && parts.length >= 1 && hasText(parts[0])) {
                    derived.put("spring.datasource.username", decodeUrlComponent(parts[0]));
                }
                if (!hasText(System.getenv("SPRING_DATASOURCE_PASSWORD")) && parts.length == 2) {
                    derived.put("spring.datasource.password", decodeUrlComponent(parts[1]));
                }
            }
        } catch (Exception ignored) {
            return Map.of();
        }

        return derived;
    }

    private static String decodeUrlComponent(String rawValue) {
        return URLDecoder.decode(rawValue, StandardCharsets.UTF_8);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
