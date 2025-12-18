


### class diagram
``` mermaid
classDiagram
direction LR

class Order {
  +OrderId id
  +OrderStatus status
  +place()
  +pay()
  +cancel()
}

class OrderLine {
  +ProductId productId
  +Quantity qty
  +Money unitPrice
}

class Money {
  +int amount
  +string currency
}

Order "1" *-- "1..*" OrderLine : contains
OrderLine --> Money : pricedBy
 ```


### state diagram

``` mermaid 
stateDiagram-v2
[*] --> Draft
Draft --> Placed: place()
Placed --> Paid: pay()
Placed --> Cancelled: cancel()
Paid --> Shipped: ship()
Cancelled --> [*]
Shipped --> [*]

```


### squence

``` mermaid 
sequenceDiagram
actor User
participant UC as PlaceOrderUsecase (Incoming)
participant Order as Order (Aggregate)
participant Repo as OrderRepository (Outgoing Port)
participant Pay as PaymentGateway (Outgoing Port)

User->>UC: placeOrder(cmd)
UC->>Repo: findById(orderId)
Repo-->>UC: Order
UC->>Order: place()
UC->>Pay: authorize(amount)
Pay-->>UC: OK
UC->>Repo: save(Order)
UC-->>User: result



```

``` mermaid 
erDiagram
ORDER ||--|{ ORDER_LINE : contains
ORDER {
  string id PK
  string status
}
ORDER_LINE {
  string order_id FK
  string product_id
  int qty
}



```
``` mermaid 



```
``` mermaid 



```
