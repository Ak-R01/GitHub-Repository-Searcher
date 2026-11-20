package com.gitRepoSearch.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import com.gitRepoSearch.server.repository.GitRepoUpsertDao;
import com.gitRepoSearch.server.dto.SearchRequestDto;
import com.gitRepoSearch.server.entity.GitRepo;
import com.gitRepoSearch.server.mapper.GitRepoMapper;
import com.gitRepoSearch.server.repository.GitRepoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({GitRepoMapper.class, GitRepoUpsertDao.class})
class RepoPersistenceTest {

//    private static EmbeddedPostgres postgres;
//
//    @DynamicPropertySource
//    static void database(DynamicPropertyRegistry registry) throws IOException {
//        if (postgres == null) {
//            postgres = EmbeddedPostgres.start();
//        }
//        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl("postgres", "postgres"));
//    }

    private static final String BASE_URL = "https://api.github.com/search/repositories";

    private static String githubResponse;

    @Autowired
    private GitRepoRepository repository;

    @Autowired
    private GitRepoMapper mapper;

    @Autowired
    private EntityManager entityManager;

    private MockRestServiceServer server;
    private RepoService service;

    @Autowired
    private GitRepoUpsertDao upsertDao;

    @BeforeAll
    static void loadFixture() throws IOException {
        githubResponse= new ClassPathResource("github-response.json")
                .getContentAsString(StandardCharsets.UTF_8);
    }
    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        service = new RepoService(repository, builder.build(), mapper, upsertDao);
    }
    private List<GitRepo> searchFromFixture() {
        server.reset();
        server.expect(requestTo(startsWith(BASE_URL)))
                .andRespond(withSuccess(githubResponse, MediaType.APPLICATION_JSON));
        return service.searchAndSave(new SearchRequestDto("spring",null,null));
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }


    void SearchResultsArePersistentAndCanBeReadBack() {
        List<GitRepo> repos = searchFromFixture();

        repository.saveAll(repos);
        flushAndClear();

        assertThat(repository.findAll()).hasSize(2);

        GitRepo first = repository.findById(1148753L).orElseThrow();
        assertThat(first.getName()).isEqualTo("spring-framework");
        assertThat(first.getDescription()).isEqualTo("Spring Framework");
        assertThat(first.getOwner()).isEqualTo("spring-projects"); // flattened from owner.login
        assertThat(first.getLanguage()).isEqualTo("Java");
        assertThat(first.getStars()).isEqualTo(60320);
        assertThat(first.getForks()).isEqualTo(38801);
        assertThat(first.getLastUpdated()).isNotNull();

    }

    void nonAsciiDescriptionPersists(){
        repository.saveAll(searchFromFixture());
        flushAndClear();

        GitRepo second = repository.findById(332357698L).orElseThrow();

        assertThat(second.getName()).isEqualTo("ruoyi-vue-pro");
        assertThat(second.getDescription()).startsWith("🔥 官方推荐 🔥");
        assertThat(second.getOwner()).isEqualTo("YunaiV");
    }


    void savingTheSameSearchTwiceDoesNotCreateDuplicates() {
        repository.saveAll(searchFromFixture());
        flushAndClear();

        repository.saveAll(searchFromFixture());
        flushAndClear();

        assertThat(repository.count()).isEqualTo(2);
    }
}
