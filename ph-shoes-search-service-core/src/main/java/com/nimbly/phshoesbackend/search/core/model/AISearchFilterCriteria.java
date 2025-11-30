package com.nimbly.phshoesbackend.search.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AISearchFilterCriteria {
    private List<String> brands;
    private String model;
    private Double priceSaleMin;
    private Double priceSaleMax;
    private Double priceOriginalMin;
    private Double priceOriginalMax;
    private String gender;
    private Boolean onSale;
    private List<String> titleKeywords;
    private List<String> subtitleKeywords;
    private String sortBy;
    private List<String> sizes;
}

