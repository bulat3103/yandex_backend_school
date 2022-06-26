package com.example.yandex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ShopUnitStatisticUnitDTO {
    private UUID id;
    private String name;
    private UUID parentId;
    private String type;
    private Integer price;
    private String date;
}
