# 核心接口 API 规范

## 1. 文档说明

- 风格：RESTful
- 基础路径：`/api`
- 数据格式：`application/json`
- 字符编码：`UTF-8`
- 时间格式：`yyyy-MM-dd HH:mm:ss`

说明：
- 结合当前后端实体设计，本文将“需求”映射为平台中的 `Good` 资源。
- “接单”映射为买家创建订单，核心资源为 `Order` 与 `OrderItem`。
- “提交评价”映射为 `Rating` 资源。

## 2. 通用响应格式

### 2.1 成功响应

```json
{
  "success": true,
  "message": "ok",
  "data": {}
}
```

### 2.2 失败响应

```json
{
  "success": false,
  "message": "username or password is incorrect",
  "data": null
}
```

## 3. 通用错误码定义

| 错误码 | HTTP 状态码 | 含义 | 说明 |
| --- | --- | --- | --- |
| `SUCCESS` | `200` | 请求成功 | 业务处理成功 |
| `BAD_REQUEST` | `400` | 请求参数错误 | 参数缺失、格式错误、JSON 非法 |
| `UNAUTHORIZED` | `401` | 未认证 | 未登录或登录信息无效 |
| `FORBIDDEN` | `403` | 无权限 | 无权操作目标资源 |
| `NOT_FOUND` | `404` | 资源不存在 | 用户、需求、订单等不存在 |
| `CONFLICT` | `409` | 资源冲突 | 用户名重复、重复接单等 |
| `VALIDATION_FAILED` | `422` | 业务校验失败 | 状态不合法、库存或权限不满足 |
| `INTERNAL_ERROR` | `500` | 服务内部错误 | 未预期异常 |

建议后端在实现中统一返回：

```json
{
  "success": false,
  "message": "error message",
  "data": null,
  "code": "BAD_REQUEST"
}
```

当前项目现有返回结构中尚未包含 `code` 字段，建议后续补充。

---

## 4. 用户注册

### 4.1 接口信息

- URL：`/api/auth/register`
- 方法：`POST`

### 4.2 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | `string` | 是 | 用户名，3 到 20 位，仅允许字母、数字、下划线 |
| `password` | `string` | 是 | 明文密码，后端使用 BCrypt 加密存储，长度建议 6 到 64 位 |
| `email` | `string` | 是 | 邮箱 |
| `phone` | `string` | 否 | 手机号 |
| `campus` | `string` | 否 | 校区 |
| `address` | `string` | 否 | 联系地址 |

### 4.3 请求示例

```json
{
  "username": "alice_01",
  "password": "123456",
  "email": "alice@example.com",
  "phone": "13800000001",
  "campus": "Xianlin",
  "address": "Dorm 1-201"
}
```

### 4.4 成功响应

```json
{
  "success": true,
  "message": "registered",
  "data": {
    "userId": 1,
    "username": "alice_01",
    "email": "alice@example.com",
    "campus": "Xianlin"
  }
}
```

### 4.5 失败响应

```json
{
  "success": false,
  "message": "username or email already exists",
  "data": null
}
```

### 4.6 可能错误码

- `BAD_REQUEST`
- `CONFLICT`
- `VALIDATION_FAILED`

---

## 5. 用户登录

### 5.1 接口信息

- URL：`/api/auth/login`
- 方法：`POST`

### 5.2 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | `string` | 是 | 用户名 |
| `password` | `string` | 是 | 登录密码，后端与 BCrypt 哈希值比对 |

### 5.3 请求示例

```json
{
  "username": "alice_01",
  "password": "123456"
}
```

### 5.4 成功响应

```json
{
  "success": true,
  "message": "logged in",
  "data": {
    "userId": 1,
    "username": "alice_01",
    "email": "alice@example.com",
    "campus": "Xianlin"
  }
}
```

### 5.5 失败响应

```json
{
  "success": false,
  "message": "username or password is incorrect",
  "data": null
}
```

### 5.6 可能错误码

- `BAD_REQUEST`
- `UNAUTHORIZED`

---

## 6. 发布需求

说明：
- 当前数据库设计中，“需求”由 `good` 表承载。
- 发布需求的本质是创建一条需求记录。

### 6.1 接口信息

- URL：`/api/goods`
- 方法：`POST`

### 6.2 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `sellerId` | `integer` | 是 | 发布人用户 ID |
| `title` | `string` | 是 | 需求标题 |
| `description` | `string` | 否 | 需求详细描述 |
| `price` | `number` | 是 | 期望价格 |
| `category` | `string` | 否 | 分类，如 `book`、`digital`、`daily` |
| `status` | `string` | 是 | 需求状态，建议值：`OPEN`、`MATCHED`、`CLOSED` |
| `condition` | `string` | 否 | 需求对应物品的新旧要求，可选 |
| `viewCount` | `integer` | 否 | 浏览次数，创建时通常为 `0` |

### 6.3 请求示例

```json
{
  "sellerId": 1,
  "title": "求购二手高数教材",
  "description": "希望九成新，玄武门或仙林可面交。",
  "price": 25.50,
  "category": "book",
  "status": "OPEN",
  "condition": "USED_GOOD",
  "viewCount": 0
}
```

### 6.4 成功响应

```json
{
  "success": true,
  "message": "created",
  "data": {
    "goodId": 101,
    "sellerId": 1,
    "title": "求购二手高数教材",
    "description": "希望九成新，玄武门或仙林可面交。",
    "price": 25.50,
    "category": "book",
    "status": "OPEN",
    "condition": "USED_GOOD",
    "viewCount": 0,
    "createdAt": "2026-05-11 15:10:00",
    "updatedAt": "2026-05-11 15:10:00"
  }
}
```

### 6.5 失败响应

```json
{
  "success": false,
  "message": "create failed",
  "data": null
}
```

### 6.6 可能错误码

- `BAD_REQUEST`
- `NOT_FOUND`
- `VALIDATION_FAILED`

---

## 7. 浏览需求列表

### 7.1 接口信息

- URL：`/api/goods`
- 方法：`GET`

### 7.2 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `category` | `string` | 否 | 按分类筛选 |
| `status` | `string` | 否 | 按状态筛选 |
| `sellerId` | `integer` | 否 | 按发布人筛选 |
| `current` | `integer` | 否 | 页码，从 1 开始 |
| `size` | `integer` | 否 | 每页大小 |

补充说明：
- 如果不传 `current` 与 `size`，返回全部列表。
- 如果同时传 `current` 与 `size`，返回分页结构。

### 7.3 请求示例

`GET /api/goods?category=book&status=OPEN&current=1&size=10`

### 7.4 成功响应

分页示例：

```json
{
  "success": true,
  "message": "ok",
  "data": {
    "records": [
      {
        "goodId": 101,
        "sellerId": 1,
        "title": "求购二手高数教材",
        "description": "希望九成新，玄武门或仙林可面交。",
        "price": 25.50,
        "category": "book",
        "status": "OPEN",
        "condition": "USED_GOOD",
        "viewCount": 13,
        "createdAt": "2026-05-11 15:10:00",
        "updatedAt": "2026-05-11 18:10:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  }
}
```

### 7.5 失败响应

```json
{
  "success": false,
  "message": "request body or parameters are invalid",
  "data": null
}
```

### 7.6 可能错误码

- `BAD_REQUEST`

---

## 8. 接单

说明：
- “接单”指买家对某条需求创建订单。
- 按当前模型，订单主表为 `order`，订单明细表为 `order_item`。
- 建议在后端封装成一个事务型接口，而不是分别让前端调用两个 CRUD 接口。

### 8.1 接口信息

- URL：`/api/orders`
- 方法：`POST`

### 8.2 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `buyerId` | `integer` | 是 | 接单人 ID |
| `sellerId` | `integer` | 是 | 发布需求的人 ID |
| `orderNumber` | `string` | 是 | 订单编号，要求唯一 |
| `totalAmount` | `number` | 是 | 订单总金额 |
| `status` | `string` | 是 | 订单状态，建议值：`PENDING`、`PAID`、`COMPLETED`、`CANCELLED` |
| `items` | `array<object>` | 是 | 订单项列表 |

`items` 子项说明：

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `goodId` | `integer` | 是 | 对应需求 ID |
| `quantity` | `integer` | 是 | 数量 |
| `unitPrice` | `number` | 是 | 单价 |
| `subtotal` | `number` | 是 | 小计 |

### 8.3 请求示例

```json
{
  "buyerId": 2,
  "sellerId": 1,
  "orderNumber": "ORD202605110001",
  "totalAmount": 25.50,
  "status": "PENDING",
  "items": [
    {
      "goodId": 101,
      "quantity": 1,
      "unitPrice": 25.50,
      "subtotal": 25.50
    }
  ]
}
```

### 8.4 成功响应

```json
{
  "success": true,
  "message": "created",
  "data": {
    "orderId": 5001,
    "buyerId": 2,
    "sellerId": 1,
    "orderNumber": "ORD202605110001",
    "totalAmount": 25.50,
    "status": "PENDING",
    "createdAt": "2026-05-11 16:00:00",
    "updatedAt": "2026-05-11 16:00:00",
    "items": [
      {
        "itemId": 9001,
        "goodId": 101,
        "quantity": 1,
        "unitPrice": 25.50,
        "subtotal": 25.50
      }
    ]
  }
}
```

### 8.5 失败响应

```json
{
  "success": false,
  "message": "order number already exists",
  "data": null
}
```

### 8.6 可能错误码

- `BAD_REQUEST`
- `NOT_FOUND`
- `CONFLICT`
- `VALIDATION_FAILED`

---

## 9. 查看订单详情

### 9.1 接口信息

- URL：`/api/orders/{orderId}`
- 方法：`GET`

### 9.2 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `orderId` | `integer` | 是 | 订单 ID |

### 9.3 请求示例

`GET /api/orders/5001`

### 9.4 成功响应

```json
{
  "success": true,
  "message": "ok",
  "data": {
    "orderId": 5001,
    "buyerId": 2,
    "sellerId": 1,
    "orderNumber": "ORD202605110001",
    "totalAmount": 25.50,
    "status": "PENDING",
    "createdAt": "2026-05-11 16:00:00",
    "updatedAt": "2026-05-11 16:00:00",
    "items": [
      {
        "itemId": 9001,
        "goodId": 101,
        "quantity": 1,
        "unitPrice": 25.50,
        "subtotal": 25.50
      }
    ]
  }
}
```

### 9.5 失败响应

```json
{
  "success": false,
  "message": "record not found",
  "data": null
}
```

### 9.6 可能错误码

- `BAD_REQUEST`
- `NOT_FOUND`
- `FORBIDDEN`

---

## 10. 提交评价

说明：
- 按当前模型，评价使用 `rating` 表保存。
- 当前表结构仅支持评分，若后续需要文字评价，建议扩展评论内容或单独增加评价内容表。

### 10.1 接口信息

- URL：`/api/ratings`
- 方法：`POST`

### 10.2 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `userId` | `integer` | 是 | 评价人 ID |
| `goodId` | `integer` | 是 | 被评价需求或商品 ID |
| `score` | `integer` | 是 | 评分，建议范围 1 到 5 |

### 10.3 请求示例

```json
{
  "userId": 2,
  "goodId": 101,
  "score": 5
}
```

### 10.4 成功响应

```json
{
  "success": true,
  "message": "created",
  "data": {
    "ratingId": 7001,
    "userId": 2,
    "goodId": 101,
    "score": 5,
    "createdAt": "2026-05-11 16:30:00"
  }
}
```

### 10.5 失败响应

```json
{
  "success": false,
  "message": "score must be between 1 and 5",
  "data": null
}
```

### 10.6 可能错误码

- `BAD_REQUEST`
- `NOT_FOUND`
- `VALIDATION_FAILED`

---

## 11. 安全建议

- 密码不得明文存储，建议统一使用 `BCryptPasswordEncoder`。
- 所有查询接口应使用参数化条件构造，避免拼接原始 SQL。
- 对前端传入的筛选字段做白名单校验，避免列名注入。
- 对文本输入进行基础清洗，移除潜在脚本标签或危险字符。
- 前端展示文本时使用安全插值，避免直接使用 `v-html` 渲染用户输入。
- 涉及登录态的接口建议后续补充 `JWT` 或服务端 Session 方案。

## 12. 后续补充建议

- 增加统一错误码字段 `code`
- 增加分页元信息规范
- 增加订单状态流转接口
- 增加文字评价接口
- 增加鉴权与权限控制说明
