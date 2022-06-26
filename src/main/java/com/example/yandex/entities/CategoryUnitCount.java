package com.example.yandex.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "category_unit_count")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUnitCount {
    @Id
    @Column(name = "id", nullable = false)
    private UUID category_id;

    @Column(name = "count")
    private Integer count;

    @Column(name = "sum")
    private Integer sum;
}
