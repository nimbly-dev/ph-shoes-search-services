package com.nimbly.phshoesbackend.search.core.service.impl;

import com.nimbly.phshoesbackend.catalog.core.model.CatalogShoe;
import com.nimbly.phshoesbackend.catalog.core.repository.jpa.CatalogShoeRepository;
import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;
import com.nimbly.phshoesbackend.search.core.util.ProductSpecs;
import com.nimbly.phshoesbackend.search.core.util.SpecificationBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SearchOrchestrator {

    private final CatalogShoeRepository catalogShoeRepository;
    private final SpecificationBuilder specBuilder;

    public SearchOrchestrator(
            CatalogShoeRepository catalogShoeRepository,
            SpecificationBuilder specBuilder
    ) {
        this.catalogShoeRepository = catalogShoeRepository;
        this.specBuilder = specBuilder;
    }

    public Page<CatalogShoe> search(
            String naturalLanguageQuery,
            AISearchFilterCriteria criteria,
            Pageable pageable
    ) {
        Specification<CatalogShoe> baseSpec = specBuilder.build(criteria);
        Specification<CatalogShoe> latestDateSpec = buildLatestCollectedDateSpec(criteria);
        if (latestDateSpec != null) {
            baseSpec = baseSpec.and(latestDateSpec);
        }

        Page<CatalogShoe> page;

        if (StringUtils.hasText(criteria.getSortBy())) {
            Sort priceSort = "price_asc".equals(criteria.getSortBy())
                    ? Sort.by("priceSale").ascending()
                    : Sort.by("priceSale").descending();

            Pageable pricePageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    priceSort
            );
            page = catalogShoeRepository.findAll(baseSpec, pricePageable);
        } else {
            page = catalogShoeRepository.findAll(baseSpec, pageable);
        }

        if (page.isEmpty()) {
            log.info("Search returned 0 results for query='{}' with filters brands={}, sizes={}, sortBy={}, page={}, size={}",
                    naturalLanguageQuery,
                    criteria.getBrands(),
                    criteria.getSizes(),
                    criteria.getSortBy(),
                    pageable.getPageNumber(),
                    pageable.getPageSize());
        } else {
            log.info("Search query='{}' yielded {} results (page {} of {}, filters: brands={}, sizes={}, sortBy={})",
                    naturalLanguageQuery,
                    page.getTotalElements(),
                    page.getNumber(),
                    page.getTotalPages(),
                    criteria.getBrands(),
                    criteria.getSizes(),
                    criteria.getSortBy());
        }

        return page;
    }

    private Specification<CatalogShoe> buildLatestCollectedDateSpec(AISearchFilterCriteria criteria) {
        List<CatalogShoeRepository.LatestData> latestByBrand = catalogShoeRepository.findLatestDatePerBrand();
        if (latestByBrand == null || latestByBrand.isEmpty()) {
            return null;
        }

        Set<String> brandSet = normalizeBrands(criteria.getBrands());
        Integer latestDateKey;
        if (!brandSet.isEmpty()) {
            latestDateKey = latestByBrand.stream()
                    .filter(item -> item.getBrand() != null && brandSet.contains(item.getBrand().toLowerCase()))
                    .map(item -> parseDateKey(item.getLatestDwid()))
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(null);
        } else {
            latestDateKey = latestByBrand.stream()
                    .map(item -> parseDateKey(item.getLatestDwid()))
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(null);
        }

        if (latestDateKey == null) {
            return null;
        }

        return ProductSpecs.collectedOnDateKey(latestDateKey);
    }

    private static Set<String> normalizeBrands(List<String> brands) {
        if (brands == null || brands.isEmpty()) {
            return Set.of();
        }
        return brands.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(brand -> !brand.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private static Integer parseDateKey(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() != 8) {
            return null;
        }
        try {
            return Integer.valueOf(digits);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
