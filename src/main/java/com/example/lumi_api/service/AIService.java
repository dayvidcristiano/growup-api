package com.example.lumi_api.service;

import com.example.lumi_api.model.UserStory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class AIService {

    private static final String SYSTEM_INSTRUCTION = """
        Você é um especialista em análise de requisitos e deve criar histórias de usuário com base no input (documento e texto adicional). A resposta deve ser **SOMENTE** um array JSON contendo objetos UserStory, no seguinte formato:
        [
          {
            "papel": "...",
            "acao": "...",
            "beneficio": "...",
            "prioridade": "ALTA|MEDIA|BAIXA",
            "estimativa": "P|M|G"
          },
          ...
        ]
        """;

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

    public List<UserStory> gerarHistorias(String prompt, String fileBase64, String mimeType) {
        String jsonBody = createGeminiRequestBody(prompt, fileBase64, mimeType);
        String fullUrl = geminiBaseUrl + "?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseGeminiResponse(response.getBody());
            } else {
                throw new RuntimeException("Erro de comunicação com o serviço Gemini: " + response.getStatusCode() + " " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro de comunicação com o serviço Gemini: " + e.getMessage(), e);
        }
    }

    private String createGeminiRequestBody(String prompt, String fileBase64, String mimeType) {
        String fullPrompt = SYSTEM_INSTRUCTION + "\n\n--- INPUT DO USUÁRIO ---\n\n" + prompt;

        GeminiRequest request = new GeminiRequest(fullPrompt, fileBase64, mimeType);
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar o corpo do request Gemini.", e);
        }
    }

    private List<UserStory> parseGeminiResponse(String responseBody) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(responseBody);
        JsonNode candidates = rootNode.path("candidates");

        if (candidates.isArray() && !candidates.isEmpty()) {
            JsonNode contentNode = candidates.get(0).path("content");
            if (contentNode.path("parts").isArray() && !contentNode.path("parts").isEmpty()) {
                String jsonText = contentNode.path("parts").get(0).path("text").asText();

                if (jsonText.startsWith("```json")) {
                    jsonText = jsonText.substring(jsonText.indexOf('\n') + 1);
                    if (jsonText.endsWith("```")) {
                        jsonText = jsonText.substring(0, jsonText.lastIndexOf("```"));
                    }
                }

                try {
                    return objectMapper.readValue(jsonText, objectMapper.getTypeFactory().constructCollectionType(List.class, UserStory.class));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Erro ao converter o JSON da IA para a lista de UserStory. JSON retornado: " + jsonText, e);
                }
            }
        }

        JsonNode promptFeedback = rootNode.path("promptFeedback");
        if (promptFeedback.has("blockReason")) {
            String blockReason = promptFeedback.path("blockReason").asText();
            throw new RuntimeException("A requisição foi bloqueada pelo Gemini. Motivo: " + blockReason);
        }

        throw new RuntimeException("O JSON de resposta do Gemini não contém o array 'candidates'.");
    }

    private static class GeminiRequest {
        public List<Content> contents;
        public GenerationConfig generationConfig;

        public GeminiRequest(String userPrompt, String fileBase64, String mimeType) {

            List<Part> userParts = new ArrayList<>();

            if (fileBase64 != null && mimeType != null) {
                userParts.add(new Part(fileBase64, mimeType));
            }

            userParts.add(new Part(userPrompt));

            this.contents = List.of(
                    new Content("user", userParts)
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
        public InlineData inlineData;

        public Part(String text) {
            this.text = text;
        }

        public Part(String data, String mimeType) {
            this.inlineData = new InlineData(data, mimeType);
        }
    }

    private static class InlineData {
        public String data;
        public String mimeType;

        public InlineData(String data, String mimeType) {
            this.data = data;
            this.mimeType = mimeType;
        }
    }

    private static class GenerationConfig {
        public String responseMimeType = "application/json";

        public GenerationConfig() {
        }
    }

    private static class PromptFeedback {
        public List<SafetyRating> safetyRatings;
        public String blockReason;
    }

    private static class SafetyRating {
        public String category;
        public String probability;
    }

    private static class Candidate {
        public Content content;
        public String finishReason;
        public List<SafetyRating> safetyRatings;
    }
}