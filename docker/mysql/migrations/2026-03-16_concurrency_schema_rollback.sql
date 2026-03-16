ALTER TABLE budget
    DROP INDEX uk_budget_user_category;

ALTER TABLE category
    DROP INDEX uk_category_user_name_type;

ALTER TABLE account
    DROP INDEX uk_account_account_number;

CREATE INDEX idx_account_number ON account(account_number);

CREATE INDEX idx_budget_user_category ON budget(user_id, category_id);

ALTER TABLE job_metadata
    DROP COLUMN version;

ALTER TABLE budget
    DROP COLUMN version;

ALTER TABLE payment
    DROP COLUMN version;

ALTER TABLE category
    DROP COLUMN version;
