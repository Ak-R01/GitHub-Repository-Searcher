package com.gitRepoSearch.server.repository;

import com.gitRepoSearch.server.entity.GitRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class GitRepoUpsertDao {

    // Column names follow Spring Boot's default naming: lastUpdated -> last_updated, htmlUrl -> html_url, etc.
    private static final String UPSERT = """
            INSERT INTO git_repo_search
                (id, name, description, owner, language, stars, forks,
                 last_updated, created_at, html_url, open_issues)
            VALUES
                (:id, :name, :description, :owner, :language, :stars, :forks,
                 :lastUpdated, :createdAt, :htmlUrl, :openIssues)
            ON CONFLICT (id) DO UPDATE SET
                name         = EXCLUDED.name,
                description  = EXCLUDED.description,
                owner        = EXCLUDED.owner,
                language     = EXCLUDED.language,
                stars        = EXCLUDED.stars,
                forks        = EXCLUDED.forks,
                last_updated = EXCLUDED.last_updated,
                created_at   = EXCLUDED.created_at,
                html_url     = EXCLUDED.html_url,
                open_issues  = EXCLUDED.open_issues
            """;

    private final NamedParameterJdbcTemplate jdbc;

    @Transactional
    public void upsertAll(List<GitRepo> repos) {
        if (repos.isEmpty()) {
            return;
        }
        SqlParameterSource[] batch = repos.stream()
                .map(this::toParams)
                .toArray(SqlParameterSource[]::new);
        jdbc.batchUpdate(UPSERT, batch);
    }

    private SqlParameterSource toParams(GitRepo r) {
        return new MapSqlParameterSource()
                .addValue("id", r.getId())
                .addValue("name", r.getName())
                .addValue("description", r.getDescription())
                .addValue("owner", r.getOwner())
                .addValue("language", r.getLanguage())
                .addValue("stars", r.getStars())
                .addValue("forks", r.getForks())
                .addValue("lastUpdated", toOffset(r.getLastUpdated()), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("createdAt", toOffset(r.getCreatedAt()), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("htmlUrl", r.getHtmlUrl())
                .addValue("openIssues", r.getOpenIssues());
    }

    // pgjdbc can't bind a raw Instant, so convert to OffsetDateTime (null stays null).
    private static OffsetDateTime toOffset(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}