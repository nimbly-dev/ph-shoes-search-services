package com.nimbly.phshoesbackend.search.core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nimbly.phshoesbackend.catalog.core.model.FactProductShoes;
import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;
import com.nimbly.phshoesbackend.search.core.service.FilterPipeline;
import com.nimbly.phshoesbackend.search.core.service.TextSearchService;
import com.nimbly.phshoesbackend.search.text.core.model.dto.TextSearchResponse;
import com.nimbly.phshoesbackend.search.text.core.model.dto.TextSearchResponseFilter;
import com.nimbly.phshoesbackend.search.text.core.model.dto.TextSearchResponseResults;
import com.nimbly.phshoesbackend.search.text.core.model.dto.TextSearchResponseResultsContentInner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class TextSearchServiceImpl implements TextSearchService {

    private final FilterPipeline pipeline;
    private final SearchOrchestrator orchestrator;
    private final ObjectMapper objectMapper;

    public TextSearchServiceImpl(
            FilterPipeline pipeline,
            SearchOrchestrator orchestrator,
            ObjectMapper objectMapper
    ) {
        this.pipeline = pipeline;
        this.orchestrator = orchestrator;
        this.objectMapper = objectMapper;
    }

    @Override
    public TextSearchResponse search(String nlQuery, boolean useVector, Pageable pageable) {
        AISearchFilterCriteria criteria = pipeline.process(nlQuery);

        Page<FactProductShoes> page = orchestrator.search(nlQuery, criteria, pageable, useVector);

        TextSearchResponseFilter filterDto = mapFilter(criteria);
        TextSearchResponseResults resultsDto = mapResults(page);

        TextSearchResponse response = new TextSearchResponse();
        response.setFilter(filterDto);
        response.setResults(resultsDto);
        return response;
    }

    private TextSearchResponseFilter mapFilter(AISearchFilterCriteria c) {
        TextSearchResponseFilter dto = new TextSearchResponseFilter();
        dto.setBrands(c.getBrands());
        dto.setModel(c.getModel());
        dto.setPriceSaleMin(c.getPriceSaleMin());
        dto.setPriceSaleMax(c.getPriceSaleMax());
        dto.setPriceOriginalMin(c.getPriceOriginalMin());
        dto.setPriceOriginalMax(c.getPriceOriginalMax());
        dto.setGender(c.getGender());
        dto.setOnSale(c.getOnSale());
        dto.setTitleKeywords(c.getTitleKeywords());
        dto.setSubtitleKeywords(c.getSubtitleKeywords());
        if (c.getSortBy() != null) {
            try {
                dto.setSortBy(TextSearchResponseFilter.SortByEnum.fromValue(c.getSortBy()));
            } catch (IllegalArgumentException ignored) {
                dto.setSortBy(null);
            }
        }
        dto.setSizes(c.getSizes());
        return dto;
    }

    private TextSearchResponseResults mapResults(Page<FactProductShoes> page) {
        TextSearchResponseResults dto = new TextSearchResponseResults();

        List<TextSearchResponseResultsContentInner> content = new ArrayList<>();
        for (FactProductShoes shoe : page.getContent()) {
            TextSearchResponseResultsContentInner r = new TextSearchResponseResultsContentInner();
            r.setId(shoe.getKey().getId());
            r.setBrand(shoe.getBrand());
            r.setTitle(shoe.getTitle());
            r.setSubtitle(shoe.getSubtitle());
            r.setUrl(parseUri(shoe.getUrl()));
            r.setImage(parseNullableUri(shoe.getImage()));
            r.setPriceSale(shoe.getPriceSale());
            r.setPriceOriginal(shoe.getPriceOriginal());
            r.setGender(shoe.getGender());
            r.setAgeGroup(shoe.getAgeGroup());
            r.setSizes(extractSizes(shoe));
            content.add(r);
        }

        dto.setContent(content);
        dto.setPage(page.getNumber());
        dto.setSize(page.getSize());
        dto.setTotalElements(page.getTotalElements());
        dto.setTotalPages(page.getTotalPages());
        dto.setFirst(page.isFirst());
        dto.setLast(page.isLast());
        dto.setEmpty(page.isEmpty());

        return dto;
    }

    private URI parseUri(String raw) {
        if (raw == null || raw.isBlank()) {
            return URI.create("about:blank");
        }
        try {
            return URI.create(raw);
        } catch (IllegalArgumentException e) {
            return URI.create("about:blank");
        }
    }

    private URI parseNullableUri(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return URI.create(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private List<String> extractSizes(FactProductShoes shoe) {
        String extra = shoe.getExtra();
        if (extra == null || extra.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(extra);
            JsonNode sizesNode = root.get("sizes");
            if (sizesNode == null || !sizesNode.isArray()) {
                return null;
            }
            List<String> sizes = new ArrayList<>();
            for (JsonNode node : (ArrayNode) sizesNode) {
                if (node.isTextual() || node.isNumber()) {
                    String val = node.asText();
                    if (!val.isBlank()) {
                        sizes.add(val);
                    }
                }
            }
            return sizes.isEmpty() ? null : sizes;
        } catch (Exception e) {
            return null;
        }
    }
}
