package io.github.kawajava.GitHubUserRepositoriesFetcher.service;

import io.github.kawajava.GitHubUserRepositoriesFetcher.exception.NotExistingUserException;
import io.github.kawajava.GitHubUserRepositoriesFetcher.model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GitHubUserService {

    private final WebClient webClient;

    public GitHubUserService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<List<RepositoryWithBranches>> getGitHubUserRepositoriesData(String username) {
        return dropForks(username)
                .flatMap(repository -> getRepositories(repository, username))
                .collectList()
                .flux();
    }

    private Flux<RepositoryWithBranches> getRepositories(UserRepository repository, String username) {
        return fetchBranches(username, repository.repositoryName())
                .map(this::mapToBranch)
                .collectList()
                .flatMapMany(branches -> Flux.just(new RepositoryWithBranches(repository.repositoryName(), repository.ownerLogin(), branches)));
    }

    Flux<RepositoryDto> fetchRepositories(String username) {
        return this.webClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new NotExistingUserException(username)))
                .bodyToFlux(RepositoryDto.class);
    }

    Branch mapToBranch(BranchDto branch) {
        return new Branch(branch.name(), branch.commit().sha());
    }

    UserRepository mapToUserRepository(RepositoryDto repository) {
        return new UserRepository(repository.name(), repository.owner().login());
    }

    Flux<BranchDto> fetchBranches(String username, String repoName) {
        return this.webClient.get()
                .uri("/repos/{user}/{repo}/branches", username, repoName)
                .retrieve()
                .bodyToFlux(BranchDto.class);
    }

    Flux<UserRepository> dropForks(String username) {
        return fetchRepositories(username)
                .filter(repository -> !repository.isFork())
                .map(this::mapToUserRepository);
    }
}
