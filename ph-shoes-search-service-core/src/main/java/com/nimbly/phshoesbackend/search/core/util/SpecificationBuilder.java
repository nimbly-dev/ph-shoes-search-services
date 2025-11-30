package com.nimbly.phshoesbackend.search.core.util;

import com.nimbly.phshoesbackend.catalog.core.model.FactProductShoes;
import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpecificationBuilder {

    public Specification<FactProductShoes> build(AISearchFilterCriteria fc) {
        Specification<FactProductShoes> spec = (root, query, cb) -> cb.conjunction();

        List<String> brands = fc.getBrands();
        if (brands != null && !brands.isEmpty()) {
            spec = spec.and(ProductSpecs.brandIn(brands));
        }

        if (fc.getSizes() != null && !fc.getSizes().isEmpty()) {
            spec = spec.and(ProductSpecs.sizeAnyInExtrasTextJson(fc.getSizes()));
        }

        if (fc.getPriceSaleMin() != null)     spec = spec.and(ProductSpecs.priceSaleMin(fc.getPriceSaleMin()));
        if (fc.getPriceSaleMax() != null)     spec = spec.and(ProductSpecs.priceSaleMax(fc.getPriceSaleMax()));
        if (fc.getPriceOriginalMin() != null) spec = spec.and(ProductSpecs.priceOriginalMin(fc.getPriceOriginalMin()));
        if (fc.getPriceOriginalMax() != null) spec = spec.and(ProductSpecs.priceOriginalMax(fc.getPriceOriginalMax()));

        if (Boolean.TRUE.equals(fc.getOnSale())) {
            spec = spec.and(ProductSpecs.onSale());
        }

        if (fc.getModel() != null && !fc.getModel().isBlank()) {
            spec = spec.and(ProductSpecs.titleMatchesPhrase(fc.getModel()));
        } else if (fc.getTitleKeywords() != null && !fc.getTitleKeywords().isEmpty()) {
            spec = spec.and(ProductSpecs.titleContainsAny(fc.getTitleKeywords()));
        }

        spec = spec.and(ProductSpecs.latestOnly());

        return spec;
    }
}

