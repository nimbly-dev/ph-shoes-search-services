package com.nimbly.phshoesbackend.search.core.service.impl;

import com.nimbly.phshoesbackend.catalog.core.model.CatalogShoe;
import com.nimbly.phshoesbackend.catalog.core.repository.jpa.CatalogShoeRepository;
import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;
import com.nimbly.phshoesbackend.search.core.util.SpecificationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchOrchestratorTest {

    @Mock
    private CatalogShoeRepository catalogShoeRepository;

    @Mock
    private SpecificationBuilder specificationBuilder;

    private SearchOrchestrator orchestrator;

    @BeforeEach
    void setup() {
        orchestrator = new SearchOrchestrator(catalogShoeRepository, specificationBuilder);
    }

    @Test
    void search_appliesPriceSortWhenSortByPresent() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        criteria.setSortBy("price_asc");
        Specification<CatalogShoe> spec = (root, query, cb) -> cb.conjunction();
        CatalogShoe latest = new CatalogShoe();
        latest.setId("id-1");
        latest.setDwid("dwid-1");
        latest.setYear(2025);
        latest.setMonth(2);
        latest.setDay(20);
        Page<CatalogShoe> latestPage = new PageImpl<>(List.of(latest));
        Page<CatalogShoe> page = new PageImpl<>(List.of());
        when(specificationBuilder.build(criteria)).thenReturn(spec);
        when(catalogShoeRepository.findAll(any(Specification.class), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(latestPage, page);

        Pageable inputPageable = PageRequest.of(1, 5, Sort.by("brand"));

        // Act
        orchestrator.search("query", criteria, inputPageable);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(catalogShoeRepository, org.mockito.Mockito.times(2))
                .findAll(any(Specification.class), pageableCaptor.capture());
        Pageable secondCall = pageableCaptor.getAllValues().get(1);
        Sort.Order order = secondCall.getSort().getOrderFor("priceSale");
        assertThat(order).isNotNull();
        assertThat(order.isAscending()).isTrue();
    }

    @Test
    void search_usesProvidedPageableWhenNoSortBy() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        Specification<CatalogShoe> spec = (root, query, cb) -> cb.conjunction();
        CatalogShoe latest = new CatalogShoe();
        latest.setId("id-1");
        latest.setDwid("dwid-1");
        latest.setYear(2025);
        latest.setMonth(2);
        latest.setDay(20);
        Page<CatalogShoe> latestPage = new PageImpl<>(List.of(latest));
        Page<CatalogShoe> page = new PageImpl<>(List.of());
        when(specificationBuilder.build(criteria)).thenReturn(spec);
        when(catalogShoeRepository.findAll(any(Specification.class), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(latestPage, page);

        Pageable inputPageable = PageRequest.of(2, 10, Sort.by("brand"));

        // Act
        orchestrator.search("query", criteria, inputPageable);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(catalogShoeRepository, org.mockito.Mockito.times(2))
                .findAll(any(Specification.class), pageableCaptor.capture());
        Pageable secondCall = pageableCaptor.getAllValues().get(1);
        assertThat(secondCall).isEqualTo(inputPageable);
    }

    @Test
    void search_addsLatestDateWhenFound() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        Specification<CatalogShoe> spec = (root, query, cb) -> cb.conjunction();
        CatalogShoe latest = new CatalogShoe();
        latest.setId("id-1");
        latest.setDwid("dwid-1");
        latest.setYear(2025);
        latest.setMonth(2);
        latest.setDay(19);
        Page<CatalogShoe> latestPage = new PageImpl<>(List.of(latest));
        Page<CatalogShoe> page = new PageImpl<>(List.of());
        when(specificationBuilder.build(criteria)).thenReturn(spec);
        when(catalogShoeRepository.findAll(any(Specification.class), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(latestPage, page);

        Pageable inputPageable = PageRequest.of(0, 5);

        // Act
        orchestrator.search("query", criteria, inputPageable);

        // Assert
        ArgumentCaptor<Specification<CatalogShoe>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(catalogShoeRepository, org.mockito.Mockito.times(2))
                .findAll(specCaptor.capture(), any(Pageable.class));
        List<Specification<CatalogShoe>> captured = specCaptor.getAllValues();
        assertThat(captured.get(0)).isSameAs(spec);
        assertThat(captured.get(1)).isNotSameAs(spec);
    }

    @Test
    void search_skipsLatestDateWhenNoLatestData() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        Specification<CatalogShoe> spec = (root, query, cb) -> cb.conjunction();
        Page<CatalogShoe> page = new PageImpl<>(List.of());
        when(specificationBuilder.build(criteria)).thenReturn(spec);
        when(catalogShoeRepository.findAll(any(Specification.class), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()), page);

        Pageable inputPageable = PageRequest.of(0, 5);

        // Act
        orchestrator.search("query", criteria, inputPageable);

        // Assert
        ArgumentCaptor<Specification<CatalogShoe>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(catalogShoeRepository, org.mockito.Mockito.times(2))
                .findAll(specCaptor.capture(), any(Pageable.class));
        List<Specification<CatalogShoe>> captured = specCaptor.getAllValues();
        assertThat(captured.get(0)).isSameAs(spec);
        assertThat(captured.get(1)).isSameAs(spec);
    }

    @Test
    void search_skipsLatestDateWhenBrandNotMatchedOrInvalidDwid() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        Specification<CatalogShoe> spec = (root, query, cb) -> cb.conjunction();
        Page<CatalogShoe> page = new PageImpl<>(List.of());
        when(specificationBuilder.build(criteria)).thenReturn(spec);
        CatalogShoe latest = new CatalogShoe();
        latest.setId("id-1");
        latest.setDwid("dwid-1");
        latest.setYear(null);
        latest.setMonth(2);
        latest.setDay(2);
        when(catalogShoeRepository.findAll(any(Specification.class), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(latest)), page);

        Pageable inputPageable = PageRequest.of(0, 5);

        // Act
        orchestrator.search("query", criteria, inputPageable);

        // Assert
        ArgumentCaptor<Specification<CatalogShoe>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(catalogShoeRepository, org.mockito.Mockito.times(2))
                .findAll(specCaptor.capture(), any(Pageable.class));
        List<Specification<CatalogShoe>> captured = specCaptor.getAllValues();
        assertThat(captured.get(0)).isSameAs(spec);
        assertThat(captured.get(1)).isSameAs(spec);
    }

    @Test
    void search_logsWhenResultsPresent() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        Specification<CatalogShoe> spec = (root, query, cb) -> cb.conjunction();
        CatalogShoe shoe = new CatalogShoe();
        shoe.setId("id-1");
        shoe.setDwid("dwid-1");
        Page<CatalogShoe> page = new PageImpl<>(List.of(shoe));
        when(specificationBuilder.build(criteria)).thenReturn(spec);
        when(catalogShoeRepository.findAll(any(Specification.class), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(shoe)), page);

        Pageable inputPageable = PageRequest.of(0, 5);

        // Act
        orchestrator.search("query", criteria, inputPageable);

        // Assert
        verify(catalogShoeRepository, org.mockito.Mockito.times(2))
                .findAll(any(Specification.class), any(Pageable.class));
    }
}
