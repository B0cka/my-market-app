package com.b0cka.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "items")
@Data
@NoArgsConstructor
public class Item {
    @Id
    private Long id;

    @Column("title")
    private String title;

    @Column("description")
    private String description;

    @Column("price")
    private Long price;

    @Column("img_path")
    private String imgPath;

    @Transient
    private int count;

}