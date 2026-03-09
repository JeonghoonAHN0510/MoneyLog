package com.moneylog_backend.moneylog;

import org.junit.jupiter.api.Test;

import com.moneylog_backend.MoneyLogApplication;

class MoneyLogApplicationTests {

    @Test
    void applicationEntryPoint가_존재한다() throws NoSuchMethodException {
        MoneyLogApplication.class.getDeclaredMethod("main", String[].class);
    }

}
