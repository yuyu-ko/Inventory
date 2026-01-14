# 库存 CSV 文件格式说明

## 文件位置

库存 CSV 文件应放在 `src/main/resources/data/` 目录下，默认文件名为 `inventory_sample.csv`

可以通过 `application.yml` 配置更改：
```yaml
inventory:
  inventory:
    csv-file: data/inventory_sample.csv  # 相对于 resources 目录
```

## CSV 格式

### 必需列

| 列名 | 类型 | 说明 | 示例 |
|------|------|------|------|
| SKU | String | 商品SKU（唯一标识） | SKU-001 |
| QUANTITY | Integer | 初始库存数量 | 1000 |

### 可选列

| 列名 | 类型 | 说明 | 默认值 | 示例 |
|------|------|------|--------|------|
| NAME | String | 商品名称 | "Item {SKU}" | Product A |
| TEMPERATURE_ZONE | String | 温度区域 | AMBIENT | AMBIENT/CHILLED/FROZEN |
| LOW_STOCK_THRESHOLD | Integer | 低库存阈值 | 100 | 100 |

## CSV 示例

```csv
SKU,NAME,QUANTITY,TEMPERATURE_ZONE,LOW_STOCK_THRESHOLD
SKU-001,Product A,1000,AMBIENT,100
SKU-002,Product B,1000,AMBIENT,100
SKU-003,Product C,1000,CHILLED,100
SKU-004,Product D,1000,AMBIENT,100
SKU-005,Product E,1000,CHILLED,100
```

## 工作原理

1. **应用启动时**：
   - `InventoryInitializer` 读取 CSV 文件
   - 为每个 SKU 初始化库存
   - 如果 SKU 已存在，则更新库存信息

2. **订单处理时**：
   - 订单处理会自动扣除库存（通过 InventoryManager）
   - 库存低于阈值时自动补货
   - 如果订单中的 SKU 不在 CSV 中，会在需要时自动创建（使用默认值）

## 配置说明

在 `application.yml` 中：

```yaml
inventory:
  inventory:
    csv-file: data/inventory_sample.csv  # CSV 文件路径
    auto-initialize: true                # 是否自动初始化
    initial-stock: 1000                  # 默认初始库存
    low-stock-threshold: 100             # 默认低库存阈值
    replenishment-quantity: 500          # 自动补货数量
```

## 注意事项

1. **SKU 唯一性**：每个 SKU 在 CSV 中应该只出现一次
2. **数量格式**：QUANTITY 和 LOW_STOCK_THRESHOLD 必须是整数
3. **温度区域**：必须是 AMBIENT、CHILLED 或 FROZEN（如果指定）
4. **文件编码**：CSV 文件应使用 UTF-8 编码
5. **文件路径**：相对于 `src/main/resources/` 目录

## 自动扣除机制

库存的自动扣除通过订单处理流程完成：

1. 订单到达 → 预留库存（RESERVE）
2. 订单处理 → 扣除库存（DEDUCT）
3. 库存低于阈值 → 自动补货（REPLENISH）

所有这些操作都由 `InventoryManager` 自动处理，无需手动干预。

## 示例场景

### 场景 1: 正常订单处理

```
初始库存: SKU-001 = 1000
订单需要: SKU-001 x 5
处理流程:
  1. 预留: 1000 - 5 (预留) = 995 可用
  2. 扣除: 1000 - 5 = 995 总库存
结果: SKU-001 = 995
```

### 场景 2: 自动补货

```
初始库存: SKU-001 = 1000, 阈值 = 100
订单处理: 扣除 950
剩余库存: 50 (< 100 阈值)
自动补货: +500
最终库存: 550
```

## 扩展说明

如果需要添加新的 SKU，只需在 CSV 文件中添加新行：

```csv
SKU,NAME,QUANTITY,TEMPERATURE_ZONE,LOW_STOCK_THRESHOLD
SKU-011,Product K,2000,AMBIENT,200
```

重启应用后，新 SKU 的库存会自动初始化。
