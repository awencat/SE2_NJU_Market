# 模块三改动整理


## 一、后端改动

### 1. 订购和退订

相关文件：

- `backend/src/main/java/com/marketback/main/controller/OrderEntityController.java`
- `backend/src/main/java/com/marketback/main/service/OrderEntityService.java`
- `backend/src/main/java/com/marketback/main/service/impl/OrderEntityServiceImpl.java`
- `backend/src/main/java/com/marketback/main/dto/PurchaseRequest.java`
- `backend/src/main/java/com/marketback/main/dto/PurchaseResponse.java`

实现内容：

- 新增订购接口：`POST /api/orders/purchase`
- 新增退订接口：`POST /api/orders/{orderId}/cancel?buyerId=用户id`
- 新增卖家订单查询：`GET /api/orders/seller/{sellerId}`
- 新增买家订单查询：`GET /api/orders/buyer/{buyerId}`
- 订购时会创建订单和订单项，并把商品状态从 `ON_SALE` 改成 `RESERVED`
- 退订时会把订单状态改成 `CANCELLED`，并把商品状态恢复为 `ON_SALE`
- 订购成功后会返回卖家联系方式，方便买家联系卖家

### 2. 评论和评分

相关文件：

- `backend/src/main/java/com/marketback/main/controller/CommentController.java`
- `backend/src/main/java/com/marketback/main/controller/RatingController.java`
- `backend/src/main/java/com/marketback/main/service/impl/CommentServiceImpl.java`
- `backend/src/main/java/com/marketback/main/service/impl/RatingServiceImpl.java`
- `backend/src/main/java/com/marketback/main/dto/RatingSummary.java`

实现内容：

- 新增商品评论查询：`GET /api/comments/good/{goodId}`
- 新增商品评分查询：`GET /api/ratings/good/{goodId}`
- 新增商品评分汇总：`GET /api/ratings/good/{goodId}/summary`
- 评论和评分增加限制：只有购买过该商品的用户才可以评论和评分
- 同一个用户对同一个商品只能评分一次

### 3. 商品信息补充卖家联系方式

相关文件：

- `backend/src/main/java/com/marketback/main/controller/GoodController.java`
- `backend/src/main/java/com/marketback/main/entity/Good.java`

实现内容：

- `Good` 增加非数据库字段：`sellerName`、`sellerPhone`、`sellerEmail`
- 商品分页接口返回商品时，会顺便填充卖家的用户名、电话、邮箱
- 前端商品列表和详情页可以直接显示卖家联系方式

### 4. 数据库配置

相关文件：

- `backend/src/main/resources/application.yaml`

实现内容：

- 数据库连接改为优先读取环境变量
- 常用变量包括：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`
- 这样不同同学本地数据库密码不同，不需要反复改配置文件

## 二、前端改动

### 1. 接口封装

相关文件：

- `frontend/src/api.js`

新增方法：

- `purchaseGood(payload)`：订购商品
- `cancelOrder(orderId, buyerId)`：退订订单
- `fetchSellerOrders(sellerId)`：查询卖家收到的订单
- `fetchBuyerOrders(buyerId)`：查询买家的订单
- `fetchGoodComments(goodId)`：查询商品评论
- `fetchGoodRatings(goodId)`：查询商品评分列表
- `fetchGoodRatingSummary(goodId)`：查询商品平均分和评分人数

### 2. 日用商城列表页

相关文件：

- `frontend/src/shops/DailyShop.vue`

实现内容：

- 商品列表页改成更简洁的卡片展示
- 卡片上显示商品名称、价格、卖家、联系方式和状态
- 点击商品卡片会跳转到详情页
- 原来直接挤在卡片里的评论、评分、举报入口已经移到详情页

### 3. 商品详情页

相关文件：

- `frontend/src/shops/GoodDetail.vue`

实现内容：

- 新增商品详情页
- 显示商品图片、名称、描述、价格、状态、卖家联系方式
- 支持订购商品
- 显示综合评分、评分人数、评分列表
- 显示评论列表，并支持发布评论
- 支持提交评分
- 支持举报商品
- 评论和评分失败时会提示：需要购买过该商品才可以操作

### 4. 路由

相关文件：

- `frontend/src/router/index.js`

实现内容：

- 新增商品详情路由：`/goods/:id`
- `DailyShop.vue` 点击商品后进入该详情页
- 顺手整理了路由文件里部分乱码标题

### 5. 个人主页订单区域

相关文件：

- `frontend/src/components/Homepage.vue`

实现内容：

- 增加买家订单展示
- 增加卖家收到的订单通知区域
- 支持买家退订订单

## 三、文档和辅助文件

相关文件：

- `docs/module3-purchase-backend.md`
- `docs/module3-change-summary.md`
- `.gitignore`
- `frontend/.gitignore`
- `AGENTS.md`

说明：

- `docs/module3-purchase-backend.md` 是之前写的模块三后端说明
- 当前这份 `docs/module3-change-summary.md` 是合并前的总整理
- `AGENTS.md` 是本地给 AI 用的项目说明，已经加入忽略，不建议提交
- `.gitignore` 和 `frontend/.gitignore` 有改动，合并时注意不要误删已有规则

## 四、当前 Git 显示的改动文件

后端已修改：

- `backend/src/main/java/com/marketback/main/controller/CommentController.java`
- `backend/src/main/java/com/marketback/main/controller/GoodController.java`
- `backend/src/main/java/com/marketback/main/controller/OrderEntityController.java`
- `backend/src/main/java/com/marketback/main/controller/RatingController.java`
- `backend/src/main/java/com/marketback/main/entity/Good.java`
- `backend/src/main/java/com/marketback/main/service/OrderEntityService.java`
- `backend/src/main/java/com/marketback/main/service/impl/CommentServiceImpl.java`
- `backend/src/main/java/com/marketback/main/service/impl/OrderEntityServiceImpl.java`
- `backend/src/main/java/com/marketback/main/service/impl/RatingServiceImpl.java`
- `backend/src/main/resources/application.yaml`

后端新增：

- `backend/src/main/java/com/marketback/main/dto/PurchaseRequest.java`
- `backend/src/main/java/com/marketback/main/dto/PurchaseResponse.java`
- `backend/src/main/java/com/marketback/main/dto/RatingSummary.java`

前端已修改：

- `frontend/src/api.js`
- `frontend/src/components/Homepage.vue`
- `frontend/src/router/index.js`
- `frontend/src/shops/DailyShop.vue`
- `frontend/.gitignore`
- `frontend/package-lock.json`

前端新增：

- `frontend/src/shops/GoodDetail.vue`

文档新增：

- `docs/module3-purchase-backend.md`
- `docs/module3-change-summary.md`

## 五、最容易冲突的文件

合并时最需要注意这些文件：

- `frontend/src/router/index.js`：如果同学也加了页面路由，很容易冲突。保留双方路由即可。
- `frontend/src/api.js`：如果同学也加了接口方法，注意不要互相覆盖。
- `frontend/src/shops/DailyShop.vue`：这个文件被重写过，冲突概率最高。
- `frontend/src/components/Homepage.vue`：增加了订单相关区域，如果同学也改个人主页，需要手动合并页面结构。
- `backend/src/main/resources/application.yaml`：数据库配置可能和同学本地配置不同，建议保留环境变量写法。
- `backend/src/main/java/com/marketback/main/service/impl/OrderEntityServiceImpl.java`：订购和退订主要逻辑都在这里，如果同学也做了订单逻辑，需要仔细合并。
- `frontend/package-lock.json`：这个文件显示有改动，但不一定和业务逻辑有关。合并时要看同学是否改了依赖。

## 六、建议合并顺序

建议先合并后端，再合并前端：

1. 先合并 DTO、Service、Controller，保证后端接口存在。
2. 再合并 `api.js`，让前端能调用接口。
3. 再合并 `router/index.js` 和页面文件。
4. 最后处理 `Homepage.vue`、`DailyShop.vue` 这种容易冲突的大文件。
5. 合并后运行一次后端编译和前端构建。

建议检查命令：

```powershell
cd backend
mvn compile
```

```powershell
cd frontend
npm run build
```

## 七、已验证情况

目前已经验证过：

```powershell
cd frontend
npm run build
```

结果：前端构建成功。

后端这轮没有重新跑 `mvn compile`。如果合并前要确认后端是否能启动，建议再跑一次。
