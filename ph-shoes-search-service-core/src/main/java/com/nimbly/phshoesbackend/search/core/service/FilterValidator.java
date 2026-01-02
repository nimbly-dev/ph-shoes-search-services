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

    public void validate(AISearchFilterCriteria criteria) {
        sanitizeBrands(criteria);

        if (criteria.getPriceSaleMin() != null && criteria.getPriceSaleMin() < 0 ||
                criteria.getPriceSaleMax() != null && criteria.getPriceSaleMax() < 0) {
            log.warn("Negative priceSale filter");
            throw new AiSearchException("Negative priceSale filter", null);
        }
        if (criteria.getPriceSaleMin() != null && criteria.getPriceSaleMax() != null
                && criteria.getPriceSaleMin() > criteria.getPriceSaleMax()) {
            log.warn("priceSaleMin > priceSaleMax");
            throw new AiSearchException("priceSaleMin > priceSaleMax", null);
        }
        if (criteria.getSortBy() != null &&
                !Set.of("price_asc", "price_desc").contains(criteria.getSortBy())) {
            log.warn("Invalid sortBy: {}", criteria.getSortBy());
            throw new AiSearchException("Invalid sortBy: " + criteria.getSortBy(), null);
        }
        if (criteria.getGender() != null && !Set.of("male", "female", "kids", "unisex").contains(criteria.getGender())) {
            log.warn("Invalid gender: {}", criteria.getGender());
            throw new AiSearchException("Invalid gender: " + criteria.getGender(), null);
        }
        if (criteria.getGender() != null) {
            List<String> subtitleKeywords = new ArrayList<>(criteria.getSubtitleKeywords() == null
                    ? List.of()
                    : criteria.getSubtitleKeywords());
            subtitleKeywords.add(criteria.getGender());
            criteria.setSubtitleKeywords(subtitleKeywords);
        }
        if (criteria.getTitleKeywords() == null) criteria.setTitleKeywords(List.of());
        if (criteria.getSubtitleKeywords() == null) criteria.setSubtitleKeywords(List.of());
    }

    private void sanitizeBrands(AISearchFilterCriteria criteria) {
        List<String> brands = criteria.getBrands();
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

        criteria.setBrands(valid.isEmpty() ? null : valid);

        if (!demoted.isEmpty()) {
            log.info("Demoting unknown brands to keywords: {}", demoted);
            List<String> title = new ArrayList<>(criteria.getTitleKeywords() == null ? List.of() : criteria.getTitleKeywords());
            title.addAll(demoted.stream()
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(String::toLowerCase)
                    .toList());
            criteria.setTitleKeywords(title);
        }
    }
}
