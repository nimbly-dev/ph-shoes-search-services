package com.nimbly.phshoesbackend.search.core.service;

import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;

public interface PreFilterExtractor {
    AISearchFilterCriteria extract(String nlQuery);
    String strip(String nlQuery);
    boolean isKnownBrand(String brand);
}

