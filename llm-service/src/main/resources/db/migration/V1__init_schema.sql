-- 知识库表
CREATE TABLE IF NOT EXISTS knowledge_base (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    embedding_model VARCHAR(100) NOT NULL DEFAULT 'text-embedding-3-small',
    milvus_collection VARCHAR(100) NOT NULL,
    tenant_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 文档表
CREATE TABLE IF NOT EXISTS document (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    knowledge_base_id UUID NOT NULL REFERENCES knowledge_base(id) ON DELETE CASCADE,
    file_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000),
    file_size BIGINT NOT NULL DEFAULT 0,
    chunk_count INT DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 对话表
CREATE TABLE IF NOT EXISTS conversation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(100) NOT NULL,
    knowledge_base_id UUID REFERENCES knowledge_base(id),
    title VARCHAR(500),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 消息表
CREATE TABLE IF NOT EXISTS message (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversation(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    token_usage JSONB,
    sources JSONB,
    latency_ms INT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Token使用统计表
CREATE TABLE IF NOT EXISTS token_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(100) NOT NULL,
    tenant_id VARCHAR(100),
    date DATE NOT NULL,
    model VARCHAR(100) NOT NULL,
    input_tokens BIGINT NOT NULL DEFAULT 0,
    output_tokens BIGINT NOT NULL DEFAULT 0,
    total_tokens BIGINT GENERATED ALWAYS AS (input_tokens + output_tokens) STORED,
    cost DECIMAL(10,4),
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, date, model)
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_kb_tenant ON knowledge_base(tenant_id);
CREATE INDEX IF NOT EXISTS idx_doc_kb ON document(knowledge_base_id);
CREATE INDEX IF NOT EXISTS idx_doc_status ON document(status);
CREATE INDEX IF NOT EXISTS idx_conv_user ON conversation(user_id);
CREATE INDEX IF NOT EXISTS idx_msg_conv ON message(conversation_id);
CREATE INDEX IF NOT EXISTS idx_usage_user_date ON token_usage(user_id, date);