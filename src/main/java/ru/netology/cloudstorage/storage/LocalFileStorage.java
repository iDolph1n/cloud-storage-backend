package ru.netology.cloudstorage.storage;

import java.io.InputStream;
import java.nio.file.*;
import org.springframework.stereotype.Component;
import ru.netology.cloudstorage.config.AppProperties;

@Component
public class LocalFileStorage implements FileStorage {

  private final Path root;

  public LocalFileStorage(AppProperties props) {
    this.root = Paths.get(props.getStorage().getRootDir()).toAbsolutePath().normalize();
  }

  @Override
  public void save(String storageKey, InputStream in) throws Exception {
    Files.createDirectories(root);
    Path target = root.resolve(storageKey).normalize();
    if (!target.startsWith(root)) {
      throw new IllegalArgumentException("Invalid storageKey");
    }
    Files.createDirectories(target.getParent());
    // Atomic Write: временный файл + перемещение
    Path tmp = Files.createTempFile(root, "upload-", ".tmp");
    try {
      Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
      Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } finally {
      try { Files.deleteIfExists(tmp); } catch (Exception ignored) {}
    }
  }

  @Override
  public InputStream open(String storageKey) throws Exception {
    Path p = resolvePath(storageKey);
    return Files.newInputStream(p, StandardOpenOption.READ);
  }

  @Override
  public void delete(String storageKey) throws Exception {
    Path p = resolvePath(storageKey);
    Files.deleteIfExists(p);
  }

  @Override
  public Path resolvePath(String storageKey) {
    Path p = root.resolve(storageKey).normalize();
    if (!p.startsWith(root)) {
      throw new IllegalArgumentException("Invalid storageKey");
    }
    return p;
  }
}
