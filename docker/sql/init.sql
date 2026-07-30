-- ============================================================
-- 保险AI知识平台 - 数据库初始化脚本
-- PostgreSQL 16+
-- ============================================================

-- 扩展：用于JSON类型
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ============================================================
-- 保险公司表
-- ============================================================
CREATE TABLE IF NOT EXISTS insurance_company (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    website VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_insurance_company_code ON insurance_company(code);
CREATE INDEX IF NOT EXISTS idx_insurance_company_deleted ON insurance_company(deleted);

-- ============================================================
-- 保险产品表
-- ============================================================
CREATE TABLE IF NOT EXISTS insurance_product (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    company_id BIGINT NOT NULL REFERENCES insurance_company(id),
    name VARCHAR(200) NOT NULL,
    product_code VARCHAR(100) UNIQUE,
    type VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description TEXT,
    properties JSONB,
    services JSONB,
    current_version_id BIGINT
);

CREATE INDEX IF NOT EXISTS idx_insurance_product_company_id ON insurance_product(company_id);
CREATE INDEX IF NOT EXISTS idx_insurance_product_deleted ON insurance_product(deleted);
CREATE INDEX IF NOT EXISTS idx_insurance_product_status ON insurance_product(status);
CREATE INDEX IF NOT EXISTS idx_insurance_product_product_code ON insurance_product(product_code);

-- ============================================================
-- 产品版本表
-- ============================================================
CREATE TABLE IF NOT EXISTS insurance_version (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    product_id BIGINT NOT NULL REFERENCES insurance_product(id),
    version_number VARCHAR(50) NOT NULL,
    version_type VARCHAR(10),
    version_description TEXT,
    effective_date DATE,
    expiry_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    previous_version_id BIGINT,
    sync_time TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_insurance_version_product_id ON insurance_version(product_id);
CREATE INDEX IF NOT EXISTS idx_insurance_version_deleted ON insurance_version(deleted);
CREATE INDEX IF NOT EXISTS idx_insurance_version_status ON insurance_version(status);

-- ============================================================
-- 保险文档表
-- ============================================================
CREATE TABLE IF NOT EXISTS insurance_document (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    version_id BIGINT NOT NULL REFERENCES insurance_version(id),
    document_type VARCHAR(50) NOT NULL,
    document_name VARCHAR(200) NOT NULL,
    file_url TEXT,
    file_hash VARCHAR(64),
    parse_status VARCHAR(20),
    created_time TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_insurance_document_version_id ON insurance_document(version_id);
CREATE INDEX IF NOT EXISTS idx_insurance_document_deleted ON insurance_document(deleted);
CREATE INDEX IF NOT EXISTS idx_insurance_document_document_type ON insurance_document(document_type);

-- ============================================================
-- 保障责任表
-- ============================================================
CREATE TABLE IF NOT EXISTS insurance_coverage (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    version_id BIGINT NOT NULL REFERENCES insurance_version(id),
    coverage_name VARCHAR(100) NOT NULL,
    coverage_desc TEXT,
    amount VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_insurance_coverage_version_id ON insurance_coverage(version_id);
CREATE INDEX IF NOT EXISTS idx_insurance_coverage_deleted ON insurance_coverage(deleted);

-- ============================================================
-- 免责条款表
-- ============================================================
CREATE TABLE IF NOT EXISTS insurance_exclusion (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    version_id BIGINT NOT NULL REFERENCES insurance_version(id),
    exclusion_name VARCHAR(100) NOT NULL,
    content TEXT
);

CREATE INDEX IF NOT EXISTS idx_insurance_exclusion_version_id ON insurance_exclusion(version_id);
CREATE INDEX IF NOT EXISTS idx_insurance_exclusion_deleted ON insurance_exclusion(deleted);

-- ============================================================
-- 投保规则表
-- ============================================================
CREATE TABLE IF NOT EXISTS insurance_underwriting_rule (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    version_id BIGINT NOT NULL REFERENCES insurance_version(id),
    rule_type VARCHAR(50) NOT NULL,
    rule_value TEXT
);

CREATE INDEX IF NOT EXISTS idx_insurance_underwriting_rule_version_id ON insurance_underwriting_rule(version_id);
CREATE INDEX IF NOT EXISTS idx_insurance_underwriting_rule_deleted ON insurance_underwriting_rule(deleted);

-- ============================================================
-- 续保规则表
-- ============================================================
CREATE TABLE IF NOT EXISTS insurance_renewal_rule (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    version_id BIGINT NOT NULL REFERENCES insurance_version(id),
    renewal_type VARCHAR(50) NOT NULL,
    content TEXT
);

CREATE INDEX IF NOT EXISTS idx_insurance_renewal_rule_version_id ON insurance_renewal_rule(version_id);
CREATE INDEX IF NOT EXISTS idx_insurance_renewal_rule_deleted ON insurance_renewal_rule(deleted);

-- ============================================================
-- 文档分块表（用于RAG向量检索）
-- ============================================================
CREATE TABLE IF NOT EXISTS insurance_document_chunk (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    document_id BIGINT NOT NULL REFERENCES insurance_document(id),
    page_no INTEGER,
    content TEXT,
    embedding vector(1536)
);

CREATE INDEX IF NOT EXISTS idx_insurance_document_chunk_document_id ON insurance_document_chunk(document_id);
CREATE INDEX IF NOT EXISTS idx_insurance_document_chunk_deleted ON insurance_document_chunk(deleted);

-- 创建向量索引（用于相似度搜索）
CREATE INDEX IF NOT EXISTS idx_insurance_document_chunk_embedding 
ON insurance_document_chunk USING ivfflat (embedding vector_cosine_ops);

-- ============================================================
-- 文档文件表（文件存储）
-- ============================================================
CREATE TABLE IF NOT EXISTS document_file (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    product_id BIGINT NOT NULL REFERENCES insurance_product(id),
    version_id BIGINT REFERENCES insurance_version(id),
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    hash VARCHAR(64) NOT NULL,
    document_type VARCHAR(20) DEFAULT 'OTHER',
    minio_bucket VARCHAR(100) NOT NULL,
    minio_object_key VARCHAR(500) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_document_file_product_id ON document_file(product_id);
CREATE INDEX IF NOT EXISTS idx_document_file_version_id ON document_file(version_id);
CREATE INDEX IF NOT EXISTS idx_document_file_deleted ON document_file(deleted);

-- ============================================================
-- 文档解析结果表
-- ============================================================
CREATE TABLE IF NOT EXISTS document_parse_result (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    file_id BIGINT NOT NULL REFERENCES document_file(id),
    product_id BIGINT NOT NULL REFERENCES insurance_product(id),
    parse_data JSONB NOT NULL,
    ai_summary TEXT,
    parse_time TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX IF NOT EXISTS idx_document_parse_result_file_id ON document_parse_result(file_id);
CREATE INDEX IF NOT EXISTS idx_document_parse_result_product_id ON document_parse_result(product_id);
CREATE INDEX IF NOT EXISTS idx_document_parse_result_deleted ON document_parse_result(deleted);

-- ============================================================
-- 同步日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS sync_log (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    company_id BIGINT,
    company_name VARCHAR(100),
    product_id BIGINT,
    product_name VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    message TEXT,
    sync_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    duration_ms BIGINT,
    files_downloaded INTEGER DEFAULT 0,
    files_skipped INTEGER DEFAULT 0,
    files_failed INTEGER DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_sync_log_company_id ON sync_log(company_id);
CREATE INDEX IF NOT EXISTS idx_sync_log_product_id ON sync_log(product_id);
CREATE INDEX IF NOT EXISTS idx_sync_log_deleted ON sync_log(deleted);
CREATE INDEX IF NOT EXISTS idx_sync_log_status ON sync_log(status);
CREATE INDEX IF NOT EXISTS idx_sync_log_sync_time ON sync_log(sync_time);

-- ============================================================
-- 插入初始数据
-- ============================================================
INSERT INTO insurance_company (name, code, type, website) VALUES
('中国平安保险', 'PAIC', 'PROPERTY', 'https://www.pingan.com'),
('中国人寿保险', 'CHINA_LIFE', 'LIFE', 'https://www.chinalife.com.cn'),
('中国太平洋保险', 'CPIC', 'PROPERTY', 'https://www.cpic.com.cn'),
('中国人民保险', 'PICC', 'PROPERTY', 'https://www.picc.com'),
('新华人寿保险', 'NEW_CHINA', 'LIFE', 'https://www.newchinalife.com')
ON CONFLICT (code) DO NOTHING;

-- 插入平安健康保险产品
INSERT INTO insurance_product (company_id, name, product_code, type, status, description) VALUES
((SELECT id FROM insurance_company WHERE code = 'PAIC'), '平安e生保长期医疗', 'PAIC_E_HEALTH_001', 'HEALTH', 'ACTIVE', '平安e生保长期医疗保险，保证续保20年')
ON CONFLICT (product_code) DO NOTHING;

-- ============================================================
-- 创建更新触发器
-- ============================================================
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
DECLARE
    table_name TEXT;
BEGIN
    FOR table_name IN (SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name IN (
        'insurance_company', 'insurance_product', 'insurance_version', 
        'insurance_document', 'insurance_coverage', 'insurance_exclusion',
        'insurance_underwriting_rule', 'insurance_renewal_rule', 
        'insurance_document_chunk', 'document_file', 'document_parse_result', 'sync_log'
    )) LOOP
        EXECUTE format('CREATE TRIGGER update_%I_modtime BEFORE UPDATE ON %I FOR EACH ROW EXECUTE FUNCTION update_modified_column()', table_name, table_name);
    END LOOP;
EXCEPTION
    WHEN duplicate_object THEN
        NULL;
END $$;

COMMIT;

-- ============================================================
-- 初始化完成
-- ============================================================
SELECT 'Database initialization completed successfully!' AS message;