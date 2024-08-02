package io.github.kawajava.GitHubUserRepositoriesFetcher.model;

import java.util.List;

public record RepositoryWithBranches(String repositoryName, String ownerLogin, List<Branch> branches) {
}
