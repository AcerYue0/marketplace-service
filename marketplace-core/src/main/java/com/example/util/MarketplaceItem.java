package com.example.util;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MarketplaceItem {
    private String name;
    private BigDecimal price;
    private String imageUrl;

    public MarketplaceItem(String name, BigDecimal price, String imageUrl) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
    }
}