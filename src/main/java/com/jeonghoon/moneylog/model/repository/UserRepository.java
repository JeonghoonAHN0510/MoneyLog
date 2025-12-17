package com.jeonghoon.moneylog.model.repository;

import com.jeonghoon.moneylog.model.entity.UserEntity;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserEntity, Integer> {

} // interface end