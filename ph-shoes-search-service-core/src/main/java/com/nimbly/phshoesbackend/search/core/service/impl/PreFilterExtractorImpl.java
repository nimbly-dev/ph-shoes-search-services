package com.nimbly.phshoesbackend.search.core.service.impl;

import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;
import com.nimbly.phshoesbackend.search.core.service.PreFilterExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PreFilterExtractorImpl implements PreFilterExtractor {

    private static final Set<String> BRANDS = Set.of(
            "nike", "adidas", "new balance", "newbalance", "asics", "world balance", "worldbalance", "hoka"
    );

    private static final Pattern UNDER = Pattern.compile("\\b(?:under|below)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern OVER  = Pattern.compile("\\b(?:over|above)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern SIZES = Pattern.compile(
            "(?:\\bsize(?:s)?\\b\\s*([0-9]+(?:\\.[05])?(?:\\s*(?:,|and)\\s*[0-9]+(?:\\.[05])?)*))" +
                    "|\\b(?:us|eu|uk)?\\s*([0-9]+(?:\\.[05])?)\\b",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public AISearchFilterCriteria extract(String q) {
        String lower = q == null ? "" : q.toLowerCase();
        AISearchFilterCriteria c = new AISearchFilterCriteria();

        List<String> found = BRANDS.stream()
                .filter(b -> lower.contains(b))
                .map(b -> b.replaceAll("\\s+", ""))
                .collect(Collectors.toList());
        if (!found.isEmpty()) c.setBrands(found);

        c.setOnSale(lower.contains("on sale"));

        Matcher m = UNDER.matcher(lower);
        if (m.find()) c.setPriceSaleMax(Double.valueOf(m.group(1)));
        m = OVER.matcher(lower);
        if (m.find()) c.setPriceSaleMin(Double.valueOf(m.group(1)));

        if (lower.matches(".*\\b(cheapest|lowest)\\b.*")) {
            c.setSortBy("price_asc");
        } else if (lower.matches(".*\\b(most expensive|highest price)\\b.*")) {
            c.setSortBy("price_desc");
        }

        boolean hasSizeCue = lower.matches(".*\\b(size|sizes|us|eu|uk)\\b.*");
        if (hasSizeCue) {
            List<String> sizes = new ArrayList<>();
            Matcher sm = SIZES.matcher(q == null ? "" : q);
            while (sm.find()) {
                String group = sm.group(1) != null ? sm.group(1) : sm.group(2);
                if (group != null) {
                    for (String tok : group.split("\\s*(?:,|and)\\s*")) {
                        String norm = tok.trim().toLowerCase().replaceAll("^(us|eu|uk)\\s*", "")
                                .replaceAll("[^0-9\\.]", "");
                        if (!norm.isBlank()) sizes.add(norm);
                    }
                }
            }
            if (!sizes.isEmpty()) {
                c.setSizes(sizes);
                log.info("Extracted explicit sizes {} from query='{}'", sizes, q);
            }
        }

        return c;
    }

    @Override
    public String strip(String q) {
        if (q == null) return "";
        String out = q;
        for (String b : BRANDS) {
            out = out.replaceAll("(?i)\\b" + Pattern.quote(b) + "\\b", "");
        }
        out = out.replaceAll(
                "(?i)\\b(on sale|under|below|over|above|cheapest|lowest|most expensive|highest price|size|sizes|us|eu|uk)\\b",
                ""
        );
        return out.trim().replaceAll("\\s{2,}", " ");
    }

    @Override
    public boolean isKnownBrand(String brand) {
        return brand != null && BRANDS.contains(brand.toLowerCase().replaceAll("\\s+", ""));
    }
}
