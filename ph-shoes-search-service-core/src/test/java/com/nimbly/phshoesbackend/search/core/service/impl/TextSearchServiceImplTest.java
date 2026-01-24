package com.nimbly.phshoesbackend.search.core.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbly.phshoesbackend.catalog.core.model.CatalogShoe;
import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;
import com.nimbly.phshoesbackend.search.core.service.FilterPipeline;
import com.nimbly.phshoesbackend.search.text.core.model.dto.TextSearchResponse;
import com.nimbly.phshoesbackend.search.text.core.model.dto.TextSearchResponseResultsContentInner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TextSearchServiceImplTest {

    @Mock
    private FilterPipeline filterPipeline;

    @Mock
    private SearchOrchestrator searchOrchestrator;

    private TextSearchServiceImpl service;

    @BeforeEach
    void setup() {
        service = new TextSearchServiceImpl(filterPipeline, searchOrchestrator, new ObjectMapper());
    }

    @Test
    void search_mapsFiltersAndResults() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        criteria.setBrands(List.of("nike"));
        criteria.setModel("pegasus");
        criteria.setSortBy("invalid");

        CatalogShoe shoe = mock(CatalogShoe.class, RETURNS_DEEP_STUBS);
        when(shoe.getId()).thenReturn("1");
        when(shoe.getBrand()).thenReturn("nike");
        when(shoe.getTitle()).thenReturn("Nike Pegasus");
        when(shoe.getSubtitle()).thenReturn("Running");
        when(shoe.getUrl()).thenReturn("https://example.com/shoe/1");
        when(shoe.getImage()).thenReturn("http://invalid uri");
        when(shoe.getPriceSale()).thenReturn(3000.0);
        when(shoe.getPriceOriginal()).thenReturn(4500.0);
        when(shoe.getYear()).thenReturn(2026);
        when(shoe.getMonth()).thenReturn(1);
        when(shoe.getDay()).thenReturn(24);
        when(shoe.getGender()).thenReturn("unisex");
        when(shoe.getAgeGroup()).thenReturn("adult");
        when(shoe.getExtra()).thenReturn("{\"sizes\": [\"10\", \"11\"]}");

        Pageable pageable = PageRequest.of(0, 1);
        Page<CatalogShoe> page = new PageImpl<>(List.of(shoe), pageable, 1);

        when(filterPipeline.process(eq("query"))).thenReturn(criteria);
        when(searchOrchestrator.search(eq("query"), eq(criteria), eq(pageable))).thenReturn(page);

        // Act
        TextSearchResponse response = service.search("query", pageable);

        // Assert
        assertThat(response.getFilter().getBrands()).containsExactly("nike");
        assertThat(response.getFilter().getModel()).isEqualTo("pegasus");
        assertThat(response.getFilter().getSortBy()).isNull();

        TextSearchResponseResultsContentInner result = response.getResults().getContent().get(0);
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getUrl().toString()).isEqualTo("https://example.com/shoe/1");
        assertThat(result.getImage()).isNull();
        assertThat(result.getCollectedDate()).isEqualTo(LocalDate.of(2026, 1, 24));
        assertThat(result.getSizes()).containsExactly("10", "11");
    }

    @Test
    void search_usesFallbackUrlWhenInvalid() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();

        CatalogShoe shoe = mock(CatalogShoe.class, RETURNS_DEEP_STUBS);
        when(shoe.getId()).thenReturn("1");
        when(shoe.getBrand()).thenReturn("nike");
        when(shoe.getTitle()).thenReturn("Nike Pegasus");
        when(shoe.getUrl()).thenReturn("http://invalid uri");
        when(shoe.getExtra()).thenReturn(null);

        Pageable pageable = PageRequest.of(0, 1);
        Page<CatalogShoe> page = new PageImpl<>(List.of(shoe), pageable, 1);

        when(filterPipeline.process(eq("query"))).thenReturn(criteria);
        when(searchOrchestrator.search(eq("query"), eq(criteria), eq(pageable))).thenReturn(page);

        // Act
        TextSearchResponse response = service.search("query", pageable);

        // Assert
        assertThat(response.getResults().getContent().get(0).getUrl().toString()).isEqualTo("about:blank");
    }
}
