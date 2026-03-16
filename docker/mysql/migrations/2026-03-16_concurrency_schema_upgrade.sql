ALTER TABLE category
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE payment
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE budget
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE job_metadata
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE account
    ADD CONSTRAINT uk_account_account_number UNIQUE (account_number);

ALTER TABLE category
    ADD CONSTRAINT uk_category_user_name_type UNIQUE (user_id, name, type);

ALTER TABLE budget
    ADD CONSTRAINT uk_budget_user_category UNIQUE (user_id, category_id);

DROP INDEX idx_account_number ON account;

DROP INDEX idx_budget_user_category ON budget;
