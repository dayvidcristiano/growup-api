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

        // 1. Chama o AIService para gerar as histórias
        List<UserStory> stories = aiService.gerarHistorias(promptCompleto, fileBase64, mimeType);

        // --- CORREÇÃO: Limpar o backlog antes de salvar as novas histórias ---
        // 2. Apaga todas as histórias existentes para iniciar um novo ciclo
        repository.deleteAllInBatch();

        // 3. Salva as novas histórias no banco
        repository.saveAll(stories);
        // -------------------------------------------------------------------

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

    /**
     * Envia uma história de usuário para o Jira, salva a chave da Issue e retorna.
     * @param id ID da UserStory no banco.
     * @param projectKey Chave do projeto Jira (Ex: KAN).
     * @return A chave da Issue do Jira criada (ex: KAN-1).
     */
    public String enviarHistoriaParaJira(Long id, String projectKey) {
        UserStory historia = repository.findById(id).orElseThrow(() -> new RuntimeException("História não encontrada"));

        // 1. Cria a Issue no Jira usando o projectKey recebido
        String jiraIssueKey = jiraService.criarIssue(historia, projectKey);

        // 2. Salva a chave da Issue na UserStory
        historia.setJiraIssueKey(jiraIssueKey);

        // 3. Salva a história atualizada no banco de dados
        repository.save(historia);

        return jiraIssueKey;
    }
}