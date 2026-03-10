CREATE TABLE IF NOT EXISTS bank (
    bank_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    code CHAR(3) NOT NULL,
    logo_image_url VARCHAR(255) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (bank_id),
    UNIQUE KEY uk_bank_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS account (
    account_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    bank_id INT UNSIGNED NULL,
    nickname VARCHAR(50) NULL,
    balance INT NULL,
    account_number VARCHAR(50) NULL,
    color ENUM('RED', 'AMBER', 'YELLOW', 'LIME', 'GREEN', 'EMERALD', 'TEAL', 'CYAN', 'BLUE', 'PURPLE', 'PINK', 'SLATE') DEFAULT 'BLUE',
    type ENUM('BANK', 'CASH', 'POINT', 'OTHER') NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user (
    user_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    account_id INT UNSIGNED NULL,
    name VARCHAR(50) NOT NULL,
    id VARCHAR(50) NOT NULL,
    password VARCHAR(255) NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    gender BOOLEAN NOT NULL,
    role ENUM('ADMIN', 'USER') DEFAULT 'USER',
    profile_image_url VARCHAR(255) NULL,
    status ENUM('ACTIVE', 'DORMANT', 'WITHDRAWN') DEFAULT 'ACTIVE',
    provider ENUM('LOCAL', 'KAKAO', 'GOOGLE') DEFAULT 'LOCAL',
    provider_id VARCHAR(255) NULL,
    last_login_at DATETIME(6) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_user_login_id (id),
    UNIQUE KEY uk_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS category (
    category_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    name VARCHAR(50) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    color ENUM('RED', 'AMBER', 'YELLOW', 'LIME', 'GREEN', 'EMERALD', 'TEAL', 'CYAN', 'BLUE', 'PURPLE', 'PINK', 'SLATE') DEFAULT 'BLUE',
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment (
    payment_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    account_id INT UNSIGNED NULL,
    name VARCHAR(50) NOT NULL,
    type ENUM('CASH', 'CREDIT_CARD', 'CHECK_CARD', 'BANK') NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (payment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS budget (
    budget_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    category_id INT UNSIGNED NOT NULL,
    amount INT NOT NULL,
    budget_date DATE NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (budget_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fixed (
    fixed_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    category_id INT UNSIGNED NOT NULL,
    account_id INT UNSIGNED NOT NULL,
    title VARCHAR(100) NOT NULL,
    amount INT NOT NULL,
    fixed_day INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (fixed_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS card_installment_plan (
    installment_plan_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    category_id INT UNSIGNED NOT NULL,
    payment_id INT UNSIGNED NOT NULL,
    account_id INT UNSIGNED NOT NULL,
    title VARCHAR(100) NOT NULL,
    memo TEXT NULL,
    total_amount INT NOT NULL,
    installment_count INT UNSIGNED NOT NULL,
    is_interest_free TINYINT(1) NOT NULL DEFAULT 0,
    first_trading_at DATE NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    settled_count INT UNSIGNED NOT NULL DEFAULT 0,
    is_completed TINYINT(1) NOT NULL DEFAULT 0,
    last_settled_at DATETIME(6) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (installment_plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS transaction (
    transaction_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    category_id INT UNSIGNED NOT NULL,
    payment_id INT UNSIGNED NULL,
    account_id INT UNSIGNED NOT NULL,
    fixed_id INT UNSIGNED NULL,
    title VARCHAR(100) NOT NULL,
    amount INT NOT NULL,
    memo TEXT NULL,
    trading_at DATE NOT NULL,
    installment_plan_id INT UNSIGNED NULL,
    installment_no INT UNSIGNED NULL,
    installment_total_count INT UNSIGNED NULL,
    is_installment TINYINT(1) NOT NULL DEFAULT 0,
    is_settled TINYINT(1) NOT NULL DEFAULT 1,
    is_interest_free TINYINT(1) NOT NULL DEFAULT 0,
    settled_at DATETIME(6) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS transfer (
    transfer_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id INT UNSIGNED NOT NULL,
    from_account INT UNSIGNED NOT NULL,
    to_account INT UNSIGNED NOT NULL,
    amount INT NOT NULL,
    transfer_at DATE NOT NULL,
    memo TEXT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (transfer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS job_metadata (
    job_name VARCHAR(120) NOT NULL,
    job_group VARCHAR(120) NULL,
    cron_expression VARCHAR(120) NULL,
    description VARCHAR(255) NULL,
    is_active BOOLEAN NOT NULL,
    PRIMARY KEY (job_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS system_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    trace_id VARCHAR(255) NULL,
    service_name VARCHAR(255) NULL,
    method_name VARCHAR(255) NULL,
    request_params TEXT NULL,
    result TEXT NULL,
    execution_time BIGINT NULL,
    status VARCHAR(255) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS file_delete_task (
    task_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    file_url VARCHAR(1024) NOT NULL,
    reason VARCHAR(100) NULL,
    status ENUM('PENDING', 'FAILED') NOT NULL,
    retry_count INT NOT NULL,
    next_retry_at DATETIME(6) NOT NULL,
    last_error VARCHAR(1000) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
