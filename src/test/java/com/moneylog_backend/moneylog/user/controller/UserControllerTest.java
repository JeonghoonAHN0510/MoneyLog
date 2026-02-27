package com.moneylog_backend.moneylog.user.controller;

import com.moneylog_backend.moneylog.user.dto.UserDto;
import com.moneylog_backend.moneylog.user.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Test
    void 프로필이미지_변경시_익명사용자는_401을_반환한다() throws IOException {
        UserService userService = mock(UserService.class);
        UserController userController = new UserController(userService);
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
            "anonymousKey",
            "anonymousUser",
            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        );
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "abc".getBytes(StandardCharsets.UTF_8));

        ResponseEntity<?> response = userController.updateProfileImage(anonymous, file);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userService, never()).updateProfileImage(any(), any());
    }

    @Test
    void 프로필이미지_변경시_인증사용자는_서비스를_호출한다() throws IOException {
        UserService userService = mock(UserService.class);
        UserController userController = new UserController(userService);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "tester",
            null,
            AuthorityUtils.createAuthorityList("ROLE_USER")
        );
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "abc".getBytes(StandardCharsets.UTF_8));
        UserDto dto = UserDto.builder().id("tester").build();
        when(userService.updateProfileImage("tester", file)).thenReturn(dto);

        ResponseEntity<?> response = userController.updateProfileImage(auth, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(userService).updateProfileImage(eq("tester"), eq(file));
    }
}
