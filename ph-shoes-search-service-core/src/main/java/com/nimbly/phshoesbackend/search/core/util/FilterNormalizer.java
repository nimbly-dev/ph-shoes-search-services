package com.nimbly.phshoesbackend.search.core.util;

import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilterNormalizer {

    public static void normalize(AISearchFilterCriteria criteria) {
        if (criteria == null) return;
        normalizeBrands(criteria);
        normalizeGender(criteria);
        normalizeKeywordLists(criteria);
        disambiguateSizeVsModel(criteria);
        normalizePrices(criteria);
        normalizeSizes(criteria);
    }

    private static void normalizeBrands(AISearchFilterCriteria criteria) {
        if (criteria.getBrands() == null) return;
        List<String> normalizedBrands = criteria.getBrands().stream()
                .filter(Objects::nonNull)
                .map(brand -> brand.trim().toLowerCase())
                .map(brand -> brand.replaceAll("\\s+", ""))
                .filter(brand -> !brand.isBlank())
                .distinct()
                .collect(Collectors.toList());
        criteria.setBrands(normalizedBrands.isEmpty() ? null : normalizedBrands);
    }

    private static void normalizeGender(AISearchFilterCriteria criteria) {
        if (criteria.getGender() == null) return;
        String normalizedGender = criteria.getGender().trim().toLowerCase();
        if (normalizedGender.startsWith("m")) normalizedGender = "male";
        else if (normalizedGender.startsWith("f")) normalizedGender = "female";
        else if (normalizedGender.contains("kid")) normalizedGender = "kids";
        else if (normalizedGender.contains("uni")) normalizedGender = "unisex";
        criteria.setGender(normalizedGender);
    }

    private static void normalizeKeywordLists(AISearchFilterCriteria criteria) {
        if (criteria.getTitleKeywords() != null) {
            criteria.setTitleKeywords(criteria.getTitleKeywords().stream()
                    .filter(Objects::nonNull)
                    .map(keyword -> keyword.trim().toLowerCase())
                    .filter(keyword -> !keyword.isBlank())
                    .distinct()
                    .collect(Collectors.toList()));
            if (criteria.getTitleKeywords().isEmpty()) criteria.setTitleKeywords(null);
        }
        if (criteria.getSubtitleKeywords() != null) {
            criteria.setSubtitleKeywords(criteria.getSubtitleKeywords().stream()
                    .filter(Objects::nonNull)
                    .map(keyword -> keyword.trim().toLowerCase())
                    .filter(keyword -> !keyword.isBlank())
                    .distinct()
                    .collect(Collectors.toList()));
            if (criteria.getSubtitleKeywords().isEmpty()) criteria.setSubtitleKeywords(null);
        }
    }

    private static void normalizePrices(AISearchFilterCriteria criteria) {
        Double sMin = criteria.getPriceSaleMin();
        Double sMax = criteria.getPriceSaleMax();

        if (sMin != null && sMax != null) {
            if (Double.compare(sMin, sMax) == 0) {
                criteria.setPriceSaleMin(null);
            } else if (sMin > sMax) {
                criteria.setPriceSaleMin(sMax);
                criteria.setPriceSaleMax(sMin);
            }
        }

        Double oMin = criteria.getPriceOriginalMin();
        Double oMax = criteria.getPriceOriginalMax();

        if (oMin != null && oMax != null) {
            if (Double.compare(oMin, oMax) == 0) {
                criteria.setPriceOriginalMin(null);
            } else if (oMin > oMax) {
                criteria.setPriceOriginalMin(oMax);
                criteria.setPriceOriginalMax(oMin);
            }
        }

        if (criteria.getPriceSaleMin() != null || criteria.getPriceSaleMax() != null) {
            if (Objects.equals(criteria.getPriceOriginalMin(), criteria.getPriceSaleMin())) criteria.setPriceOriginalMin(null);
            if (Objects.equals(criteria.getPriceOriginalMax(), criteria.getPriceSaleMax())) criteria.setPriceOriginalMax(null);
        }
    }

    private static void disambiguateSizeVsModel(AISearchFilterCriteria criteria) {
        List<String> sizes = criteria.getSizes();
        if (sizes == null || sizes.isEmpty()) return;

        String modelLc = Optional.ofNullable(criteria.getModel()).orElse("").toLowerCase();
        String titleKwLc = String.join(" ",
                Optional.ofNullable(criteria.getTitleKeywords()).orElse(List.of())
        ).toLowerCase();

        boolean unitInSizes = sizes.stream()
                .filter(Objects::nonNull)
                .map(sizeToken -> sizeToken.toLowerCase().trim())
                .anyMatch(sizeToken -> sizeToken.matches("^(?:us|eu|uk)\\s*\\d+(?:\\.\\d+)?")
                        || sizeToken.startsWith("size "));

        boolean cueInKeywords =
                containsCueTokens(criteria.getSubtitleKeywords()) || containsCueTokens(criteria.getTitleKeywords());

        boolean hasExplicitSizeCue = unitInSizes || cueInKeywords;

        boolean overlapsModel = sizes.stream()
                .filter(Objects::nonNull)
                .map(sizeToken -> sizeToken.trim().toLowerCase())
                .filter(sizeToken -> !sizeToken.isEmpty())
                .anyMatch(sizeToken -> {
                    String boundary = "\\b" + Pattern.quote(sizeToken) + "\\b";
                    return Pattern.compile(boundary).matcher(modelLc).find()
                            || Pattern.compile(boundary).matcher(titleKwLc).find();
                });

        if (!hasExplicitSizeCue && overlapsModel) {
            criteria.setSizes(null);
        }
    }

    private static boolean containsCueTokens(List<String> words) {
        if (words == null || words.isEmpty()) return false;
        for (String word : words) {
            if (word == null) continue;
            String token = word.toLowerCase();
            if (token.contains(" size ") || token.startsWith("size ")
                    || token.equals("size") || token.equals("sizes")
                    || token.contains(" us ") || token.startsWith("us ")
                    || token.contains(" eu ") || token.startsWith("eu ")
                    || token.contains(" uk ") || token.startsWith("uk ")) {
                return true;
            }
        }
        return false;
    }

    private static void normalizeSizes(AISearchFilterCriteria criteria) {
        if (criteria.getSizes() == null) return;
        List<String> normalizedSizes = criteria.getSizes().stream()
                .filter(Objects::nonNull)
                .map(sizeToken -> sizeToken.trim().toLowerCase())
                .map(sizeToken -> sizeToken.replaceAll("^(us|eu|uk)\\s*", ""))
                .map(sizeToken -> sizeToken.replaceAll("[^0-9\\.]", ""))
                .filter(sizeToken -> !sizeToken.isBlank())
                .distinct()
                .collect(Collectors.toList());
        criteria.setSizes(normalizedSizes.isEmpty() ? null : normalizedSizes);
    }
}

