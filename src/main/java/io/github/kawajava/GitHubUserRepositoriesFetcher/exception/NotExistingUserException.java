package io.github.kawajava.GitHubUserRepositoriesFetcher.exception;

public class NotExistingUserException extends RuntimeException {

    public NotExistingUserException(String username) {
        super("GitHub user: " + username + " not found!");
    }
}
