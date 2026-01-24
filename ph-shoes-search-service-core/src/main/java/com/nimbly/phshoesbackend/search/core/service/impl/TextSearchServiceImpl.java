package com.nimbly.phshoesbackend.search.core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nimbly.phshoesbackend.catalog.core.model.CatalogShoe;
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
import java.time.LocalDate;
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
    public TextSearchResponse search(String nlQuery, Pageable pageable) {
        AISearchFilterCriteria criteria = pipeline.process(nlQuery);

        Page<CatalogShoe> page = orchestrator.search(nlQuery, criteria, pageable);

        TextSearchResponseFilter filterDto = mapFilter(criteria);
        TextSearchResponseResults resultsDto = mapResults(page);

        TextSearchResponse response = new TextSearchResponse();
        response.setFilter(filterDto);
        response.setResults(resultsDto);
        return response;
    }

    private TextSearchResponseFilter mapFilter(AISearchFilterCriteria criteria) {
        TextSearchResponseFilter filter = new TextSearchResponseFilter();
        filter.setBrands(criteria.getBrands());
        filter.setModel(criteria.getModel());
        filter.setPriceSaleMin(criteria.getPriceSaleMin());
        filter.setPriceSaleMax(criteria.getPriceSaleMax());
        filter.setPriceOriginalMin(criteria.getPriceOriginalMin());
        filter.setPriceOriginalMax(criteria.getPriceOriginalMax());
        filter.setGender(criteria.getGender());
        filter.setOnSale(criteria.getOnSale());
        filter.setTitleKeywords(criteria.getTitleKeywords());
        filter.setSubtitleKeywords(criteria.getSubtitleKeywords());
        filter.setSortBy(parseSortBy(criteria.getSortBy()));
        filter.setSizes(criteria.getSizes());
        return filter;
    }

    private TextSearchResponseResults mapResults(Page<CatalogShoe> page) {
        TextSearchResponseResults results = new TextSearchResponseResults();
        List<TextSearchResponseResultsContentInner> content = page.getContent()
                .stream()
                .map(this::toResultItem)
                .toList();

        results.setContent(content);
        results.setPage(page.getNumber());
        results.setSize(page.getSize());
        results.setTotalElements(page.getTotalElements());
        results.setTotalPages(page.getTotalPages());
        results.setFirst(page.isFirst());
        results.setLast(page.isLast());
        results.setEmpty(page.isEmpty());

        return results;
    }

    private TextSearchResponseResultsContentInner toResultItem(CatalogShoe shoe) {
        TextSearchResponseResultsContentInner result = new TextSearchResponseResultsContentInner();
        result.setId(shoe.getId());
        result.setBrand(shoe.getBrand());
        result.setTitle(shoe.getTitle());
        result.setSubtitle(shoe.getSubtitle());
        result.setUrl(parseUri(shoe.getUrl(), URI.create("about:blank")));
        result.setImage(parseUri(shoe.getImage(), null));
        result.setPriceSale(shoe.getPriceSale());
        result.setPriceOriginal(shoe.getPriceOriginal());
        result.setCollectedDate(buildCollectedDate(shoe));
        result.setGender(shoe.getGender());
        result.setAgeGroup(shoe.getAgeGroup());
        result.setSizes(extractSizes(shoe));
        return result;
    }

    private TextSearchResponseFilter.SortByEnum parseSortBy(String sortBy) {
        if (sortBy == null) {
            return null;
        }
        try {
            return TextSearchResponseFilter.SortByEnum.fromValue(sortBy);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private URI parseUri(String raw, URI fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return URI.create(raw);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private List<String> extractSizes(CatalogShoe shoe) {
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

    private LocalDate buildCollectedDate(CatalogShoe shoe) {
        Integer year = shoe.getYear();
        Integer month = shoe.getMonth();
        Integer day = shoe.getDay();
        if (year == null || month == null || day == null) {
            return null;
        }
        try {
            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            return null;
        }
    }
}
