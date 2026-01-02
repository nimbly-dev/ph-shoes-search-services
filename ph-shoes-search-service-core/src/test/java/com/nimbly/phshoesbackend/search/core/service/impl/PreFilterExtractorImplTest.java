package com.nimbly.phshoesbackend.search.core.service.impl;

import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PreFilterExtractorImplTest {

    private final PreFilterExtractorImpl extractor = new PreFilterExtractorImpl();

    @Test
    void extract_parsesBrandsSizesAndPriceHints() {
        // Arrange
        String query = "Nike running shoes size 10 and 10.5 under 5000 on sale cheapest";

        // Act
        AISearchFilterCriteria criteria = extractor.extract(query);

        // Assert
        assertThat(criteria.getBrands()).containsExactly("nike");
        assertThat(criteria.getSizes()).containsExactlyInAnyOrder("10", "10.5");
        assertThat(criteria.getPriceSaleMax()).isEqualTo(5000.0);
        assertThat(criteria.getOnSale()).isTrue();
        assertThat(criteria.getSortBy()).isEqualTo("price_asc");
    }

    @Test
    void strip_removesKnownTokensAndBrands() {
        // Arrange
        String query = "Nike on sale size 10";

        // Act
        String stripped = extractor.strip(query);

        // Assert
        assertThat(stripped).isEqualTo("10");
    }
}
