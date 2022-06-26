package com.example.yandex.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ShopUnitImportRequest {

    private List<ShopUnitImport> items;

    private Date updateDate;

    public void setShopUnitImport(ShopUnitImport unit) {
        items.add(unit);
    }
}
