# CyberFlora — Smart Growing System

> Sistema de horta inteligente com aplicativo multiplataforma (Android, Desktop e Web) e API REST segura.

## 1. Identificação

| | |
|---|---|
| **Projeto** | CyberFlora — Smart Growing System |
| **Grupo** | Grupo individual |
| **Repositório** | https://github.com/AraujoThiago0822/cyberflora |

## 2. Integrante e responsabilidades

| Integrante | RA | Entidades sob responsabilidade |
|---|---|---|
| Thiago Araujo Silva | 1111392321021 | `Plant` (Plantas) e `GrowingArea` (Áreas de Cultivo): backend e frontend completos |

Além dessas duas entidades, existe a entidade `User`, usada apenas para autenticação (cadastro e login com JWT).

## 3. O que a aplicação faz

O CyberFlora é um sistema de **horta inteligente**. Cada usuário se cadastra, faz login e gerencia as suas **plantas** (espécie, data de plantio, faixa ideal de umidade, se está ativa) e as **áreas de cultivo** onde elas ficam (capacidade em m² e tipo de irrigação). Os dados de cada usuário são privados: um usuário nunca enxerga os registros de outro.

**Problema que resolve:** produtores e entusiastas de hortas costumam anotar o cultivo em papel ou em planilhas soltas. O CyberFlora centraliza essas informações com autenticação, validação e busca, em um único aplicativo que roda em Android, Desktop e Web. Ele também serve de base para a integração com sensores IoT (umidade do solo) e irrigação automática, prevista nas próximas entregas.

## 4. Tecnologias utilizadas

| Camada | Tecnologias |
|---|---|
| Backend | Kotlin, Spring Boot 4.1, Spring Web, Spring Data JPA (JPQL), Bean Validation, Spring Security, JWT (jjwt 0.12.6), Springdoc OpenAPI / Swagger |
| Banco de dados | PostgreSQL 16 (Docker) |
| Frontend | Kotlin 2.4 Multiplatform, Compose Multiplatform 1.12 (Material 3), Navigation3, Ktor Client 3.0, kotlinx.serialization, ViewModel e Coroutines, Material Icons |
| Build e infraestrutura | Gradle Wrapper, Docker Compose, JDK 17 |

## 5. Arquitetura: camadas e pastas

### Estrutura do repositório

| Pasta ou arquivo | Conteúdo |
|---|---|
| `backend/` | API REST (Kotlin e Spring Boot) |
| `frontend-kmp/` | Aplicativo Android, Desktop e Web (Kotlin Multiplatform) |
| `docker/` | `docker-compose.yml` do PostgreSQL |
| `docs/` | Script de teste da API (`teste-api.ps1`) e evidências do Swagger |
| `.env.example` | Modelo das variáveis de ambiente (o `.env` real não é versionado) |

### Backend (`backend/src/main/kotlin/com/cyberflora/backend/`)

| Camada | Pasta | Responsabilidade |
|---|---|---|
| Entidades | `entity/` | Classes JPA: `Plant`, `GrowingArea`, `User` |
| Repositórios | `repository/` | Acesso ao banco (Spring Data JPA) e consultas JPQL com `LIKE` |
| Serviços | `service/` | Regras de negócio e CRUD, sempre filtrando pelo dono do registro |
| Controladores | `controller/` | Rotas REST (`/plants`, `/growing-areas`, `/signin`, `/signup`) |
| DTOs | `dto/` | Objetos de Request (com Bean Validation) e de Response |
| Mappers | `mapper/` | Conversão entre DTO e Entidade |
| Segurança | `security/` | `JwtUtil`, filtro de autenticação JWT, `SecurityConfig` |
| Configuração | `config/` | Documentação OpenAPI |
| Exceções | `exception/` | Tratamento global de erros com mensagens claras |

### Frontend (`frontend-kmp/`)

| Camada | Pasta | Responsabilidade |
|---|---|---|
| Código compartilhado | `shared/src/commonMain/kotlin/com/cyberflora/frontend/` | Todo o app: UI, estado e rede |
| Modelos | `.../model/` | Classes de dados das entidades |
| Rede (RepositorioRemoto) | `.../network/` | Cliente HTTP (Ktor), repositórios remotos, sessão JWT |
| ViewModels | `.../viewmodel/` | Estado dos campos, lista e ações do CRUD |
| Telas | `.../screens/` | Login, Registro, Listagem e Formulário (Gravar e Limpar Campos) |
| Navegação | `.../navigation/` | Navigation3 em 2 níveis: entre CRUDs e entre Listagem e Formulário |
| Tema | `.../theme/` | Cores e tipografia da identidade CyberFlora |
| App Android | `androidApp/` | Ponto de entrada Android |
| App Desktop | `desktopApp/` | Ponto de entrada Desktop (JVM) |
| App Web | `webApp/` | Ponto de entrada Web (Wasm) |

Fluxo: `Tela → ViewModel → Repositório remoto (Ktor) → API REST → Controller → Service → Repository → PostgreSQL`

## 6. Instalação, execução e acesso

Os comandos abaixo são para o PowerShell do Windows (no Linux ou macOS, use `./gradlew` no lugar de `.\gradlew.bat`).

### 6.1 Pré-requisitos
- JDK 17
- Docker Desktop (aberto e com o status "Engine running")
- Git
- Para o Android: Android SDK (variável `ANDROID_HOME`) e um celular com Depuração USB, ou emulador

### 6.2 Obter o projeto
```powershell
git clone https://github.com/AraujoThiago0822/cyberflora.git
cd cyberflora
```

### 6.3 Configurar as variáveis de ambiente (obrigatório)
O projeto lê a senha do banco e a chave do JWT de um arquivo `.env` na raiz, que não é versionado. Crie-o a partir do modelo:
```powershell
Copy-Item .env.example .env
```
(Linux ou macOS: `cp .env.example .env`)

Os valores do modelo servem para rodar localmente. Para uso real, troque `POSTGRES_PASSWORD` e informe uma `JWT_SECRET` com 64 ou mais caracteres. Se `JWT_SECRET` ficar vazia, o backend gera uma chave aleatória temporária e mostra um aviso no console (os logins deixam de valer quando o backend reinicia).

### 6.4 Banco de dados (PostgreSQL via Docker)
```powershell
cd docker
docker compose up -d
docker ps --format "{{.Names}} {{.Ports}}"
cd ..
```
Deve aparecer `cyberflora-postgres 127.0.0.1:5432->5432/tcp`. O banco só aceita conexões da própria máquina.

### 6.5 Backend
O backend lê o `.env` da raiz do projeto, então execute a partir da pasta `backend`:
```powershell
cd backend
.\gradlew.bat bootRun
```
Espere a mensagem `Tomcat started on port 8080`.

- API: http://localhost:8080
- Swagger / OpenAPI: http://localhost:8080/swagger-ui/index.html

### 6.6 Frontend
Em outro terminal, na raiz do projeto:
```powershell
cd frontend-kmp
.\gradlew.bat :desktopApp:run                      # Desktop
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun  # Web (abre no navegador)
.\gradlew.bat :androidApp:installDebug             # Android (celular via USB)
```

- **Web:** a primeira compilação demora de 5 a 15 minutos. Depois que abrir, use `Ctrl+Shift+R`.
- **Android:** o celular e o computador precisam estar na mesma rede Wi-Fi. Edite o IP do computador em `shared/src/androidMain/kotlin/com/cyberflora/frontend/network/PlatformConfig.android.kt` (descubra o IP com `ipconfig`).

### 6.7 Acesso
Crie uma conta pela tela **Registro**. A senha precisa ter de 8 a 72 caracteres, com uma letra maiúscula, um número e um caractere especial.

Usuário de teste sugerido (crie-o pela tela de Registro): `teste@cyberflora.com` / `Senha@123`

### 6.8 Teste automático da API
Na raiz do projeto, com o backend rodando:
```powershell
powershell -ExecutionPolicy Bypass -File docs\teste-api.ps1
```
O script cria dois usuários e verifica CRUD, busca, validação e isolamento de dados. Todas as linhas devem sair em `PASS`.

### 6.9 Solução de problemas

| Sintoma | Causa e solução |
|---|---|
| `failed to connect to the docker API` | O Docker Desktop está fechado. Abra-o e espere "Engine running" |
| `ports are not available ... 5432` | Outro programa usa a porta 5432 (por exemplo, um PostgreSQL instalado no Windows). Pare-o, ou reinicie o Docker Desktop e repita o passo 6.4 |
| Backend: `Connection to 127.0.0.1:5432 refused` | O banco não subiu. Refaça o passo 6.4 |
| Backend: `password authentication failed` | O banco foi criado com outra senha. Use no `.env` a senha com que ele foi criado, ou recrie o banco de testes (`docker compose down -v` apaga os dados) |
| `env file ... not found` | Falta o `.env` na raiz. Faça o passo 6.3 |
| Backend: porta 8080 em uso | Feche o outro backend que está rodando |
| App Android não conecta | Confira o IP em `PlatformConfig.android.kt` e se celular e computador estão na mesma rede Wi-Fi |

## 7. Principais rotas da API

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/signup` (ou `/register`) | Pública | Cadastra o usuário e retorna o JWT |
| POST | `/signin` (ou `/login`) | Pública | Autentica e retorna o JWT |
| GET | `/plants`, `/growing-areas` | JWT | Lista os registros do usuário logado |
| GET | `/plants/{id}`, `/growing-areas/{id}` | JWT | Busca por id |
| GET | `/plants/search?name=`, `/growing-areas/search?name=` | JWT | Busca parcial por nome (JPQL com `LIKE`) |
| POST | `/plants`, `/growing-areas` | JWT | Cria um registro |
| PUT | `/plants/{id}`, `/growing-areas/{id}` | JWT | Atualiza um registro |
| DELETE | `/plants/{id}`, `/growing-areas/{id}` | JWT | Remove um registro |

## 8. Segurança e validações

- Senhas armazenadas com **BCrypt**. A senha forte é exigida no aplicativo e também no backend (cadastro).
- **JWT** com expiração de 2 horas, enviado no cabeçalho `Authorization: Bearer <token>`.
- Rotas públicas apenas: `/signin`, `/signup`, `/login`, `/register` e a documentação. Todas as demais exigem token.
- **Isolamento por usuário:** o dono do registro vem do token, nunca do corpo da requisição. Acessar o registro de outro usuário retorna 404.
- **Bean Validation** nos DTOs de Request (campos obrigatórios, tamanhos máximos e valores positivos).
- Consultas JPQL com parâmetros nomeados, sem concatenação de texto.
- **Segredos fora do repositório:** a senha do banco e a chave do JWT ficam no `.env` (ignorado pelo Git). O repositório contém só o `.env.example`, sem valores reais.
- O PostgreSQL só aceita conexões da própria máquina (`127.0.0.1`).

**Variáveis de ambiente** (arquivo `.env`, criado a partir de `.env.example`):

| Variável | Uso |
|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Banco de dados (lidas pelo Docker Compose e pelo backend) |
| `JWT_SECRET` | Chave de assinatura do JWT. Se vazia, o backend gera uma chave aleatória temporária a cada inicialização |

## 9. Limitações conhecidas (escopo acadêmico)

- Comunicação em HTTP, sem TLS. Em produção, seria preciso HTTPS.
- Sem limite de tentativas (rate limiting) nas rotas de login e cadastro.
- O logout é feito no cliente: o token continua válido até expirar (2 horas).
- A documentação Swagger é pública.

## 10. Observações

- O alvo **Web** do Compose Multiplatform ainda é experimental. O funcionamento principal foi validado em Desktop, Android e Web.
- O Gradle configura todos os módulos do frontend. Por isso, o plugin do Android pode exigir o Android SDK instalado (`ANDROID_HOME`, ou `sdk.dir` em `frontend-kmp/local.properties`), mesmo para rodar só o Desktop.