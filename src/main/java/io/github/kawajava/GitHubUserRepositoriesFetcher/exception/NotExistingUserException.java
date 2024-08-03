package io.github.kawajava.GitHubUserRepositoriesFetcher.exception;

public class NotExistingUserException extends RuntimeException {

    public NotExistingUserException(String username) {
        super("GitHub User: " + username + " Not Found!");
    }
}
