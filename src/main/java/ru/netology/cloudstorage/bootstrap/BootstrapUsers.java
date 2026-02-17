package ru.netology.cloudstorage.bootstrap;

import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloudstorage.config.AppProperties;
import ru.netology.cloudstorage.domain.UserEntity;
import ru.netology.cloudstorage.repo.UserRepository;

@Component
public class BootstrapUsers {

  private final AppProperties props;
  private final UserRepository userRepo;
  private final PasswordEncoder encoder;

  public BootstrapUsers(AppProperties props, UserRepository userRepo, PasswordEncoder encoder) {
    this.props = props;
    this.userRepo = userRepo;
    this.encoder = encoder;
  }

  @PostConstruct
  @Transactional
  public void init() {
    for (AppProperties.Bootstrap.UserSeed seed : props.getBootstrap().getUsers()) {
      if (seed.getLogin() == null || seed.getLogin().isBlank()) continue;

      userRepo.findByLogin(seed.getLogin().trim()).orElseGet(() -> {
        UserEntity u = new UserEntity();
        u.setLogin(seed.getLogin().trim());
        u.setPasswordHash(encoder.encode(seed.getPassword() == null ? "" : seed.getPassword()));
        return userRepo.save(u);
      });
    }
  }
}
