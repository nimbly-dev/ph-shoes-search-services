package com.nimbly.phshoesbackend.search.core.util;

import com.nimbly.phshoesbackend.catalog.core.model.CatalogShoe;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductSpecsTest {

    @Test
    void brandIn_buildsPredicates() {
        // Arrange
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Root<CatalogShoe> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        @SuppressWarnings({"unchecked", "rawtypes"})
        Path brandPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<String> brandExpression = mock(Expression.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("brand")).thenReturn(brandPath);
        when(criteriaBuilder.lower(brandPath)).thenReturn(brandExpression);
        when(criteriaBuilder.equal(brandExpression, "nike")).thenReturn(predicate);
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(predicate);

        // Act
        ProductSpecs.brandIn(List.of("Nike")).toPredicate(root, query, criteriaBuilder);

        // Assert
        verify(criteriaBuilder).lower(brandPath);
        verify(criteriaBuilder).equal(brandExpression, "nike");
        verify(criteriaBuilder).or(any(Predicate[].class));
    }

    @Test
    void titleContainsAny_buildsLikePredicates() {
        // Arrange
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Root<CatalogShoe> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        @SuppressWarnings({"unchecked", "rawtypes"})
        Path titlePath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<String> titleExpression = mock(Expression.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("title")).thenReturn(titlePath);
        when(criteriaBuilder.lower(titlePath)).thenReturn(titleExpression);
        when(criteriaBuilder.like(titleExpression, "%running%")).thenReturn(predicate);
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(predicate);

        // Act
        ProductSpecs.titleContainsAny(List.of("Running")).toPredicate(root, query, criteriaBuilder);

        // Assert
        verify(criteriaBuilder).like(titleExpression, "%running%");
        verify(criteriaBuilder).or(any(Predicate[].class));
    }

    @Test
    void sizeAnyInExtrasTextJson_buildsRegexPredicate() {
        // Arrange
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Root<CatalogShoe> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        @SuppressWarnings({"unchecked", "rawtypes"})
        Path extraPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Expression<String> extraExpression = mock(Expression.class);
        @SuppressWarnings("unchecked")
        Expression<Boolean> regexExpression = mock(Expression.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("extra")).thenReturn(extraPath);
        when(criteriaBuilder.function(eq("TO_VARCHAR"), eq(String.class), eq(extraPath))).thenReturn(extraExpression);
        when(criteriaBuilder.literal(Mockito.anyString())).thenAnswer(invocation -> mock(Expression.class));
        when(criteriaBuilder.function(eq("REGEXP_LIKE"), eq(Boolean.class), eq(extraExpression), any(Expression.class)))
                .thenReturn(regexExpression);
        when(criteriaBuilder.isTrue(regexExpression)).thenReturn(predicate);
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(predicate);

        // Act
        ProductSpecs.sizeAnyInExtrasTextJson(List.of("10")).toPredicate(root, query, criteriaBuilder);

        // Assert
        verify(criteriaBuilder).function(eq("TO_VARCHAR"), eq(String.class), eq(extraPath));
        verify(criteriaBuilder).function(eq("REGEXP_LIKE"), eq(Boolean.class), eq(extraExpression), any(Expression.class));
        verify(criteriaBuilder).isTrue(regexExpression);
    }
}
