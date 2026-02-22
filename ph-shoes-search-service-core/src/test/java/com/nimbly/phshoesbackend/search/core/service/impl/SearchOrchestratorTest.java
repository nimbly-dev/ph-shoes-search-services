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
        Page<CatalogShoe> page = new PageImpl<>(List.of());
        when(specificationBuilder.build(criteria)).thenReturn(spec);
        CatalogShoeRepository.LatestData latestData = new CatalogShoeRepository.LatestData() {
            @Override
            public String getBrand() {
                return "Nike";
            }

            @Override
            public String getLatestDwid() {
                return "20250220";
            }
        };
        when(catalogShoeRepository.findLatestDatePerBrand()).thenReturn(List.of(latestData));
        when(catalogShoeRepository.findAll(any(Specification.class), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(page);

        Pageable inputPageable = PageRequest.of(1, 5, Sort.by("brand"));

        // Act
        orchestrator.search("query", criteria, inputPageable);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(catalogShoeRepository).findLatestDatePerBrand();
        verify(catalogShoeRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("priceSale");
        assertThat(order).isNotNull();
        assertThat(order.isAscending()).isTrue();
    }

    @Test
    void search_usesProvidedPageableWhenNoSortBy() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        Specification<CatalogShoe> spec = (root, query, cb) -> cb.conjunction();
        Page<CatalogShoe> page = new PageImpl<>(List.of());
        when(specificationBuilder.build(criteria)).thenReturn(spec);
        CatalogShoeRepository.LatestData latestData = new CatalogShoeRepository.LatestData() {
            @Override
            public String getBrand() {
                return "Nike";
            }

            @Override
            public String getLatestDwid() {
                return "20250220";
            }
        };
        when(catalogShoeRepository.findLatestDatePerBrand()).thenReturn(List.of(latestData));
        when(catalogShoeRepository.findAll(any(Specification.class), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(page);

        Pageable inputPageable = PageRequest.of(2, 10, Sort.by("brand"));

        // Act
        orchestrator.search("query", criteria, inputPageable);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(catalogShoeRepository).findLatestDatePerBrand();
        verify(catalogShoeRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue()).isEqualTo(inputPageable);
    }

    @Test
    void search_usesBrandSpecificLatestDateWhenBrandsProvided() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        criteria.setBrands(List.of("  Nike  ", "Adidas"));
        Specification<CatalogShoe> spec = (root, query, cb) -> cb.conjunction();
        Page<CatalogShoe> page = new PageImpl<>(List.of());
        when(specificationBuilder.build(criteria)).thenReturn(spec);
        CatalogShoeRepository.LatestData nike = new CatalogShoeRepository.LatestData() {
            @Override
            public String getBrand() {
                return "Nike";
            }

            @Override
            public String getLatestDwid() {
                return "20250219";
            }
        };
        CatalogShoeRepository.LatestData adidas = new CatalogShoeRepository.LatestData() {
            @Override
            public String getBrand() {
                return "Adidas";
            }

            @Override
            public String getLatestDwid() {
                return "20250218";
            }
        };
        when(catalogShoeRepository.findLatestDatePerBrand()).thenReturn(List.of(nike, adidas));
        when(catalogShoeRepository.findAll(any(Specification.class), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(page);

        Pageable inputPageable = PageRequest.of(0, 5);

        // Act
        orchestrator.search("query", criteria, inputPageable);

        // Assert
        verify(catalogShoeRepository).findLatestDatePerBrand();
        verify(catalogShoeRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void search_skipsLatestDateWhenNoLatestData() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        Specification<CatalogShoe> spec = (root, query, cb) -> cb.conjunction();
        Page<CatalogShoe> page = new PageImpl<>(List.of());
        when(specificationBuilder.build(criteria)).thenReturn(spec);
        when(catalogShoeRepository.findLatestDatePerBrand()).thenReturn(List.of());
        when(catalogShoeRepository.findAll(eq(spec), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(page);

        Pageable inputPageable = PageRequest.of(0, 5);

        // Act
        orchestrator.search("query", criteria, inputPageable);

        // Assert
        verify(catalogShoeRepository).findLatestDatePerBrand();
        verify(catalogShoeRepository).findAll(eq(spec), any(Pageable.class));
    }

    @Test
    void search_skipsLatestDateWhenBrandNotMatchedOrInvalidDwid() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        criteria.setBrands(List.of("Nike"));
        Specification<CatalogShoe> spec = (root, query, cb) -> cb.conjunction();
        Page<CatalogShoe> page = new PageImpl<>(List.of());
        when(specificationBuilder.build(criteria)).thenReturn(spec);
        CatalogShoeRepository.LatestData otherBrand = new CatalogShoeRepository.LatestData() {
            @Override
            public String getBrand() {
                return "Adidas";
            }

            @Override
            public String getLatestDwid() {
                return "2025-2-2"; // invalid date key length after digits-only
            }
        };
        when(catalogShoeRepository.findLatestDatePerBrand()).thenReturn(List.of(otherBrand));
        when(catalogShoeRepository.findAll(eq(spec), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(page);

        Pageable inputPageable = PageRequest.of(0, 5);

        // Act
        orchestrator.search("query", criteria, inputPageable);

        // Assert
        verify(catalogShoeRepository).findLatestDatePerBrand();
        verify(catalogShoeRepository).findAll(eq(spec), any(Pageable.class));
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
        CatalogShoeRepository.LatestData latestData = new CatalogShoeRepository.LatestData() {
            @Override
            public String getBrand() {
                return "Nike";
            }

            @Override
            public String getLatestDwid() {
                return "20250220";
            }
        };
        when(catalogShoeRepository.findLatestDatePerBrand()).thenReturn(List.of(latestData));
        when(catalogShoeRepository.findAll(any(Specification.class), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(page);

        Pageable inputPageable = PageRequest.of(0, 5);

        // Act
        orchestrator.search("query", criteria, inputPageable);

        // Assert
        verify(catalogShoeRepository).findLatestDatePerBrand();
        verify(catalogShoeRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}
