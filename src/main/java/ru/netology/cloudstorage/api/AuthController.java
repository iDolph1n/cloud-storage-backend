package ru.netology.cloudstorage.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.cloudstorage.api.dto.LoginRequest;
import ru.netology.cloudstorage.api.dto.LoginResponse;
import ru.netology.cloudstorage.security.AuthTokenFilter;
import ru.netology.cloudstorage.service.AuthService;
import ru.netology.cloudstorage.service.TokenService;

@RestController
public class AuthController {

  private final AuthService authService;
  private final TokenService tokenService;

  public AuthController(AuthService authService, TokenService tokenService) {
    this.authService = authService;
    this.tokenService = tokenService;
  }

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    var token = authService.login(request.login(), request.password());
    return new LoginResponse(token.getToken());
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader(name = AuthTokenFilter.AUTH_TOKEN_HEADER) String token) {
    tokenService.revoke(token);
    return ResponseEntity.ok().build();
  }
}
