package com.example.yandex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ShopUnitStatisticResponse {
    private List<ShopUnitStatisticUnitDTO> items;
}
