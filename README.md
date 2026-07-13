# java-llm-production-ready

企业级 Java 大模型应用生产脚手架 —— 帮企业把 AI 从 Demo 变成可监控、可扩展、可运维的生产系统。

## 技术栈

- **基础框架**: Spring Boot 3.2.x
- **AI 集成**: LangChain4j 1.16.2
- **前端**: Vue 3 + TypeScript + Vite + Tailwind CSS
- **向量数据库**: Milvus 2.4
- **缓存**: Redis 7
- **主数据库**: PostgreSQL 16
- **文档解析**: Apache Tika
- **防护层**: Resilience4j（熔断、重试、降级）
- **监控**: Prometheus + Grafana
- **API 文档**: Knife4j

## 项目结构

```
java-llm-production-ready/
├── llm-common/          # 公共模块（工具类、异常处理、统一返回）
├── llm-service/         # 核心业务模块（RAG、文档处理、防护层）
├── llm-gateway/         # 网关模块（鉴权、限流、路由）
├── frontend/            # Vue 3 前端（智能问答、知识库管理）
├── docs/                # 技术文档
├── deploy/              # 部署配置（Prometheus、Grafana）
└── docker-compose.yml   # 一键启动基础设施
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- Node.js 18+
- Docker 24+

### 1. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 填入你的 OpenAI API Key
```

### 2. 启动基础设施

```bash
docker-compose up -d milvus redis postgres
```

### 3. 启动后端

```bash
mvn clean install -DskipTests
mvn spring-boot:run -pl llm-service
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 5. 访问服务

- 前端页面: http://localhost:3000
- API 文档: http://localhost:8080/swagger-ui.html
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3001 (admin/admin)

## API 接口

### 文档管理

```bash
# 上传文档
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@test.pdf" \
  -F "knowledgeBaseId=kb_demo"
```

### RAG 问答

```bash
# 基础问答
curl -X POST http://localhost:8080/api/v1/rag/query \
  -H "Content-Type: application/json" \
  -d '{"question": "文档主要内容是什么？", "knowledgeBaseId": "kb_demo"}'

# 流式问答（SSE）
curl -X POST http://localhost:8080/api/v1/rag/query/stream \
  -H "Content-Type: application/json" \
  -d '{"question": "文档主要内容是什么？", "knowledgeBaseId": "kb_demo"}'

# Advanced RAG 问答（查询扩展 + 多轮对话 + 重排序）
curl -X POST http://localhost:8080/api/v1/rag/query/stream \
  -H "Content-Type: application/json" \
  -d '{
    "question": "继续解释上一点",
    "knowledgeBaseId": "kb_demo",
    "topK": 5,
    "enableQueryExpansion": true,
    "enableRerank": false,
    "conversationId": "conv-001",
    "historyMessages": [
      {"role": "user", "content": "什么是RAG？"},
      {"role": "assistant", "content": "RAG是检索增强生成..."}
    ],
    "metadataFilters": {"userId": "12345"}
  }'
```

### Advanced RAG 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `question` | String | 是 | 用户问题 |
| `knowledgeBaseId` | String | 是 | 知识库ID（数据隔离） |
| `topK` | Integer | 否 | 向量检索返回的最大结果数（默认5） |
| `temperature` | Double | 否 | LLM 生成温度（默认0.7） |
| `enableQueryExpansion` | Boolean | 否 | 查询扩展：生成多个查询变体提升召回率（默认true） |
| `enableRerank` | Boolean | 否 | 重排序：使用 Cohere ScoringModel 精确重排（需配置 COHERE_API_KEY） |
| `conversationId` | String | 否 | 会话ID，用于多轮对话上下文关联 |
| `historyMessages` | ChatMessage[] | 否 | 历史消息，用于查询压缩（多轮对话） |
| `metadataFilters` | Map | 否 | 元数据过滤条件，如 `{"userId": "12345"}` |

> **向后兼容**：不传 Advanced RAG 参数时，自动使用 Naive RAG Pipeline。

## 核心特性

### RAG Pipeline

- **Naive RAG**: 文档上传 → 解析分块 → 向量化 → 存储 → 检索增强问答
- **Advanced RAG**（基于 [LangChain4j Advanced RAG](https://docs.langchain4j.dev/tutorials/rag/#advanced-rag)）：
  - **查询转换**：查询压缩（多轮对话上下文）+ 查询扩展（多查询变体提升召回率）
  - **内容检索**：向量语义搜索 + 动态元数据过滤（按 knowledgeBaseId 等条件）
  - **内容聚合**：RRF 融合排序（默认）/ Cohere 重排序（可选）
  - **内容注入**：检索内容 + 元数据（文件名、来源等）注入 Prompt

### 生产级防护

- 输入安全审核（长度限制、Prompt 注入检测）
- 输出内容审核
- 限流、熔断、重试、超时

### 完整可观测性

- OpenTelemetry 全链路追踪
- Prometheus 指标 + Grafana 看板

### 多模型支持

- 通过配置切换 OpenAI / 通义千问 / 本地模型
- 重排序模型可选（Cohere）

## License

Apache 2.0