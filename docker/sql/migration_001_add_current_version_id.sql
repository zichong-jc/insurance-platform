-- ============================================================
-- 数据库迁移脚本 001
-- 添加 current_version_id 字段到 insurance_product 表
-- ============================================================

-- 检查字段是否存在，如果不存在则添加
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'insurance_product'
        AND column_name = 'current_version_id'
    ) THEN
        ALTER TABLE insurance_product ADD COLUMN current_version_id BIGINT;
        RAISE NOTICE 'Added current_version_id column to insurance_product table';
    ELSE
        RAISE NOTICE 'current_version_id column already exists';
    END IF;
END $$;