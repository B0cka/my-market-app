package com.b0cka.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("order_items")
@Data
@NoArgsConstructor
public class OrderItem {
    @Id
    private Long id;

    @Column("order_id")
    private Long orderId;

    @Column("item_id")
    private Long itemId;

    private int quantity;

    @Column("price_per_item")
    private Long pricePerItem;

    @Transient
    private Item item;
}