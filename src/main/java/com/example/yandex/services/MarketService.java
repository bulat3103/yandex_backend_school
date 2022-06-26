package com.example.yandex.services;

import com.example.yandex.dto.ShopUnitImport;
import com.example.yandex.dto.ShopUnitImportRequest;
import com.example.yandex.entities.CategoryUnitCount;
import com.example.yandex.entities.ShopUnit;
import com.example.yandex.entities.ShopUnitStatisticUnit;
import com.example.yandex.enums.ShopUnitType;
import com.example.yandex.exceptions.NotValidDataException;
import com.example.yandex.exceptions.UnitNotFoundException;
import com.example.yandex.repositories.CategoryUnitCountRepository;
import com.example.yandex.repositories.ShopUnitRepository;
import com.example.yandex.repositories.StatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
public class MarketService {
    @Autowired
    private ShopUnitRepository shopUnitRepository;
    @Autowired
    private StatisticRepository statisticRepository;
    @Autowired
    private CategoryUnitCountRepository categoryUnitCountRepository;

    public List<ShopUnitStatisticUnit> getStatistic(UUID id, Date dateStart, Date dateEnd) throws NotValidDataException {
        if (dateStart == null) dateStart = new Date(0);
        if (dateEnd == null) dateEnd = new Date(System.currentTimeMillis());
        return statisticRepository.getShopUnitStatisticUnitByUnitIdAndDate(id, dateStart, dateEnd);
    }

    public List<ShopUnit> getSales(Date date) {
        Date leftDate = Date.from(date.toInstant().minus(Duration.ofHours(24)));
        return shopUnitRepository.getShopUnitsByDate(leftDate, date);
    }

    public ShopUnit getNodes(UUID id) throws UnitNotFoundException {
        ShopUnit fromDatabase = shopUnitRepository.getShopUnitById(id);
        if (fromDatabase == null) throw new UnitNotFoundException();
        return fromDatabase;
    }

    public void deleteShopUnit(UUID id) throws UnitNotFoundException {
        ShopUnit fromDatabase = shopUnitRepository.getShopUnitById(id);
        if (fromDatabase == null) throw new UnitNotFoundException();
        recursiveDelete(fromDatabase);
        shopUnitRepository.deleteById(fromDatabase.getId());
        statisticRepository.deleteAllByUnitId(fromDatabase.getId());
        categoryUnitCountRepository.deleteById(fromDatabase.getId());
    }

    private void recursiveDelete(ShopUnit unit) {
        if (unit.getChildren().isEmpty()) {
            return;
        }
        for (ShopUnit children : unit.getChildren()) {
            recursiveDelete(children);
            shopUnitRepository.deleteById(children.getId());
            statisticRepository.deleteAllByUnitId(children.getId());
            categoryUnitCountRepository.deleteById(children.getId());
        }
    }

    private void updateParentsChildrens(ShopUnit shopUnit) {
        if (shopUnit.getParentId() == null) return;
        ShopUnit parent = shopUnitRepository.getShopUnitById(shopUnit.getParentId().getId());
        List<ShopUnit> childrens = parent.getChildren() == null ? new ArrayList<>() : parent.getChildren();
        childrens.add(shopUnit);
        parent.setChildren(childrens);
        shopUnitRepository.save(parent);
    }

    private void updateParentsDates(ShopUnit start, Date date) {
        while (start.getParentId() != null) {
            start = start.getParentId();
            start.setDate(date);
            shopUnitRepository.save(start);
        }
    }

    private void updateCategoryCountAndSum(UUID parentId, int countDiff, int priceDiff) {
        ShopUnit parent = shopUnitRepository.getShopUnitById(parentId);
        while (parent != null) {
            CategoryUnitCount category = categoryUnitCountRepository.getCategoryUnitCountById(parent.getId());
            category.setCount(category.getCount() + countDiff);
            category.setSum(category.getSum() + priceDiff);
            categoryUnitCountRepository.save(category);
            parent = parent.getParentId() == null ? null : shopUnitRepository.getShopUnitById(parent.getParentId().getId());
        }
    }

    private void updateCategoryPrices(UUID parentId) {
        ShopUnit parent = shopUnitRepository.getShopUnitById(parentId);
        while (parent != null) {
            CategoryUnitCount categoryUnitCount = categoryUnitCountRepository.getCategoryUnitCountById(parent.getId());
            parent.setPrice(categoryUnitCount.getSum() / categoryUnitCount.getCount());
            shopUnitRepository.save(parent);
            parent = parent.getParentId() == null ? null : shopUnitRepository.getShopUnitById(parent.getParentId().getId());
        }
    }

    public void addShopUnit(ShopUnitImport unit, Date date) throws NotValidDataException {
        ShopUnit fromDatabase = shopUnitRepository.getShopUnitById(unit.getId());
        ShopUnit add = new ShopUnit(
                unit.getId(),
                unit.getName(),
                date,
                unit.getParentId() == null ? null : shopUnitRepository.getShopUnitById(unit.getParentId()),
                fromDatabase == null ? unit.getType() : fromDatabase.getType(),
                unit.getPrice()
        );
        shopUnitRepository.save(add);
        statisticRepository.save(new ShopUnitStatisticUnit(add.getId(), add.getName(), add.getParentId() == null ? null : add.getParentId().getId(), add.getType(), add.getPrice(), date));
        if (fromDatabase == null && ShopUnitType.valueOf(unit.getType()) == ShopUnitType.CATEGORY) {
            categoryUnitCountRepository.save(new CategoryUnitCount(add.getId(), 0, 0));
        }
        updateParentsChildrens(add);
        updateParentsDates(add, date);
        if (ShopUnitType.valueOf(unit.getType()) == ShopUnitType.OFFER && unit.getParentId() != null) {
            updateCategoryCountAndSum(unit.getParentId(), fromDatabase == null ? 1 : 0, fromDatabase == null ? add.getPrice() : add.getPrice() - fromDatabase.getPrice());
            updateCategoryPrices(unit.getParentId());
        }
    }

    public ShopUnitImportRequest validateShopUnitImportRequest(ShopUnitImportRequest importRequest) {
        TreeMap<UUID, ShopUnitImport> map = new TreeMap<>();
        for (ShopUnitImport unit : importRequest.getItems()) {
            if (map.containsKey(unit.getId())) return null;
            map.put(unit.getId(), unit);
        }
        for (ShopUnitImport unit : importRequest.getItems()) {
            ShopUnit parent = unit.getParentId() == null ? null : shopUnitRepository.getShopUnitById(unit.getParentId());
            if (unit.getParentId() != null && !map.containsKey(unit.getParentId()) && parent == null) return null;
            if (unit.getParentId() != null && parent == null && ShopUnitType.valueOf(map.get(unit.getParentId()).getType()) != ShopUnitType.CATEGORY) return null;
            if (unit.getParentId() != null && parent != null && ShopUnitType.valueOf(parent.getType()) != ShopUnitType.CATEGORY) return null;
            if (unit.getName() == null) return null;
            if (ShopUnitType.valueOf(unit.getType()) == ShopUnitType.OFFER) {
                if (unit.getPrice() == null || unit.getPrice() < 0) return null;
            } else {
                if (unit.getPrice() != null) return null;
            }
        }
        Queue<ShopUnitImport> queue = new LinkedList<>(map.values());
        ShopUnitImportRequest toReturn = new ShopUnitImportRequest();
        toReturn.setUpdateDate(importRequest.getUpdateDate());
        toReturn.setItems(new ArrayList<>());
        TreeSet<UUID> inRequest = new TreeSet<>();
        while (!queue.isEmpty()) {
            ShopUnitImport unit = queue.poll();
            ShopUnit parent = unit.getParentId() == null ? null : shopUnitRepository.getShopUnitById(unit.getParentId());
            if (unit.getParentId() != null && !inRequest.contains(unit.getParentId()) && parent == null) {
                queue.add(unit);
                continue;
            }
            toReturn.setShopUnitImport(unit);
            inRequest.add(unit.getId());
        }
        return toReturn;
    }
}
