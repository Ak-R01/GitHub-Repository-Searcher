package com.gitRepoSearch.server.controller;

import com.gitRepoSearch.server.dto.*;
import com.gitRepoSearch.server.exception.ApiError;
import com.gitRepoSearch.server.service.RepoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Github Repositories")
@RestController
@RequestMapping("/api/github/")
@RequiredArgsConstructor
public class GitRepoController {

    private final RepoService repoService;

    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(value = """
                { "query": "spring boot", "language": "Java", "sort": "stars" }
                """)))
    @Operation(summary = "Search from Github Repositories and Store the results",
    description = "Upserts the data in a PostgreSQL database and return them")
    @ApiResponse(responseCode = "200",description = "Repositories fetched and saved successfuly")
    @ApiResponse(responseCode = "400", description = "Invalid request body",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "429", description = "GitHub rate limit reached",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "502", description = "GitHub returned an error or invalid response",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    @PostMapping(path ="/search")
    public ResponseEntity<SearchResponseDto> search(@Valid @RequestBody SearchRequestDto request){

        List<RepoResponseDto> result = repoService.searchAndSave(request);
        String message = result.isEmpty()
                ? "No results found for the query"
                : "Repositories fetched and saved successfully";

        SearchResponseDto response = new SearchResponseDto(message,result);

        return ResponseEntity.status(200).body(response);
    }

    @Operation(summary= "Retrieve Stored Repositories",description = "Optional filters: language, minStars , Sort defaults to stars")

    @GetMapping(path = "/repositories")
    public ResponseEntity<RetrieveResponseDto> RetrieveStored(@Valid @ParameterObject RetrieveRequestDto request){
        List<RepoResponseDto> fetchedData = repoService.RequestData(request);
        RetrieveResponseDto response = new RetrieveResponseDto(fetchedData);
        return ResponseEntity.status(200).body(response);
    }

}