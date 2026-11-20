package com.gitRepoSearch.server.service;


import com.gitRepoSearch.server.dto.GithubDto;
import com.gitRepoSearch.server.dto.RetrieveRequestDto;
import com.gitRepoSearch.server.dto.SearchRequestDto;
import com.gitRepoSearch.server.dto.RepoResponseDto;
import com.gitRepoSearch.server.entity.GitRepo;
import com.gitRepoSearch.server.mapper.GitRepoMapper;
import com.gitRepoSearch.server.repository.GitRepoRepository;
import com.gitRepoSearch.server.repository.GitRepoUpsertDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepoService<getRepo> {
    private final GitRepoRepository gitRepoRepository;
    private final RestClient externalRestClient;
    private final GitRepoMapper mapper;
    private final GitRepoUpsertDao upsertDao;


    public List<RepoResponseDto> getAll() {
        return gitRepoRepository.findAll().stream()
                .map(s -> new RepoResponseDto(
                        s.getId(),
                        s.getName(),
                        s.getDescription(),
                        s.getOwner(),
                        s.getLanguage(),
                        s.getStars(),
                        s.getForks(),
                        s.getLastUpdated()
                ))
                .toList();
    }

    public List<RepoResponseDto> searchAndSave(SearchRequestDto request){
        String searchQuery = request.query();
        if(request.language() != null && !request.language().isBlank()){
            searchQuery += " language:" + request.language();
        }
        String finalSearchQuery = searchQuery;
        GithubDto response = externalRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("q", "{q}")
                        .queryParamIfPresent("sort", Optional.ofNullable(request.sort()))
                        .build(finalSearchQuery))
                .retrieve()
                .body(GithubDto.class);
        List<GitRepo> fetchedRepos = mapper.toGitRepos(response == null ? List.of() :response.items());
        try{
        upsertDao.upsertAll(fetchedRepos);
        }catch (DataAccessException e) {
            log.error("Failed to persist {} repos",fetchedRepos.size(),e);
            throw e;
        }
        return mapper.toRepoDtos(fetchedRepos);
    }



    public List<RepoResponseDto> RequestData(RetrieveRequestDto request) {

        Sort sort = request.sort() == null ? Sort.unsorted() : request.sort().toSort();
        List<GitRepo> data = gitRepoRepository.search(request.language(),request.minStars(),sort);
//        log.info(data.toString());
        return mapper.toRepoDtos(data);
    }



}
