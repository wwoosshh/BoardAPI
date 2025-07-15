// src/main/java/com/example/board/controller/HealthController.java
package com.example.board.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HealthController {

    private final LocalDateTime startTime = LocalDateTime.now();

    /**
     * 기본 헬스체크 엔드포인트
     * 프론트엔드에서 서버 상태를 확인하는 용도
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();

        response.put("status", "UP");
        response.put("message", "서버가 정상적으로 동작 중입니다.");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("server", "Board Application Server");
        response.put("version", "1.0.0");

        // 서버 가동 시간 정보
        LocalDateTime now = LocalDateTime.now();
        response.put("uptime", calculateUptime(startTime, now));

        return ResponseEntity.ok(response);
    }

    /**
     * 상세 헬스체크 엔드포인트
     * 더 자세한 시스템 정보 제공
     */
    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> response = new HashMap<>();

        // 기본 상태 정보
        response.put("status", "UP");
        response.put("message", "서버가 정상적으로 동작 중입니다.");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 서버 정보
        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("name", "Board Application Server");
        serverInfo.put("version", "1.0.0");
        serverInfo.put("environment", System.getProperty("spring.profiles.active", "default"));
        serverInfo.put("startTime", startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        serverInfo.put("uptime", calculateUptime(startTime, LocalDateTime.now()));
        response.put("server", serverInfo);

        // 시스템 리소스 정보
        Map<String, Object> systemInfo = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        systemInfo.put("processors", runtime.availableProcessors());
        systemInfo.put("maxMemoryMB", maxMemory / 1024 / 1024);
        systemInfo.put("totalMemoryMB", totalMemory / 1024 / 1024);
        systemInfo.put("usedMemoryMB", usedMemory / 1024 / 1024);
        systemInfo.put("freeMemoryMB", freeMemory / 1024 / 1024);
        systemInfo.put("memoryUsagePercent", (double) usedMemory / totalMemory * 100);
        response.put("system", systemInfo);

        // 애플리케이션 정보
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("javaVersion", System.getProperty("java.version"));
        appInfo.put("springBootVersion", getClass().getPackage().getImplementationVersion());
        appInfo.put("osName", System.getProperty("os.name"));
        appInfo.put("osVersion", System.getProperty("os.version"));
        response.put("application", appInfo);

        return ResponseEntity.ok(response);
    }

    /**
     * 간단한 ping 엔드포인트
     * 가장 빠른 응답을 위한 최소한의 체크
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "pong");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return ResponseEntity.ok(response);
    }

    /**
     * 서버 상태 확인 (부하 테스트용)
     * 의도적으로 약간의 지연을 주어 서버 응답 시간 테스트
     */
    @GetMapping("/health/load-test")
    public ResponseEntity<Map<String, Object>> loadTest() throws InterruptedException {
        // 100ms 지연 (실제 서버 작업 시뮬레이션)
        Thread.sleep(100);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "부하 테스트 완료");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("processingTime", "100ms");

        return ResponseEntity.ok(response);
    }

    /**
     * 점검 모드 시뮬레이션 (개발/테스트용)
     * 쿼리 파라미터로 maintenance=true 전달 시 점검 중 응답
     */
    @GetMapping("/health/maintenance")
    public ResponseEntity<Map<String, Object>> maintenanceCheck(
            @RequestParam(value = "maintenance", defaultValue = "false") boolean maintenance) {

        Map<String, Object> response = new HashMap<>();

        if (maintenance) {
            response.put("status", "MAINTENANCE");
            response.put("message", "시스템 점검 중입니다. 잠시 후 다시 접속해 주세요.");
            response.put("estimatedTime", "약 30분 후 점검 완료 예정");
            response.put("contactEmail", "admin@board.com");

            // 503 Service Unavailable 상태 코드 반환
            return ResponseEntity.status(503).body(response);
        } else {
            response.put("status", "UP");
            response.put("message", "서버가 정상적으로 동작 중입니다.");
        }

        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return ResponseEntity.ok(response);
    }

    /**
     * 서버 가동 시간 계산
     */
    private Map<String, Object> calculateUptime(LocalDateTime start, LocalDateTime end) {
        long seconds = java.time.Duration.between(start, end).getSeconds();

        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        Map<String, Object> uptime = new HashMap<>();
        uptime.put("days", days);
        uptime.put("hours", hours);
        uptime.put("minutes", minutes);
        uptime.put("seconds", seconds);
        uptime.put("totalSeconds", java.time.Duration.between(start, end).getSeconds());
        uptime.put("formatted", String.format("%d일 %d시간 %d분 %d초", days, hours, minutes, seconds));

        return uptime;
    }
}