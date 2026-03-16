alter table category auto_increment = 10001;
alter table category add column if not exists version bigint not null default 0;
alter table payment auto_increment = 20001;
alter table payment add column if not exists version bigint not null default 0;
alter table account auto_increment = 30001;
alter table budget auto_increment = 40001;
alter table budget add column if not exists version bigint not null default 0;
alter table fixed auto_increment = 50001;
alter table transfer auto_increment = 60001;
alter table bank auto_increment = 70001;
alter table transaction auto_increment = 100001;
alter table job_metadata add column if not exists version bigint not null default 0;

CREATE INDEX idx_transaction_user_date ON transaction(user_id, trading_at);
