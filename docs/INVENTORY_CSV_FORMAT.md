# Inventory CSV File Format Specification

## File Location

Inventory CSV files should be placed in the `src/main/resources/data/` directory, with the default filename `inventory_sample.csv`

Can be configured via `application.yml`:
```yaml
inventory:
  inventory:
    csv-file: data/inventory_sample.csv  # Relative to resources directory
```

## CSV Format

### Required Columns

| Column Name | Type | Description | Example |
|-------------|------|-------------|---------|
| SKU | String | Product SKU (unique identifier) | SKU-001 |
| QUANTITY | Integer | Initial inventory quantity | 1000 |

### Optional Columns

| Column Name | Type | Description | Default Value | Example |
|-------------|------|-------------|---------------|---------|
| NAME | String | Product name | "Item {SKU}" | Product A |
| TEMPERATURE_ZONE | String | Temperature zone | AMBIENT | AMBIENT/CHILLED/FROZEN |
| LOW_STOCK_THRESHOLD | Integer | Low stock threshold | 100 | 100 |

## CSV Example

```csv
SKU,NAME,QUANTITY,TEMPERATURE_ZONE,LOW_STOCK_THRESHOLD
SKU-001,Product A,1000,AMBIENT,100
SKU-002,Product B,1000,AMBIENT,100
SKU-003,Product C,1000,CHILLED,100
SKU-004,Product D,1000,AMBIENT,100
SKU-005,Product E,1000,CHILLED,100
```

## How It Works

1. **On application startup**:
   - `InventoryInitializer` reads the CSV file
   - Initializes inventory for each SKU
   - If SKU already exists, updates inventory information

2. **During order processing**:
   - Order processing automatically deducts inventory (via InventoryManager)
   - Auto-replenishes when inventory falls below threshold
   - If SKU in order is not in CSV, it will be automatically created when needed (using default values)

## Configuration

In `application.yml`:

```yaml
inventory:
  inventory:
    csv-file: data/inventory_sample.csv  # CSV file path
    auto-initialize: true                # Whether to auto-initialize
    initial-stock: 1000                  # Default initial stock
    low-stock-threshold: 100             # Default low stock threshold
    replenishment-quantity: 500          # Auto-replenishment quantity
```

## Notes

1. **SKU uniqueness**: Each SKU should appear only once in the CSV
2. **Quantity format**: QUANTITY and LOW_STOCK_THRESHOLD must be integers
3. **Temperature zone**: Must be AMBIENT, CHILLED, or FROZEN (if specified)
4. **File encoding**: CSV files should use UTF-8 encoding
5. **File path**: Relative to `src/main/resources/` directory

## Auto-Deduction Mechanism

Inventory auto-deduction is completed through the order processing flow:

1. Order arrives → Reserve inventory (RESERVE)
2. Order processing → Deduct inventory (DEDUCT)
3. Inventory below threshold → Auto-replenish (REPLENISH)

All these operations are automatically handled by `InventoryManager`, no manual intervention needed.

## Example Scenarios

### Scenario 1: Normal Order Processing

```
Initial inventory: SKU-001 = 1000
Order requires: SKU-001 x 5
Processing flow:
  1. Reserve: 1000 - 5 (reserved) = 995 available
  2. Deduct: 1000 - 5 = 995 total inventory
Result: SKU-001 = 995
```

### Scenario 2: Auto-Replenishment

```
Initial inventory: SKU-001 = 1000, threshold = 100
Order processing: Deduct 950
Remaining inventory: 50 (< 100 threshold)
Auto-replenish: +500
Final inventory: 550
```

## Extension Notes

If you need to add new SKUs, simply add new rows to the CSV file:

```csv
SKU,NAME,QUANTITY,TEMPERATURE_ZONE,LOW_STOCK_THRESHOLD
SKU-011,Product K,2000,AMBIENT,200
```

After restarting the application, the new SKU's inventory will be automatically initialized.
