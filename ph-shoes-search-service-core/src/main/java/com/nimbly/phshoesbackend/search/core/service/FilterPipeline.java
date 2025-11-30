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
    private final FilterNormalizer normalizer;
    private final FilterValidator validator;

    public FilterPipeline(
            PreFilterExtractor pre,
            OpenAiIntentParserService intent,
            FilterNormalizer normalizer,
            FilterValidator validator
    ) {
        this.pre = pre;
        this.intent = intent;
        this.normalizer = normalizer;
        this.validator = validator;
    }

    public AISearchFilterCriteria process(String nlQuery) {
        AISearchFilterCriteria base = pre.extract(nlQuery);

        String leftover = pre.strip(nlQuery);
        AISearchFilterCriteria fuzzy = intent.parseIntent(leftover);

        merge(base, fuzzy);

        FilterNormalizer.normalize(base);
        validator.validate(base);
        FilterNormalizer.normalize(base);

        log.info("AI filter pipeline for query='{}' → brands={}, model={}, titleKeywords={}, sizes={}, priceSaleMin={}, priceSaleMax={}, sortBy={}",
                nlQuery,
                base.getBrands(),
                base.getModel(),
                base.getTitleKeywords(),
                base.getSizes(),
                base.getPriceSaleMin(),
                base.getPriceSaleMax(),
                base.getSortBy());

        return base;
    }

    private void merge(AISearchFilterCriteria base, AISearchFilterCriteria fuzzy) {
        if (fuzzy.getBrands() != null && !fuzzy.getBrands().isEmpty()) {
            base.setBrands(new ArrayList<>(fuzzy.getBrands()));
        }

        Optional.ofNullable(fuzzy.getGender()).ifPresent(base::setGender);
        Optional.ofNullable(fuzzy.getPriceSaleMin()).ifPresent(base::setPriceSaleMin);
        Optional.ofNullable(fuzzy.getPriceSaleMax()).ifPresent(base::setPriceSaleMax);
        Optional.ofNullable(fuzzy.getPriceOriginalMin()).ifPresent(base::setPriceOriginalMin);
        Optional.ofNullable(fuzzy.getPriceOriginalMax()).ifPresent(base::setPriceOriginalMax);
        base.setOnSale(fuzzy.getOnSale());

        String aiSort = fuzzy.getSortBy();
        if (StringUtils.hasText(aiSort)) {
            base.setSortBy(aiSort);
            base.setBrands(List.of());
            base.setModel(null);
            base.setTitleKeywords(List.of());
            base.setSubtitleKeywords(List.of());
            return;
        }

        base.setModel(fuzzy.getModel());

        List<String> titleKeywords = new ArrayList<>();
        if (base.getTitleKeywords() != null) titleKeywords.addAll(base.getTitleKeywords());
        if (fuzzy.getTitleKeywords() != null) titleKeywords.addAll(fuzzy.getTitleKeywords());
        base.setTitleKeywords(titleKeywords);

        base.setSubtitleKeywords(List.of());
    }
}
