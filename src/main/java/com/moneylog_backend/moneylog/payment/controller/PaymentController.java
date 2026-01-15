package com.moneylog_backend.moneylog.payment.controller;

import com.moneylog_backend.global.auth.annotation.LoginUser;
import com.moneylog_backend.moneylog.payment.dto.PaymentDto;
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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> savePayment (@RequestBody PaymentDto paymentDto, @LoginUser Integer user_id) {
        return ResponseEntity.ok(paymentService.savePayment(paymentDto, user_id));
    }

    @GetMapping
    public ResponseEntity<?> getPaymentsByUserId (@LoginUser Integer user_id) {
        return ResponseEntity.ok(paymentService.getPaymentsByUserId(user_id));
    }

    @PutMapping
    public ResponseEntity<?> updatePayment (@RequestBody PaymentDto paymentDto, @LoginUser Integer user_id) {
        return ResponseEntity.ok(paymentService.updatePayment(paymentDto, user_id));
    }

    @DeleteMapping
    public ResponseEntity<?> deletePayment (@RequestParam int payment_id, @LoginUser Integer user_id) {
        return ResponseEntity.ok(paymentService.deletePayment(payment_id, user_id));
    }
}