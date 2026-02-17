package ru.netology.cloudstorage.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.netology.cloudstorage.domain.TokenEntity;
import ru.netology.cloudstorage.domain.UserEntity;
import ru.netology.cloudstorage.exception.UnauthorizedException;
import ru.netology.cloudstorage.repo.UserRepository;

import java.util.Optional;

class AuthServiceTest {

  @Test
  void login_success_issues_token() {
    UserRepository userRepo = mock(UserRepository.class);
    TokenService tokenService = mock(TokenService.class);
    PasswordEncoder encoder = mock(PasswordEncoder.class);

    UserEntity u = new UserEntity();
    u.setId(1L);
    u.setLogin("user");
    u.setPasswordHash("hash");

    when(userRepo.findByLogin("user")).thenReturn(Optional.of(u));
    when(encoder.matches("password", "hash")).thenReturn(true);

    TokenEntity t = new TokenEntity();
    t.setToken("t");
    t.setUser(u);
    t.setExpiresAt(Instant.now().plusSeconds(3600));

    when(tokenService.issue(u)).thenReturn(t);

    AuthService svc = new AuthService(userRepo, tokenService, encoder);
    TokenEntity out = svc.login("user", "password");

    assertEquals("t", out.getToken());
    verify(tokenService).issue(u);
  }

  @Test
  void login_bad_credentials_throws_unauthorized() {
    UserRepository userRepo = mock(UserRepository.class);
    TokenService tokenService = mock(TokenService.class);
    PasswordEncoder encoder = mock(PasswordEncoder.class);

    when(userRepo.findByLogin("user")).thenReturn(Optional.empty());

    AuthService svc = new AuthService(userRepo, tokenService, encoder);
    assertThrows(UnauthorizedException.class, () -> svc.login("user", "password"));
    verifyNoInteractions(tokenService);
  }
}
