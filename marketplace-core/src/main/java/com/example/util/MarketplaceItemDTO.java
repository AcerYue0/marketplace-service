package com.example.util;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MarketplaceItemDTO {
    private String name;
    private BigDecimal price;

    public MarketplaceItemDTO(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }
}
