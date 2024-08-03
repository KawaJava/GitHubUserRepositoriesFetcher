package io.github.kawajava.GitHubUserRepositoriesFetcher.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.kawajava.GitHubUserRepositoriesFetcher.exception.NotExistingUserException;
import io.github.kawajava.GitHubUserRepositoriesFetcher.model.*;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.github.kawajava.GitHubUserRepositoriesFetcher.service.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest
class GitHubUserServiceTest {

    private final WireMockServer wireMockServer = new WireMockServer();

    private final boolean ISFORK = true;
    private final boolean ISNOTFORK = false;

    @Mock
    private WebClient webClient;

//    @Mock
//    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;
//
//    @Mock
//    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private GitHubUserService userService = new GitHubUserService(webClient);

//    @Before
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
//    }
    @Before
    public void setUp() {
        WebClient webClient = WebClient.builder().baseUrl("https://api.github.com").build();
//        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
//        when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    public void shouldFetchRepositoriesCorrectly() {
        var repositoryDto = new RepositoryDto("aspectj", new Owner("kamilbrzezinski"), ISFORK);

        stubFor(get(urlEqualTo("/users/kamilbrzezinski/repos"))
                .willReturn(aResponse()
                        .withBody(getUserRepositoriesResponse())
                        .withStatus(HttpStatus.OK.value())));
        var repositoryDtoFlux = userService.fetchRepositories("kamilbrzezinski");
        StepVerifier.create(repositoryDtoFlux)
                .expectNext(repositoryDto)
                .verifyComplete();
    }

    @Test
    public void shouldFetchBranchesCorrectly() {
        var commit = new Commit("5be19feeced5afc98de8e19deff86d5feff2a795");
        var branch = new BranchDto("master", commit);

        stubFor(get(urlEqualTo("/users/kamilbrzezinski/repos"))
                .willReturn(aResponse()
                        .withBody(getUserRepositoriesResponseNonFork())
                        .withStatus(HttpStatus.OK.value())));

        stubFor(get(urlEqualTo("/repos/kamilbrzezinski/aspectj/branches"))
                .willReturn(aResponse()
                        .withBody(getRepositoryBranchesResponse())
                        .withStatus(HttpStatus.OK.value())));

        var branchDtoFlux = userService.fetchBranches("kamilbrzezinski", "aspectj");
        StepVerifier.create(branchDtoFlux)
                .expectNext(branch)
                .verifyComplete();
    }

    @Test
    void shouldThrowNotExistingUserException() {
        stubFor(get(urlEqualTo("/users/kamilbrzezinski753/repos"))
                .willReturn(aResponse()
                        .withBody("GitHub User: kamilbrzezinski753 Not Found!")
                        .withStatus(HttpStatus.NOT_FOUND.value())));

        var repositoryDtoFlux = userService.fetchRepositories("kamilbrzezinski753");
        StepVerifier.create(repositoryDtoFlux)
                .expectErrorMatches(throwable -> throwable instanceof NotExistingUserException &&
                        throwable.getMessage().contains("GitHub User: kamilbrzezinski753 Not Found!"))
                .verify();
    }

    @Test
    void testMapToUserRepositoryCorrectly() {
        var owner = new Owner("kamilbrzezinski");
        var repository = new RepositoryDto( "aspectj", owner, ISNOTFORK);
        var repositoryDto = userService.mapToUserRepository(repository);
        assertThat(repositoryDto.repositoryName()).isEqualTo("aspectj");
        assertThat(repositoryDto.ownerLogin()).isEqualTo("kamilbrzezinski");
    }

    @Test
    void testMapToBranchCorrectly() {
        var commit = new Commit("5be19feeced5afc98de8e19deff86d5feff2a795");
        var branch = new BranchDto("aspectj", commit);
        var branchDto = userService.mapToBranch(branch);
        assertThat(branchDto.name()).isEqualTo("aspectj");
        assertThat(branchDto.lastCommitSha()).isEqualTo("5be19feeced5afc98de8e19deff86d5feff2a795");
    }

    @Test
    public void shouldDropForksCorrectly() {
        var repo1 = new RepositoryDto("aspectj", new Owner("kamilbrzezinski"), ISNOTFORK);
        var repo2 = new RepositoryDto("fork", new Owner("kamilbrzezinski753"), ISFORK);
        when(responseSpec.bodyToFlux(RepositoryDto.class)).thenReturn(Flux.just(repo1, repo2));

        StepVerifier.create(userService.dropForks("kamilbrzezinski"))
                .expectNext(new UserRepository("repo1", "kamilbrzezinski"))
                .verifyComplete();
    }
}