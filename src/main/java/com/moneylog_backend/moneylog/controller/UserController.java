package com.moneylog_backend.moneylog.controller;

import com.moneylog_backend.moneylog.service.UserService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;


} // class end