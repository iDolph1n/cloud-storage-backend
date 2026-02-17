package ru.netology.cloudstorage.repo;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.netology.cloudstorage.domain.TokenEntity;

public interface TokenRepository extends JpaRepository<TokenEntity, String> {

  @Query("select t from TokenEntity t join fetch t.user u where t.token = :token")
  Optional<TokenEntity> findWithUserByToken(String token);

  @Modifying
  @Query("update TokenEntity t set t.revoked = true where t.token = :token")
  int revokeByToken(String token);

  @Modifying
  @Query("delete from TokenEntity t where t.expiresAt < :now or t.revoked = true")
  int deleteExpiredOrRevoked(Instant now);
}
