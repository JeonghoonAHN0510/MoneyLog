package com.moneylog_backend.moneylog.payment.controller;

import com.moneylog_backend.global.auth.annotation.LoginUser;
import com.moneylog_backend.moneylog.payment.dto.req.PaymentReqDto;
import com.moneylog_backend.moneylog.payment.service.PaymentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> savePayment(@RequestBody @Valid PaymentReqDto paymentReqDto, @LoginUser Integer userId) {
        return ResponseEntity.ok(paymentService.savePayment(paymentReqDto, userId));
    }

    @GetMapping
    public ResponseEntity<?> getPaymentsByUserId(@LoginUser Integer userId) {
        return ResponseEntity.ok(paymentService.getPaymentsByUserId(userId));
    }

    @PutMapping
    public ResponseEntity<?> updatePayment(@RequestBody @Valid PaymentReqDto paymentReqDto, @LoginUser Integer userId) {
        return ResponseEntity.ok(paymentService.updatePayment(paymentReqDto, userId));
    }

    @DeleteMapping
    public ResponseEntity<?> deletePayment(@RequestParam int paymentId, @LoginUser Integer userId) {
        return ResponseEntity.ok(paymentService.deletePayment(paymentId, userId));
    }
}