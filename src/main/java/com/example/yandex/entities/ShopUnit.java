package com.example.yandex.entities;

import com.example.yandex.dto.ShopUnitDTO;
import com.example.yandex.enums.ShopUnitType;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "shop_units")
public class ShopUnit {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "date", nullable = false)
    private Date date;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "parent_id")
    private ShopUnit parentId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "price")
    private Integer price;

    @OneToMany(mappedBy = "parentId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ShopUnit> children;

    public ShopUnit(UUID id, String name, Date date, ShopUnit parentId, String type, Integer price) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.parentId = parentId;
        this.type = type;
        this.price = price;
    }

    public ShopUnitDTO toShopUnitDTO() {
        List<ShopUnitDTO> childrenDTO = (children == null || children.isEmpty()) ? (ShopUnitType.valueOf(type) == ShopUnitType.OFFER ? null : Collections.emptyList()) : children.stream().map(ShopUnit::toShopUnitDTO).collect(Collectors.toList());
        String dateString = date.toInstant().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        return new ShopUnitDTO(id, name, dateString, parentId == null ? null : parentId.getId(), type, price, childrenDTO);
    }
}
