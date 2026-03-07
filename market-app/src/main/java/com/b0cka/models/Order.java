package com.b0cka.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;


@Table("orders")
@Data
@NoArgsConstructor
public class Order {
    @Id
    private Long id;

    @Column("total_sum")
    private Long totalSum;

    @Column("user_id")
    private String userId;

    @Transient
    private List<OrderItem> items = new ArrayList<>();

}