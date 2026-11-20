package com.gitRepoSearch.server.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "git_repo_search", indexes ={
        @Index(name = "idx_repo_stars", columnList = "stars"),
        @Index(name="idx_repo_forks",columnList = "forks"),
        @Index(name = "idx_last_updated",columnList = "last_updated")
})
public class GitRepo {
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 40,nullable = false)
    private String owner;

    @Column(length = 100)
    private String language;

    @Column
    private Integer stars;

    @Column
    private Integer forks;

    @Column
    private Instant lastUpdated;

    @Column
    private Instant createdAt;

    @Column
    private String htmlUrl;

    @Column
    private Integer openIssues;


}
