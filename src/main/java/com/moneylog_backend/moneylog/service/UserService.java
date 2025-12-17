package com.moneylog_backend.moneylog.service;

import com.moneylog_backend.moneylog.model.mapper.UserMapper;
import com.moneylog_backend.moneylog.model.repository.UserRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;


} // class end