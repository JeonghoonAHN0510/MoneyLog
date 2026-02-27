package com.moneylog_backend.moneylog.user.service;

import com.moneylog_backend.moneylog.user.dto.UserDto;

public record ProfileImageUpdateTxResult(
    UserDto userDto,
    String oldFileUrl
) {}
