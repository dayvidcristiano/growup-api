package com.example.lumi_api.service;

import com.example.lumi_api.model.UserStory;
import com.example.lumi_api.repository.UserStoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
public class UserStoryService {

    private final UserStoryRepository repository;
    private final AIService aiService;
    private final JiraService jiraService;

    public UserStoryService(UserStoryRepository repository, AIService aiService, JiraService jiraService) {
        this.repository = repository;
        this.aiService = aiService;
        this.jiraService = jiraService;
    }

    public List<UserStory> gerarEProcessarHistorias(String prompt, MultipartFile file) throws IOException {
        String textoDoDocumento = FileExtractorService.extractText(file);
        String promptCompleto = "Nome do Projeto: " + prompt + "\n" + "Contexto Adicional: " + textoDoDocumento;

        List<UserStory> stories = aiService.gerarHistorias(promptCompleto);

        repository.saveAll(stories);

        return stories;
    }
}