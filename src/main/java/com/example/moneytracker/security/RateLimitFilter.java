package com.example.moneytracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optional rate limiting for auth endpoints to prevent brute-force and abuse.
 * In-memory, per-IP; for production consider Redis-based limiting.
 */
@Component
@Order(-100)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String REGISTER_PATH = "/api/auth/register";
    private static final long WINDOW_MS = 60_000;

    private final RateLimitProperties rateLimitProperties;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties rateLimitProperties) {
        this.rateLimitProperties = rateLimitProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!rateLimitProperties.isEnabled() || !isAuthRequest(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket());
        boolean allowed = bucket.tryAcquire(rateLimitProperties.getAuthRequestsPerMinute(), WINDOW_MS);

        if (!allowed) {
            log.warn("Rate limit exceeded for {} on {}", key, request.getRequestURI());
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthRequest(String path) {
        return path != null && (path.startsWith(LOGIN_PATH) || path.startsWith(REGISTER_PATH));
    }

    private String clientKey(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private static final class Bucket {
        private int count;
        private long windowStartMs = System.currentTimeMillis();

        synchronized boolean tryAcquire(int limit, long windowMs) {
            long now = System.currentTimeMillis();
            if (now - windowStartMs >= windowMs) {
                count = 0;
                windowStartMs = now;
            }
            if (count >= limit) {
                return false;
            }
            count++;
            return true;
        }
    }
}
