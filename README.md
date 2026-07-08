# java-llm-production-ready

企业级 Java 大模型应用生产脚手架 —— 帮企业把 AI 从 Demo 变成可监控、可扩展、可运维的生产系统。

## 技术栈

- **基础框架**: Spring Boot 3.2.x
- **AI 集成**: LangChain4j 0.31
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
├── deploy/              # 部署配置（Prometheus、Grafana）
└── docker-compose.yml   # 一键启动基础设施
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
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

### 3. 启动应用

```bash
mvn clean install -DskipTests
mvn spring-boot:run -pl llm-service
```

### 4. 访问服务

- API 文档: http://localhost:8080/swagger-ui.html
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

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
# 同步问答
curl -X POST http://localhost:8080/api/v1/rag/query \
  -H "Content-Type: application/json" \
  -d '{"question": "文档主要内容是什么？", "knowledgeBaseId": "kb_demo"}'

# 流式问答（SSE）
curl -X POST http://localhost:8080/api/v1/rag/query/stream \
  -H "Content-Type: application/json" \
  -d '{"question": "文档主要内容是什么？", "knowledgeBaseId": "kb_demo"}'
```

## 核心特性

- **RAG Pipeline**: 文档上传 → 解析分块 → 向量化 → 存储 → 检索增强问答
- **生产级防护**: 限流、熔断、重试、超时、内容安全审核
- **完整可观测性**: OpenTelemetry 全链路追踪 + Prometheus 指标 + Grafana 看板
- **多模型支持**: 通过配置切换 OpenAI / 通义千问 / 本地模型

## License

Apache 2.0