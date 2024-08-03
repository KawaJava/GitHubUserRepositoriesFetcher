package io.github.kawajava.GitHubUserRepositoriesFetcher.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.kawajava.GitHubUserRepositoriesFetcher.model.Branch;
import io.github.kawajava.GitHubUserRepositoriesFetcher.model.RepositoryWithBranches;
import io.github.kawajava.GitHubUserRepositoriesFetcher.service.GitHubUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = GitHubUserController.class)
class GitHubUserControllerTest {

    private final String username = "kamilbrzezinski";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GitHubUserService userService;

    private WireMockServer wireMockServer;

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    @Test
    public void shouldGetUserDataWithValidAcceptHeadersCorrectly() {
        var repo1 = new RepositoryWithBranches("aspectj", "kamilbrzezinski",
                List.of(new Branch("master", "5be19feeced5afc98de8e19deff86d5feff2a795")));
        var repo2 = new RepositoryWithBranches("repo2", "",
                List.of(new Branch("dev", "sha2")));
        var repos = List.of(repo1, repo2);

        when(userService.getGitHubUserRepositoriesData("kamilbrzezinski")).thenReturn(Flux.just(repos));

        webTestClient.get()
                .uri("/kamilbrzezinski")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBodyList(RepositoryWithBranches.class)
                .consumeWith(response -> {
                    List<RepositoryWithBranches> responseBody = response.getResponseBody();
                    assertThat(responseBody).isNotNull();
                    assertThat(responseBody).hasSize(2);

                    var responseRepo1 = responseBody.get(0);
                    assertThat(responseRepo1.repositoryName()).isEqualTo("aspectj");
                    assertThat(responseRepo1.ownerLogin()).isEqualTo("kamilbrzezinski");
                    assertThat(responseRepo1.branches()).hasSize(1);
                    assertThat(responseRepo1.branches().get(0).name()).isEqualTo("master");
                    assertThat(responseRepo1.branches().get(0).lastCommitSha()).isEqualTo("5be19feeced5afc98de8e19deff86d5feff2a795");

                    var responseRepo2 = responseBody.get(1);
                    assertThat(responseRepo2.repositoryName()).isEqualTo("repo2");
                    assertThat(responseRepo2.ownerLogin()).isEmpty();
                    assertThat(responseRepo2.branches()).hasSize(1);
                    assertThat(responseRepo2.branches().get(0).name()).isEqualTo("dev");
                    assertThat(responseRepo2.branches().get(0).lastCommitSha()).isEqualTo("sha2");
                });
    }

    @Test
    void shouldNotReturnRepositoriesWhenHeaderIsWrong() {
        when(userService.getGitHubUserRepositoriesData(username)).thenReturn(Flux.just());
        Flux<List<RepositoryWithBranches>> response = Flux.just(Collections.emptyList());
        Mockito.when(userService.getGitHubUserRepositoriesData(username)).thenReturn(response);

        webTestClient.get()
                .uri("/" + username)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBodyList(Object.class)
                .hasSize(1)
        ;
    }
}