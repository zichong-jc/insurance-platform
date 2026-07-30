-- ============================================================
-- 保险AI知识平台 - 数据库初始化脚本
-- PostgreSQL 16+
-- ============================================================

BEGIN;

-- ============================================================
-- 扩展
-- ============================================================

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


CREATE INDEX IF NOT EXISTS idx_insurance_company_code
    ON insurance_company(code);

CREATE INDEX IF NOT EXISTS idx_insurance_company_deleted
    ON insurance_company(deleted);



-- ============================================================
-- 保险产品表
-- ============================================================

CREATE TABLE IF NOT EXISTS insurance_product (

                                                 id BIGSERIAL PRIMARY KEY,

                                                 created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                                 deleted BOOLEAN NOT NULL DEFAULT FALSE,
                                                 version INTEGER NOT NULL DEFAULT 0,


                                                 company_id BIGINT NOT NULL
                                                 REFERENCES insurance_company(id),


    name VARCHAR(200) NOT NULL,

    type VARCHAR(20) NOT NULL DEFAULT 'OTHER',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',


    CONSTRAINT uk_product_company_name
    UNIQUE(company_id, name)

    );



CREATE INDEX IF NOT EXISTS idx_insurance_product_company_id
    ON insurance_product(company_id);


CREATE INDEX IF NOT EXISTS idx_insurance_product_deleted
    ON insurance_product(deleted);


CREATE INDEX IF NOT EXISTS idx_insurance_product_status
    ON insurance_product(status);



-- ============================================================
-- 产品版本表
-- ============================================================

CREATE TABLE IF NOT EXISTS insurance_version (

                                                 id BIGSERIAL PRIMARY KEY,


                                                 created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,


                                                 deleted BOOLEAN NOT NULL DEFAULT FALSE,
                                                 version INTEGER NOT NULL DEFAULT 0,


                                                 product_id BIGINT NOT NULL
                                                 REFERENCES insurance_product(id),


    version_number VARCHAR(50) NOT NULL,

    hash VARCHAR(64) NOT NULL,

    download_url VARCHAR(500) NOT NULL,

    sync_time TIMESTAMP WITH TIME ZONE,

                            status VARCHAR(20) NOT NULL DEFAULT 'PENDING'

    );



CREATE INDEX IF NOT EXISTS idx_insurance_version_product_id
    ON insurance_version(product_id);


CREATE INDEX IF NOT EXISTS idx_insurance_version_deleted
    ON insurance_version(deleted);


CREATE INDEX IF NOT EXISTS idx_insurance_version_status
    ON insurance_version(status);




-- ============================================================
-- 文档文件表
-- ============================================================

CREATE TABLE IF NOT EXISTS document_file (

                                             id BIGSERIAL PRIMARY KEY,


                                             created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                             updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,


                                             deleted BOOLEAN NOT NULL DEFAULT FALSE,

                                             version INTEGER NOT NULL DEFAULT 0,


                                             product_id BIGINT NOT NULL
                                             REFERENCES insurance_product(id),


    version_id BIGINT
    REFERENCES insurance_version(id),



    file_name VARCHAR(255) NOT NULL,

    file_path VARCHAR(500) NOT NULL,

    file_size BIGINT NOT NULL,


    hash VARCHAR(64) NOT NULL,


    document_type VARCHAR(20)
    DEFAULT 'OTHER',


    minio_bucket VARCHAR(100) NOT NULL,

    minio_object_key VARCHAR(500) NOT NULL

    );



CREATE INDEX IF NOT EXISTS idx_document_file_product_id
    ON document_file(product_id);


CREATE INDEX IF NOT EXISTS idx_document_file_version_id
    ON document_file(version_id);


CREATE INDEX IF NOT EXISTS idx_document_file_deleted
    ON document_file(deleted);


CREATE INDEX IF NOT EXISTS idx_document_file_hash
    ON document_file(hash);





-- ============================================================
-- 文档解析结果表
-- ============================================================

CREATE TABLE IF NOT EXISTS document_parse_result (

                                                     id BIGSERIAL PRIMARY KEY,


                                                     created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                                     updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,


                                                     deleted BOOLEAN NOT NULL DEFAULT FALSE,

                                                     version INTEGER NOT NULL DEFAULT 0,


                                                     file_id BIGINT NOT NULL
                                                     REFERENCES document_file(id),


    product_id BIGINT NOT NULL
    REFERENCES insurance_product(id),



    parse_data JSONB NOT NULL,


    ai_summary TEXT,


    parse_time TIMESTAMP WITH TIME ZONE,


                             status VARCHAR(20) NOT NULL DEFAULT 'PENDING'

    );



CREATE INDEX IF NOT EXISTS idx_document_parse_result_file_id
    ON document_parse_result(file_id);


CREATE INDEX IF NOT EXISTS idx_document_parse_result_product_id
    ON document_parse_result(product_id);


CREATE INDEX IF NOT EXISTS idx_document_parse_result_deleted
    ON document_parse_result(deleted);


CREATE INDEX IF NOT EXISTS idx_document_parse_result_status
    ON document_parse_result(status);





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



CREATE INDEX IF NOT EXISTS idx_sync_log_company_id
    ON sync_log(company_id);


CREATE INDEX IF NOT EXISTS idx_sync_log_product_id
    ON sync_log(product_id);


CREATE INDEX IF NOT EXISTS idx_sync_log_deleted
    ON sync_log(deleted);


CREATE INDEX IF NOT EXISTS idx_sync_log_status
    ON sync_log(status);


CREATE INDEX IF NOT EXISTS idx_sync_log_sync_time
    ON sync_log(sync_time);





-- ============================================================
-- 初始化保险公司数据
-- ============================================================

INSERT INTO insurance_company
(
    name,
    code,
    type,
    website
)
VALUES

    ('中国平安保险','PAIC','PROPERTY','https://www.pingan.com'),

    ('中国人寿保险','CHINA_LIFE','LIFE','https://www.chinalife.com.cn'),

    ('中国太平洋保险','CPIC','PROPERTY','https://www.cpic.com.cn'),

    ('中国人民保险','PICC','PROPERTY','https://www.picc.com'),

    ('新华人寿保险','NEW_CHINA','LIFE','https://www.newchinalife.com')

    ON CONFLICT(code)
DO NOTHING;





-- ============================================================
-- 初始化产品数据
-- ============================================================

INSERT INTO insurance_product
(
    company_id,
    name,
    type,
    status
)

VALUES

    (
        (SELECT id FROM insurance_company WHERE code='PAIC'),
        '平安福终身寿险',
        'LIFE',
        'ACTIVE'
    ),

    (
        (SELECT id FROM insurance_company WHERE code='PAIC'),
        '平安车险',
        'PROPERTY',
        'ACTIVE'
    ),

    (
        (SELECT id FROM insurance_company WHERE code='CHINA_LIFE'),
        '国寿福',
        'LIFE',
        'ACTIVE'
    ),

    (
        (SELECT id FROM insurance_company WHERE code='CPIC'),
        '太平洋车险',
        'PROPERTY',
        'ACTIVE'
    ),

    (
        (SELECT id FROM insurance_company WHERE code='PICC'),
        '人保健康险',
        'HEALTH',
        'ACTIVE'
    )


    ON CONFLICT(company_id,name)
DO NOTHING;





-- ============================================================
-- 更新时间函数
-- ============================================================

CREATE OR REPLACE FUNCTION update_modified_column()

RETURNS TRIGGER AS $$

BEGIN

    NEW.updated_at = CURRENT_TIMESTAMP;

RETURN NEW;

END;

$$ LANGUAGE plpgsql;





-- ============================================================
-- 自动创建更新时间触发器
-- ============================================================


DO $$

DECLARE

tbl_name TEXT;


BEGIN


FOR tbl_name IN

SELECT t.table_name

FROM information_schema.tables t

WHERE t.table_schema='public'

  AND t.table_name IN

      (
       'insurance_company',
       'insurance_product',
       'insurance_version',
       'document_file',
       'document_parse_result',
       'sync_log'
          )


    LOOP


        EXECUTE format(
            'DROP TRIGGER IF EXISTS update_%I_modtime ON %I',
            tbl_name,
            tbl_name
        );


EXECUTE format(
        'CREATE TRIGGER update_%I_modtime
         BEFORE UPDATE ON %I
         FOR EACH ROW
         EXECUTE FUNCTION update_modified_column()',
        tbl_name,
        tbl_name
        );


END LOOP;


END $$;




COMMIT;



-- ============================================================
-- 初始化完成
-- ============================================================

SELECT 'Database initialization completed successfully!' AS message;