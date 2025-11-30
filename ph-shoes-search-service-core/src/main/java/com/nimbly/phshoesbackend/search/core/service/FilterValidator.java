package com.nimbly.phshoesbackend.search.core.service;

import com.nimbly.phshoesbackend.search.core.exception.AiSearchException;
import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class FilterValidator {

    private final PreFilterExtractor preExtractor;

    public FilterValidator(PreFilterExtractor preExtractor) {
        this.preExtractor = preExtractor;
    }

    public void validate(AISearchFilterCriteria c) {
        sanitizeBrands(c);

        if (c.getPriceSaleMin() != null && c.getPriceSaleMin() < 0 ||
                c.getPriceSaleMax() != null && c.getPriceSaleMax() < 0) {
            log.warn("Negative priceSale filter");
            throw new AiSearchException("Negative priceSale filter", null);
        }
        if (c.getPriceSaleMin() != null && c.getPriceSaleMax() != null
                && c.getPriceSaleMin() > c.getPriceSaleMax()) {
            log.warn("priceSaleMin > priceSaleMax");
            throw new AiSearchException("priceSaleMin > priceSaleMax", null);
        }
        if (c.getSortBy() != null &&
                !Set.of("price_asc", "price_desc").contains(c.getSortBy())) {
            log.warn("Invalid sortBy: {}", c.getSortBy());
            throw new AiSearchException("Invalid sortBy: " + c.getSortBy(), null);
        }
        if (c.getGender() != null && !Set.of("male", "female", "unisex").contains(c.getGender())) {
            log.warn("Invalid gender: {}", c.getGender());
            throw new AiSearchException("Invalid gender: " + c.getGender(), null);
        }
        if (c.getTitleKeywords() == null) c.setTitleKeywords(List.of());
        if (c.getSubtitleKeywords() == null) c.setSubtitleKeywords(List.of());
    }

    private void sanitizeBrands(AISearchFilterCriteria c) {
        List<String> brands = c.getBrands();
        if (brands == null || brands.isEmpty()) {
            return;
        }

        List<String> valid = new ArrayList<>();
        List<String> demoted = new ArrayList<>();

        for (String brand : brands) {
            String normalized = brand == null ? null : brand.trim();
            if (!StringUtils.hasText(normalized)) {
                continue;
            }
            String compact = normalized.toLowerCase().replaceAll("\\s+", "");
            if (preExtractor.isKnownBrand(compact)) {
                valid.add(compact);
            } else {
                demoted.add(normalized);
            }
        }

        c.setBrands(valid.isEmpty() ? null : valid);

        if (!demoted.isEmpty()) {
            log.info("Demoting unknown brands to keywords: {}", demoted);
            List<String> title = new ArrayList<>(c.getTitleKeywords() == null ? List.of() : c.getTitleKeywords());
            title.addAll(demoted.stream()
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(String::toLowerCase)
                    .toList());
            c.setTitleKeywords(title);
        }
    }
}
