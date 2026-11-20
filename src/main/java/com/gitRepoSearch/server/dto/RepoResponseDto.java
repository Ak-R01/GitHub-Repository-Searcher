package com.gitRepoSearch.server.dto;

import java.time.Instant;

public record RepoResponseDto(
        Long id,
        String name,
        String description,
        String owner,
        String language,
        Integer stars,
        Integer forks,
        Instant lastUpdated
) {
}
