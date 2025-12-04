# GrowUp - Planejamento Inteligente com Jira

> Residência Tecnológica do Porto Digital - 2025.2

## 📋 Sobre o Projeto

O **GrowUp** é uma solução desenvolvida para integrar agentes de Inteligência Artificial com o Jira. O objetivo é automatizar o planejamento do roadmap, a criação de histórias de usuário e a quebra de tarefas, aumentando a eficiência e a qualidade do backlog no processo de desenvolvimento de software.

### 🎯 O Problema
A dificuldade em planejar o roadmap, criar histórias de usuário e quebrar tarefas eficientemente resulta em perda de tempo, baixa clareza e perda de qualidade no processo de desenvolvimento.

### 💡 A Solução
Uma plataforma que utiliza IA para analisar documentos de requisitos, gerar histórias de usuário, sugerir quebra de tarefas complexas e sincronizar tudo diretamente com a API do Jira.

---

## 👥 Squad

* **Dayvid Cristiano Viana da Silva**
* **Enzo Antuña Ferreira**
* **Jeniffer Cristine Lopes da Conceição**
* **Letícia Gabriella da Costa Silva**
* **Manuele Macêdo Pereira da Silva**

---

## 🚀 Funcionalidades Principais (MVP)

O sistema prioriza as seguintes funcionalidades baseadas no método MoSCoW:

### Must Have (Essenciais)
* **Integração com API do Jira:** Autenticação e sincronização bidirecional de tarefas.
* **Criação Automática de Histórias:** Geração de histórias de usuário a partir de upload de documentos (PDF/DOCX).
* **Análise de Requisitos:** Identificação automática de complexidade e dependências entre histórias.

### Should Have (Importantes)
* **Sugestão de Quebra de Tarefas:** A IA sugere a divisão de histórias complexas em subtarefas menores.
* **Roadmap Inicial:** Sugestão de priorização e sequência temporal para o desenvolvimento.

### Could Have (Futuro)
* Edição de tarefas na interface do aplicativo (Ajuste Fino).
* Sugestões avançadas para organização de Backlog.

---

## 🛠️ Especificação Técnica

### Backend
* **Linguagem:** Java
* **Framework:** Spring Boot
* **Segurança:** JWT (JSON Web Tokens)
* **Integração:** Jira Cloud REST API

### Frontend
* **Framework:** Angular

### Banco de Dados
* **SGBD:** MariaDB

### Inteligência Artificial
* **Tecnologias:** LLMs via OpenAI API, LangChain e Hugging Face.

---

## 🗂️ Estrutura do Banco de Dados

O sistema utiliza as seguintes entidades principais para persistência:

1.  **USUARIOS:** Gerencia autenticação e função (Persona).
2.  **DOCUMENTOS:** Armazena referências aos arquivos de requisitos enviados.
3.  **HISTORIAS_USUARIO:** Contém as histórias geradas, prioridade, complexidade e ID do Jira.
4.  **TAREFAS:** Subtarefas vinculadas às histórias, com responsáveis e status.
5.  **LOG_INTERACAO:** Registro de auditoria das ações da IA e usuários.

---

## 👤 Personas e Jornadas

O sistema foi desenhado pensando em três perfis principais:

1.  **Carla Rocha (Gerente de Projetos):** Focada na qualidade dos requisitos e refinamento. Utiliza o sistema para upload de docs e análise inicial.
2.  **Bruno Mendes (Líder Técnico):** Focado na gestão e priorização. Utiliza a IA para quebrar tarefas grandes e gerar o roadmap.
3.  **Ana Silva (Desenvolvedora):** Focada na execução. Recebe as tarefas já detalhadas e sincronizadas no Jira para iniciar o trabalho.

---

## ⚙️ Como Executar (Em desenvolvimento)

### Pré-requisitos
* Java JDK 17+
* Node.js & Angular CLI
* MariaDB
* Conta no Jira (para Tokens de API)

### Passos
1.  Clone o repositório.
2.  Configure o arquivo `application.properties` com as credenciais do Banco e API do Jira.
3.  Execute o backend via Maven/Gradle.
4.  Instale as dependências do frontend (`npm install`) e inicie o servidor (`ng serve`).

---

## 📄 Licença

Este projeto faz parte da **Residência Tecnológica do Porto Digital - CESAR School**.
