# CyberFlora — Smart Growing System

> Sistema de horta inteligente com aplicativo multiplataforma (Android, Desktop e Web) e API REST segura.

## 1. Identificação

| | |
|---|---|
| **Projeto** | CyberFlora — Smart Growing System |
| **Grupo** | Grupo individual |
| **Repositório** | https://github.com/AraujoThiago0822/cyberflora |

## 2. Integrante e responsabilidades

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

### 6.1 Pré-requisitos
- JDK 17
- Docker Desktop
- Git
- Para o Android: Android SDK (variável `ANDROID_HOME`) e um celular com Depuração USB, ou emulador

### 6.2 Obter o projeto
```powershell
git clone https://github.com/AraujoThiago0822/cyberflora.git
cd cyberflora
```

### 6.3 Banco de dados (PostgreSQL via Docker)
```powershell
cd docker
docker compose up -d
```

### 6.4 Backend
```powershell
cd backend
.\gradlew.bat bootRun
```
Espere a mensagem `Tomcat started on port 8080`.

- API: http://localhost:8080
- Swagger / OpenAPI: http://localhost:8080/swagger-ui/index.html

### 6.5 Frontend
Em outro terminal, na pasta `frontend-kmp`:

```powershell
.\gradlew.bat :desktopApp:run                      # Desktop
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun  # Web (abre no navegador)
.\gradlew.bat :androidApp:installDebug             # Android (celular via USB)
```

- **Web:** a primeira compilação demora de 5 a 15 minutos. Depois que abrir, use `Ctrl+Shift+R`.
- **Android:** o celular e o computador precisam estar na mesma rede Wi-Fi. Edite o IP do computador em `shared/src/androidMain/kotlin/com/cyberflora/frontend/network/PlatformConfig.android.kt`.

### 6.6 Acesso
Crie uma conta pela tela **Registro**. A senha precisa ter 8 ou mais caracteres, uma letra maiúscula, um número e um caractere especial.

Usuário de teste sugerido: `teste@cyberflora.com` / `Senha@123`

### 6.7 Teste automático da API
Com o backend rodando:
```powershell
powershell -ExecutionPolicy Bypass -File docs\teste-api.ps1
```
O script cria dois usuários e verifica CRUD, busca, validação e isolamento de dados. Todas as linhas devem sair em `PASS`.

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

- Senhas armazenadas com **BCrypt**.
- **JWT** com expiração de 2 horas, enviado no cabeçalho `Authorization: Bearer <token>`.
- Rotas públicas apenas: `/signin`, `/signup`, `/login`, `/register` e a documentação. Todas as demais exigem token.
- **Isolamento por usuário:** o dono do registro vem do token, nunca do corpo da requisição. Acessar o registro de outro usuário retorna 404.
- **Bean Validation** nos DTOs de Request (campos obrigatórios, tamanhos máximos e valores positivos).
- Consultas JPQL com parâmetros nomeados, sem concatenação de texto.

## 9. Observações

- O alvo **Web** do Compose Multiplatform ainda é experimental. O funcionamento principal foi validado em Desktop, Android e Web.
- O Gradle configura todos os módulos do frontend. Por isso, o plugin do Android pode exigir o Android SDK instalado (`ANDROID_HOME`, ou `sdk.dir` em `frontend-kmp/local.properties`), mesmo para rodar só o Desktop.
