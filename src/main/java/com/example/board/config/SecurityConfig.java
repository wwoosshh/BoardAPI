package com.example.board.config;

import com.example.board.security.JwtFilter;
import com.example.board.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;

    public SecurityConfig(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .authorizeHttpRequests(auth -> auth
                        // 🌐 인증 없이 접근 가능한 경로
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // 💬 댓글 관련 API
                        .requestMatchers(HttpMethod.GET, "/api/posts/*/comments").permitAll()  // 댓글 조회는 누구나
                        .requestMatchers(HttpMethod.GET, "/api/posts/*/comments/count").permitAll()  // 댓글 수 조회는 누구나
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/comments").hasAnyRole("USER", "MODERATOR", "ADMIN", "MANAGER")  // 댓글 작성은 로그인 필요
                        .requestMatchers(HttpMethod.PUT, "/api/comments/*").hasAnyRole("USER", "MODERATOR", "ADMIN", "MANAGER")  // 댓글 수정은 로그인 필요
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/*").hasAnyRole("USER", "MODERATOR", "ADMIN", "MANAGER")  // 댓글 삭제는 로그인 필요

                        // 🏢 매니저 전용 API (최고 권한)
                        .requestMatchers("/api/manager/**").hasRole("MANAGER")

                        // 🛡️ 관리자 API는 ADMIN 이상 권한 필요 (사용자 관리)
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "MANAGER")

                        // ✍️ 게시글 작성은 로그인한 사용자 누구나 가능
                        .requestMatchers(HttpMethod.POST, "/api/posts/**").hasAnyRole("USER", "MODERATOR", "ADMIN", "MANAGER")

                        // 📝 게시글 수정/삭제는 MODERATOR 이상 권한 또는 본인 글
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").hasAnyRole("USER", "MODERATOR", "ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").hasAnyRole("USER", "MODERATOR", "ADMIN", "MANAGER")

                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        // H2 콘솔 프레임 허용
        http.headers(headers -> headers.frameOptions().disable());

        // 🔑 JWT 필터 추가
        http.addFilterBefore(new JwtFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🌐 기존 로컬 개발환경
        configuration.addAllowedOrigin("http://localhost:3000");

        // 🌐 Netlify 배포 도메인 추가
        configuration.addAllowedOrigin("https://kaleidoscopic-crostata-c119df.netlify.app");

        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}