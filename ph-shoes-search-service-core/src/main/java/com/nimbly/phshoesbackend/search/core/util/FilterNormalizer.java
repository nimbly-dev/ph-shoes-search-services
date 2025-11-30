package com.nimbly.phshoesbackend.search.core.util;

import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class FilterNormalizer {

    public static void normalize(AISearchFilterCriteria c) {
        if (c == null) return;
        normalizeBrands(c);
        normalizeGender(c);
        normalizeKeywordLists(c);
        disambiguateSizeVsModel(c);
        normalizePrices(c);
        normalizeSizes(c);
    }

    private static void normalizeBrands(AISearchFilterCriteria c) {
        if (c.getBrands() == null) return;
        List<String> b = c.getBrands().stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase())
                .map(s -> s.replaceAll("\\s+", ""))
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
        c.setBrands(b.isEmpty() ? null : b);
    }

    private static void normalizeGender(AISearchFilterCriteria c) {
        if (c.getGender() == null) return;
        String g = c.getGender().trim().toLowerCase();
        if (g.startsWith("m")) g = "male";
        else if (g.startsWith("f")) g = "female";
        else if (g.contains("kid")) g = "kids";
        else if (g.contains("uni")) g = "unisex";
        c.setGender(g);
    }

    private static void normalizeKeywordLists(AISearchFilterCriteria c) {
        if (c.getTitleKeywords() != null) {
            c.setTitleKeywords(c.getTitleKeywords().stream()
                    .filter(Objects::nonNull)
                    .map(s -> s.trim().toLowerCase())
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .collect(Collectors.toList()));
            if (c.getTitleKeywords().isEmpty()) c.setTitleKeywords(null);
        }
        if (c.getSubtitleKeywords() != null) {
            c.setSubtitleKeywords(c.getSubtitleKeywords().stream()
                    .filter(Objects::nonNull)
                    .map(s -> s.trim().toLowerCase())
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .collect(Collectors.toList()));
            if (c.getSubtitleKeywords().isEmpty()) c.setSubtitleKeywords(null);
        }
    }

    private static void normalizePrices(AISearchFilterCriteria c) {
        Double sMin = c.getPriceSaleMin();
        Double sMax = c.getPriceSaleMax();

        if (sMin != null && sMax != null) {
            if (Double.compare(sMin, sMax) == 0) {
                c.setPriceSaleMin(null);
            } else if (sMin > sMax) {
                c.setPriceSaleMin(sMax);
                c.setPriceSaleMax(sMin);
            }
        }

        Double oMin = c.getPriceOriginalMin();
        Double oMax = c.getPriceOriginalMax();

        if (oMin != null && oMax != null) {
            if (Double.compare(oMin, oMax) == 0) {
                c.setPriceOriginalMin(null);
            } else if (oMin > oMax) {
                c.setPriceOriginalMin(oMax);
                c.setPriceOriginalMax(oMin);
            }
        }

        if (c.getPriceSaleMin() != null || c.getPriceSaleMax() != null) {
            if (Objects.equals(c.getPriceOriginalMin(), c.getPriceSaleMin())) c.setPriceOriginalMin(null);
            if (Objects.equals(c.getPriceOriginalMax(), c.getPriceSaleMax())) c.setPriceOriginalMax(null);
        }
    }

    private static void disambiguateSizeVsModel(AISearchFilterCriteria c) {
        List<String> sizes = c.getSizes();
        if (sizes == null || sizes.isEmpty()) return;

        String modelLc = Optional.ofNullable(c.getModel()).orElse("").toLowerCase();
        String titleKwLc = String.join(" ",
                Optional.ofNullable(c.getTitleKeywords()).orElse(List.of())
        ).toLowerCase();

        boolean unitInSizes = sizes.stream()
                .filter(Objects::nonNull)
                .map(s -> s.toLowerCase().trim())
                .anyMatch(s -> s.matches("^(?:us|eu|uk)\\s*\\d+(?:\\.\\d+)?") || s.startsWith("size "));

        boolean cueInKeywords =
                containsCueTokens(c.getSubtitleKeywords()) || containsCueTokens(c.getTitleKeywords());

        boolean hasExplicitSizeCue = unitInSizes || cueInKeywords;

        boolean overlapsModel = sizes.stream()
                .filter(Objects::nonNull)
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isEmpty())
                .anyMatch(s -> {
                    String boundary = "\\b" + Pattern.quote(s) + "\\b";
                    return Pattern.compile(boundary).matcher(modelLc).find()
                            || Pattern.compile(boundary).matcher(titleKwLc).find();
                });

        if (!hasExplicitSizeCue && overlapsModel) {
            c.setSizes(null);
        }
    }

    private static boolean containsCueTokens(List<String> words) {
        if (words == null || words.isEmpty()) return false;
        for (String w : words) {
            if (w == null) continue;
            String s = w.toLowerCase();
            if (s.contains(" size ") || s.startsWith("size ")
                    || s.equals("size") || s.equals("sizes")
                    || s.contains(" us ") || s.startsWith("us ")
                    || s.contains(" eu ") || s.startsWith("eu ")
                    || s.contains(" uk ") || s.startsWith("uk ")) {
                return true;
            }
        }
        return false;
    }

    private static void normalizeSizes(AISearchFilterCriteria c) {
        if (c.getSizes() == null) return;
        List<String> s = c.getSizes().stream()
                .filter(Objects::nonNull)
                .map(x -> x.trim().toLowerCase())
                .map(x -> x.replaceAll("^(us|eu|uk)\\s*", ""))
                .map(x -> x.replaceAll("[^0-9\\.]", ""))
                .filter(x -> !x.isBlank())
                .distinct()
                .collect(Collectors.toList());
        c.setSizes(s.isEmpty() ? null : s);
    }
}

