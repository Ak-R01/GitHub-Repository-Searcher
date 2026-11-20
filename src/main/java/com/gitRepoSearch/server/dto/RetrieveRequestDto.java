package com.gitRepoSearch.server.dto;

import com.gitRepoSearch.server.entity.SortCategory;
import jakarta.annotation.Nullable;

public record RetrieveRequestDto(
        @Nullable String language,
        @Nullable Integer minStars,
        SortCategory sort
) {
    public RetrieveRequestDto{
        if(sort == null){
            sort = SortCategory.stars;
        }
        language = (language == null || language.isBlank()) ? null : language.trim();
    }
}
