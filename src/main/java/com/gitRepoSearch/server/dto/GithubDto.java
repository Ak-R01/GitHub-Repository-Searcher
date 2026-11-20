package com.gitRepoSearch.server.dto;

import java.util.List;

public record GithubDto(
        Integer total_count,
        Boolean incomplete_results,
        List<GithubRepoDto> items
) {
}
