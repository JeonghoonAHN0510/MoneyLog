package com.jeonghoon.moneylog.service;

import com.jeonghoon.moneylog.model.mapper.UserMapper;
import com.jeonghoon.moneylog.model.repository.UserRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;


} // class end