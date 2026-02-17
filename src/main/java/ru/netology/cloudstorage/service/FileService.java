package ru.netology.cloudstorage.service;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudstorage.domain.FileEntity;
import ru.netology.cloudstorage.domain.UserEntity;
import ru.netology.cloudstorage.exception.ConflictException;
import ru.netology.cloudstorage.exception.NotFoundException;
import ru.netology.cloudstorage.exception.StorageException;
import ru.netology.cloudstorage.repo.FileRepository;
import ru.netology.cloudstorage.storage.FileStorage;
import ru.netology.cloudstorage.util.FilenameSanitizer;

@Service
public class FileService {

  private final FileRepository fileRepo;
  private final FileStorage storage;

  public FileService(FileRepository fileRepo, FileStorage storage) {
    this.fileRepo = fileRepo;
    this.storage = storage;
  }

  @Transactional(readOnly = true)
  public List<FileEntity> list(UserEntity user, Integer limit) {
      List<FileEntity> all = fileRepo.findAllByUserIdOrderByCreatedAtDesc(user.getId());
      if (limit == null || limit <= 0 || limit >= all.size()) {
          return all;
      }
      return all.subList(0, limit);
  }

    @Transactional
  public void upload(UserEntity user, String filenameRaw, MultipartFile file) {
    String filename = FilenameSanitizer.sanitize(filenameRaw);
    fileRepo.findByUserIdAndFilename(user.getId(), filename).ifPresent(f -> {
      throw new ConflictException("File already exists");
    });

    String storageKey = user.getId() + "/" + UUID.randomUUID();

    try (InputStream in = file.getInputStream()) {
      storage.save(storageKey, in);
    } catch (Exception ex) {
      throw new StorageException("Storage error", ex);
    }

    FileEntity fe = new FileEntity();
    fe.setUser(user);
    fe.setFilename(filename);
    fe.setSizeBytes(file.getSize());
    fe.setStorageKey(storageKey);
    fe.setCreatedAt(Instant.now());
    fileRepo.save(fe);
  }

  @Transactional
  public void delete(UserEntity user, String filenameRaw) {
    String filename = FilenameSanitizer.sanitize(filenameRaw);
    FileEntity fe = fileRepo.findByUserIdAndFilename(user.getId(), filename)
        .orElseThrow(() -> new NotFoundException("File not found"));

    try {
      storage.delete(fe.getStorageKey());
    } catch (Exception ex) {
      throw new StorageException("Storage error", ex);
    }
    fileRepo.delete(fe);
  }

  @Transactional(readOnly = true)
  public FileEntity get(UserEntity user, String filenameRaw) {
    String filename = FilenameSanitizer.sanitize(filenameRaw);
    return fileRepo.findByUserIdAndFilename(user.getId(), filename)
        .orElseThrow(() -> new NotFoundException("File not found"));
  }

  @Transactional
  public void rename(UserEntity user, String filenameRaw, String newFilenameRaw) {
    String filename = FilenameSanitizer.sanitize(filenameRaw);
    String newFilename = FilenameSanitizer.sanitize(newFilenameRaw);

    FileEntity fe = fileRepo.findByUserIdAndFilename(user.getId(), filename)
        .orElseThrow(() -> new NotFoundException("File not found"));

    fileRepo.findByUserIdAndFilename(user.getId(), newFilename).ifPresent(existing -> {
      throw new ConflictException("Target filename already exists");
    });

    fe.setFilename(newFilename);
    fileRepo.save(fe);
  }

  public InputStream openContent(FileEntity fileEntity) {
    try {
      return storage.open(fileEntity.getStorageKey());
    } catch (Exception ex) {
      throw new StorageException("Storage error", ex);
    }
  }
}
