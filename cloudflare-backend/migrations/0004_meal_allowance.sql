-- 0004_meal_allowance.sql
-- Add meal allowance (2 meals/day * 3 days = 6) and current meals balance to users table

ALTER TABLE users ADD COLUMN meal_allowance INTEGER DEFAULT 6;
ALTER TABLE users ADD COLUMN meals_balance INTEGER DEFAULT 6;

-- Initialize existing delegates to the 6-meal conference default
UPDATE users SET meal_allowance = 6, meals_balance = 6 WHERE meal_allowance IS NULL OR meals_balance IS NULL;
