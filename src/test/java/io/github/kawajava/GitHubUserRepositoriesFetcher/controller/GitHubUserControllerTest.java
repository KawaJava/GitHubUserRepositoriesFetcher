package io.github.kawajava.GitHubUserRepositoriesFetcher.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.kawajava.GitHubUserRepositoriesFetcher.model.RepositoryWithBranches;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GitHubUserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

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
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/kamilbrzezinski"))
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(getValidAcceptHeaderResponse())));
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
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/kamilbrzezinski"))
                .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_XML_VALUE))
                .willReturn(WireMock.aResponse()
                        .withStatus(406)));

        webTestClient.get()
                .uri("/kamilbrzezinski")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectBodyList(Object.class)
                .hasSize(0);
    }

    private String getValidAcceptHeaderResponse() {
        return """
                [
                    {
                        "repositoryName": "aspectj",
                        "ownerLogin": "kamilbrzezinski",
                        "branches": [
                            {
                                "name": "master",
                                "lastCommitSha": "5be19feeced5afc98de8e19deff86d5feff2a795"
                            }
                        ]
                    },
                    {
                        "repositoryName": "repo2",
                        "ownerLogin": "",
                        "branches": [
                            {
                                "name": "dev",
                                "lastCommitSha": "sha2"
                            }
                        ]
                    }
                ]
                """;
        }
}