package com.moneylog_backend.moneylog.transaction.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.EmptyFileException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.UnsupportedFileFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileParsingService {
    private static final long IMPORT_MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final int IMPORT_MAX_ROWS = 20_000;
    private static final int IMPORT_MAX_COLUMNS = 200;
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    public void validateFileSize (MultipartFile file) {
        if (file.getSize() > IMPORT_MAX_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 파일 크기(10MB)를 초과했습니다.");
        }
    }

    public String safeFileName (String fileName) {
        return fileName == null ? "" : fileName.trim().toLowerCase();
    }

    public List<List<String>> parseRowsByFile (String fileName, MultipartFile file) {
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            return parseExcel(file);
        }
        if (fileName.endsWith(".csv")) {
            return parseCsv(file);
        }
        throw new IllegalArgumentException("CSV 또는 Excel(xlsx/xls) 파일만 업로드할 수 있습니다.");
    }

    private List<List<String>> parseCsv (MultipartFile file) {
        List<List<String>> rows = new ArrayList<>();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                                               .setIgnoreEmptyLines(true)
                                               .build();
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            CSVParser parser = csvFormat.parse(reader)
        ) {
            for (CSVRecord record : parser) {
                if (rows.size() >= IMPORT_MAX_ROWS) {
                    throw new ResponseStatusException(
                        HttpStatus.PAYLOAD_TOO_LARGE,
                        "업로드 가능한 최대 행 수(" + IMPORT_MAX_ROWS + "행)를 초과했습니다."
                    );
                }
                List<String> parsedRow = new ArrayList<>();
                for (String value : record) {
                    parsedRow.add(value == null ? "" : value.trim());
                }
                if (rows.isEmpty() && !parsedRow.isEmpty() && parsedRow.get(0).startsWith("\uFEFF")) {
                    parsedRow.set(0, parsedRow.get(0).substring(1));
                }
                if (parsedRow.size() > IMPORT_MAX_COLUMNS) {
                    throw new ResponseStatusException(
                        HttpStatus.PAYLOAD_TOO_LARGE,
                        "업로드 가능한 최대 열 수(" + IMPORT_MAX_COLUMNS + "열)를 초과했습니다."
                    );
                }
                rows.add(parsedRow);
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (CSVException e) {
            log.warn("CSV parse format error: sizeBytes={}, exceptionType={}",
                     file.getSize(),
                     e.getClass().getSimpleName(),
                     e);
            throw new IllegalArgumentException("CSV 파일 형식이 올바르지 않거나 손상되었습니다.", e);
        } catch (IOException e) {
            log.warn("CSV parse I/O error: sizeBytes={}, exceptionType={}",
                     file.getSize(),
                     e.getClass().getSimpleName(),
                     e);
            throw new IllegalArgumentException("CSV 파일을 읽는 중 오류가 발생했습니다.", e);
        }
        return rows;
    }

    private List<List<String>> parseExcel (MultipartFile file) {
        List<List<String>> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            var sheet = workbook.getSheetAt(0);
            if (sheet.getLastRowNum() + 1 > IMPORT_MAX_ROWS) {
                throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "업로드 가능한 최대 행 수(" + IMPORT_MAX_ROWS + "행)를 초과했습니다."
                );
            }
            for (int rowIdx = 0; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) {
                    rows.add(List.of());
                    continue;
                }
                int lastCell = Math.max(0, row.getLastCellNum());
                if (lastCell > IMPORT_MAX_COLUMNS) {
                    throw new ResponseStatusException(
                        HttpStatus.PAYLOAD_TOO_LARGE,
                        "업로드 가능한 최대 열 수(" + IMPORT_MAX_COLUMNS + "열)를 초과했습니다."
                    );
                }
                List<String> values = new ArrayList<>();
                for (int col = 0; col <= lastCell; col++) {
                    values.add(cellToString(row.getCell(col)));
                }
                rows.add(values);
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IOException e) {
            log.warn("Excel parse I/O error: sizeBytes={}, exceptionType={}",
                     file.getSize(),
                     e.getClass().getSimpleName(),
                     e);
            throw new IllegalArgumentException("Excel 파일을 읽는 중 오류가 발생했습니다.", e);
        } catch (EncryptedDocumentException | UnsupportedFileFormatException | EmptyFileException e) {
            log.warn("Excel parse format error: sizeBytes={}, exceptionType={}",
                     file.getSize(),
                     e.getClass().getSimpleName(),
                     e);
            throw new IllegalArgumentException("Excel 파일 형식이 올바르지 않거나 손상되었습니다.", e);
        } catch (RuntimeException e) {
            log.error("Excel parse unexpected runtime error: sizeBytes={}, exceptionType={}",
                      file.getSize(),
                      e.getClass().getSimpleName(),
                      e);
            throw new IllegalArgumentException("Excel 파일을 읽는 중 오류가 발생했습니다.", e);
        }
        return rows;
    }

    private String cellToString (Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate().toString();
        }
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }
}
