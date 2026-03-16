SELECT account_number, COUNT(*) AS duplicate_count
FROM account
WHERE account_number IS NOT NULL
GROUP BY account_number
HAVING COUNT(*) > 1;

SELECT user_id, name, type, COUNT(*) AS duplicate_count
FROM category
GROUP BY user_id, name, type
HAVING COUNT(*) > 1;

SELECT user_id, category_id, COUNT(*) AS duplicate_count
FROM budget
GROUP BY user_id, category_id
HAVING COUNT(*) > 1;

SELECT table_name, column_name
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name IN ('category', 'payment', 'budget', 'job_metadata')
  AND column_name = 'version';

SELECT table_name, index_name
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND (
      (table_name = 'account' AND index_name = 'uk_account_account_number')
   OR (table_name = 'category' AND index_name = 'uk_category_user_name_type')
   OR (table_name = 'budget' AND index_name = 'uk_budget_user_category')
  );
