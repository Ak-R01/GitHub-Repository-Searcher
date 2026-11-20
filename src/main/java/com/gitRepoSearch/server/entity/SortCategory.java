package com.gitRepoSearch.server.entity;

import org.springframework.data.domain.Sort;

public enum SortCategory {
    stars("stars"),
    forks("forks"),
    updated("lastUpdated");

    private final String property;
    SortCategory(String property){
        this.property = property;
    }

    public Sort toSort(){
        return Sort.by(Sort.Direction.DESC,property);
    }
}
