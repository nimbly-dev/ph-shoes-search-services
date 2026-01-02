package com.nimbly.phshoesbackend.search.core.service;

import com.nimbly.phshoesbackend.search.text.core.model.dto.TextSearchResponse;
import org.springframework.data.domain.Pageable;

public interface TextSearchService {

    TextSearchResponse search(String nlQuery, Pageable pageable);
}
