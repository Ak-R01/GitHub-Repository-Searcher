package com.gitRepoSearch.server.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubRepoDto(
        Long id,
        String name,
        String description,
        String owner,
        String language,
        Integer stars,
        Integer forks,
        Instant lastUpdated,
        Instant createdAt,
        String htmlUrl,
        Integer openIssues

) {
    @JsonCreator
    public GithubRepoDto(
        @JsonProperty("id") Long id,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("owner") Map<String,Object> owner,
        @JsonProperty("language") String language,
        @JsonProperty("stargazers_count") Integer stars,
        @JsonProperty("forks") Integer forks,
        @JsonProperty("pushed_at") Instant lastUpdated,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("open_issues") Integer openIssues
        ) {
        this(id,name,description, owner == null ? null : (String) owner.get("login"),
                language,stars,forks,lastUpdated,createdAt,htmlUrl,openIssues);
    }
}
