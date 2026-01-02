package com.nimbly.phshoesbackend.search.core.util;

import com.nimbly.phshoesbackend.catalog.core.model.CatalogShoe;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductSpecs {

    private ProductSpecs() {}

    public static Specification<CatalogShoe> brandIn(List<String> brands) {
        return (root, query, cb) -> {
            if (brands == null || brands.isEmpty()) return cb.conjunction();
            Expression<String> brand = cb.lower(root.get("brand"));
            List<Predicate> predicates = new ArrayList<>();
            for (String brandValue : brands) {
                predicates.add(cb.equal(brand, brandValue.toLowerCase()));
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<CatalogShoe> priceSaleMin(double v) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("priceSale"), v);
    }

    public static Specification<CatalogShoe> priceSaleMax(double v) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("priceSale"), v);
    }

    public static Specification<CatalogShoe> priceOriginalMin(double v) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("priceOriginal"), v);
    }

    public static Specification<CatalogShoe> priceOriginalMax(double v) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("priceOriginal"), v);
    }

    public static Specification<CatalogShoe> onSale() {
        return (root, query, cb) -> cb.lessThan(root.get("priceSale"), root.get("priceOriginal"));
    }

    public static Specification<CatalogShoe> titleMatchesPhrase(String phrase) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + phrase.toLowerCase() + "%");
    }

    public static Specification<CatalogShoe> titleContainsAny(List<String> phrases) {
        return (Root<CatalogShoe> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (phrases == null || phrases.isEmpty()) return cb.conjunction();
            Expression<String> title = cb.lower(root.get("title"));
            Predicate[] titleMatches = phrases.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(phrase -> !phrase.isBlank())
                    .map(phrase -> cb.like(title, "%" + phrase + "%"))
                    .toArray(Predicate[]::new);
            return titleMatches.length == 0 ? cb.conjunction() : cb.or(titleMatches);
        };
    }

    public static Specification<CatalogShoe> subtitleContainsAny(List<String> phrases) {
        return (Root<CatalogShoe> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (phrases == null || phrases.isEmpty()) return cb.conjunction();
            Expression<String> subtitle = cb.lower(root.get("subtitle"));
            Predicate[] subtitleMatches = phrases.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(phrase -> !phrase.isBlank())
                    .map(phrase -> cb.like(subtitle, "%" + phrase + "%"))
                    .toArray(Predicate[]::new);
            return subtitleMatches.length == 0 ? cb.conjunction() : cb.or(subtitleMatches);
        };
    }

    public static Specification<CatalogShoe> sizeAnyInExtrasTextJson(List<String> sizes) {
        return (root, query, cb) -> {
            if (sizes == null || sizes.isEmpty()) return cb.conjunction();

            Expression<String> extraTxt = cb.function("TO_VARCHAR", String.class, root.get("extra"));

            Predicate[] sizeMatches = sizes.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(sizeValue -> !sizeValue.isEmpty())
                    .map(sizeValue -> {
                        String esc = sizeValue.replace(".", "\\.");
                        return cb.isTrue(cb.function(
                                "REGEXP_LIKE", Boolean.class,
                                extraTxt,
                                cb.literal("\\\"sizes\\\"\\s*:\\s*\\[[^\\]]*\\\"" + esc + "\\\"")
                        ));
                    })
                    .toArray(Predicate[]::new);
            return cb.or(sizeMatches);
        };
    }
}

