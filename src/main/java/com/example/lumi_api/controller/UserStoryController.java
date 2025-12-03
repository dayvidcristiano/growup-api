package com.example.lumi_api.controller;

import com.example.lumi_api.model.UserStory;
import com.example.lumi_api.service.UserStoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/historias")
public class UserStoryController {

    private final UserStoryService userStoryService;

    public UserStoryController(UserStoryService userStoryService) {
        this.userStoryService = userStoryService;
    }

    @PostMapping("/gerar-ia")
    public ResponseEntity<List<UserStory>> gerarHistoriasComIA(
            @RequestParam String prompt,
            @RequestParam(required = false) MultipartFile file) throws IOException {

        List<UserStory> historias = userStoryService.gerarEProcessarHistorias(prompt, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(historias);
    }

    @GetMapping
    public ResponseEntity<List<UserStory>> buscarHistorias() {
        List<UserStory> historias = userStoryService.buscarHistorias();
        return ResponseEntity.ok(historias);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserStory> atualizarHistoria(@PathVariable Long id, @RequestBody UserStory historiaAtualizada) {
        UserStory historia = userStoryService.atualizarHistoria(id, historiaAtualizada);
        return ResponseEntity.ok(historia);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarHistoria(@PathVariable Long id) {
        userStoryService.deletarHistoria(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/enviar-jira/{id}")
    public ResponseEntity<String> enviarParaJira(@PathVariable Long id, @RequestParam String projectKey) {
        String jiraIssueKey = userStoryService.enviarHistoriaParaJira(id, projectKey);
        return ResponseEntity.ok("{\"key\": \"" + jiraIssueKey + "\"}");
    }
}