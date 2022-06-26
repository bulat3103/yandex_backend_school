package com.example.yandex.entities;

import com.example.yandex.dto.ShopUnitStatisticUnitDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "statistics")
@NoArgsConstructor
public class ShopUnitStatisticUnit {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    @Column(name = "unit_id", nullable = false)
    private UUID unitId;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "parent_id")
    private UUID parentId;
    @Column(name = "type", nullable = false)
    private String type;
    @Column(name = "price")
    private Integer price;
    @Column(name = "date", nullable = false)
    private Date date;

    public ShopUnitStatisticUnit(UUID unitId, String name, UUID parentId, String type, Integer price, Date date) {
        this.unitId = unitId;
        this.name = name;
        this.parentId = parentId;
        this.type = type;
        this.price = price;
        this.date = date;
    }

    public ShopUnitStatisticUnitDTO toShopUnitStatisticUnitDTO() {
        String dateString = date.toInstant().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        return new ShopUnitStatisticUnitDTO(unitId, name, parentId, type, price, dateString);
    }
}
