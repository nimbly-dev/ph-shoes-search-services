package com.nimbly.phshoesbackend.search.core.util;

import com.nimbly.phshoesbackend.catalog.core.model.FactProductShoes;
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

    public static Specification<FactProductShoes> brandIn(List<String> brands) {
        return (root, query, cb) -> {
            if (brands == null || brands.isEmpty()) return cb.conjunction();
            Expression<String> brand = cb.lower(root.get("brand"));
            List<Predicate> ps = new ArrayList<>();
            for (String b : brands) {
                ps.add(cb.equal(brand, b.toLowerCase()));
            }
            return cb.or(ps.toArray(new Predicate[0]));
        };
    }

    public static Specification<FactProductShoes> priceSaleMin(double v) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("priceSale"), v);
    }

    public static Specification<FactProductShoes> priceSaleMax(double v) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("priceSale"), v);
    }

    public static Specification<FactProductShoes> priceOriginalMin(double v) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("priceOriginal"), v);
    }

    public static Specification<FactProductShoes> priceOriginalMax(double v) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("priceOriginal"), v);
    }

    public static Specification<FactProductShoes> onSale() {
        return (root, query, cb) -> cb.lessThan(root.get("priceSale"), root.get("priceOriginal"));
    }

    public static Specification<FactProductShoes> titleMatchesPhrase(String phrase) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + phrase.toLowerCase() + "%");
    }

    public static Specification<FactProductShoes> titleContainsAny(List<String> phrases) {
        return (Root<FactProductShoes> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (phrases == null || phrases.isEmpty()) return cb.conjunction();
            Expression<String> title = cb.lower(root.get("title"));
            Predicate[] likes = phrases.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(s -> !s.isBlank())
                    .map(s -> cb.like(title, "%" + s + "%"))
                    .toArray(Predicate[]::new);
            return likes.length == 0 ? cb.conjunction() : cb.or(likes);
        };
    }

    public static Specification<FactProductShoes> titleContainsAll(List<String> phrases) {
        return (root, query, cb) -> {
            if (phrases == null || phrases.isEmpty()) return cb.conjunction();
            Expression<String> title = cb.lower(root.get("title"));
            Predicate[] likes = phrases.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(s -> !s.isBlank())
                    .map(s -> cb.like(title, "%" + s + "%"))
                    .toArray(Predicate[]::new);
            return likes.length == 0 ? cb.conjunction() : cb.and(likes);
        };
    }

    public static Specification<FactProductShoes> latestOnly() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<FactProductShoes> sizeAnyInExtrasTextJson(List<String> sizes) {
        return (root, query, cb) -> {
            if (sizes == null || sizes.isEmpty()) return cb.conjunction();

            Expression<String> extraTxt = cb.function("TO_VARCHAR", String.class, root.get("extra"));

            Predicate[] any = sizes.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> {
                        String esc = s.replace(".", "\\.");
                        return cb.isTrue(cb.function(
                                "REGEXP_LIKE", Boolean.class,
                                extraTxt,
                                cb.literal("\\\"sizes\\\"\\s*:\\s*\\[[^\\]]*\\\"" + esc + "\\\"")
                        ));
                    })
                    .toArray(Predicate[]::new);
            return cb.or(any);
        };
    }
}

