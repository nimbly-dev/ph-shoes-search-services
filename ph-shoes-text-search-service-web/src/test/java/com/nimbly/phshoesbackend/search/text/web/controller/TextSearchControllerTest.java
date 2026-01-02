package com.nimbly.phshoesbackend.search.text.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbly.phshoesbackend.search.text.core.model.dto.TextSearchResponse;
import com.nimbly.phshoesbackend.search.text.core.model.dto.TextSearchResponseFilter;
import com.nimbly.phshoesbackend.search.text.core.model.dto.TextSearchResponseResults;
import com.nimbly.phshoesbackend.search.text.core.model.dto.TextSearchResponseResultsContentInner;
import com.nimbly.phshoesbackend.search.core.service.TextSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TextSearchControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TextSearchService textSearchService;

    @BeforeEach
    void setup() {
        TextSearchController controller = new TextSearchController(textSearchService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void searchFactProductShoesByText_returnsResults() throws Exception {
        // Arrange
        TextSearchResponse response = buildResponse();
        when(textSearchService.search(any(), any(Pageable.class))).thenReturn(response);

        // Act
        mockMvc.perform(get("/search/fact-product-shoes")
                        .param("q", "nike shoes")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "priceSale,desc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));

        // Assert
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(textSearchService).search(queryCaptor.capture(), pageableCaptor.capture());

        assertThat(queryCaptor.getValue()).isEqualTo("nike shoes");
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("priceSale")).isNotNull();
    }

    @Test
    void searchFactProductShoesByText_rejectsInvalidCharacters() throws Exception {
        // Arrange / Act / Assert
        mockMvc.perform(get("/search/fact-product-shoes")
                        .param("q", "<script>")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private TextSearchResponse buildResponse() {
        TextSearchResponseFilter filter = new TextSearchResponseFilter()
                .brands(List.of("nike"))
                .model("pegasus")
                .priceSaleMin(1000.0)
                .priceSaleMax(5000.0)
                .onSale(true);

        TextSearchResponseResultsContentInner item = new TextSearchResponseResultsContentInner()
                .id("1")
                .brand("nike")
                .title("Nike Pegasus")
                .url(URI.create("https://example.com/shoe/1"))
                .priceSale(3000.0)
                .priceOriginal(4500.0);

        TextSearchResponseResults results = new TextSearchResponseResults()
                .content(List.of(item))
                .page(0)
                .size(15)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .empty(false);

        return new TextSearchResponse(filter, results);
    }
}
