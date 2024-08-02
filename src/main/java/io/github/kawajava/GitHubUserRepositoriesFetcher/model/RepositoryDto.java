package io.github.kawajava.GitHubUserRepositoriesFetcher.model;

public record RepositoryDto(String name, Owner owner, boolean isFork) {
}
