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
powershell -ExecutionPolicy Bypass -File docker\mosquitto\setup-mosquitto.ps1
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
## 11. Broker MQTT (Mosquitto)

Base da integração IoT: o ESP32 publica as leituras dos sensores e o backend as assina.

- **Credenciais:** o script `docker\mosquitto\setup-mosquitto.ps1` lê as senhas `MQTT_*_PASSWORD` do `.env` e gera `docker/mosquitto/passwd` (não versionado). Rode-o uma vez, depois de criar o `.env` e antes de `docker compose up -d` (passo 6.4).
- **Acesso:** porta `1883`, somente em `127.0.0.1`. Conexões anônimas são recusadas.
- **Usuários e permissões (ACL):** `cyberflora_backend` lê todos os tópicos e envia comandos. Cada dispositivo (usuário = `deviceId`, por exemplo `esp32-001`) só publica as próprias leituras e só recebe os próprios comandos. O usuário `ponte` só publica leituras e status.

| Tópico | Sentido | Conteúdo |
|---|---|---|
| `cyberflora/{deviceId}/sensor/{tipo}` | dispositivo para backend | leitura, por exemplo `{"value": 68.0, "unit": "%"}` |
| `cyberflora/{deviceId}/status` | dispositivo para backend | `online` ou `offline` |
| `cyberflora/{deviceId}/command/{alvo}` | backend para dispositivo | comando, por exemplo `ON` |

O `deviceId` e o tipo do sensor são lidos do tópico, que a ACL garante, nunca do conteúdo da mensagem. Tamanho máximo da mensagem: 4096 bytes.

**Teste do broker** (com o Docker rodando):
```powershell
powershell -ExecutionPolicy Bypass -File docs\teste-mqtt.ps1
```
Todas as linhas devem sair em `PASS`.

**Limitação:** o MQTT trafega sem TLS (porta 1883), o que só é aceitável porque a porta fica restrita ao `127.0.0.1`. Em produção, seria preciso TLS (porta 8883).
## 12. Leituras de sensores (IoT, backend)

O backend assina `cyberflora/+/sensor/+` no broker MQTT (sessão persistente) e grava cada leitura na tabela `sensor_readings`.

- O `deviceId` e o tipo do sensor são lidos do **tópico**, que a ACL do broker garante. O conteúdo da mensagem nunca define quem é o dispositivo.
- Tipos aceitos e faixas válidas: `soil_moisture` (0–100 %), `air_humidity` (0–100 %), `temperature` (−40 a 85 °C), `luminosity` (0–200000 lx). Mensagens inválidas, de tipo desconhecido, com campos extras ou fora da faixa são descartadas e registradas no log.
- Payload: `{"value": 68.0, "unit": "%"}` ou apenas o número. A unidade gravada é sempre a do tipo de sensor.
- O listener assina em QoS 1. O ESP32 (biblioteca PubSubClient) publica em QoS 0, o que é suficiente para telemetria periódica.
- Cada dispositivo é vinculado a **uma** área de cultivo (`deviceId` único). O usuário só consulta as leituras do dispositivo das suas áreas.

| Método | Rota | Descrição |
|---|---|---|
| GET | `/growing-areas/{id}/readings?hours=24&type=soil_moisture` | Histórico (até 72 h, no máximo 500 pontos) |
| GET | `/growing-areas/{id}/readings/latest` | Última leitura de cada tipo de sensor |

**Testes:**
- Unidade: `cd backend` e `.\gradlew.bat test --tests "*SensorReadingServiceTest"`
- Integração (com broker e backend rodando): `powershell -ExecutionPolicy Bypass -File docs\teste-iot.ps1`

**Limitações:** o MQTT trafega sem TLS (porta 1883, restrita ao `127.0.0.1`); a tabela de leituras não tem rotina de limpeza; quem descobre o `deviceId` de outra pessoa pode vinculá-lo antes dela, e um produto real exigiria provisionamento do dispositivo.
## 14. Simulação do ESP32 no Wokwi

Projeto online: https://wokwi.com/projects/480486713386253313

Enquanto o hardware físico não está disponível, o ESP32 é simulado no Wokwi. O projeto está em `iot/wokwi/`: `sketch.ino` (firmware), `diagram.json` (circuito), `libraries.txt` e `secrets.h.example`.

**Circuito:** ESP32, potenciômetro no GPIO34 (simula o sensor de umidade do solo; na montagem física é substituído pelo sensor capacitivo, no mesmo pino), DHT22 no GPIO15 (temperatura e umidade do ar), LED verde no GPIO2 (conectado ao broker) e LED azul no GPIO4 (bomba, liga abaixo de 40 % e desliga acima de 60 %). Imagem: `iot/wokwi/esquema-wokwi.png`.

**Caminho dos dados:** Wokwi, broker público de testes, ponte (Docker), Mosquitto local, backend, aplicativo. O simulador gratuito não alcança o `localhost`, por isso ele publica em um broker público, com um prefixo aleatório nos tópicos. A **ponte** (`docker/ponte/ponte.sh`) assina só esse prefixo, valida o `deviceId`, o tipo e o tamanho, e republica as leituras no Mosquitto local com o usuário `ponte`, que só pode publicar leituras. O backend nunca se conecta ao broker público.

**Como usar:**

1. `powershell -ExecutionPolicy Bypass -File docs\configurar-wokwi.ps1` grava `WOKWI_*` no `.env` e gera `iot/wokwi/secrets.h` (não versionado).
2. `cd docker` e `docker compose --profile wokwi up -d` sobem o banco, o Mosquitto e a ponte.
3. Crie o projeto em wokwi.com com os arquivos de `iot/wokwi/` (mais o `secrets.h` gerado) e inicie a simulação.
4. No aplicativo, informe o dispositivo `esp32-001` em uma área e abra o histórico.

**Teste da ponte** (com o backend rodando): `powershell -ExecutionPolicy Bypass -File docs\teste-wokwi.ps1`. Todas as linhas devem sair em `PASS`.

**Limitações:** o broker público não tem autenticação nem TLS, então só se usam dados fictícios. O prefixo reduz colisões, mas não impede quem o descobrir. O caminho é de uma via (o simulador não recebe comandos do backend). O PubSubClient publica em QoS 0.