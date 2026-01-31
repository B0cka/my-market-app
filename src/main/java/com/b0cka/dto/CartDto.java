package com.b0cka.dto;

import com.b0cka.models.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class CartDto {
    private List<Item> items;
    private Long totalSum;
}