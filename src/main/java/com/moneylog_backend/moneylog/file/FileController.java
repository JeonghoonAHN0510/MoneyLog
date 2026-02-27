package com.moneylog_backend.moneylog.file;

import com.moneylog_backend.global.file.FileDownloadResult;
import com.moneylog_backend.global.file.FileStorageService;
import com.moneylog_backend.global.file.FileUploadResult;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {
    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResult> upload(@RequestPart("file") MultipartFile file,
                                                   @RequestParam(required = false) String dir) throws IOException {
        return ResponseEntity.ok(fileStorageService.uploadFile(file, dir));
    }

    // url 파라미터 예시: /api/files/download?fileUrl=/uploads/2024/01/01/abcd.jpg&originalName=내사진.jpg
    @GetMapping("/download")
    public ResponseEntity<?> download(@RequestParam String fileUrl, @RequestParam String originalName) {
        FileDownloadResult downloadResult = fileStorageService.downloadFile(fileUrl, originalName);
        if (downloadResult.isRedirect()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                                 .location(URI.create(downloadResult.getRedirectUrl()))
                                 .build();
        }

        Resource resource = downloadResult.getResource();

        // 한글 파일명 깨짐 방지 인코딩
        String encodedUploadFileName = UriUtils.encode(originalName, StandardCharsets.UTF_8);

        // 다운로드 헤더 설정 (attachment)
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition).body(resource);
    }

    @GetMapping("/view")
    public ResponseEntity<?> view(@RequestParam String fileUrl) {
        FileDownloadResult downloadResult = fileStorageService.downloadFile(fileUrl, null);
        if (downloadResult.isRedirect()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                                 .location(URI.create(downloadResult.getRedirectUrl()))
                                 .build();
        }

        Resource resource = downloadResult.getResource();
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                                              .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                             .contentType(mediaType)
                             .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                             .header("X-Content-Type-Options", "nosniff")
                             .body(resource);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestParam String fileUrl) {
        fileStorageService.deleteFile(fileUrl);
        return ResponseEntity.ok("Deleted");
    }
}
