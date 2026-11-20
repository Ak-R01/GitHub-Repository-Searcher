package com.gitRepoSearch.server.dto;

import java.util.List;

public record SearchResponseDto(
        String message,
        List<RepoResponseDto> repositories

) {
}
