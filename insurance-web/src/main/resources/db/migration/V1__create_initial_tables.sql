
-- Create insurance_company table
CREATE TABLE insurance_company (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    website VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0
);

-- Create indexes for insurance_company
CREATE INDEX idx_company_name ON insurance_company(name);
CREATE UNIQUE INDEX uk_company_code ON insurance_company(code);

-- Create insurance_product table
CREATE TABLE insurance_product (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES insurance_company(id),
    name VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0
);

-- Create indexes for insurance_product
CREATE INDEX idx_product_company ON insurance_product(company_id);
CREATE INDEX idx_product_type ON insurance_product(type);
CREATE INDEX idx_product_name ON insurance_product(name);

-- Create insurance_version table
CREATE TABLE insurance_version (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES insurance_product(id),
    version_number VARCHAR(50) NOT NULL,
    hash VARCHAR(64) NOT NULL,
    download_url VARCHAR(500) NOT NULL,
    sync_time TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0
);

-- Create indexes for insurance_version
CREATE INDEX idx_version_product ON insurance_version(product_id);
CREATE UNIQUE INDEX uk_version_product_hash ON insurance_version(product_id, hash);

-- Create document_file table
CREATE TABLE document_file (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES insurance_product(id),
    version_id BIGINT REFERENCES insurance_version(id),
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    hash VARCHAR(64) NOT NULL,
    document_type VARCHAR(20) DEFAULT 'OTHER',
    minio_bucket VARCHAR(100) NOT NULL,
    minio_object_key VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0
);

-- Create indexes for document_file
CREATE INDEX idx_file_product ON document_file(product_id);
CREATE INDEX idx_file_version ON document_file(version_id);
CREATE UNIQUE INDEX uk_file_hash ON document_file(hash);
CREATE INDEX idx_file_type ON document_file(document_type);

-- Create document_parse_result table
CREATE TABLE document_parse_result (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL REFERENCES document_file(id),
    product_id BIGINT NOT NULL REFERENCES insurance_product(id),
    parse_data JSONB NOT NULL,
    ai_summary TEXT,
    parse_time TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0
);

-- Create indexes for document_parse_result
CREATE INDEX idx_parse_file ON document_parse_result(file_id);
CREATE INDEX idx_parse_product ON document_parse_result(product_id);
CREATE INDEX idx_parse_status ON document_parse_result(status);

-- Create sync_log table
CREATE TABLE sync_log (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT REFERENCES insurance_company(id),
    company_name VARCHAR(100),
    product_id BIGINT REFERENCES insurance_product(id),
    product_name VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    message TEXT,
    sync_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    duration_ms BIGINT,
    files_downloaded INT DEFAULT 0,
    files_skipped INT DEFAULT 0,
    files_failed INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for sync_log
CREATE INDEX idx_sync_company ON sync_log(company_id);
CREATE INDEX idx_sync_status ON sync_log(status);
CREATE INDEX idx_sync_time ON sync_log(sync_time);

-- Insert initial insurance companies
INSERT INTO insurance_company (name, code, type, website) VALUES
('平安保险', 'PA', 'PING_AN', 'https://www.pingan.com'),
('太平洋保险', 'PC', 'PACIFIC', 'https://www.cpic.com.cn'),
('中国人保', 'PI', 'PICC', 'https://www.picc.com'),
('阳光保险', 'SS', 'SUNSHINE', 'https://www.sunshine.com');