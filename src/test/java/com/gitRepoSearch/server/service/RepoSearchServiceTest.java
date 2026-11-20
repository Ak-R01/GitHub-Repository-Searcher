package com.gitRepoSearch.server.service;

import com.gitRepoSearch.server.dto.GithubRepoDto;
import com.gitRepoSearch.server.dto.RepoResponseDto;
import com.gitRepoSearch.server.dto.SearchRequestDto;
import com.gitRepoSearch.server.entity.GitRepo;
import com.gitRepoSearch.server.entity.SortCategory;
import com.gitRepoSearch.server.mapper.GitRepoMapper;
import com.gitRepoSearch.server.repository.GitRepoRepository;
import com.gitRepoSearch.server.repository.GitRepoUpsertDao;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
public class RepoSearchServiceTest {
    private static final String BASE_URL = "https://api.github.com/search/repositories";
    private static String githubResponse;

    @BeforeAll
    static void loadFixture() throws IOException {
        githubResponse = new ClassPathResource("github-response.json")
                .getContentAsString(StandardCharsets.UTF_8);

    }
    private static RequestMatcher decodedQueryParam(String name, String expected) {
        return request -> {
            String raw = UriComponentsBuilder.fromUri(request.getURI())
                    .build().getQueryParams().getFirst(name);
            assertThat(raw).as("query param " + name).isNotNull();
            assertThat(UriUtils.decode(raw, StandardCharsets.UTF_8)).isEqualTo(expected);
        };
    }

    @Captor
    private ArgumentCaptor<List<GithubRepoDto>> dtoCaptor;

    @Mock
    private GitRepoRepository gitRepoRepository;

    @Mock
    private GitRepoMapper gitRepoMapper;

    private MockRestServiceServer server;
    private RepoService service;

    @Mock
    private GitRepoUpsertDao upsertDao;

    @BeforeEach
    void setUp(){
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        service = new RepoService(gitRepoRepository,builder.build(),gitRepoMapper,upsertDao);
    }

    @Test
    void appendsLanguageQualifierWhenLanguageProvided(){
        server.expect(requestTo(startsWith(BASE_URL)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(decodedQueryParam("q","spring language:java"))
                .andRespond(withSuccess(githubResponse, MediaType.APPLICATION_JSON));
        when(gitRepoMapper.toGitRepos(anyList())).thenReturn(List.of());

        service.searchAndSave(new SearchRequestDto("spring","java", SortCategory.stars));

        server.verify();
    }

    @Test
    void encodesPlusSignsInTheQuery() {
        server.expect(requestTo(startsWith(BASE_URL)))
                .andExpect(decodedQueryParam("q", "c++"))
                .andExpect(request -> assertThat(request.getURI().getRawQuery()).contains("q=c%2B%2B"))
                .andRespond(withSuccess(githubResponse, MediaType.APPLICATION_JSON));
        when(gitRepoMapper.toGitRepos(anyList())).thenReturn(List.of());

        service.searchAndSave(new SearchRequestDto("c++", null, null));

        server.verify();
    }

    @Test
    void doesNotAppendLanguageWhenNull(){
        server.expect(queryParam("q","spring"))
                .andRespond(withSuccess(githubResponse,MediaType.APPLICATION_JSON));
        when(gitRepoMapper.toGitRepos(anyList())).thenReturn(List.of());

        service.searchAndSave(new SearchRequestDto("spring",null, SortCategory.stars));

        server.verify();
    }

    @Test
    void doesNotAppendLanguageWhenBlank(){
        server.expect(queryParam("q", "spring"))
                .andRespond(withSuccess(githubResponse, MediaType.APPLICATION_JSON));
        when(gitRepoMapper.toGitRepos(anyList())).thenReturn(List.of());

        service.searchAndSave(new SearchRequestDto("spring", "   ", SortCategory.stars));

        server.verify();
    }

    @Test
    void sendsSortParameter() {
        server.expect(queryParam("sort","stars"))
                .andRespond(withSuccess(githubResponse,MediaType.APPLICATION_JSON));
        when(gitRepoMapper.toGitRepos(anyList())).thenReturn(List.of());
        service.searchAndSave(new SearchRequestDto("spring",null, SortCategory.stars));
        server.verify();
    }


    @Test
    void omitsSortParameterWhenSortIsNull(){
        server.expect(queryParam("q","spring"))
                .andExpect(request -> assertThat(request.getURI().getQuery()).doesNotContain("sort"))
                .andRespond(withSuccess(githubResponse,MediaType.APPLICATION_JSON));
        when(gitRepoMapper.toGitRepos(anyList())).thenReturn(List.of());
        service.searchAndSave(new SearchRequestDto("spring",null,null));
        server.verify();
    }

    // Responses

    @Test
    void returnsResponseDtosAndUpsertsTheMappedEntities() {
        server.expect(requestTo(startsWith(BASE_URL)))
                .andRespond(withSuccess(githubResponse, MediaType.APPLICATION_JSON));

        List<GitRepo> entities = List.of(GitRepo.builder()
                .id(1148753L).name("spring-framework").owner("spring-projects").build());
        List<RepoResponseDto> dtos = List.of(new RepoResponseDto(
                1148753L, "spring-framework", "Spring Framework", "spring-projects",
                "Java", 60320, 38801, Instant.parse("2026-11-18T16:19:00Z")));

        when(gitRepoMapper.toGitRepos(anyList())).thenReturn(entities);
        when(gitRepoMapper.toRepoDtos(entities)).thenReturn(dtos);

        List<RepoResponseDto> result =
                service.searchAndSave(new SearchRequestDto("spring", null, SortCategory.stars));

        assertThat(result).isSameAs(dtos);
        verify(upsertDao).upsertAll(entities);
    }

    @Test
    void returnsEmptyListWhenResponseBodyIsEmpty() {
        server.expect(requestTo(startsWith(BASE_URL))).andRespond(withSuccess().contentType(MediaType.APPLICATION_JSON));
        when(gitRepoMapper.toGitRepos(List.of())).thenReturn(List.of());

        List<RepoResponseDto> result = service.searchAndSave(new SearchRequestDto("spring",null, SortCategory.stars));

        assertThat(result.isEmpty());
        verify(gitRepoMapper).toGitRepos(List.of());
    }



     //fails right now because no case of items: null considered
    @Test
    void returnEmptyListWhenItemsIsNull() {
        server.expect(requestTo(startsWith(BASE_URL)))
                .andRespond(withSuccess("{\"total_count\":0,\"items\":[]}",MediaType.APPLICATION_JSON));
        when(gitRepoMapper.toGitRepos(List.of())).thenReturn(List.of());

        List<RepoResponseDto> result = service.searchAndSave(new SearchRequestDto("spring",null, SortCategory.stars));
        assertThat(result).isEmpty();
        verify(gitRepoMapper).toGitRepos(List.of());
    }

    @Test
    void propagatesErrorWhenGithubRateLimits() {
        server.expect(requestTo(startsWith(BASE_URL)))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));
        assertThatThrownBy(()-> service.searchAndSave(new SearchRequestDto("spring",null, SortCategory.stars)))
                .isInstanceOf(HttpClientErrorException.class);
    }

    // parsing

    @Test
    void parsesAndFlattenedFirstRepoFromRealResponse() {
        server.expect(requestTo(startsWith(BASE_URL)))
                .andRespond(withSuccess(githubResponse,MediaType.APPLICATION_JSON));
        when(gitRepoMapper.toGitRepos(anyList())).thenReturn(List.of());
        service.searchAndSave(new SearchRequestDto("spring",null, SortCategory.stars));
        verify(gitRepoMapper).toGitRepos(dtoCaptor.capture());
        GithubRepoDto first = dtoCaptor.getValue().get(0);
        assertThat(first.id()).isEqualTo(1148753L);
        assertThat(first.name()).isEqualTo("spring-framework");
        assertThat(first.description()).isEqualTo("Spring Framework");
        assertThat(first.owner()).isEqualTo("spring-projects"); // flattened from owner.login
        assertThat(first.language()).isEqualTo("Java");
        assertThat(first.stars()).isEqualTo(60320);
        assertThat(first.forks()).isEqualTo(38801);
        assertThat(first.lastUpdated()).isEqualTo(Instant.parse("2026-11-18T16:19:00Z"));
    }

    @Test
    void parsesAllItemsAndKeepsNonAsciiDescription() {
        server.expect(requestTo(startsWith(BASE_URL)))
                .andRespond(withSuccess(githubResponse,MediaType.APPLICATION_JSON));
        when(gitRepoMapper.toGitRepos(anyList())).thenReturn(List.of());

        service.searchAndSave(new SearchRequestDto("spring",null, SortCategory.stars));

        verify(gitRepoMapper).toGitRepos(dtoCaptor.capture());
        List<GithubRepoDto> dtos = dtoCaptor.getValue();

        assertThat(dtos).hasSize(2);
        GithubRepoDto second = dtos.get(1);
        assertThat(second.id()).isEqualTo(332357698L);
        assertThat(second.name()).isEqualTo("ruoyi-vue-pro");
        assertThat(second.owner()).isEqualTo("YunaiV");
        assertThat(second.stars()).isEqualTo(40516);
        assertThat(second.forks()).isEqualTo(8686);
        assertThat(second.description()).startsWith("🔥 官方推荐 🔥 RuoYi-Vue 全新 Pro 版本，优化重构所有功能。基于 Spring Boot + MyBatis Plus + Vue & Element 实现的后台管理系统 + 微信小程序，支持 RBAC 动态权限、数据权限、SaaS 多租户、Flowable 工作流、三方登录、支付、短信、商城、CRM、ERP、MES、IM、AI 大模型、IoT 物联网等功能。你的 ⭐️ Star ⭐️，是作者生发的动力！");
        assertThat(second.lastUpdated()).isEqualTo(Instant.parse("2026-11-13T13:14:23Z"));


    }



}
