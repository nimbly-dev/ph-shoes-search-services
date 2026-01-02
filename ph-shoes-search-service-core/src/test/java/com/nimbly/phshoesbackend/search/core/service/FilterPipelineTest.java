package com.nimbly.phshoesbackend.search.core.service;

import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilterPipelineTest {

    @Mock
    private PreFilterExtractor preFilterExtractor;

    @Mock
    private OpenAiIntentParserService openAiIntentParserService;

    @Mock
    private FilterValidator filterValidator;

    private FilterPipeline filterPipeline;

    @BeforeEach
    void setup() {
        filterPipeline = new FilterPipeline(preFilterExtractor, openAiIntentParserService, filterValidator);
    }

    @Test
    void process_prefersAiSortWhenProvided() {
        // Arrange
        AISearchFilterCriteria base = new AISearchFilterCriteria();
        base.setBrands(java.util.List.of("nike"));
        base.setModel("pegasus");
        base.setTitleKeywords(java.util.List.of("running"));
        base.setSubtitleKeywords(java.util.List.of("road"));

        AISearchFilterCriteria fuzzy = new AISearchFilterCriteria();
        fuzzy.setSortBy("price_asc");

        when(preFilterExtractor.extract("query")).thenReturn(base);
        when(preFilterExtractor.strip("query")).thenReturn("query");
        when(openAiIntentParserService.parseIntent("query")).thenReturn(fuzzy);
        doNothing().when(filterValidator).validate(base);

        // Act
        AISearchFilterCriteria result = filterPipeline.process("query");

        // Assert
        assertThat(result.getSortBy()).isEqualTo("price_asc");
        assertThat(result.getBrands()).isNull();
        assertThat(result.getModel()).isNull();
        assertThat(result.getTitleKeywords()).isNull();
        assertThat(result.getSubtitleKeywords()).isNull();
    }
}
