package com.gitRepoSearch.server.dto;

import com.gitRepoSearch.server.entity.SortCategory;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;

public record SearchRequestDto(

        @NotBlank String query,
        String language,
        @Enumerated(EnumType.STRING)
        SortCategory sort
) {
    public SearchRequestDto{

    }
}
