package com.example.yandex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ShopUnitDTO {
    private UUID id;
    private String name;
    private String date;
    private UUID parentId;
    private String type;
    private Integer price;
    private List<ShopUnitDTO> children;
}
