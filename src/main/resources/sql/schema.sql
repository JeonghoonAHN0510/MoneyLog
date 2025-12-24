alter table category auto_increment = 10001;
alter table payment auto_increment = 20001;
alter table account auto_increment = 30001;
alter table budget auto_increment = 40001;
alter table fixed auto_increment = 50001;
alter table transfer auto_increment = 60001;
alter table bank auto_increment = 70001;
alter table loan auto_increment = 80001;
alter table ledger auto_increment = 100001;

CREATE INDEX idx_ledger_user_date ON ledger(user_id, trading_at);