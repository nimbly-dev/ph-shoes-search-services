package com.nimbly.phshoesbackend.search.core.service;

import com.nimbly.phshoesbackend.search.core.exception.AiSearchException;
import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;
import com.nimbly.phshoesbackend.search.core.service.impl.PreFilterExtractorImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilterValidatorTest {

    private final FilterValidator validator = new FilterValidator(new PreFilterExtractorImpl());

    @Test
    void validate_rejectsNegativePrice() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        criteria.setPriceSaleMin(-1.0);

        // Act + Assert
        assertThatThrownBy(() -> validator.validate(criteria))
                .isInstanceOf(AiSearchException.class)
                .hasMessageContaining("Negative priceSale filter");
    }

    @Test
    void validate_rejectsInvalidSortBy() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        criteria.setSortBy("invalid");

        // Act + Assert
        assertThatThrownBy(() -> validator.validate(criteria))
                .isInstanceOf(AiSearchException.class)
                .hasMessageContaining("Invalid sortBy");
    }

    @Test
    void validate_demotesUnknownBrandsToTitleKeywords() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        criteria.setBrands(List.of("nike", "unknown"));
        criteria.setGender("kids");

        // Act
        validator.validate(criteria);

        // Assert
        assertThat(criteria.getBrands()).containsExactly("nike");
        assertThat(criteria.getTitleKeywords()).contains("unknown");
        assertThat(criteria.getSubtitleKeywords()).contains("kids");
    }
}
