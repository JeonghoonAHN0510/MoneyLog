package com.moneylog_backend.moneylog.file;


import com.moneylog_backend.global.file.FileStore;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {
    private final FileStore fileStore;

    // url 파라미터 예시: /api/files/download?fileUrl=/uploads/2024/01/01/abcd.jpg&originalName=내사진.jpg
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String fileUrl,
                                             @RequestParam String originalName) throws MalformedURLException {

        // 실제 파일 경로 가져오기
        Path path = fileStore.getFullPath(fileUrl);
        UrlResource resource = new UrlResource("file:" + path.toString());

        // 한글 파일명 깨짐 방지 인코딩
        String encodedUploadFileName = UriUtils.encode(originalName, StandardCharsets.UTF_8);

        // 다운로드 헤더 설정 (attachment)
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    @DeleteMapping("/delete")
    public String delete(@RequestParam String fileUrl) {
        fileStore.deleteFile(fileUrl);
        return "Deleted";
    }
}