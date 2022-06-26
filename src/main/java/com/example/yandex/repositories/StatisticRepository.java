package com.example.yandex.repositories;

import com.example.yandex.entities.ShopUnitStatisticUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface StatisticRepository extends JpaRepository<ShopUnitStatisticUnit, Long> {

    @Query(value = "select * from statistics where unit_id = :unitId and date >= :dateStart and date <= :dateEnd", nativeQuery = true)
    List<ShopUnitStatisticUnit> getShopUnitStatisticUnitByUnitIdAndDate(@Param("unitId") UUID unitId, @Param("dateStart") Date dateStart, @Param("dateEnd") Date dateEnd);

    @Modifying
    @Transactional
    @Query(value = "delete from statistics where unit_id = :unitId", nativeQuery = true)
    void deleteAllByUnitId(@Param("unitId") UUID unitId);
}
