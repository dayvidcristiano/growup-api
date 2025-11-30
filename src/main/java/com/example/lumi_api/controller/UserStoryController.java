package com.example.lumi_api.controller;

import com.example.lumi_api.model.UserStory;
import com.example.lumi_api.service.UserStoryService;
import com.example.lumi_api.service.JiraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/historias")
public class UserStoryController {

    private final UserStoryService userStoryService;
    private final JiraService jiraService;

    public UserStoryController(UserStoryService userStoryService, JiraService jiraService) {
        this.userStoryService = userStoryService;
        this.jiraService = jiraService;
    }

    @PostMapping("/gerar-ia")
    public ResponseEntity<List<UserStory>> gerarHistorias(
            @RequestParam("prompt") String prompt,
            @RequestParam("file") MultipartFile file) {

        try {
            List<UserStory> historias = userStoryService.gerarEProcessarHistorias(prompt, file);
            return ResponseEntity.ok(historias);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    @GetMapping("/jira-conexao")
    public ResponseEntity<String> verificarConexaoJira() {
        try {
            String resultado = jiraService.verificarConexaoProjeto();
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Falha ao verificar conexão com o Jira: " + e.getMessage());
        }
    }
}