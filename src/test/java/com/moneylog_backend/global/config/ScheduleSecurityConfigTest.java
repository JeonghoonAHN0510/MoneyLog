package com.moneylog_backend.global.config;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moneylog_backend.global.auth.jwt.JwtProperties;
import com.moneylog_backend.global.auth.jwt.JwtProvider;
import com.moneylog_backend.global.auth.jwt.RedisTokenKeyResolver;
import com.moneylog_backend.global.auth.security.CustomUserDetails;
import com.moneylog_backend.global.auth.security.CustomUserDetailsService;
import com.moneylog_backend.global.auth.security.RestAccessDeniedHandler;
import com.moneylog_backend.global.auth.security.RestAuthenticationEntryPoint;
import com.moneylog_backend.global.type.ProviderEnum;
import com.moneylog_backend.global.type.RoleEnum;
import com.moneylog_backend.global.type.StatusEnum;
import com.moneylog_backend.global.util.RedisService;
import com.moneylog_backend.moneylog.schedule.controller.ScheduleController;
import com.moneylog_backend.moneylog.schedule.service.ScheduleService;
import com.moneylog_backend.moneylog.user.entity.UserEntity;

@SpringBootTest(
    classes = ScheduleSecurityConfigTest.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "spring.autoconfigure.exclude="
            + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
            + "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration"
    }
)
@AutoConfigureMockMvc
class ScheduleSecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private RedisService redisService;

    @MockBean
    private JwtProperties jwtProperties;

    @MockBean
    private RedisTokenKeyResolver redisTokenKeyResolver;

    @Test
    void 익명_사용자는_관리자_스케줄_조회에_401을_받는다() throws Exception {
        mockMvc.perform(get("/api/admin/schedule/list"))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.status").value(401))
               .andExpect(jsonPath("$.message").value("인증이 필요합니다."));

        verifyNoInteractions(scheduleService);
    }

    @Test
    void 일반_사용자는_관리자_스케줄_조회에_403을_받는다() throws Exception {
        mockUserToken("user-token", "user-login", RoleEnum.USER);

        mockMvc.perform(get("/api/admin/schedule/list")
                            .header("Authorization", "Bearer user-token"))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("$.status").value(403))
               .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));

        verifyNoInteractions(scheduleService);
    }

    @Test
    void 관리자_사용자는_관리자_스케줄_조회에_성공한다() throws Exception {
        mockUserToken("admin-token", "admin-login", RoleEnum.ADMIN);
        when(scheduleService.getAllSchedules()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/schedule/list")
                            .header("Authorization", "Bearer admin-token"))
               .andExpect(status().isOk());
    }

    @Test
    void 블랙리스트_토큰은_보호_경로에서_401을_반환한다() throws Exception {
        when(jwtProvider.validateToken("blacklisted-token")).thenReturn(true);
        when(redisTokenKeyResolver.blacklist("blacklisted-token")).thenReturn("BL:blacklisted-token");
        when(redisService.hasKey("BL:blacklisted-token")).thenReturn(true);

        mockMvc.perform(get("/api/admin/schedule/list")
                            .header("Authorization", "Bearer blacklisted-token"))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.status").value(401))
               .andExpect(jsonPath("$.message").value("로그아웃된 사용자입니다."));

        verifyNoInteractions(scheduleService);
    }

    @Test
    void 잘못된_토큰은_보호_경로에서_401을_반환한다() throws Exception {
        when(jwtProvider.validateToken("invalid-token")).thenReturn(false);

        mockMvc.perform(post("/api/admin/schedule/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {"jobName":"LOG_CLEANUP","frequency":"DAILY","time":"03:00"}
                                """)
                            .header("Authorization", "Bearer invalid-token"))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.status").value(401))
               .andExpect(jsonPath("$.message").value("유효하지 않은 인증 토큰입니다."));
    }

    @Test
    void 공개_인증_경로는_블랙리스트_토큰이_있어도_차단하지_않는다() throws Exception {
        mockMvc.perform(post("/api/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")
                            .header("Authorization", "Bearer blacklisted-token"))
               .andExpect(status().isOk())
               .andExpect(content().string("login-ok"));

        verifyNoInteractions(redisService, customUserDetailsService);
    }

    @Test
    void 공개_인증_경로는_잘못된_토큰이_있어도_차단하지_않는다() throws Exception {
        mockMvc.perform(post("/api/user/password-reset/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")
                            .header("Authorization", "Bearer invalid-token"))
               .andExpect(status().isOk())
               .andExpect(content().string("password-reset-request-ok"));

        verifyNoInteractions(redisService, customUserDetailsService);
    }

    private void mockUserToken(String token, String loginId, RoleEnum role) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            loginId, "", List.of(new SimpleGrantedAuthority(role.name()))
        );

        when(jwtProvider.validateToken(token)).thenReturn(true);
        when(redisTokenKeyResolver.blacklist(token)).thenReturn("BL:" + token);
        when(redisService.hasKey("BL:" + token)).thenReturn(false);
        when(jwtProvider.getAuthentication(token)).thenReturn(authentication);
        when(jwtProvider.getExpiration(token)).thenReturn(3_600_000L);
        when(jwtProperties.getAccessTokenRefreshThresholdInSeconds()).thenReturn(300L);
        when(customUserDetailsService.loadUserByUsername(loginId)).thenReturn(new CustomUserDetails(user(role, loginId)));
    }

    private UserEntity user(RoleEnum role, String loginId) {
        return UserEntity.builder()
                         .userId(1)
                         .accountId(1)
                         .name("테스트")
                         .loginId(loginId)
                         .password("encoded-password")
                         .email(loginId + "@moneylog.test")
                         .phone("010-1234-5678")
                         .gender(true)
                         .role(role)
                         .status(StatusEnum.ACTIVE)
                         .provider(ProviderEnum.LOCAL)
                         .build();
    }

    @RestController
    @RequestMapping("/api/user")
    static class PublicAuthTestController {
        @PostMapping("/login")
        ResponseEntity<String> login() {
            return ResponseEntity.ok("login-ok");
        }

        @PostMapping("/password-reset/request")
        ResponseEntity<String> requestPasswordReset() {
            return ResponseEntity.ok("password-reset-request-ok");
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({ SecurityConfig.class, ScheduleController.class, PublicAuthTestController.class,
              RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class })
    static class TestApplication {
    }
}
