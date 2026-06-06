# 交付项 5：CI/CD 配置与运行记录

## 1. 配置文件

本阶段新增基础 CI/CD 配置：

```text
.github/workflows/p4-ci.yml
```

原仓库已有：

```text
.github/workflows/maven-publish.yml
```

但该文件只在 GitHub Release 创建时触发，且默认从仓库根目录执行 Maven 发布流程；本项目后端实际位于 `backend/`，因此 P4 验收采用新增的 `p4-ci.yml` 作为持续集成入口。

## 2. 触发条件

`p4-ci.yml` 支持以下触发方式：

| 触发方式 | 说明 |
| --- | --- |
| `push` 到 `main` 或 `master` | 主分支提交后自动运行 |
| `pull_request` | 合并前自动验证 |
| `workflow_dispatch` | 可在 GitHub Actions 页面手动触发 |

## 3. 流水线步骤

| Job | 环境 | 步骤 | 覆盖要求 |
| --- | --- | --- | --- |
| `backend` | Ubuntu + JDK 17 | Checkout、安装 JDK、Maven 缓存、`./mvnw test`、`./mvnw -DskipTests package`、上传 jar | 自动安装依赖、运行单元测试、运行集成测试、构建后端 |
| `frontend` | Ubuntu + Node.js 22 | Checkout、安装 Node、npm 缓存、`npm ci`、`npm run build`、上传 dist | 自动安装依赖、构建前端 |

说明：当前前端项目尚未配置 ESLint/Prettier 脚本，因此静态检查暂以 Vite 构建阶段的语法检查和打包校验承担。后续可在 `package.json` 增加 `lint` 脚本后加入流水线。

## 4. 配置内容摘要

后端核心配置：

```yaml
backend:
  defaults:
    run:
      working-directory: backend
  steps:
    - uses: actions/setup-java@v4
      with:
        distribution: temurin
        java-version: "17"
        cache: maven
    - run: ./mvnw test
    - run: ./mvnw -DskipTests package
```

前端核心配置：

```yaml
frontend:
  defaults:
    run:
      working-directory: frontend
  steps:
    - uses: actions/setup-node@v4
      with:
        node-version: "22"
        cache: npm
        cache-dependency-path: frontend/package-lock.json
    - run: npm ci
    - run: npm run build
```

## 5. 本地流水线运行记录

运行环境：

| 项目 | 版本/说明 |
| --- | --- |
| 日期 | 2026-06-06 |
| 操作系统 | Windows，PowerShell |
| 后端 | JDK 17.0.17，项目自带 Maven Wrapper |
| 前端 | Node.js/npm，本地使用 `npm ci` 和 `npm run build` 验证 |

### 5.1 首次前端依赖安装检查

命令：

```powershell
cd frontend
npm ci
```

首次结果：

| 结果 | 说明 |
| --- | --- |
| 失败 | `package.json` 与 `package-lock.json` 不同步 |
| 关键错误 | `Missing: @emnapi/core@1.10.0 from lock file`、`Missing: @emnapi/runtime@1.10.0 from lock file` |
| 处理 | 执行 `npm install` 同步锁文件，保留 `frontend/package-lock.json` 变更 |

同步锁文件命令：

```powershell
cd frontend
npm install
```

同步结果：

| 指标 | 结果 |
| --- | --- |
| 安装 | added 33 packages |
| 审计 | 1 moderate severity vulnerability |
| 处理策略 | 不阻塞 P4 构建；记录为后续依赖治理风险 |

### 5.2 前端依赖安装复验

命令：

```powershell
cd frontend
npm ci
```

结果：

| 指标 | 结果 |
| --- | --- |
| 状态 | 通过 |
| 安装包 | added 92 packages |
| 审计 | 1 moderate severity vulnerability |

### 5.3 前端构建

命令：

```powershell
cd frontend
npm run build
```

结果：

| 指标 | 结果 |
| --- | --- |
| 状态 | 通过 |
| 构建工具 | Vite 8.0.8 |
| 模块转换 | 1623 modules transformed |
| 输出 | `dist/index.html`、`dist/assets/hero-5sT3BiRD.png`、`dist/assets/index-1T0wFW1v.css`、`dist/assets/index-B_kuUpX9.js` |
| 耗时 | 约 756 ms |
| 警告 | JS chunk 大于 500 kB，建议后续使用动态导入或代码拆分 |

### 5.4 后端单元测试与集成测试

命令：

```powershell
cd backend
cmd /c mvnw.cmd test
```

结果：

| 测试集 | 数量 | 失败 | 错误 | 跳过 |
| --- | ---: | ---: | ---: | ---: |
| `CoreBusinessFlowIntegrationTest` | 4 | 0 | 0 | 0 |
| `DemoApplicationTests` | 1 | 0 | 0 | 0 |
| `OrderEntityServiceImplTest` | 6 | 0 | 0 | 0 |
| `RatingServiceImplTest` | 5 | 0 | 0 | 0 |
| **合计** | **16** | **0** | **0** | **0** |

构建摘要：

| 指标 | 结果 |
| --- | --- |
| 状态 | BUILD SUCCESS |
| 总耗时 | 8.945 s |
| 完成时间 | 2026-06-06 17:16:42 +08:00 |

### 5.5 后端打包

命令：

```powershell
cd backend
cmd /c mvnw.cmd -DskipTests package
```

结果：

| 指标 | 结果 |
| --- | --- |
| 状态 | BUILD SUCCESS |
| 产物 | `backend/target/boy-0.0.1-SNAPSHOT.jar` |
| 总耗时 | 2.893 s |
| 完成时间 | 2026-06-06 17:16:06 +08:00 |

## 6. 验收结论

| 验收项 | 结果 | 说明 |
| --- | --- | --- |
| 自动安装依赖 | 通过 | 后端由 Maven 自动解析依赖；前端 `npm ci` 已通过 |
| 自动运行单元测试 | 通过 | 订单和评分服务层测试通过 |
| 自动运行集成测试 | 通过 | 核心业务流集成测试通过 |
| 自动构建项目 | 通过 | 后端 jar 和前端 dist 均可生成 |
| CI/CD 配置文件 | 通过 | 已新增 `.github/workflows/p4-ci.yml` |
| 最近一次运行结果记录 | 通过 | 本文档记录了 2026-06-06 本地流水线命令和结果 |

## 7. 后续改进项

| 问题 | 影响 | 建议 |
| --- | --- | --- |
| 前端缺少 `lint` 脚本 | CI 暂无法独立执行静态检查 | 后续引入 ESLint，并在流水线中增加 `npm run lint` |
| npm audit 有 1 个中危风险 | 依赖安全需要持续跟踪 | 后续评估 `npm audit fix` 对 Vite/Element Plus 依赖树的影响 |
| 前端主 JS chunk 较大 | 可能影响弱网首屏加载 | 对路由页面使用动态导入，拆分管理端和商品详情页代码 |
