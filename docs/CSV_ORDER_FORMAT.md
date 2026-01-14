# CSV 订单文件格式说明

## 文件位置

CSV 文件应放在 `src/main/resources/data/` 目录下，默认文件名为 `orders_sample.csv`

可以通过 `application.yml` 配置更改：
```yaml
inventory:
  order-injector:
    csv-file: data/orders_sample.csv  # 相对于 resources 目录
```

## CSV 格式（长格式 - Long Format）

采用**长格式（每行一个订单项）**，这样设计更灵活，可以支持任意数量的 SKU，无需修改代码。

### 必需列

| 列名 | 类型 | 说明 | 示例 |
|------|------|------|------|
| ORDER_ID | String | 订单ID（同一订单的多行使用相同的 ORDER_ID） | ORD-000001 |
| ORDER_TYPE | String | 订单类型：PICKUP 或 DELIVERY | PICKUP |
| ORDER_PLACED_TIME | DateTime | 下单时间（ISO格式） | 2024-01-13T08:00:00 |
| ORDER_DUE_TIME | DateTime | 到期时间（ISO格式） | 2024-01-13T12:00:00 |
| CUSTOMER_ID | String | 客户ID | CUST-001 |
| SKU | String | 商品SKU（可以是任意SKU，无需预先定义） | SKU-001 |
| QUANTITY | Integer | 该SKU的数量 | 2 |
| TEMPERATURE_ZONE | String | 温度区域（可选，默认为 AMBIENT） | AMBIENT |

### 格式说明

- **同一订单的多个商品使用多行表示**，每行一个 SKU
- **ORDER_ID 相同的行会被合并为一个订单**
- **支持任意数量的 SKU**，无需修改代码

## CSV 示例

```csv
ORDER_ID,ORDER_TYPE,ORDER_PLACED_TIME,ORDER_DUE_TIME,CUSTOMER_ID,SKU,QUANTITY,TEMPERATURE_ZONE
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-001,2,AMBIENT
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-003,1,CHILLED
ORD-000002,DELIVERY,2024-01-13T08:15:00,2024-01-13T14:00:00,CUST-002,SKU-002,3,AMBIENT
ORD-000002,DELIVERY,2024-01-13T08:15:00,2024-01-13T14:00:00,CUST-002,SKU-004,2,CHILLED
```

上面的示例中：
- `ORD-000001` 包含 2 个商品：SKU-001 (2个) 和 SKU-003 (1个)
- `ORD-000002` 包含 2 个商品：SKU-002 (3个) 和 SKU-004 (2个)

## 优势

### ✅ 可扩展性强
- **支持任意 SKU**：可以添加任何新的 SKU（SKU-999, SKU-ABC 等），无需修改代码
- **支持任意数量的商品**：一个订单可以包含任意数量的商品
- **无需预定义列**：不需要 SKU_001, SKU_002 等固定列

### ✅ 灵活性高
- **每个商品可以有不同的温度区域**：同一订单中可以混合 AMBIENT、CHILLED、FROZEN
- **易于维护**：添加新订单或新商品只需添加新行

### ✅ 符合数据库设计最佳实践
- 类似于数据库的订单表和订单项表的格式
- 符合关系型数据库的规范化设计

## 订单处理逻辑

1. **启动时加载**：应用启动时从 CSV 文件加载所有订单
2. **按 ORDER_ID 分组**：将具有相同 ORDER_ID 的行合并为一个订单
3. **按时间排序**：订单按 `ORDER_PLACED_TIME` 排序
4. **定时发送**：每 5 秒检查一次，发送所有到期的订单（`ORDER_PLACED_TIME <= 当前时间`）
5. **顺序处理**：订单按照 CSV 文件中的顺序和时间顺序发送

## 配置选项

在 `application.yml` 中：

```yaml
inventory:
  order-injector:
    use-csv: true  # true = 从 CSV 读取
    csv-file: data/orders_sample.csv  # CSV 文件路径
    injection-interval-seconds: 5  # 检查间隔（秒）
```

## 注意事项

1. **时间格式**：必须使用 ISO 8601 格式：`YYYY-MM-DDTHH:mm:ss`
   - 正确：`2024-01-13T08:00:00`
   - 错误：`2024-01-13 08:00:00`

2. **订单类型**：必须是 `PICKUP` 或 `DELIVERY`（大小写敏感）

3. **ORDER_ID 一致性**：同一订单的所有行必须具有相同的 ORDER_ID、ORDER_TYPE、ORDER_PLACED_TIME、ORDER_DUE_TIME 和 CUSTOMER_ID

4. **温度区域**：
   - 可选列，如果为空则默认为 `AMBIENT`
   - 支持：`AMBIENT`、`CHILLED`、`FROZEN`

5. **文件编码**：CSV 文件应使用 UTF-8 编码

6. **文件路径**：相对于 `src/main/resources/` 目录

## 示例：添加新订单

要添加新订单，只需在 CSV 文件中添加新行：

```csv
ORDER_ID,ORDER_TYPE,ORDER_PLACED_TIME,ORDER_DUE_TIME,CUSTOMER_ID,SKU,QUANTITY,TEMPERATURE_ZONE
ORD-000011,PICKUP,2024-01-13T13:00:00,2024-01-13T17:00:00,CUST-011,SKU-999,5,AMBIENT
ORD-000011,PICKUP,2024-01-13T13:00:00,2024-01-13T17:00:00,CUST-011,SKU-888,3,CHILLED
ORD-000011,PICKUP,2024-01-13T13:00:00,2024-01-13T17:00:00,CUST-011,SKU-777,2,FROZEN
```

无需修改任何代码！
