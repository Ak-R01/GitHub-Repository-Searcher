package com.gitRepoSearch.server.service;

import com.gitRepoSearch.server.dto.RepoResponseDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.gitRepoSearch.server.entity.GitRepo;
import com.gitRepoSearch.server.repository.GitRepoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class RepoServiceTest {

    @Mock
    private GitRepoRepository gitRepoRepository;

    @InjectMocks
    private RepoService repoService;


    @Test
    void ReturnAllRepositories() {
        Instant now = Instant.now();

        GitRepo repo1 = GitRepo.builder()
                .id(1L)
                .name("Spring Boot")
                .description("Java framework")
                .owner("spring-projects")
                .language("Java")
                .stars(100)
                .forks(50)
                .lastUpdated(now)
                .build();
        GitRepo repo2 = GitRepo.builder()
                .id(2L)
                .name("React")
                .description("JavaScript library")
                .owner("facebook")
                .language("JavaScript")
                .stars(200)
                .forks(80)
                .lastUpdated(now)
                .build();
        when(gitRepoRepository.findAll()).thenReturn(List.of(repo1,repo2));

        List<RepoResponseDto> result = repoService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Spring Boot");
        assertThat(result.get(0).owner()).isEqualTo("spring-projects");

        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).name()).isEqualTo("React");
        assertThat(result.get(1).owner()).isEqualTo("facebook");

        verify(gitRepoRepository,times(1)).findAll();

    }

    @Test
    void ReturnEmptyListWhenNoRepositoriesExist() {

        when(gitRepoRepository.findAll()).thenReturn(List.of());
        List<RepoResponseDto> result = repoService.getAll();
        assertThat(result).isEmpty();
        verify(gitRepoRepository,times(1)).findAll();
    }



}
