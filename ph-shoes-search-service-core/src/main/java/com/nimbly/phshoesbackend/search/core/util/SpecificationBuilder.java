package com.nimbly.phshoesbackend.search.core.util;

import com.nimbly.phshoesbackend.catalog.core.model.CatalogShoe;
import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpecificationBuilder {
    public Specification<CatalogShoe> build(AISearchFilterCriteria criteria) {
        Specification<CatalogShoe> spec = (root, query, cb) -> cb.conjunction();

        List<String> brands = criteria.getBrands();
        if (brands != null && !brands.isEmpty()) {
            spec = spec.and(ProductSpecs.brandIn(brands));
        }

        if (criteria.getSizes() != null && !criteria.getSizes().isEmpty()) {
            spec = spec.and(ProductSpecs.sizeAnyInExtrasTextJson(criteria.getSizes()));
        }

        if (criteria.getPriceSaleMin() != null) {
            spec = spec.and(ProductSpecs.priceSaleMin(criteria.getPriceSaleMin()));
        }
        if (criteria.getPriceSaleMax() != null) {
            spec = spec.and(ProductSpecs.priceSaleMax(criteria.getPriceSaleMax()));
        }
        if (criteria.getPriceOriginalMin() != null) {
            spec = spec.and(ProductSpecs.priceOriginalMin(criteria.getPriceOriginalMin()));
        }
        if (criteria.getPriceOriginalMax() != null) {
            spec = spec.and(ProductSpecs.priceOriginalMax(criteria.getPriceOriginalMax()));
        }

        if (Boolean.TRUE.equals(criteria.getOnSale())) {
            spec = spec.and(ProductSpecs.onSale());
        }

        if (criteria.getModel() != null && !criteria.getModel().isBlank()) {
            spec = spec.and(ProductSpecs.titleMatchesPhrase(criteria.getModel()));
        } else if ((criteria.getTitleKeywords() != null && !criteria.getTitleKeywords().isEmpty())
                || (criteria.getSubtitleKeywords() != null && !criteria.getSubtitleKeywords().isEmpty())) {
            Specification<CatalogShoe> keywordSpec = null;
            if (criteria.getTitleKeywords() != null && !criteria.getTitleKeywords().isEmpty()) {
                keywordSpec = ProductSpecs.titleContainsAny(criteria.getTitleKeywords());
            }
            if (criteria.getSubtitleKeywords() != null && !criteria.getSubtitleKeywords().isEmpty()) {
                Specification<CatalogShoe> subtitleSpec =
                        ProductSpecs.subtitleContainsAny(criteria.getSubtitleKeywords());
                keywordSpec = keywordSpec == null ? subtitleSpec : keywordSpec.or(subtitleSpec);
            }
            if (keywordSpec != null) {
                spec = spec.and(keywordSpec);
            }
        }

        return spec;
    }
}
