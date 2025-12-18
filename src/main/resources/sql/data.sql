INSERT INTO user(name, id, password, email, phone, gender, created_at)
    VALUES ('안정훈', 'wjdgnsdl1342', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'wjdgnsdl1342@naver.com', '010-5109-1342', '0', now());

INSERT INTO category(user_id, name, type, created_at)
    VALUES ('1', '월급', 'INCOME', now()),
           ('1', '기타', 'INCOME', now()),
           ('1', '식비', 'EXPENSE', now()),
           ('1', '교통', 'EXPENSE', now());

INSERT INTO payment(user_id, name, type, created_at)
    VALUES ('1', '현금', 'CASH', now()),
           ('1', '계좌이체', 'BANK', now()),
           ('1', 'KB나라사랑카드', 'CHECK_CARD', now()),
           ('1', '신한S20카드', 'CHECK_CARD', now());

INSERT INTO bank(name, code, created_at)
    VALUES ('국민은행', '004', now()),
           ('신한은행', '088', now()),
           ('카카오뱅크', '090', now());

INSERT INTO account(user_id, bank_id, nickname, balance, account_number, created_at)
    VALUES ('1', '70001', '국민계좌', '50000', '941602-00-581596', now()),
           ('1', '70002', '신한계좌', '30000', '110-459-893115', now()),
           ('1', '70003', '카카오계좌', '300000', '3333-11-2756766', now());