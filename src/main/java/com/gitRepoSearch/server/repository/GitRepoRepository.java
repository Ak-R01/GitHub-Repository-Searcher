package com.gitRepoSearch.server.repository;

import com.gitRepoSearch.server.entity.GitRepo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GitRepoRepository extends JpaRepository<GitRepo,Long> {

    @Query("""
       select r from GitRepo r
       where (cast(:language as string) is null
              or lower(r.language) = lower(cast(:language as string)))
         and (cast(:minStars as integer) is null
              or r.stars >= :minStars)
       """)
    List<GitRepo> search(@Param("language") String language,
                         @Param("minStars") Integer minStars,
                         Sort sort);
}
