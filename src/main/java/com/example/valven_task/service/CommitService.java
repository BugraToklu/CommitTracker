package com.example.valven_task.service;

import com.example.valven_task.model.Commit;
import com.example.valven_task.model.Developer;
import com.example.valven_task.repository.CommitRepository;
import com.example.valven_task.repository.DeveloperRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommitService {
    private final RestTemplate restTemplate;
    private final CommitRepository commitRepository;
    private final DeveloperRepository developerRepository;

    @Value("${github.api.url}")
    private String githubApiUrl;

    @Value("${gitlab.api.url}")
    private String gitlabApiUrl;

    @Value("${gitlab.project.id}")
    private String gitlabProjectId;

    @Value("${gitlab.access.token}")
    private String gitlabAccessToken;

    @Value("${github.access.token}")
    private String githubAccessToken;

    @PostConstruct
    public void init() {
        fetchCommitsFromGitHub();
        fetchCommitsFromGitLab();
    }


    private String getOneMonthAgoISO() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        return oneMonthAgo.format(DateTimeFormatter.ISO_DATE_TIME);
    }


    public void fetchCommitsFromGitHub() {
        String url = githubApiUrl + "/repos/f-droid/fdroidclient/commits?since=" + getOneMonthAgoISO();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
        JsonNode commits = response.getBody();
        System.out.println("GitHub API Response: " + commits);


        if (commits != null) {
            for (Iterator<JsonNode> it = commits.elements(); it.hasNext(); ) {
                JsonNode commitNode = it.next();
                String dateStr = commitNode.get("commit")
                        .get("author")
                        .get("date")
                        .asText();

                // OffsetDateTime kullanarak parse ediyoruz.
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                String email = commitNode.get("commit")
                        .get("author")
                        .get("email")
                        .asText();

                Optional<Developer> developerOpt = developerRepository.findByEmail(email);
                Developer developer = developerOpt.orElseGet(() -> {
                    Developer newDev = new Developer();
                    newDev.setUsername(commitNode.get("commit")
                            .get("author")
                            .get("name")
                            .asText());
                    newDev.setEmail(email);
                    return developerRepository.save(newDev);
                });

                Commit newCommit = new Commit();
                newCommit.setSha(commitNode.get("sha").asText());
                newCommit.setAuthorName(developer.getUsername());
                newCommit.setAuthorEmail(email);
                newCommit.setMessage(commitNode.get("commit")
                        .get("message")
                        .asText());
                newCommit.setTimestamp(offsetDateTime);
                newCommit.setProvider("GitHub");
                newCommit.setPatch(commitNode.get("html_url").asText());
                newCommit.setRepositoryName("fdroidclient");
                newCommit.setDeveloper(developer);

                if (!commitRepository.existsByShaAndProvider(newCommit.getSha(), newCommit.getProvider())) {
                    System.out.println("Saving GitHub commit: " + newCommit.getSha());
                    commitRepository.save(newCommit);
                } else {
                    System.out.println("GitHub commit already exists: " + newCommit.getSha());
                }
            }
        }
    }


    public void fetchCommitsFromGitLab() {
        String url = gitlabApiUrl + "/projects/" + gitlabProjectId + "/repository/commits?since=" + getOneMonthAgoISO();
        HttpHeaders headers = new HttpHeaders();
        headers.set("PRIVATE-TOKEN", gitlabAccessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
        JsonNode commits = response.getBody();
        System.out.println("GitLab API Response: " + commits); // API yaniti

        if (commits != null && commits.isArray()) {
            for (Iterator<JsonNode> it = commits.elements(); it.hasNext(); ) {
                JsonNode commitNode = it.next();
                String email = commitNode.get("author_email").asText();

                Optional<Developer> developerOpt = developerRepository.findByEmail(email);
                Developer developer = developerOpt.orElseGet(() -> {
                    Developer newDev = new Developer();
                    newDev.setUsername(commitNode.get("author_name").asText());
                    newDev.setEmail(email);
                    return developerRepository.save(newDev);
                });

                // Commit oluşturma
                Commit newCommit = new Commit();
                newCommit.setSha(commitNode.get("id").asText());
                newCommit.setAuthorName(developer.getUsername());
                newCommit.setAuthorEmail(email);
                newCommit.setMessage(commitNode.get("message").asText());

                newCommit.setTimestamp(
                        OffsetDateTime.parse(
                                commitNode.get("created_at").asText(),
                                DateTimeFormatter.ISO_OFFSET_DATE_TIME
                        )
                );
                newCommit.setProvider("GitLab");
                newCommit.setPatch(commitNode.get("web_url").asText());
                newCommit.setRepositoryName("fdroidclient");
                newCommit.setDeveloper(developer);

                if (!commitRepository.existsByShaAndProvider(newCommit.getSha(), newCommit.getProvider())) {
                    System.out.println("Saving GitLab commit: " + newCommit.getSha());
                    commitRepository.save(newCommit);
                } else {
                    System.out.println("GitLab commit already exists: " + newCommit.getSha());
                }
            }
        }
    }

    public List<Commit> getAllCommits() {
        return commitRepository.findAll();
    }
    public Commit getCommitBySha(String sha) {
        return commitRepository.findBySha(sha).orElse(null);
    }

    public List<Commit> getCommitsByDeveloperEmail(String email) {
        return commitRepository.findByDeveloper_Email(email);
    }
}
