package com.moneylog_backend.moneylog.fixed.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moneylog_backend.moneylog.fixed.mapper.FixedMapper;
import com.moneylog_backend.moneylog.fixed.repository.FixedRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FixedService {
    private final FixedRepository fixedRepository;
    private final FixedMapper fixedMapper;
}
