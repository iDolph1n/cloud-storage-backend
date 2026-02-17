package ru.netology.cloudstorage.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudstorage.domain.FileEntity;
import ru.netology.cloudstorage.domain.UserEntity;
import ru.netology.cloudstorage.exception.ConflictException;
import ru.netology.cloudstorage.repo.FileRepository;
import ru.netology.cloudstorage.storage.FileStorage;

class FileServiceTest {

  @Test
  void upload_conflict_when_filename_exists() {
    FileRepository fileRepo = mock(FileRepository.class);
    FileStorage storage = mock(FileStorage.class);
    FileService svc = new FileService(fileRepo, storage);

    UserEntity u = new UserEntity();
    u.setId(1L);

    FileEntity existing = new FileEntity();
    existing.setFilename("a.txt");

    when(fileRepo.findByUserIdAndFilename(1L, "a.txt")).thenReturn(Optional.of(existing));

    MultipartFile mf = mock(MultipartFile.class);
    assertThrows(ConflictException.class, () -> svc.upload(u, "a.txt", mf));
    verifyNoInteractions(storage);
  }

  @Test
  void rename_changes_filename() {
    FileRepository fileRepo = mock(FileRepository.class);
    FileStorage storage = mock(FileStorage.class);
    FileService svc = new FileService(fileRepo, storage);

    UserEntity u = new UserEntity();
    u.setId(1L);

    FileEntity fe = new FileEntity();
    fe.setUser(u);
    fe.setFilename("old.txt");

    when(fileRepo.findByUserIdAndFilename(1L, "old.txt")).thenReturn(Optional.of(fe));
    when(fileRepo.findByUserIdAndFilename(1L, "new.txt")).thenReturn(Optional.empty());
    when(fileRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    svc.rename(u, "old.txt", "new.txt");
    assertEquals("new.txt", fe.getFilename());
    verify(fileRepo).save(fe);
  }

  @Test
  void openContent_uses_storage() throws Exception {
    FileRepository fileRepo = mock(FileRepository.class);
    FileStorage storage = mock(FileStorage.class);
    FileService svc = new FileService(fileRepo, storage);

    FileEntity fe = new FileEntity();
    fe.setStorageKey("k");
    when(storage.open("k")).thenReturn(new ByteArrayInputStream(new byte[]{1,2,3}));

    assertNotNull(svc.openContent(fe));
    verify(storage).open("k");
  }
}
