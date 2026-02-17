package ru.netology.cloudstorage.repo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.netology.cloudstorage.domain.FileEntity;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

  @Query("select f from FileEntity f join fetch f.user u where u.id = :userId and f.filename = :filename")
  Optional<FileEntity> findByUserIdAndFilename(long userId, String filename);

  @Query("select f from FileEntity f join fetch f.user u where u.id = :userId order by f.createdAt desc")
  List<FileEntity> findAllByUserIdOrderByCreatedAtDesc(long userId);

  @Query("select f from FileEntity f join fetch f.user u where u.id = :userId order by f.createdAt desc")
  List<FileEntity> findAllByUserId(long userId);
}
