package com.example.lumi_api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service // Agora é um componente Spring
public class FileExtractorService {

    // Método não mais estático
    public String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return "Nenhum arquivo de requisitos fornecido.";
        }

        String nomeArquivo = file.getOriginalFilename().toLowerCase();

        if (nomeArquivo.endsWith(".txt")) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }

        // Aviso se for um arquivo binário complexo (PDF, DOCX)
        if (nomeArquivo.endsWith(".pdf") || nomeArquivo.endsWith(".docx")) {
            return "O conteúdo do arquivo '" + nomeArquivo + "' não pôde ser lido. Considere apenas os requisitos de 'Nome do Projeto' e 'Contexto Adicional'.";
        }

        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "Erro ao ler o conteúdo do arquivo.";
        }
    }
}