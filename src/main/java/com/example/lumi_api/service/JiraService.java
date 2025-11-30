package com.example.lumi_api.service;

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

@Service
public class JiraService {

    private static final String ISSUE_TYPE_STORY = "Story";

    @Value("${jira.base-url}")
    private String jiraBaseUrl; // Mantém a URL base (Issue endpoint)

    @Value("${jira.project-key}")
    private String jiraProjectKey;

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

    // --- NOVO MÉTODO: Puxar algo do quadro para testar a conexão ---
    public String verificarConexaoProjeto() {
        String projectUrl = "https://cesar-team-qbew2ary.atlassian.net/rest/api/3/project/" + jiraProjectKey;

        String auth = jiraUserEmail + ":" + jiraApiToken;
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Chamada GET para o endpoint de projeto
            ResponseEntity<String> response = restTemplate.exchange(
                    projectUrl, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String nomeProjeto = root.path("name").asText();
            String idProjeto = root.path("id").asText();

            return "Conexão com o Jira estabelecida! Projeto: " + nomeProjeto + " (ID: " + idProjeto + "). A chave '" + jiraProjectKey + "' é válida.";

        } catch (HttpClientErrorException.NotFound e) {
            return "Falha na conexão: Chave do projeto '" + jiraProjectKey + "' não encontrada ou usuário sem permissão de acesso ao projeto.";
        } catch (HttpClientErrorException.Forbidden e) {
            return "Falha na conexão: Autenticação bem-sucedida, mas o usuário não tem permissão para visualizar o projeto '" + jiraProjectKey + "'.";
        } catch (Exception e) {
            System.err.println("Erro ao verificar conexão com o Jira: " + e.getMessage());
            return "Falha grave na conexão ou autenticação. Verifique o token e o e-mail.";
        }
    }

    // O método criarIssueDeTeste foi mantido aqui para referência, mas a lógica de Business (IssueADF) foi movida abaixo para manter a consistência.
    public String criarIssueDeTeste(String titulo, String descricao) {

        String auth = jiraUserEmail + ":" + jiraApiToken;
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());

        String issueJson = createIssueJson(titulo, descricao, ISSUE_TYPE_STORY);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);

        HttpEntity<String> entity = new HttpEntity<>(issueJson, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    jiraBaseUrl, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return "Issue criada com sucesso. Chave: " + root.path("key").asText() + ", URL: " + root.path("self").asText();

        } catch (Exception e) {
            System.err.println("Erro ao criar Issue no Jira: " + e.getMessage());
            throw new RuntimeException("Falha ao se comunicar com a API do Jira.", e);
        }
    }

    // Métodos privados para construção do JSON (ADF)
    private String createIssueJson(String summary, String description, String issueTypeName) {
        try {
            DescriptionADF descriptionADF = new DescriptionADF(description);

            JiraIssueRequest request = new JiraIssueRequest(
                    new Fields(
                            summary,
                            new Project(jiraProjectKey),
                            new IssueType(issueTypeName),
                            descriptionADF
                    )
            );
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar JSON para o Jira.", e);
        }
    }

    private static class JiraIssueRequest {
        public Fields fields;
        public JiraIssueRequest(Fields fields) { this.fields = fields; }
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

    // DTOs para o formato Atlassian Document Format (ADF)
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