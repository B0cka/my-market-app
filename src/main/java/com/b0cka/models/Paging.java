package com.b0cka.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paging {
    private int pageSize;

    private Boolean hasPrevious;
    private Boolean hasNext;
    private int pageNumber;

}
