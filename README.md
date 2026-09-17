# CampusGigs — Projeto Diamante

Plataforma de freelas entre estudantes universitários desenvolvida com Spring Boot, Spring Security com JWT, Flyway, Docker Compose e Spring HttpExchange.

## 1. Tecnologias Utilizadas

- Java 17
- Spring Boot 4.1.1
- Spring Security com autenticação stateless e tokens JWT via jjwt 0.12.6
- Spring Data JPA e Hibernate
- Flyway para versionamento e migrações do banco de dados
- PostgreSQL 16 via Docker Compose
- Spring HttpExchange como cliente HTTP declarativo para integração com ViaCEP
- Lombok e Bean Validation com Jakarta Validation

## 2. Instruções de Execução

### Pré-requisitos

- Docker Desktop instalado e em execução.
- Java 17 JDK instalado ou uso do wrapper Gradle incluso no projeto.

### 2.1 Subindo o Banco de Dados com Docker

O projeto conta com o `compose.yaml` configurado. Graças à dependência `spring-boot-docker-compose`, o próprio comando de execução da aplicação já sobe o container do PostgreSQL automaticamente, não é necessário rodar `docker compose up` manualmente.

### 2.2 Executando a Aplicação Localmente

No Linux ou macOS:
```bash
./gradlew bootRun
```

No Windows, no PowerShell ou Prompt de Comando:
```bash
.\gradlew.bat bootRun
```

A API estará disponível em `http://localhost:8080`.

### 2.3 Executando a Suíte de Testes Automatizados

```bash
.\gradlew.bat test
```

## 3. Principais Endpoints da API

### Autenticação e Usuários

- `POST /usuarios` — Cadastra um novo usuário aluno. Caso o CEP seja informado, a API consulta o ViaCEP de forma declarativa e preenche cidade e UF automaticamente.
- `POST /auth/login` — Autentica o usuário e retorna o token JWT no formato Bearer.
- `GET /usuarios/me` — Retorna os dados do usuário autenticado no momento (rota protegida).
- `PATCH /usuarios/me/cep` — Atualiza o CEP do usuário logado, reconsultando cidade e UF no serviço externo.

### Serviços

- `POST /servicos` — Publica um novo serviço com autenticação obrigatória, definindo o autor autenticado como prestador.
- `GET /servicos` — Lista os serviços publicados, aberto para consulta pública (sem necessidade de token).
- `PATCH /servicos/{id}/encerrar` — Encerra um serviço publicado, restrito ao prestador dono ou usuário com papel ADMIN.

### Contratações

- `POST /contratacoes` — Contrata um serviço ativo com autenticação obrigatória, vedada a contratação do próprio serviço ou de serviços fora da situação ativa.

## 4. Exemplos de Chamadas e Evidências de Testes Manuais

Todos os testes abaixo foram executados via PowerShell (`Invoke-RestMethod`), na mesma sessão de terminal.

### 4.1 CP1 — Ambiente e Schema Inicial

Ambiente sobe automaticamente via `spring-boot-docker-compose` (Postgres) e a primeira migration do Flyway aplica o schema inicial (`usuarios`, `servicos`, `contratacoes`) na inicialização.

### 4.2 CP2 — Cadastro e Autenticação

**Cadastro de novo usuário — sucesso esperado:**
```powershell
Invoke-RestMethod -Uri http://localhost:8080/usuarios -Method POST -ContentType "application/json" -Body '{"nome": "Maria Souza", "email": "maria@campusgigs.br", "senha": "senha123"}'
```
Resultado: `201 Created`, retorna os dados da Maria com senha nunca exposta em texto puro (hash BCrypt armazenado no banco).

**Login com senha correta — sucesso esperado:**
```powershell
Invoke-RestMethod -Uri http://localhost:8080/auth/login -Method POST -ContentType "application/json" -Body '{"email": "maria@campusgigs.br", "senha": "senha123"}'
```
Resultado: `200 OK`, retorna o token JWT.

**Login com senha errada — erro esperado:**
```powershell
try {
    Invoke-RestMethod -Uri http://localhost:8080/auth/login -Method POST -ContentType "application/json" -Body '{"email": "maria@campusgigs.br", "senha": "senhaerrada"}'
} catch {
    $_.ErrorDetails.Message
}
```
Resultado: `401 Unauthorized`
```json
{
  "timestamp": "...",
  "status": 401,
  "erro": "Credenciais inválidas"
}
```

**Cadastro com e-mail repetido — erro esperado:**
```powershell
Invoke-RestMethod -Uri http://localhost:8080/usuarios -Method POST -ContentType "application/json" -Body '{"nome": "Maria Souza", "email": "maria@campusgigs.br", "senha": "senha123"}'
```
Resultado: `400 Bad Request` — a API bloqueou o cadastro duplicado.

### 4.3 CP3 — Emissão e Validação de Token JWT

**Rota protegida sem token — erro esperado:**
```powershell
Invoke-RestMethod -Uri http://localhost:8080/usuarios/me -Method GET
```
Resultado: `401 Unauthorized`
```json
{
  "timestamp": "...",
  "status": 401,
  "erro": "Não autenticado"
}
```

**Rota protegida com token válido — sucesso esperado:**
```powershell
$resp = Invoke-RestMethod -Uri http://localhost:8080/auth/login -Method POST -ContentType "application/json" -Body '{"email": "maria@campusgigs.br", "senha": "senha123"}'
$token = $resp.token

Invoke-RestMethod -Uri http://localhost:8080/usuarios/me -Method GET -Headers @{Authorization = "Bearer $token"}
```
Resultado: `200 OK`, retorna os dados da Maria.

### 4.4 CP4 — Regras de Autorização por Papel

**Evidência de Acesso Negado por Papel (403 Forbidden):**

Cadastro de um segundo usuário (Pedro) e tentativa de encerrar um serviço que pertence à Maria:
```powershell
Invoke-RestMethod -Uri http://localhost:8080/usuarios -Method POST -ContentType "application/json" -Body '{"nome": "Pedro Alves", "email": "pedro@campusgigs.br", "senha": "senha123"}'
$respPedro = Invoke-RestMethod -Uri http://localhost:8080/auth/login -Method POST -ContentType "application/json" -Body '{"email": "pedro@campusgigs.br", "senha": "senha123"}'
$tokenPedro = $respPedro.token

try {
    Invoke-RestMethod -Uri http://localhost:8080/servicos/1/encerrar -Method PATCH -Headers @{Authorization = "Bearer $tokenPedro"}
} catch {
    $_.Exception.Response.StatusCode
}
```
Resultado: `403 Forbidden` — Pedro não é o dono do serviço e não pode encerrá-lo.

**Regra de Negócio: Proibição de Contratar o Próprio Serviço:**
```powershell
try {
    Invoke-RestMethod -Uri http://localhost:8080/contratacoes -Method POST -ContentType "application/json" -Headers @{Authorization = "Bearer $token"} -Body '{"servicoId": 1}'
} catch {
    $_.Exception.Response.StatusCode
}
```
Resultado: `400 Bad Request` — Maria (dona do serviço) não pode contratar o próprio serviço.

**Contratação por outro aluno — sucesso esperado:**
```powershell
Invoke-RestMethod -Uri http://localhost:8080/contratacoes -Method POST -ContentType "application/json" -Headers @{Authorization = "Bearer $tokenPedro"} -Body '{"servicoId": 1}'
```
Resultado: `201 Created`
```json
{
  "id": 1,
  "servicoId": 1,
  "servicoTitulo": "Aulas de Java",
  "contratanteId": 2,
  "situacao": "SOLICITADA",
  "criadoEm": "..."
}
```

### 4.5 CP5 — Integração Declarativa com ViaCEP via HttpExchange

**Cadastro de usuário com CEP válido — integração bem-sucedida:**
```powershell
Invoke-RestMethod -Uri http://localhost:8080/usuarios -Method POST -ContentType "application/json" -Body '{"nome": "Lucas Silva", "email": "lucas@campusgigs.br", "senha": "senha123", "cep": "01310-100"}'
```
Resultado: `201 Created`
```json
{
  "id": 3,
  "nome": "Lucas Silva",
  "email": "lucas@campusgigs.br",
  "cep": "01310100",
  "cidade": "São Paulo",
  "uf": "SP",
  "papel": "USER"
}
```

**Cadastro de usuário com CEP inexistente — erro claro esperado:**
```powershell
try {
    Invoke-RestMethod -Uri http://localhost:8080/usuarios -Method POST -ContentType "application/json" -Body '{"nome": "Teste Falha", "email": "falha@campusgigs.br", "senha": "senha123", "cep": "99999-999"}'
} catch {
    $_.Exception.Response.StatusCode
}
```
Resultado: `400 Bad Request` — o sistema rejeita o CEP inexistente de forma clara, sem deixar o cadastro incompleto.

**Atualização de CEP de um usuário já autenticado:**
```powershell
$respLucas = Invoke-RestMethod -Uri http://localhost:8080/auth/login -Method POST -ContentType "application/json" -Body '{"email": "lucas@campusgigs.br", "senha": "senha123"}'
$tokenLucas = $respLucas.token

Invoke-RestMethod -Uri http://localhost:8080/usuarios/me/cep -Method PATCH -ContentType "application/json" -Headers @{Authorization = "Bearer $tokenLucas"} -Body '{"cep": "20020-010"}'
```
Resultado: `200 OK`, cidade e UF reconsultados e atualizados no serviço externo.

## 5. Justificativa das Decisões por Checkpoint

**CP1:** Ambiente configurado com Docker Compose e PostgreSQL, subindo automaticamente via `spring-boot-docker-compose` junto com a execução da aplicação. Flyway aplica o schema inicial (`V1__create_tables.sql`) na primeira inicialização.

**CP2:** Cadastro e autenticação seguros usando hash BCrypt para senhas e validação de credenciais centralizada, sem expor detalhes internos ao cliente.

**CP3:** Emissão e validação de tokens JWT com filtro customizado (`JwtAuthenticationFilter`, baseado em `OncePerRequestFilter`), garantindo autenticação prévia em rotas restritas. Um `AuthenticationEntryPoint` customizado distingue corretamente requisições sem autenticação (401) de requisições sem permissão (403).

**CP4:** Modelagem do domínio de Serviços e Contratações aplicando controle estrito de permissões: apenas o prestador dono ou um usuário ADMIN pode encerrar um serviço; contratações bloqueiam o contratante de contratar o próprio serviço ou um serviço fora da situação ativa.

**CP5:** Implementação de cliente declarativo Spring HTTP (`@HttpExchange` / `@GetExchange`) integrado ao ViaCEP, com timeout de conexão (3s) e leitura (4s) configurados explicitamente. Falhas do serviço externo (indisponibilidade, lentidão, CEP inexistente) são interceptadas e convertidas em respostas claras e centralizadas, evitando que o cadastro fique silenciosamente incompleto.

## Equipe de Desenvolvimento

- Erik Naoki Miyasato RM565771
- Juliana da Silva Stigliani RM561171
