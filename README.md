# 大数据实时营销系统 (Bigdata Realtime Marketing)

## 项目简介
本项目是一个基于大数据技术的实时营销系统，通过对用户行为数据的实时分析和处理，为企业提供精准的营销决策支持。系统利用实时计算引擎，根据预设的规则模型，对用户行为进行实时判断，并触发相应的营销策略，从而提高营销效率和用户体验。

## 系统架构

### 整体架构
该系统采用模块化设计，主要包含以下几个核心模块：
- **通用模块**（realtime-marketing-common）：提供系统中共用的实体类、工具类和接口定义
- **规则引擎模块**（realtime-marketing-engine）：负责规则解析、执行和计算的核心逻辑
- **管理服务模块**（realtime-marketing-manager）：提供Web界面和API接口，用于规则管理和系统监控
- **规则模型资源模块**（rule_model_resources）：存储规则模板和计算逻辑相关资源

### 技术栈
- **后端框架**：Spring Boot
- **大数据处理**：Spark/Flink
- **数据存储**：MySQL、Redis、HDFS
- **消息队列**：Kafka
- **规则引擎**：自研+Groovy动态脚本

## 模块说明

### 通用模块（realtime-marketing-common）
该模块包含系统中共用的实体类（POJO）、工具类和接口定义：
- `pojo`：数据实体类
- `utils`：通用工具类
- `interfaces`：系统间接口定义

### 规则引擎模块（realtime-marketing-engine）
该模块是系统的核心，负责规则的解析、执行和计算：
- `pojo`：引擎相关的数据模型
- `utils`：引擎工具类
- `functions`：自定义函数库
- `main`：引擎主体逻辑实现

### 管理服务模块（realtime-marketing-manager）
提供Web管理界面和API接口，用于规则的管理、执行和监控：
- `controller`：API接口控制器
- `service`：业务逻辑服务
- `dao`：数据访问层
- `pojo`：业务实体类

### 规则模型资源模块（rule_model_resources）
存储规则模板和计算逻辑相关资源：
- `templates`：规则模板文件
- `caculator_groovy_templates`：基于Groovy的计算逻辑模板

## 系统流程
1. 用户行为数据通过数据采集层收集并发送到消息队列
2. 规则引擎实时消费数据，根据预设规则进行计算和判断
3. 触发相应的营销策略（如推送、短信、邮件等）
4. 管理平台对规则进行配置、管理和监控

## 部署说明
1. 确保已安装Maven、JDK 8或以上版本
2. 克隆仓库：`git clone https://github.com/funisgoou/bigdata_realtime_marketing.git`
3. 构建项目：`mvn clean package`
4. 部署各模块：
   - 规则引擎：`java -jar realtime-marketing-engine/target/realtime-marketing-engine-1.0-SNAPSHOT.jar`
   - 管理服务：`java -jar realtime-marketing-manager/target/realtime-marketing-manager-1.0-SNAPSHOT.jar`

## 后续开发计划
- 规则引擎性能优化
- 营销策略精细化控制
- 用户行为分析增强
- 统计报表功能完善 