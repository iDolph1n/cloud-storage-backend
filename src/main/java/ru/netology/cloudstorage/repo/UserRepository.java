package ru.netology.cloudstorage.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.netology.cloudstorage.domain.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByLogin(String login);
}
