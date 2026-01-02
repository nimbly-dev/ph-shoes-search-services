package com.nimbly.phshoesbackend.search.core.util;

import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FilterNormalizerTest {

    @Test
    void normalize_cleansAndNormalizesCriteria() {
        // Arrange
        AISearchFilterCriteria criteria = new AISearchFilterCriteria();
        criteria.setBrands(List.of(" Nike ", "NIKE", "adidas "));
        criteria.setGender("Male ");
        criteria.setTitleKeywords(Arrays.asList("  Trail ", "", null));
        criteria.setSubtitleKeywords(List.of("  RUNNING  "));
        criteria.setSizes(List.of("US 10", "10.5", "uk 9"));
        criteria.setPriceSaleMin(5000.0);
        criteria.setPriceSaleMax(3000.0);
        criteria.setPriceOriginalMin(3000.0);
        criteria.setPriceOriginalMax(3000.0);

        // Act
        FilterNormalizer.normalize(criteria);

        // Assert
        assertThat(criteria.getBrands()).containsExactly("nike", "adidas");
        assertThat(criteria.getGender()).isEqualTo("male");
        assertThat(criteria.getTitleKeywords()).containsExactly("trail");
        assertThat(criteria.getSubtitleKeywords()).containsExactly("running");
        assertThat(criteria.getSizes()).containsExactly("10", "10.5", "9");
        assertThat(criteria.getPriceSaleMin()).isEqualTo(3000.0);
        assertThat(criteria.getPriceSaleMax()).isEqualTo(5000.0);
    }
}
