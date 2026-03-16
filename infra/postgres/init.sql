CREATE SCHEMA IF NOT EXISTS arm_support;

GRANT ALL PRIVILEGES ON SCHEMA arm_support TO admin;

ALTER USER admin SET search_path TO arm_support;