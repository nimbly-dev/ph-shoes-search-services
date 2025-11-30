package com.nimbly.phshoesbackend.search.text.web.controller;

import com.nimbly.phshoesbackend.search.text.api.TextSearchApi;
import com.nimbly.phshoesbackend.search.core.service.TextSearchService;
import com.nimbly.phshoesbackend.search.text.core.model.dto.TextSearchResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.HtmlUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("${api.base-path:/api/v1}")
public class TextSearchController implements TextSearchApi {

    private static final Pattern SAFE_QUERY_PATTERN = Pattern.compile("^[\\p{Alnum}\\s.,?!\\-’']+$");

    private final TextSearchService textSearchService;

    public TextSearchController(TextSearchService textSearchService) {
        this.textSearchService = textSearchService;
    }

    @Override
    public ResponseEntity<TextSearchResponse> searchFactProductShoesByText(
            String q,
            Boolean useVector,
            Integer page,
            Integer size,
            String sort
    ) {
        String sanitized = sanitizeQuery(q);
        Pageable pageable = buildPageable(page, size, sort);
        boolean vectorEnabled = useVector == null || useVector;

        TextSearchResponse response = textSearchService.search(sanitized, vectorEnabled, pageable);
        return ResponseEntity.ok(response);
    }

    private String sanitizeQuery(String query) {
        if (!StringUtils.hasText(query)) {
            throw new ResponseStatusException(BAD_REQUEST, "Query must not be blank.");
        }
        if (query.contains("<") || query.contains(">") || query.contains("%") || query.contains(";")) {
            throw new ResponseStatusException(BAD_REQUEST, "Search query contains invalid characters.");
        }
        String escaped = HtmlUtils.htmlEscape(query);
        if (!SAFE_QUERY_PATTERN.matcher(escaped).matches()) {
            throw new ResponseStatusException(BAD_REQUEST, "Search query contains invalid characters.");
        }
        return escaped.trim();
    }

    private Pageable buildPageable(Integer page, Integer size, String sort) {
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? 15 : size;
        Sort sortSpec = Sort.unsorted();
        if (StringUtils.hasText(sort)) {
            String[] parts = sort.split(",");
            String property = parts[0].trim();
            if (!property.isBlank()) {
                Sort.Direction direction = Sort.Direction.ASC;
                if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
                    direction = Sort.Direction.DESC;
                }
                sortSpec = Sort.by(direction, property);
            }
        }
        return PageRequest.of(pageNumber, pageSize, sortSpec);
    }
}
