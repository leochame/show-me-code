-- 死锁示例：两个事务以不同顺序访问相同资源

CREATE TABLE accounts (
    id INT PRIMARY KEY,
    balance DECIMAL(10, 2)
);

INSERT INTO accounts VALUES (1, 1000.00);
INSERT INTO accounts VALUES (2, 2000.00);

-- 会话1
BEGIN TRANSACTION;
UPDATE accounts SET balance = balance - 100 WHERE id = 1;
UPDATE accounts SET balance = balance + 100 WHERE id = 2;
COMMIT;

-- 会话2（同时执行，不同顺序）
BEGIN TRANSACTION;
UPDATE accounts SET balance = balance - 200 WHERE id = 2;
UPDATE accounts SET balance = balance + 200 WHERE id = 1;
COMMIT;

