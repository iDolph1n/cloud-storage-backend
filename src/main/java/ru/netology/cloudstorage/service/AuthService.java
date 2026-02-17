package ru.netology.cloudstorage.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloudstorage.domain.TokenEntity;
import ru.netology.cloudstorage.domain.UserEntity;
import ru.netology.cloudstorage.exception.UnauthorizedException;
import ru.netology.cloudstorage.repo.UserRepository;

@Service
public class AuthService {

  private final UserRepository userRepo;
  private final TokenService tokenService;
  private final PasswordEncoder encoder;

  public AuthService(UserRepository userRepo, TokenService tokenService, PasswordEncoder encoder) {
    this.userRepo = userRepo;
    this.tokenService = tokenService;
    this.encoder = encoder;
  }

  @Transactional(readOnly = true)
  public TokenEntity login(String login, String password) {
    UserEntity user = userRepo.findByLogin(login)
        .orElseThrow(() -> new UnauthorizedException("Bad credentials"));

    if (!encoder.matches(password, user.getPasswordHash())) {
      throw new UnauthorizedException("Bad credentials");
    }
    return tokenService.issue(user);
  }
}
