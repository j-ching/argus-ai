# Argus AI 项目

## 项目概述

Argus AI 是一个基于Spring Boot 3.4.5和Spring AI 1.0.0-M8的AI服务项目，旨在提供一个使用Ollama模型与SSE协议的大模型通话服务。该项目允许用户通过SSE协议实时接收大模型的响应，并提供了一个简单的前端示例来展示如何使用该服务。

## 主要功能

- 基于Ollama模型的大模型通话服务
- 使用SSE协议实现实时响应
- 提供简单的前端示例

## 技术栈

- Spring Boot 3.4.5
- Spring AI 1.0.0-M8
- Ollama 模型
- SSE 协议

## 快速开始

### 克隆项目
```aiignore
git clone https://github.com/your-username/argus-ai.git
```

### 安装依赖
确保您已经安装了Java 17和Maven。然后运行以下命令来安装项目依赖：

### 配置Ollama模型
1. 下载并安装Ollama模型：Ollama官网
2. 启动Ollama服务
3. 在application.properties文件中配置Ollama的API地址： 
```aiignore
  ollama.api.url=http://localhost:11434
```

### 运行项目
```aiignore
 mvn spring-boot:run
```
### 访问前端示例
打开浏览器并访问以下URL：http://localhost:8080
```
curl -X GET http://localhost:8080/api/chat
```

## 项目结构
```
argus-ai/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── argusai/
│   │   │           ├── controller/
│   │   │           ├── service/
│   │   │           └── config/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           └── index.html
│   └── test/
│       └── java/
│           └── com/
│               └── argusai/
│                   └── ArgusAITests.java
├── pom.xml
└── README.md

```

## 配置说明

### Server configuration
```aiignore
server.port=8080
```

### Ollama API configuration
```aiignore
ollama.api.url=http://localhost:11434
```

### SSE configuration
```
spring.mvc.async.request-timeout=30000
```