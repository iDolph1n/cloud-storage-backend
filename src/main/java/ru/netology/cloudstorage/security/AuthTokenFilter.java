package ru.netology.cloudstorage.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.netology.cloudstorage.domain.TokenEntity;
import ru.netology.cloudstorage.repo.TokenRepository;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

  public static final String AUTH_TOKEN_HEADER = "auth-token";

  private final TokenRepository tokenRepo;

  public AuthTokenFilter(TokenRepository tokenRepo) {
    this.tokenRepo = tokenRepo;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // Пусть CORS пройдет префлайт
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      filterChain.doFilter(request, response);
      return;
    }

    if ("/login".equals(request.getRequestURI())) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = request.getHeader(AUTH_TOKEN_HEADER);
    if (token == null || token.isBlank()) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("Unauthorized");
        return;
    }

    TokenEntity tokenEntity = findValidToken(token.trim());
    if (tokenEntity == null) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("Unauthorized");
        return;
    }

    AuthenticatedUser principal = new AuthenticatedUser(tokenEntity.getUser());
    var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
        principal, null, principal.getAuthorities());
    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

    filterChain.doFilter(request, response);
  }

  @Transactional(readOnly = true)
  protected TokenEntity findValidToken(String token) {
    return tokenRepo.findWithUserByToken(token)
        .filter(t -> !t.isRevoked())
        .filter(t -> t.getExpiresAt() != null && t.getExpiresAt().isAfter(Instant.now()))
        .orElse(null);
  }
}
