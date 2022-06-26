package com.example.yandex.repositories;

import com.example.yandex.entities.ShopUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface ShopUnitRepository extends JpaRepository<ShopUnit, UUID> {

    @Query(value = "select * from shop_units where type = 'OFFER' and date between :leftDate and :rightDate", nativeQuery = true)
    List<ShopUnit> getShopUnitsByDate(@Param("leftDate") Date leftDate, @Param("rightDate") Date rightDate);

    @Query(value = "select * from shop_units where id = :id", nativeQuery = true)
    ShopUnit getShopUnitById(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query(value = "delete from shop_units su where su.id = :id", nativeQuery = true)
    void deleteById(@Param("id") UUID id);
}
