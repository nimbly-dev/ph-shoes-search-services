package com.nimbly.phshoesbackend.search.core.service;

import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;
import com.nimbly.phshoesbackend.search.core.util.FilterNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class FilterPipeline {
    private final PreFilterExtractor pre;
    private final OpenAiIntentParserService intent;
    private final FilterValidator validator;

    public FilterPipeline(
            PreFilterExtractor pre,
            OpenAiIntentParserService intent,
            FilterValidator validator
    ) {
        this.pre = pre;
        this.intent = intent;
        this.validator = validator;
    }

    public AISearchFilterCriteria process(String nlQuery) {
        AISearchFilterCriteria baseCriteria = pre.extract(nlQuery);

        String leftover = pre.strip(nlQuery);
        AISearchFilterCriteria aiCriteria = intent.parseIntent(leftover);

        merge(baseCriteria, aiCriteria);

        FilterNormalizer.normalize(baseCriteria);
        validator.validate(baseCriteria);
        FilterNormalizer.normalize(baseCriteria);

        log.info("AI filter pipeline for query='{}' → brands={}, model={}, titleKeywords={}, sizes={}, priceSaleMin={}, priceSaleMax={}, sortBy={}",
                nlQuery,
                baseCriteria.getBrands(),
                baseCriteria.getModel(),
                baseCriteria.getTitleKeywords(),
                baseCriteria.getSizes(),
                baseCriteria.getPriceSaleMin(),
                baseCriteria.getPriceSaleMax(),
                baseCriteria.getSortBy());

        return baseCriteria;
    }

    private void merge(AISearchFilterCriteria baseCriteria, AISearchFilterCriteria aiCriteria) {
        if (aiCriteria.getBrands() != null && !aiCriteria.getBrands().isEmpty()) {
            baseCriteria.setBrands(new ArrayList<>(aiCriteria.getBrands()));
        }

        Optional.ofNullable(aiCriteria.getGender()).ifPresent(baseCriteria::setGender);
        Optional.ofNullable(aiCriteria.getPriceSaleMin()).ifPresent(baseCriteria::setPriceSaleMin);
        Optional.ofNullable(aiCriteria.getPriceSaleMax()).ifPresent(baseCriteria::setPriceSaleMax);
        Optional.ofNullable(aiCriteria.getPriceOriginalMin()).ifPresent(baseCriteria::setPriceOriginalMin);
        Optional.ofNullable(aiCriteria.getPriceOriginalMax()).ifPresent(baseCriteria::setPriceOriginalMax);
        baseCriteria.setOnSale(aiCriteria.getOnSale());

        if (applyAiSortOverride(baseCriteria, aiCriteria)) {
            return;
        }

        baseCriteria.setModel(aiCriteria.getModel());

        List<String> titleKeywords = new ArrayList<>();
        if (baseCriteria.getTitleKeywords() != null) {
            titleKeywords.addAll(baseCriteria.getTitleKeywords());
        }
        if (aiCriteria.getTitleKeywords() != null) {
            titleKeywords.addAll(aiCriteria.getTitleKeywords());
        }
        baseCriteria.setTitleKeywords(titleKeywords);

        baseCriteria.setSubtitleKeywords(List.of());
    }

    private boolean applyAiSortOverride(AISearchFilterCriteria baseCriteria, AISearchFilterCriteria aiCriteria) {
        String aiSort = aiCriteria.getSortBy();
        if (!StringUtils.hasText(aiSort)) {
            return false;
        }
        baseCriteria.setSortBy(aiSort);
        baseCriteria.setBrands(List.of());
        baseCriteria.setModel(null);
        baseCriteria.setTitleKeywords(List.of());
        baseCriteria.setSubtitleKeywords(List.of());
        return true;
    }
}
