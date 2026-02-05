package com.moneylog_backend.moneylog.fixed.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moneylog_backend.moneylog.fixed.service.FixedService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fixed")
@RequiredArgsConstructor
public class FixedController {
    private final FixedService fixedService;
}
