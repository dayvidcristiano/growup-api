package com.example.lumi_api.service;

import com.example.lumi_api.model.UserStory;
import com.example.lumi_api.repository.UserStoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class UserStoryService {

    private final UserStoryRepository repository;
    private final AIService aiService;
    private final JiraService jiraService;
    private final FileExtractorService fileExtractorService;

    public UserStoryService(UserStoryRepository repository, AIService aiService, JiraService jiraService, FileExtractorService fileExtractorService) {
        this.repository = repository;
        this.aiService = aiService;
        this.jiraService = jiraService;
        this.fileExtractorService = fileExtractorService;
    }

    public List<UserStory> gerarEProcessarHistorias(String prompt, MultipartFile file) throws IOException {

        String textoDoDocumento = fileExtractorService.extractText(file);
        String promptCompleto = "Nome do Projeto: " + prompt + "\n" + "Contexto Adicional: " + textoDoDocumento;

        String fileBase64 = null;
        String mimeType = null;

        if (file != null && !file.isEmpty()) {
            fileBase64 = Base64.getEncoder().encodeToString(file.getBytes());
            mimeType = file.getContentType();
        }

        List<UserStory> stories = aiService.gerarHistorias(promptCompleto, fileBase64, mimeType);

        repository.deleteAllInBatch();

        repository.saveAll(stories);

        return stories;
    }

    public List<UserStory> buscarHistorias() {
        return repository.findAll();
    }

    public UserStory atualizarHistoria(Long id, UserStory historiaAtualizada) {
        UserStory historia = repository.findById(id).orElseThrow(() -> new RuntimeException("História não encontrada"));

        historia.setPapel(historiaAtualizada.getPapel());
        historia.setAcao(historiaAtualizada.getAcao());
        historia.setBeneficio(historiaAtualizada.getBeneficio());
        historia.setPrioridade(historiaAtualizada.getPrioridade());
        historia.setEstimativa(historiaAtualizada.getEstimativa());

        return repository.save(historia);
    }

    public void deletarHistoria(Long id) {
        repository.deleteById(id);
    }

    public String enviarHistoriaParaJira(Long id, String projectKey) {
        UserStory historia = repository.findById(id).orElseThrow(() -> new RuntimeException("História não encontrada"));

        String jiraIssueKey = jiraService.criarIssue(historia, projectKey);

        historia.setJiraIssueKey(jiraIssueKey);

        repository.save(historia);

        return jiraIssueKey;
    }
}