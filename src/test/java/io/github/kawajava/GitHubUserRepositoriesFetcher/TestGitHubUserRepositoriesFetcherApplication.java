package io.github.kawajava.GitHubUserRepositoriesFetcher;

import org.springframework.boot.SpringApplication;

public class TestGitHubUserRepositoriesFetcherApplication {

	public static void main(String[] args) {
		SpringApplication.from(GitHubUserRepositoriesFetcherApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
