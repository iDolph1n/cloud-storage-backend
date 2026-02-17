package ru.netology.cloudstorage.api;

import java.io.InputStream;
import java.util.List;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudstorage.api.dto.FileInfoResponse;
import ru.netology.cloudstorage.api.dto.RenameFileRequest;
import ru.netology.cloudstorage.security.AuthenticatedUser;
import ru.netology.cloudstorage.service.FileService;

@RestController
@Validated
public class FileController {

  private final FileService fileService;

  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  @GetMapping("/list")
  public List<FileInfoResponse> list(
          @AuthenticationPrincipal AuthenticatedUser principal,
          @RequestParam(name = "limit", required = false) Integer limit
  ) {
      return fileService.list(principal.getUser(), limit).stream()
              .map(f -> new FileInfoResponse(f.getFilename(), f.getSizeBytes()))
              .toList();
  }


    @PostMapping(path = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Void> upload(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @RequestParam("filename") String filename,
      @RequestPart("file") MultipartFile file
  ) {
    fileService.upload(principal.getUser(), filename, file);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/file")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @RequestParam("filename") String filename
  ) {
    fileService.delete(principal.getUser(), filename);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/file")
  public ResponseEntity<InputStreamResource> download(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @RequestParam("filename") String filename
  ) {
    var fe = fileService.get(principal.getUser(), filename);
    InputStream in = fileService.openContent(fe);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fe.getFilename() + "\"")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .contentLength(fe.getSizeBytes())
        .body(new InputStreamResource(in));
  }

  @PutMapping("/file")
  public ResponseEntity<Void> rename(
      @AuthenticationPrincipal AuthenticatedUser principal,
      @RequestParam("filename") String filename,
      @RequestBody RenameFileRequest request
  ) {
    fileService.rename(principal.getUser(), filename, request.filename());
    return ResponseEntity.ok().build();
  }
}
