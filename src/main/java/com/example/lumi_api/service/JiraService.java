package com.example.lumi_api.service;

import com.example.lumi_api.model.UserStory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;
import java.util.List;
import java.nio.charset.StandardCharsets;

@Service
public class JiraService {

    private static final String ISSUE_TYPE_STORY = "Task";

    @Value("${jira.base-url}")
    private String jiraBaseUrl;

    @Value("${jira.user-email}")
    private String jiraUserEmail;

    @Value("${jira.api-token}")
    private String jiraApiToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public JiraService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String criarIssue(UserStory historia, String projectKey) {

        String titulo = "Como " + historia.getPapel() + ", quero " + historia.getAcao() + " para " + historia.getBeneficio();
        String descricao = String.format(
                "Prioridade Sugerida: %s\nEstimativa Sugerida: %s\n\n**Detalhes da História:**\nComo %s, eu quero %s para %s.",
                historia.getPrioridade(),
                historia.getEstimativa(),
                historia.getPapel(),
                historia.getAcao(),
                historia.getBeneficio()
        );

        CreateIssueRequest requestBody = new CreateIssueRequest(
                titulo,
                projectKey,
                ISSUE_TYPE_STORY,
                descricao
        );

        HttpHeaders headers = createHeaders();
        HttpEntity<CreateIssueRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    jiraBaseUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("key").asText();

        } catch (HttpClientErrorException e) {
            String errorMsg = e.getResponseBodyAsString();
            throw new RuntimeException("Falha ao criar issue no Jira. Código: " + e.getStatusCode() + ". Erro: " + errorMsg, e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao processar a resposta JSON do Jira.", e);
        }
    }

    private HttpHeaders createHeaders() {
        String auth = jiraUserEmail + ":" + jiraApiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private static class CreateIssueRequest {
        public Fields fields;

        public CreateIssueRequest(String summary, String projectKey, String issueType, String description) {
            this.fields = new Fields(
                    summary,
                    new Project(projectKey),
                    new IssueType(issueType),
                    new DescriptionADF(description)
            );
        }
    }

    private static class Fields {
        public String summary;
        public Project project;
        public IssueType issuetype;
        public DescriptionADF description;

        public Fields(String summary, Project project, IssueType issuetype, DescriptionADF description) {
            this.summary = summary;
            this.project = project;
            this.issuetype = issuetype;
            this.description = description;
        }
    }

    private static class Project {
        public String key;
        public Project(String key) { this.key = key; }
    }

    private static class IssueType {
        public String name;
        public IssueType(String name) { this.name = name; }
    }

    private static class DescriptionADF {
        public String type = "doc";
        public int version = 1;
        public List<Content> content;

        public DescriptionADF(String text) {
            this.content = List.of(
                    new Content("paragraph", List.of(new TextPart(text)))
            );
        }
    }

    private static class Content {
        public String type;
        public List<TextPart> content;
        public Content(String type, List<TextPart> content) {
            this.type = type;
            this.content = content;
        }
    }

    private static class TextPart {
        public String type = "text";
        public String text;
        public TextPart(String text) {
            this.text = text;
        }
    }
}