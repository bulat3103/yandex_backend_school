package com.example.yandex.repositories;

import com.example.yandex.entities.CategoryUnitCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.UUID;

public interface CategoryUnitCountRepository extends JpaRepository<CategoryUnitCount, UUID> {

    @Query(value = "select * from category_unit_count where id = :id", nativeQuery = true)
    CategoryUnitCount getCategoryUnitCountById(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query(value = "delete from category_unit_count where id = :id", nativeQuery = true)
    void deleteById(@Param("id") UUID id);
}
