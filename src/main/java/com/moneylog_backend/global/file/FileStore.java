package com.moneylog_backend.global.file;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class FileStore {
    // todo 추후 배포하게 된다면, 외부 경로로 변경
    private final String rootPath = System.getProperty("user.dir") + "/build/resources/main/static/uploads/";

    public String storeFile (MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        // 원본 파일명 -> UUID 저장용 파일명 변환
        String storeFileName = createStoreFileName(originalFilename);

        // 날짜별 폴더 생성 (실무 방식: /2024/05/15/)
        String datePath = createDatePath();

        // 최종 저장 경로: rootPath + /2024/05/15/ + uuid.jpg
        String savePath = rootPath + datePath;

        // 폴더가 없으면 생성
        File folder = new File(savePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // 파일 저장 실행
        multipartFile.transferTo(new File(savePath + storeFileName));

        // 결과 반환 (DB 저장용)
        // fileUrl은 웹에서 접근할 때 경로 (예: /uploads/2024/05/15/uuid.jpg)
        return "/uploads/" + datePath + storeFileName;
    }

    public void deleteFile (String fileUrl) {
        // fileUrl 예시: /uploads/2024/05/15/uuid.jpg
        // 실제 경로로 변환이 필요함.
        // 여기서는 간단하게 DB에 저장된 'storeFileName'이 아닌 '전체 경로'나 '날짜 정보'를 안다고 가정하거나
        // DB 설계에 따라 storeFileName 안에 날짜 경로를 포함시키는 전략을 쓰기도 함.

        // 편의상 fileUrl에서 "/uploads/"를 제외한 부분이 실제 저장 경로라고 가정
        if (fileUrl == null || fileUrl.isEmpty())
            return;

        String relativePath = fileUrl.replace("/uploads/", "");
        Path path = Paths.get(rootPath + relativePath);

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // 로그 처리: 삭제 실패해도 에러를 던지지 않거나 로그만 남김
            e.printStackTrace();
        }
    }

    /**
     * 3. 파일 수정 (기존 삭제 -> 새 파일 업로드)
     */
    public String updateFile (String oldFileUrl, MultipartFile newFile) throws IOException {
        // 기존 파일 삭제
        deleteFile(oldFileUrl);
        // 새 파일 업로드
        return storeFile(newFile);
    }

    /**
     * 4. 다운로드를 위한 전체 경로 조회
     */
    public Path getFullPath (String fileUrl) {
        String relativePath = fileUrl.replace("/uploads/", "");
        return Paths.get(rootPath + relativePath);
    }

    // --- 내부 유틸 메서드 ---

    // UUID 파일명 생성
    private String createStoreFileName (String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    // 확장자 추출
    private String extractExt (String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    // 날짜 폴더 경로 생성 (2024/01/01/)
    private String createDatePath () {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd/"));
    }
}