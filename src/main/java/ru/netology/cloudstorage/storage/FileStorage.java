package ru.netology.cloudstorage.storage;

import java.io.InputStream;
import java.nio.file.Path;

public interface FileStorage {

  /**
   * Сохраняйте поток в StorageKey. Нужно либо полностью выполнить, либо бросить.
   */
  void save(String storageKey, InputStream in) throws Exception;

  /**
   * Откройте сохранённый файл для чтения.
   */
  InputStream open(String storageKey) throws Exception;

  /**
   * Удалить сохранённый файл; должно быть идемпотентным.
   */
  void delete(String storageKey) throws Exception;

  /**
   * Разрешить storageKey к пути файловой системы (для отладочных операций).
   */
  Path resolvePath(String storageKey);
}
