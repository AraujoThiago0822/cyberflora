CyberFlora — Smart Growing System
**Grupo:CyberFlora (grupo individual)

Integrante e responsabilidades
| Thiago Araujo Silva | 1111392321021 | Plant (Plantas) e GrowingArea (Áreas de Cultivo) — backend e frontend completos |

Além das duas entidades acima, o sistema possui a entidade User, usada apenas para autenticação (cadastro/login com JWT).

O que a aplicação faz
O CyberFlora é um sistema de horta inteligente. Permite cadastrar e acompanhar plantas (espécie, data de plantio, faixa ideal de umidade, se está ativa) e as áreas de cultivo onde elas ficam (capacidade em m², tipo de irrigação). O usuário se cadastra, faz login e gerencia tudo por um aplicativo único que roda em Android, Desktop e Web.

Problema que resolve: produtores e entusiastas de hortas costumam anotar informações de cultivo em papel ou planilhas soltas. O CyberFlora centraliza esses dados com autenticação, validação e busca, e serve de base para a integração com sensores IoT (umidade do solo) e irrigação automática, prevista nas próximas entregas.

Tecnologias utilizadas
Backend: Kotlin, Spring Boot, Spring Web, Spring Data JPA (JPQL), Bean Validation, Spring Security, JWT (jjwt), PostgreSQL 16, Springdoc OpenAPI (Swagger), Gradle (Wrapper), Docker / Docker Compose.

Frontend: Kotlin Multiplatform, Compose Multiplatform (Material 3), Navigation3, Ktor Client, kotlinx.serialization, ViewModel + Coroutines, Material Icons.

Camadas da arquitetura e pastas
Backend (backend/src/main/kotlin/com/cyberflora/backend/)
Camada	Pasta	Responsabilidade
Entidades	entity/	Classes JPA (Plant, GrowingArea, User)
Repositórios	repository/	Acesso ao banco (Spring Data JPA + consultas JPQL com LIKE)
Serviços	service/	Regras de negócio / CRUD
Controladores	controller/	Rotas REST (/plants, /growing-areas, /signin, /signup)
DTOs	dto/	Objetos de Request (com Bean Validation) e Response
Mappers	mapper/	Conversão DTO ⇄ Entidade
Segurança	security/	JWT (JwtUtil), filtro de autenticação, SecurityConfig
Configuração	config/	Documentação OpenAPI
Frontend (frontend-kmp/)
Camada	Pasta	Responsabilidade
Código compartilhado	shared/src/commonMain/kotlin/com/cyberflora/frontend/	Todo o app (UI, estado, rede)
Modelos	.../model/	Entidades/DTOs do frontend
Rede (Repositórios remotos)	.../network/	Cliente HTTP (Ktor), *Repository, sessão JWT
ViewModels	.../viewmodel/	Estado dos campos, lista e ações do CRUD
Telas	.../screens/	Listagem e Formulário (Gravar / Limpar Campos), Login, Registro
Navegação	.../navigation/	Navigation3 (2 níveis)
Tema	.../theme/	Cores e tipografia (identidade CyberFlora)
App Android	androidApp/	Ponto de entrada Android
App Desktop	desktopApp/	Ponto de entrada Desktop (JVM)
App Web	webApp/	Ponto de entrada Web (Wasm)
Instalação, execução e acesso
Pré-requisitos: JDK 17, Docker Desktop, Git. Para Android: Android SDK + celular com Depuração USB (ou emulador).

1. Banco de dados (PostgreSQL via Docker)
cd docker
docker compose up -d
2. Backend
cd backend
.\gradlew.bat bootRun
API: http://localhost:8080
Swagger/OpenAPI: http://localhost:8080/swagger-ui/index.html
Rotas públicas: /signin, /signup (e os aliases /login, /register). Todas as demais exigem Authorization: Bearer <JWT>.
3. Frontend
cd frontend-kmp
.\gradlew.bat :desktopApp:run                    # Desktop
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun   # Web (abre no navegador)
.\gradlew.bat :androidApp:installDebug           # Android (celular via USB)
Android: edite o IP do seu PC em shared/src/androidMain/kotlin/com/cyberflora/frontend/network/PlatformConfig.android.kt (celular e PC na mesma rede Wi-Fi).

4. Acesso
Crie uma conta pela tela Registro (senha forte: 8+ caracteres, 1 maiúscula, 1 número, 1 caractere especial) ou use o usuário de teste: <email-de-teste> / <senha-de-teste>.
