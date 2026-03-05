package com.moneylog_backend.moneylog.transaction.controller;

import com.moneylog_backend.global.auth.annotation.LoginUser;
import com.moneylog_backend.moneylog.transaction.dto.req.TransactionReqDto;
import com.moneylog_backend.moneylog.transaction.dto.req.TransactionSearchReqDto;
import com.moneylog_backend.moneylog.transaction.dto.req.TransactionImportCommitRequest;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportCommitResponse;
import com.moneylog_backend.moneylog.transaction.dto.res.TransactionImportPreviewResponse;
import com.moneylog_backend.moneylog.transaction.service.TransactionService;
import com.moneylog_backend.moneylog.transaction.service.TransactionImportService;

import java.time.LocalDate;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final TransactionImportService transactionImportService;

    @PostMapping
    public ResponseEntity<?> saveTransaction (@RequestBody @Valid TransactionReqDto transactionReqDto,
                                              @LoginUser Integer userId) {
        int resultValue = transactionService.saveTransaction(transactionReqDto, userId);
        if (resultValue == -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            return ResponseEntity.ok(resultValue);
        }
    }

    @GetMapping
    public ResponseEntity<?> getTransactionsByUserId (@LoginUser Integer userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(transactionService.getTransactionsByUserId(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchTransactions (@LoginUser Integer userId,
                                                 @ModelAttribute TransactionSearchReqDto searchDto) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(transactionService.searchTransactions(userId, searchDto));
    }

    @PutMapping
    public ResponseEntity<?> updateTransaction (@RequestBody @Valid TransactionReqDto transactionReqDto,
                                                @LoginUser Integer userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(transactionService.updateTransaction(transactionReqDto, userId));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteTransaction (@RequestParam Integer transactionId, @LoginUser Integer userId) {
        if (transactionId == null || userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(transactionService.deleteTransaction(transactionId, userId));
    }

    @GetMapping("/calendar")
    public ResponseEntity<?> getCalendarData (@RequestParam(required = false) Integer year,
                                              @RequestParam(required = false) Integer month,
                                              @LoginUser Integer userId) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }

        return ResponseEntity.ok(transactionService.getCalendarData(userId, year, month));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData (@RequestParam(required = false) Integer year,
                                               @RequestParam(required = false) Integer month,
                                               @LoginUser Integer userId) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }

        return ResponseEntity.ok(transactionService.getDashboardData(userId, year, month));
    }

    @PostMapping(value = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TransactionImportPreviewResponse> previewImport (@RequestPart("file") MultipartFile file,
                                                                        @LoginUser Integer userId) {
        return ResponseEntity.ok(transactionImportService.previewImport(file, userId));
    }

    @PostMapping("/import/commit")
    public ResponseEntity<TransactionImportCommitResponse> commitImport (@RequestBody TransactionImportCommitRequest request,
                                                                        @LoginUser Integer userId) {
        return ResponseEntity.ok(transactionImportService.commitImport(request, userId));
    }
}
