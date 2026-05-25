# 模块三：购买商品功能后端说明

本文档说明当前后端对买家购买商品功能的支持范围。

## 已实现接口

### 1. 订购商品

```text
POST /api/orders/purchase
```

请求体：

```json
{
  "buyerId": 1,
  "goodId": 2
}
```

处理逻辑：

1. 检查买家是否存在。
2. 检查商品是否存在。
3. 买家不能购买自己的商品。
4. 商品状态必须是 `ON_SALE` 或为空。
5. 创建订单，订单状态为 `PENDING`。
6. 创建订单项。
7. 商品状态改为 `RESERVED`。
8. 返回卖家联系方式，方便买家线下联系。

返回示例：

```json
{
  "success": true,
  "message": "purchased",
  "data": {
    "orderId": 4,
    "orderNumber": "ORD20260525153000123",
    "status": "PENDING",
    "goodId": 2,
    "goodTitle": "机械键盘",
    "totalAmount": 120.00,
    "sellerId": 2,
    "sellerName": "bob",
    "sellerPhone": "13800000002",
    "sellerEmail": "bob@example.com"
  }
}
```

### 2. 退订订单

```text
POST /api/orders/{orderId}/cancel?buyerId=1
```

处理逻辑：

1. 检查订单是否存在。
2. 如果传入 `buyerId`，检查是否为订单买家。
3. 订单状态改为 `CANCELLED`。
4. 如果商品仍是 `RESERVED`，商品状态恢复为 `ON_SALE`。

### 3. 卖家查看被订购订单

```text
GET /api/orders/seller/{sellerId}
```

用于卖家查看自己商品产生的订单。当前没有单独实现消息系统，这个接口可以作为卖家消息通知的简化版本。

## 已复用的功能

评论、评分、举报已有通用 CRUD 接口：

```text
POST /api/comments
POST /api/ratings
POST /api/reports
```

前端可以直接调用这些接口提交评论、评分和举报。

举报数据进入 `report` 表，后续管理员系统可以根据 `status = PENDING` 查询并处理。

## 当前状态约定

商品状态：

```text
ON_SALE   在售
RESERVED  已被订购，等待买卖双方联系
SOLD      已完成交易，后续可扩展
```

订单状态：

```text
PENDING    已订购，待线下联系
CANCELLED  已退订
COMPLETED  已完成，后续可扩展
```

## 后续可扩展

1. 增加订单完成接口，把订单状态改为 `COMPLETED`，商品状态改为 `SOLD`。
2. 增加消息表，实现真正的卖家通知。
3. 评论和评分可以限制为只有购买过该商品的买家才能提交。
4. 举报可以增加管理员处理接口。
