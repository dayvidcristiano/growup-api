package com.example.lumi_api.service;

import com.example.lumi_api.model.UserStory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AIService {

    @Value("${integrations.gemini.api-key}")
    private String geminiApiKey;

    @Value("${integrations.gemini.base-url}")
    private String geminiBaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AIService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<UserStory> gerarHistorias(String prompt) {
        String jsonBody = createGeminiRequestBody(prompt);
        String fullUrl = geminiBaseUrl + "?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    fullUrl, HttpMethod.POST, entity, String.class);

            return processGeminiResponse(response.getBody());

        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (e.getMessage().contains("400 Bad Request")) {
                errorMessage = e.getMessage().substring(e.getMessage().indexOf("{"));
            }
            throw new RuntimeException("Falha na comunicação com a API Gemini: " + errorMessage, e);
        }
    }

    private String createGeminiRequestBody(String prompt) {
        String systemInstruction = "Você é um especialista em engenharia de requisitos. Gere uma lista de histórias de usuário no formato JSON. Cada história deve ser um objeto com os campos: id (number), papel (string), acao (string), beneficio (string), prioridade (string: ALTA, MEDIA, BAIXA), e estimativa (string: ex. '4h'). Envolva a lista de objetos em um array chamado 'historias'.";
        String userPrompt = "Requisitos do projeto: " + prompt;

        try {
            return objectMapper.writeValueAsString(new GeminiRequest(systemInstruction, userPrompt));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao construir o JSON de requisição para o Gemini.", e);
        }
    }

    private List<UserStory> processGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode candidate = root.path("candidates").get(0);
            JsonNode content = candidate.path("content");
            String jsonText = content.path("parts").get(0).path("text").asText();

            JsonNode generatedJson = objectMapper.readTree(jsonText);
            JsonNode historiasNode = generatedJson.path("historias");

            if (historiasNode.isArray()) {
                return objectMapper.readValue(
                        historiasNode.toString(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, UserStory.class)
                );
            }
            throw new RuntimeException("O JSON de resposta do Gemini não contém o array 'historias'.");

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao processar o JSON de resposta da API Gemini.", e);
        }
    }

    private static class GeminiRequest {
        public List<Content> contents;
        public GenerationConfig generationConfig;

        // O campo systemInstruction foi removido do nível superior

        public GeminiRequest(String instruction, String userPrompt) {
            // A System Instruction é adicionada como o primeiro Content com a role 'system'
            this.contents = List.of(
                    new Content("system", List.of(new Part(instruction))),
                    new Content("user", List.of(new Part(userPrompt)))
            );
            this.generationConfig = new GenerationConfig();
        }
    }

    private static class Content {
        public String role;
        public List<Part> parts;
        public Content(String role, List<Part> parts) {
            this.role = role;
            this.parts = parts;
        }
    }

    private static class Part {
        public String text;
        public Part(String text) {
            this.text = text;
        }
    }

    private static class GenerationConfig {
        public String responseMimeType = "application/json";

        public GenerationConfig() {
        }
    }
}