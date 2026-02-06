package com.moneylog_backend.moneylog.fixed.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moneylog_backend.global.auth.annotation.LoginUser;
import com.moneylog_backend.moneylog.fixed.dto.req.FixedReqDto;
import com.moneylog_backend.moneylog.fixed.service.FixedService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fixed")
@RequiredArgsConstructor
public class FixedController {
    private final FixedService fixedService;

    @PostMapping
    public ResponseEntity<?> saveFixed(@RequestBody @Valid FixedReqDto fixedReqDto, @LoginUser Integer userId){
        return ResponseEntity.ok(fixedService.saveFixed(fixedReqDto, userId));
    }
}
