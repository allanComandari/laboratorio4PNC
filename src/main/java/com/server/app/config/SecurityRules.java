package com.server.app.config;

import org.springframework.util.AntPathMatcher;
import java.util.Map;
import java.util.Set;

public class SecurityRules {

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    public static final Map<String, Set<String>> PUBLIC = Map.of(
            "GET", Set.of("/api/public/info"),
            "POST", Set.of("/api/auth/login","/api/auth/signup")
    );

    public static final Map<String, Set<String>> AUTH_ONLY = Map.of(
            "GET", Set.of("/api/auth/profile", "/api/prestamos/**", "/api/plan-pagos/**", "/api/abonos/**"),
            "POST", Set.of("/api/auth/logout", "/api/prestamos", "/api/abonos"),
            "PUT", Set.of("/api/prestamos/**"),
            "DELETE", Set.of("/api/prestamos/**")
    );

    public static final Set<String> IGNORED = Set.of("/error");

    public static boolean isPublic(String method, String path) {
        return PUBLIC.containsKey(method) && PUBLIC.get(method).stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    public static boolean isAuthOnly(String method, String path) {
        return AUTH_ONLY.containsKey(method) && AUTH_ONLY.get(method).stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    public static boolean isIgnored(String path) {
        return IGNORED.contains(path);
    }

    public static boolean requiresAuth(String method, String path) {
        return !isPublic(method, path) && !isIgnored(path);
    }
}