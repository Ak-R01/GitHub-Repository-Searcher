package com.gitRepoSearch.server;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


import com.gitRepoSearch.server.entity.GitRepo;
import com.gitRepoSearch.server.repository.GitRepoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;


@DataJpaTest
public class gitRepoTest {
    @Autowired
    private GitRepoRepository gitRepoRepository;

    @Test
    void SaveAndRetrieveGitRepo(){
        Instant now = Instant.now();
        GitRepo repo = GitRepo.builder()
                .id(1L)
                .name("Spring Boot")
                .description("Java application framework")
                .owner("spring-projects")
                .language("Java")
                .stars(100)
                .forks(50)
                .lastUpdated(now)
                .build();
        gitRepoRepository.saveAndFlush(repo);

        GitRepo savedRepo = gitRepoRepository.findById(1L).orElseThrow();

        assertThat(savedRepo.getId()).isEqualTo(1L);
        assertThat(savedRepo.getName()).isEqualTo("Spring Boot");
        assertThat(savedRepo.getDescription())
                .isEqualTo("Java application framework");
        assertThat(savedRepo.getOwner()).isEqualTo("spring-projects");
        assertThat(savedRepo.getLanguage()).isEqualTo("Java");
        assertThat(savedRepo.getStars()).isEqualTo(100);
        assertThat(savedRepo.getForks()).isEqualTo(50);
        assertThat(savedRepo.getLastUpdated()).isEqualTo(now);
    }

    @Test
    void RejectRepoWithNullName(){
        GitRepo repo = GitRepo.builder()
                .id(2L)
                .owner("spring-projects")
                .build();
        assertThatThrownBy(()->gitRepoRepository.saveAndFlush(repo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
    @Test
    void RejectWithNullOwner(){
        GitRepo repo = GitRepo.builder()
                .id(3L)
                .name("Spring Boot")
                .build();

        assertThatThrownBy(()->gitRepoRepository.saveAndFlush(repo)
                ).isInstanceOf(DataIntegrityViolationException.class);


    }
    @Test
    void ReturnEmptyWhenRepoDoesNotExist() {
        assertThat(gitRepoRepository.findById(999L).isEmpty());
    }

}
