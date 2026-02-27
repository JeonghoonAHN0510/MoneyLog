alter table category auto_increment = 10001;
alter table payment auto_increment = 20001;
alter table account auto_increment = 30001;
alter table budget auto_increment = 40001;
alter table fixed auto_increment = 50001;
alter table transfer auto_increment = 60001;
alter table bank auto_increment = 70001;
alter table transaction auto_increment = 100001;

CREATE INDEX idx_transaction_user_date ON transaction(user_id, trading_at);

CREATE TABLE IF NOT EXISTS file_delete_task (
    task_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    file_url VARCHAR(1024) NOT NULL,
    reason VARCHAR(100),
    status ENUM('PENDING', 'FAILED') NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_at DATETIME(6) NOT NULL,
    last_error VARCHAR(1000),
    created_at DATETIME(6),
    updated_at DATETIME(6)
);

CREATE INDEX idx_file_delete_task_status_retry
    ON file_delete_task(status, next_retry_at);
