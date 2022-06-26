package com.example.yandex.controllers;

import com.example.yandex.dto.*;
import com.example.yandex.dto.Error;
import com.example.yandex.entities.ShopUnit;
import com.example.yandex.entities.ShopUnitStatisticUnit;
import com.example.yandex.exceptions.NotValidDataException;
import com.example.yandex.exceptions.UnitNotFoundException;
import com.example.yandex.services.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class MarketController {
    @Autowired
    private MarketService marketService;

    @CrossOrigin
    @GetMapping(value = "nodes/{id}", produces = "application/json")
    public ResponseEntity<?> getNodes(@PathVariable UUID id) {
        try {
            ShopUnit unit = marketService.getNodes(id);
            return new ResponseEntity<>(unit.toShopUnitDTO(), HttpStatus.OK);
        } catch (UnitNotFoundException e) {
            return new ResponseEntity<>(new Error(404, "Категория/товар не найден."), HttpStatus.NOT_FOUND);
        }
    }

    @CrossOrigin
    @DeleteMapping(value = "delete/{id}", produces = "application/json")
    public ResponseEntity<?> deleteItem(@PathVariable UUID id) {
        try {
            marketService.deleteShopUnit(id);
        } catch (UnitNotFoundException e) {
            return new ResponseEntity<>(new Error(404, "Категория/товар не найден."), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("Удаление прошло успешно.", HttpStatus.OK);
    }

    @CrossOrigin
    @PostMapping(value = "imports", produces = "application/json")
    public ResponseEntity<?> importItems(@RequestBody ShopUnitImportRequest shopUnitImportRequest) {
        ShopUnitImportRequest newShopUnitImportRequest = marketService.validateShopUnitImportRequest(shopUnitImportRequest);
        if (newShopUnitImportRequest == null) {
            return new ResponseEntity<>(new Error(400, "Невалидная схема документа или входные данные не верны."), HttpStatus.BAD_REQUEST);
        }
        for (ShopUnitImport unit : newShopUnitImportRequest.getItems()) {
            try {
                marketService.addShopUnit(unit, newShopUnitImportRequest.getUpdateDate());
            } catch (NotValidDataException e) {
                return new ResponseEntity<>(new Error(400, "Невалидная схема документа или входные данные не верны."), HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("Вставка или обновление прошли успешно.", HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "sales", produces = "application/json")
    public ResponseEntity<?> getSales(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date date) {
        List<ShopUnit> shopUnitList = marketService.getSales(date);
        List<ShopUnitStatisticUnitDTO> listToResponse = new ArrayList<>();
        for (ShopUnit unit : shopUnitList) {
            String dateString = unit.getDate().toInstant().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
            listToResponse.add(new ShopUnitStatisticUnitDTO(
                    unit.getId(),
                    unit.getName(),
                    unit.getParentId() == null ? null : unit.getParentId().getId(),
                    unit.getType(),
                    unit.getPrice(),
                    dateString
            ));
        }
        return new ResponseEntity<>(new ShopUnitStatisticResponse(listToResponse), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping(value = "node/{id}/statistic")
    public ResponseEntity<?> getStatistic(@PathVariable UUID id, @RequestParam(value = "dateStart", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date dateStart, @RequestParam(value = "dateEnd", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date dateEnd) {
        try {
            List<ShopUnitStatisticUnit> unitList = marketService.getStatistic(id, dateStart, dateEnd);
            List<ShopUnitStatisticUnitDTO> toReturn = unitList.stream().map(ShopUnitStatisticUnit::toShopUnitStatisticUnitDTO).collect(Collectors.toList());
            return new ResponseEntity<>(new ShopUnitStatisticResponse(toReturn), HttpStatus.OK);
        } catch (NotValidDataException e) {
            return new ResponseEntity<>(new Error(400, "Невалидная схема документа или входные данные не верны."), HttpStatus.BAD_REQUEST);
        }
    }
}
