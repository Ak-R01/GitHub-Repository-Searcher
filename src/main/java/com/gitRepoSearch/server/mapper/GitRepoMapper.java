package com.gitRepoSearch.server.mapper;

import com.gitRepoSearch.server.dto.GithubRepoDto;
import com.gitRepoSearch.server.dto.RepoResponseDto;
import com.gitRepoSearch.server.entity.GitRepo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GitRepoMapper {
    public GitRepo toGitRepo(GithubRepoDto dto) {
        GitRepo repo = new GitRepo();
        repo.setId(dto.id());
        repo.setName(dto.name());
        repo.setDescription(dto.description());
        repo.setOwner(dto.owner());
        repo.setLanguage(dto.language());
        repo.setStars(dto.stars());
        repo.setForks(dto.forks());
        repo.setLastUpdated(dto.lastUpdated());
        repo.setCreatedAt(dto.createdAt());
        repo.setHtmlUrl(dto.htmlUrl());
        repo.setOpenIssues(dto.openIssues());
        return repo;
    }

    public List<GitRepo> toGitRepos(List<GithubRepoDto> dtos) {
        return dtos.stream()
                .map(this::toGitRepo)
                .toList();
    }

    public RepoResponseDto toRepoDto(GitRepo repo){
        RepoResponseDto dto = new RepoResponseDto(
                repo.getId(),
                repo.getName(),
                repo.getDescription(),
                repo.getOwner(),
                repo.getLanguage(),
                repo.getStars(),
                repo.getForks(),
                repo.getLastUpdated()
        );
        return dto;
    }
    public List<RepoResponseDto> toRepoDtos(List<GitRepo> repos){
        return repos.stream()
                .map(this::toRepoDto)
                .toList();
    }
}
