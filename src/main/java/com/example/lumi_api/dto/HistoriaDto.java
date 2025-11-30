package com.example.lumi_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriaDto {
    private Long id;
    private String titulo;
    private String descricao;
    private Integer prioridade;
    private String status;
    private String jiraIssueKey;
}