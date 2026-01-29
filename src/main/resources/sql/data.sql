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

INSERT INTO job_metadata (job_name, job_group, cron_expression, description, is_active)
VALUES ('logCleanupJob', 'system', '0 0 3 * * ?', '오래된 로그 자동 삭제', true);