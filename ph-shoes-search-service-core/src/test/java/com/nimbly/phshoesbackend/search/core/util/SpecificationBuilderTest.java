package com.nimbly.phshoesbackend.search.core.util;

import com.nimbly.phshoesbackend.catalog.core.model.CatalogShoe;
import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpecificationBuilderTest {

    @Test
    void build_includesModelAndPricingFilters() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        criteria.setBrands(List.of("nike"));
        criteria.setModel("air max");
        criteria.setPriceSaleMin(1000.0);
        criteria.setPriceSaleMax(5000.0);
        criteria.setPriceOriginalMin(1500.0);
        criteria.setPriceOriginalMax(6000.0);
        criteria.setOnSale(true);

        SpecificationBuilder builder = new SpecificationBuilder();
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Root<CatalogShoe> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Predicate predicate = mock(Predicate.class);
        @SuppressWarnings({"unchecked", "rawtypes"})
        Path brandPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<String> brandExpression = mock(Expression.class);

        when(criteriaBuilder.conjunction()).thenReturn(predicate);
        when(root.get("brand")).thenReturn(brandPath);
        when(criteriaBuilder.lower(brandPath)).thenReturn(brandExpression);
        when(criteriaBuilder.equal(eq(brandExpression), any())).thenReturn(predicate);
        when(criteriaBuilder.greaterThanOrEqualTo(any(Path.class), any(Double.class))).thenReturn(predicate);
        when(criteriaBuilder.lessThanOrEqualTo(any(Path.class), any(Double.class))).thenReturn(predicate);
        when(criteriaBuilder.lessThan(any(Path.class), any(Path.class))).thenReturn(predicate);
        when(criteriaBuilder.like(any(Expression.class), any(String.class))).thenReturn(predicate);
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(predicate);
        when(criteriaBuilder.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);

        // Act
        builder.build(criteria).toPredicate(root, query, criteriaBuilder);
    }

    @Test
    void build_includesKeywordFilters() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        criteria.setTitleKeywords(List.of("running"));
        criteria.setSubtitleKeywords(List.of("kids"));

        SpecificationBuilder builder = new SpecificationBuilder();
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Root<CatalogShoe> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Predicate predicate = mock(Predicate.class);
        @SuppressWarnings({"unchecked", "rawtypes"})
        Path titlePath = mock(Path.class);
        @SuppressWarnings({"unchecked", "rawtypes"})
        Path subtitlePath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<String> titleExpression = mock(Expression.class);
        @SuppressWarnings("unchecked")
        Expression<String> subtitleExpression = mock(Expression.class);

        when(criteriaBuilder.conjunction()).thenReturn(predicate);
        when(root.get("title")).thenReturn(titlePath);
        when(root.get("subtitle")).thenReturn(subtitlePath);
        when(criteriaBuilder.lower(titlePath)).thenReturn(titleExpression);
        when(criteriaBuilder.lower(subtitlePath)).thenReturn(subtitleExpression);
        when(criteriaBuilder.like(any(Expression.class), any(String.class))).thenReturn(predicate);
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(predicate);
        when(criteriaBuilder.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);

        // Act
        builder.build(criteria).toPredicate(root, query, criteriaBuilder);
    }
}
