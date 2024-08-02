package io.github.kawajava.GitHubUserRepositoriesFetcher.controller;

import io.github.kawajava.GitHubUserRepositoriesFetcher.model.RepositoryWithBranches;
import io.github.kawajava.GitHubUserRepositoriesFetcher.service.GitHubUserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
public class GitHubUserController {

    private final GitHubUserService userService;

    public GitHubUserController(GitHubUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public Flux<List<RepositoryWithBranches>> getUserData(@PathVariable String username,
                                                          @RequestHeader(HttpHeaders.ACCEPT) String accept) {
        return accept.equals(MediaType.APPLICATION_JSON_VALUE) ?
                userService.getGitHubUserRepositoriesData(username) : Flux.empty();
    }

}
