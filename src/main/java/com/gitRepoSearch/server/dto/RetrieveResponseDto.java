package com.gitRepoSearch.server.dto;

import java.util.List;

public record RetrieveResponseDto(
    List<RepoResponseDto> repositories
) {
}
