package com.b0cka.dto;

import com.b0cka.models.Item;
import com.b0cka.models.Paging;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ItemsPageDto {
    private List<List<Item>> grid;
    private Paging paging;
}