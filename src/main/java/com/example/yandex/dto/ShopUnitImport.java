package com.example.yandex.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ShopUnitImport {
    private UUID id;

    private String name;

    private UUID parentId;

    private String type;

    private Integer price;
}
