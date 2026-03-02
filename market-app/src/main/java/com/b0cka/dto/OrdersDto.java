package com.b0cka.dto;

import com.b0cka.models.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class OrdersDto {

    private List<Item> items;
    private long totalSum;


}
