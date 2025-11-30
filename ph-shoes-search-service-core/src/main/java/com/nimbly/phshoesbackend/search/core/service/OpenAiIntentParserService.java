package com.nimbly.phshoesbackend.search.core.service;

import com.nimbly.phshoesbackend.search.core.model.AISearchFilterCriteria;

public interface OpenAiIntentParserService {
    AISearchFilterCriteria parseIntent(String nlQuery);
}

