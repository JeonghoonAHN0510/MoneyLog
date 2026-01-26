INSERT INTO user(name, id, password, email, phone, gender, role, created_at)
VALUES ('안정훈', 'wjdgnsdl1342', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'wjdgnsdl1342@naver.com',
        '010-5109-1342', '0', 'ADMIN', now());

INSERT INTO category(user_id, name, type, color, created_at)
VALUES ('1', '월급', 'INCOME', 'BLUE', now()),
       ('1', '기타', 'INCOME', 'RED', now()),
       ('1', '식비', 'EXPENSE', 'YELLOW', now()),
       ('1', '교통', 'EXPENSE', 'PINK', now());

INSERT INTO payment(user_id, name, type, created_at)
VALUES ('1', '현금', 'CASH', now()),
       ('1', '계좌이체', 'BANK', now()),
       ('1', 'KB나라사랑카드', 'CHECK_CARD', now()),
       ('1', '신한S20카드', 'CHECK_CARD', now());

INSERT INTO bank (code, name, logo_image_url, created_at)
VALUES ('001', '한국은행', '001_logo.png', now()),
       ('002', 'KDB산업은행', '002_logo.png', now()),
       ('003', 'IBK기업은행', '003_logo.png', now()),
       ('004', 'KB국민은행', '004_logo.png', now()),
       ('007', '수협은행', '007_logo.png', now()),
       ('011', 'NH농협은행', '011_logo.png', now()),
       ('020', '우리은행', '020_logo.png', now()),
       ('023', 'SC제일은행', '023_logo.png', now()),
       ('027', '한국씨티은행', '027_logo.png', now()),
       ('031', 'DGB대구은행', '031_logo.png', now()),
       ('032', 'BNK부산은행', '032_logo.png', now()),
       ('034', '광주은행', '034_logo.png', now()),
       ('035', '제주은행', '035_logo.png', now()),
       ('037', '전북은행', '036_logo.png', now()),
       ('039', 'BNK경남은행', '039_logo.png', now()),
       ('045', '새마을금고', '045_logo.png', now()),
       ('048', '신협', '048_logo.png', now()),
       ('050', '저축은행', '050_logo.png', now()),
       ('071', '우체국', '071_logo.png', now()),
       ('081', '하나은행', '081_logo.png', now()),
       ('088', '신한은행', '088_logo.png', now()),
       ('089', '케이뱅크', '089_logo.png', now()),
       ('090', '카카오뱅크', '090_logo.png', now()),
       ('092', '토스뱅크', '092_logo.png', now());

INSERT INTO account(user_id, bank_id, nickname, balance, account_number, type, color, created_at)
VALUES ('1', '70001', '국민계좌', '50000', '941602-00-581596', 'BANK', 'CYAN', now()),
       ('1', '70002', '신한계좌', '30000', '110-459-893115', 'BANK', 'BLUE', now()),
       ('1', '70003', '카카오계좌', '300000', '3333-11-2756766', 'BANK', 'YELLOW', now());

INSERT INTO budget(user_id, category_id, budget_date, amount, created_at)
VALUES ('1', '10003', '2025-12-22', '200000', now()),
       ('1', '10004', '2025-12-22', '80000', now());

INSERT INTO fixed(user_id, category_id, title, amount, fixed_day, start_date, created_at)
VALUES ('1', '10001', '월급', '2400000', '1', '2025-12-22', now());

INSERT INTO ledger(user_id, category_id, account_id, fixed_id, title, amount, trading_at, created_at)
VALUES ('1', '10001', '30003', '50001', '월급', '2400000', '2025-12-22', now());

INSERT INTO transfer(user_id, from_account, to_account, amount, transfer_at, memo, created_at)
VALUES ('1', '30001', '30002', '50000', '2025-12-22', '', now()),
       ('1', '30001', '30003', '40000', '2025-12-22', '테스트 이체', now());

INSERT INTO job_metadata (job_name, job_group, cron_expression, description, is_active)
VALUES ('logCleanupJob', 'system', '0 0 3 * * ?', '오래된 로그 자동 삭제', true);