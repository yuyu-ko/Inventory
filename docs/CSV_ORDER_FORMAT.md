# CSV Order File Format Specification

## File Location

CSV files should be placed in the `src/main/resources/data/` directory, with the default filename `orders_sample.csv`

Can be configured via `application.yml`:
```yaml
inventory:
  order-injector:
    csv-file: data/orders_sample.csv  # Relative to resources directory
```

## CSV Format (Long Format)

Uses **long format (one order item per line)**, which is more flexible and can support any number of SKUs without code modifications.

### Required Columns

| Column Name | Type | Description | Example |
|-------------|------|-------------|---------|
| ORDER_ID | String | Order ID (multiple rows with the same ORDER_ID belong to the same order) | ORD-000001 |
| ORDER_TYPE | String | Order type: PICKUP or DELIVERY | PICKUP |
| ORDER_PLACED_TIME | DateTime | Order placed time (ISO format) | 2024-01-13T08:00:00 |
| ORDER_DUE_TIME | DateTime | Order due time (ISO format) | 2024-01-13T12:00:00 |
| CUSTOMER_ID | String | Customer ID | CUST-001 |
| SKU | String | Product SKU (can be any SKU, no pre-definition required) | SKU-001 |
| QUANTITY | Integer | Quantity of this SKU | 2 |
| TEMPERATURE_ZONE | String | Temperature zone (optional, defaults to AMBIENT) | AMBIENT |

### Format Description

- **Multiple items in the same order are represented by multiple rows**, one SKU per line
- **Rows with the same ORDER_ID are merged into one order**
- **Supports any number of SKUs** without code modifications

## CSV Example

```csv
ORDER_ID,ORDER_TYPE,ORDER_PLACED_TIME,ORDER_DUE_TIME,CUSTOMER_ID,SKU,QUANTITY,TEMPERATURE_ZONE
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-001,2,AMBIENT
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-003,1,CHILLED
ORD-000002,DELIVERY,2024-01-13T08:15:00,2024-01-13T14:00:00,CUST-002,SKU-002,3,AMBIENT
ORD-000002,DELIVERY,2024-01-13T08:15:00,2024-01-13T14:00:00,CUST-002,SKU-004,2,CHILLED
```

In the example above:
- `ORD-000001` contains 2 items: SKU-001 (2 units) and SKU-003 (1 unit)
- `ORD-000002` contains 2 items: SKU-002 (3 units) and SKU-004 (2 units)

## Advantages

### ✅ High Scalability
- **Supports any SKU**: Can add any new SKU (SKU-999, SKU-ABC, etc.) without code modifications
- **Supports any number of items**: An order can contain any number of items
- **No pre-defined columns**: No need for fixed columns like SKU_001, SKU_002, etc.

### ✅ High Flexibility
- **Each item can have different temperature zones**: Can mix AMBIENT, CHILLED, FROZEN in the same order
- **Easy to maintain**: Adding new orders or items only requires adding new rows

### ✅ Follows Database Design Best Practices
- Similar to database order table and order item table format
- Follows relational database normalization design

## Order Processing Logic

1. **Load on startup**: Application loads all orders from CSV file on startup
2. **Group by ORDER_ID**: Rows with the same ORDER_ID are merged into one order
3. **Sort by time**: Orders are sorted by `ORDER_PLACED_TIME`
4. **Timed sending**: Check every 5 seconds, send all due orders (`ORDER_PLACED_TIME <= current time`)
5. **Sequential processing**: Orders are sent in the order they appear in the CSV file and by time

## Configuration Options

In `application.yml`:

```yaml
inventory:
  order-injector:
    use-csv: true  # true = read from CSV
    csv-file: data/orders_sample.csv  # CSV file path
    injection-interval-seconds: 5  # Check interval (seconds)
```

## Notes

1. **Time format**: Must use ISO 8601 format: `YYYY-MM-DDTHH:mm:ss`
   - Correct: `2024-01-13T08:00:00`
   - Incorrect: `2024-01-13 08:00:00`

2. **Order type**: Must be `PICKUP` or `DELIVERY` (case-sensitive)

3. **ORDER_ID consistency**: All rows of the same order must have the same ORDER_ID, ORDER_TYPE, ORDER_PLACED_TIME, ORDER_DUE_TIME, and CUSTOMER_ID

4. **Temperature zone**:
   - Optional column, defaults to `AMBIENT` if empty
   - Supported: `AMBIENT`, `CHILLED`, `FROZEN`

5. **File encoding**: CSV files should use UTF-8 encoding

6. **File path**: Relative to `src/main/resources/` directory

## Example: Adding New Orders

To add a new order, simply add new rows to the CSV file:

```csv
ORDER_ID,ORDER_TYPE,ORDER_PLACED_TIME,ORDER_DUE_TIME,CUSTOMER_ID,SKU,QUANTITY,TEMPERATURE_ZONE
ORD-000011,PICKUP,2024-01-13T13:00:00,2024-01-13T17:00:00,CUST-011,SKU-999,5,AMBIENT
ORD-000011,PICKUP,2024-01-13T13:00:00,2024-01-13T17:00:00,CUST-011,SKU-888,3,CHILLED
ORD-000011,PICKUP,2024-01-13T13:00:00,2024-01-13T17:00:00,CUST-011,SKU-777,2,FROZEN
```

No code modifications needed!
