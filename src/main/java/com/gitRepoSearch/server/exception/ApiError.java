package com.gitRepoSearch.server.exception;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApiError(
        @Schema(example = "GitHub rate limit reached, try again later")
        String message) {}
