package ru.netology.cloudstorage.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloudstorage.config.AppProperties;
import ru.netology.cloudstorage.domain.TokenEntity;
import ru.netology.cloudstorage.domain.UserEntity;
import ru.netology.cloudstorage.repo.TokenRepository;

@Service
public class TokenService {

  private static final SecureRandom RNG = new SecureRandom();
  private static final HexFormat HEX = HexFormat.of();

  private final TokenRepository tokenRepo;
  private final AppProperties props;

  public TokenService(TokenRepository tokenRepo, AppProperties props) {
    this.tokenRepo = tokenRepo;
    this.props = props;
  }

  public TokenEntity issue(UserEntity user) {
    TokenEntity t = new TokenEntity();
    t.setToken(randomToken());
    t.setUser(user);
    t.setCreatedAt(Instant.now());
    t.setExpiresAt(Instant.now().plus(props.getSecurity().getTokenTtl()));
    t.setRevoked(false);
    return tokenRepo.save(t);
  }

  @Transactional
  public void revoke(String token) {
    if (token == null || token.isBlank()) return;
    tokenRepo.revokeByToken(token.trim());
  }

  private static String randomToken() {
    byte[] bytes = new byte[32]; // 64 hex chars
    RNG.nextBytes(bytes);
    return HEX.formatHex(bytes);
  }
}
