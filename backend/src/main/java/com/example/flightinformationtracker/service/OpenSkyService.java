package com.example.flightinformationtracker.service;

import com.example.flightinformationtracker.config.OpenSkyConfig;
import com.example.flightinformationtracker.dto.FlightState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenSky API 接入核心服务：
 * - OAuth2 Token 获取与缓存
 * - 实时航班拉取与字段映射
 * - 重试与429限流降级
 */
@Service
public class OpenSkyService {
    private static final Logger log = LoggerFactory.getLogger(OpenSkyService.class);

    private final OpenSkyConfig openSkyConfig;
    private final WebClient webClient;
    private final Environment environment;

    private volatile String accessToken;
    private volatile Instant accessTokenExpireAt = Instant.EPOCH;

    private volatile Instant lastUpdateTime;
    private volatile Long apiRemainingQuota;
    private volatile String lastError;

    private volatile Instant rateLimitedUntil = Instant.EPOCH;
    private volatile List<FlightState> cachedFlights = Collections.emptyList();
    private volatile boolean lastFetchStale;

    public OpenSkyService(OpenSkyConfig openSkyConfig,
                          WebClient.Builder webClientBuilder,
                          Environment environment) {
        this.openSkyConfig = openSkyConfig;
        this.webClient = webClientBuilder.build();
        this.environment = environment;
    }

    /**
     * 拉取当前航班数据（供广播任务调用）。
     */
    public List<FlightState> fetchCurrentFlights() {
        Instant now = Instant.now();

        if (now.isBefore(rateLimitedUntil)) {
            lastError = "OpenSky 429限流冷却中";
            return copyWithStale(cachedFlights, true);
        }

        int attempts = Math.max(1, openSkyConfig.getRetryMaxAttempts());
        Exception lastException = null;

        for (int i = 1; i <= attempts; i++) {
            try {
                List<FlightState> flights = requestStatesOnce();
                cachedFlights = copyWithStale(flights, false);
                lastUpdateTime = Instant.now();
                lastFetchStale = false;
                lastError = null;
                log.info("OpenSky 拉取成功：{} 架飞机", flights.size());
                return copyWithStale(cachedFlights, false);
            } catch (WebClientResponseException.TooManyRequests e) {
                rateLimitedUntil = Instant.now().plusSeconds(openSkyConfig.getRateLimitPauseSeconds());
                lastError = "OpenSky 返回 429，进入冷却期";
                lastFetchStale = true;
                log.warn("OpenSky 429 限流，暂停 {} 秒，使用缓存数据", openSkyConfig.getRateLimitPauseSeconds());
                return copyWithStale(cachedFlights, true);
            } catch (Exception e) {
                lastException = e;
                if (i < attempts) {
                    safeSleep(openSkyConfig.getRetryIntervalSeconds());
                }
            }
        }

        lastFetchStale = true;
        lastError = lastException == null ? "未知错误" : lastException.getMessage();
        log.error("OpenSky 拉取失败，使用缓存数据：{}", lastError);
        return copyWithStale(cachedFlights, true);
    }

    /**
     * 供 B 角色 REST 接口读取当前缓存数据。
     */
    public List<FlightState> getCachedFlights() {
        return copyWithStale(cachedFlights, false);
    }

    /**
     * 定时任务轮询周期（毫秒）。
     * 优先使用 opensky.polling-interval-seconds；
     * 未配置时按 profile 约定：dev=60s, test=30s, demo=15s。
     */
    public long getPollingIntervalMillis() {
        if (openSkyConfig.getPollingIntervalSeconds() != null && openSkyConfig.getPollingIntervalSeconds() > 0) {
            return openSkyConfig.getPollingIntervalSeconds() * 1000;
        }

        List<String> activeProfiles = List.of(environment.getActiveProfiles());
        if (activeProfiles.contains("demo")) {
            return 15_000L;
        }
        if (activeProfiles.contains("test")) {
            return 30_000L;
        }
        return 60_000L;
    }

    /**
     * 系统状态信息（供 /api/system/status 使用）。
     */
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("cachedFlightCount", cachedFlights.size());
        status.put("lastUpdateTime", lastUpdateTime == null ? null : lastUpdateTime.toString());
        status.put("apiRemainingQuota", apiRemainingQuota);
        status.put("tokenExpireAt", accessTokenExpireAt == null ? null : accessTokenExpireAt.toString());
        status.put("rateLimitedUntil", rateLimitedUntil == null ? null : rateLimitedUntil.toString());
        status.put("lastFetchStale", lastFetchStale);
        status.put("lastError", lastError);
        status.put("pollingIntervalMillis", getPollingIntervalMillis());
        return status;
    }

    @SuppressWarnings("unchecked")
    private List<FlightState> requestStatesOnce() {
        String token = getAccessToken();
        OpenSkyConfig.Bbox bbox = openSkyConfig.getBbox();

        ResponseEntity<Map> response = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/states/all")
                        .queryParam("lamin", bbox.getLamin())
                        .queryParam("lamax", bbox.getLamax())
                        .queryParam("lomin", bbox.getLomin())
                        .queryParam("lomax", bbox.getLomax())
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(Map.class)
                .block();

        if (response == null || response.getBody() == null) {
            throw new IllegalStateException("OpenSky 响应为空");
        }

        apiRemainingQuota = parseQuotaHeader(response.getHeaders());
        return parseFlightStates(response.getBody());
    }

    @SuppressWarnings("unchecked")
    private synchronized String getAccessToken() {
        Instant now = Instant.now();
        if (accessToken != null && now.isBefore(accessTokenExpireAt)) {
            return accessToken;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", openSkyConfig.getClientId());
        form.add("client_secret", openSkyConfig.getClientSecret());

        Map<String, Object> tokenJson = webClient
                .post()
                .uri(openSkyConfig.getAuthUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (tokenJson == null || tokenJson.get("access_token") == null) {
            throw new IllegalStateException("获取 OpenSky Token 失败");
        }

        String newToken = String.valueOf(tokenJson.get("access_token"));
        long expiresIn = toLong(tokenJson.get("expires_in"), 1800L);
        long refreshAdvance = Math.max(60, openSkyConfig.getTokenRefreshAdvanceSeconds());
        long effectiveSeconds = Math.max(60, expiresIn - refreshAdvance);

        accessToken = newToken;
        accessTokenExpireAt = Instant.now().plusSeconds(effectiveSeconds);
        return accessToken;
    }

    @SuppressWarnings("unchecked")
    private List<FlightState> parseFlightStates(Map<String, Object> body) {
        Object statesObj = body.get("states");
        if (!(statesObj instanceof List<?> states)) {
            return Collections.emptyList();
        }

        List<FlightState> result = new ArrayList<>();
        for (Object rowObj : states) {
            if (!(rowObj instanceof List<?> row)) {
                continue;
            }

            Double longitude = toDouble(at(row, 5));
            Double latitude = toDouble(at(row, 6));
            if (longitude == null || latitude == null) {
                continue;
            }

            Boolean onGround = toBoolean(at(row, 8));
            if (openSkyConfig.isFilterOnGround() && Boolean.TRUE.equals(onGround)) {
                continue;
            }

            FlightState state = new FlightState();
            state.setIcao24(toStringValue(at(row, 0)));
            state.setCallsign(toStringValue(at(row, 1)));
            state.setOriginCountry(toStringValue(at(row, 2)));
            state.setTimePosition(toLong(at(row, 3), null));
            state.setLastContact(toLong(at(row, 4), null));
            state.setLongitude(longitude);
            state.setLatitude(latitude);
            state.setBaroAltitude(toDouble(at(row, 7)));
            state.setOnGround(onGround);
            state.setVelocity(toDouble(at(row, 9)));
            state.setHeading(toDouble(at(row, 10)));
            state.setStale(false);
            result.add(state);
        }

        return result;
    }

    private Object at(List<?> row, int index) {
        if (index < 0 || index >= row.size()) {
            return null;
        }
        return row.get(index);
    }

    private String toStringValue(Object obj) {
        if (obj == null) {
            return null;
        }
        return String.valueOf(obj).trim();
    }

    private Long toLong(Object obj, Long defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        if (obj instanceof Number num) {
            return num.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(obj));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Double toDouble(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number num) {
            return num.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(obj));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean toBoolean(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Boolean bool) {
            return bool;
        }
        if (obj instanceof Number num) {
            return num.intValue() != 0;
        }
        String s = String.valueOf(obj).trim();
        if ("true".equalsIgnoreCase(s)) {
            return true;
        }
        if ("false".equalsIgnoreCase(s)) {
            return false;
        }
        return null;
    }

    private Long parseQuotaHeader(HttpHeaders headers) {
        List<String> names = List.of("X-Rate-Limit-Remaining", "X-RateLimit-Remaining", "x-ratelimit-remaining");
        for (String name : names) {
            String value = headers.getFirst(name);
            if (value != null && !value.isBlank()) {
                try {
                    return Long.parseLong(value.trim());
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private List<FlightState> copyWithStale(List<FlightState> source, boolean stale) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        List<FlightState> copy = new ArrayList<>(source.size());
        for (FlightState item : source) {
            FlightState state = new FlightState();
            state.setIcao24(item.getIcao24());
            state.setCallsign(item.getCallsign());
            state.setOriginCountry(item.getOriginCountry());
            state.setLongitude(item.getLongitude());
            state.setLatitude(item.getLatitude());
            state.setBaroAltitude(item.getBaroAltitude());
            state.setVelocity(item.getVelocity());
            state.setHeading(item.getHeading());
            state.setOnGround(item.getOnGround());
            state.setTimePosition(item.getTimePosition());
            state.setLastContact(item.getLastContact());
            state.setStale(stale);
            copy.add(state);
        }
        return copy;
    }

    private void safeSleep(int seconds) {
        try {
            Thread.sleep(Math.max(seconds, 1) * 1000L);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
