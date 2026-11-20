package com.gitRepoSearch.server.repository;

import com.gitRepoSearch.server.entity.GitRepo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitRepoRepository extends JpaRepository<GitRepo,Long> {

}
