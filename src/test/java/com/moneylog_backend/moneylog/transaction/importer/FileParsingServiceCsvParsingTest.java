package com.moneylog_backend.moneylog.transaction.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class FileParsingServiceCsvParsingTest {
    private FileParsingService fileParsingService;

    @BeforeEach
    void setUp() {
        fileParsingService = new FileParsingService();
    }

    @Test
    void 인용부호_내_줄바꿈은_하나의_필드로_파싱된다() {
        String csv = "거래일자,메모\n2026-03-01,\"첫줄\n둘째줄\"\n";

        var rows = fileParsingService.parseRowsByFile("sample.csv", csvFile(csv));

        assertEquals(2, rows.size());
        assertEquals("첫줄\n둘째줄", rows.get(1).get(1));
    }

    @Test
    void 이스케이프된_인용부호를_포함한_필드가_정상_파싱된다() {
        String csv = "메모\n\"He said \"\"Hi\"\"\"\n";

        var rows = fileParsingService.parseRowsByFile("sample.csv", csvFile(csv));

        assertEquals(2, rows.size());
        assertEquals("He said \"Hi\"", rows.get(1).get(0));
    }

    @Test
    void BOM이_포함된_첫_헤더는_제거된다() {
        String csv = "\uFEFF거래일자,적요\n2026-03-01,테스트\n";

        var rows = fileParsingService.parseRowsByFile("sample.csv", csvFile(csv));

        assertEquals("거래일자", rows.get(0).get(0));
    }

    @Test
    void 열_제한을_초과하면_413을_반환한다() {
        String row = String.join(",", Collections.nCopies(201, "x"));
        String csv = row + "\n";

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> fileParsingService.parseRowsByFile("sample.csv", csvFile(csv))
        );

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, exception.getStatusCode());
    }

    @Test
    void 행_제한을_초과하면_413을_반환한다() {
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < 20_001; i++) {
            csv.append("r").append(i).append('\n');
        }

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> fileParsingService.parseRowsByFile("sample.csv", csvFile(csv.toString()))
        );

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, exception.getStatusCode());
    }

    private MockMultipartFile csvFile(String content) {
        return new MockMultipartFile(
            "file",
            "sample.csv",
            "text/csv",
            content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
