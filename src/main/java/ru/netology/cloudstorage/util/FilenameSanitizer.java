package ru.netology.cloudstorage.util;

public final class FilenameSanitizer {
  private FilenameSanitizer() {}

  /**
   * Normalize a filename used in query params. For safety we forbid path separators and trim.
   * FRONT uses plain names, so this should not break it.
   */
  public static String sanitize(String filename) {
    if (filename == null) return null;
    String s = filename.trim();
    s = s.replace("\\", "/");
    if (s.contains("/") || s.isBlank()) {
      throw new IllegalArgumentException("Invalid filename");
    }
    return s;
  }
}
