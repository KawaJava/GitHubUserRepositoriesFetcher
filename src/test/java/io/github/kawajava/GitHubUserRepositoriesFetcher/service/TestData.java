package io.github.kawajava.GitHubUserRepositoriesFetcher.service;

public class TestData {
    public static String getUserRepositoriesResponse() {
        return """
               [
                 {
                     "name": "aspectj",
                     "owner": {
                         "login": "kamilbrzezinski"
                     },
                     "fork": true
                 }
               ]
               """;
    }

    public static String getUserRepositoriesResponseNonFork() {
        return """
               [
                 {
                     "name": "aspectj",
                     "owner": {
                         "login": "kamilbrzezinski"
                     },
                     "fork": false
                 }
               ]
               """;
    }

    public static String getRepositoryBranchesResponse() {
        return """
               [
                 {
                     "name": "master",
                     "commit": {
                         "sha": "5be19feeced5afc98de8e19deff86d5feff2a795"
                     }
                 }
               ]
               """;
    }
}
