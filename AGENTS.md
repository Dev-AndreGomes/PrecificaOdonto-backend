# AGENTS.md — PrecificaOdonto Backend

> Contexto permanente para agentes de IA (e humanos) que trabalham neste repositório.
> **Leia este arquivo inteiro antes de qualquer alteração.** Se algo aqui ficar desatualizado, atualize-o no mesmo commit da mudança.

---

## 1. Visão do Produto

**PrecificaOdonto** é um sistema web, *mobile-first*, de **precificação odontológica** para **dentistas autônomos** (recém-formados ou com consultório próprio onde atendem sozinhos). Substitui a planilha de Excel que a categoria costuma montar por conta própria.

**Problema:** o profissional recém-formado cobra "no achismo" ou copia a concorrência, sem saber o custo real da sua hora clínica, dos insumos e das taxas. Resultado: trabalha muito e sobra pouco ("prejuízo invisível").

**Proposta de valor:** a partir da meta de renda líquida, das horas trabalhadas e dos custos do local de atendimento, o sistema calcula a **Hora Clínica** e o **preço mínimo e sugerido** de cada procedimento, mostrando de forma visual para onde vai cada real cobrado.

### Usuária zero
A primeira usuária é a noiva do autor, que está se formando em Odontologia. Ela terá conta `LIFETIME` (sem cobrança, sem expiração). O produto deve parecer exclusivo dela, sem telas de cobrança ou avisos de plano.

### Visão de negócio (futuro)
Potencial de venda como SaaS para outros dentistas autônomos. Por isso o código nasce **multi-tenant por usuário** (todo dado pertence a um `usuario_id`) e com **plano de assinatura** modelado desde cedo. Cobrança é feita **"por fora"** (checkout externo + webhook), nunca processando cartão dentro do app.

---

## 2. Escopo

### Dentro do escopo (MVP)
- Dentista autônomo / consultório individual (1 profissional, 1 cadeira).
- Assistente de início (*wizard* de 3 passos) que já devolve a Hora Clínica.
- Custos fixos, insumos e fichas técnicas de procedimentos.
- Preço mínimo (*breakeven*), preço sugerido (Markup Divisor) e decomposição do preço.
- Kit inicial pré-carregado (10 procedimentos comuns + insumos com preço médio).
- Exportação de planilha **.xlsx** com fórmulas vivas.
- Autenticação com JWT e controle de acesso por plano.

### Fora do escopo (por enquanto) — mas "preparar o terreno"
Modo **Clínica Consolidada**: múltiplas cadeiras, folha de funcionários (ASB, secretária), depreciação patrimonial, comissionamento de equipe parceira. **Não implementar agora.** Pode-se deixar o modelo extensível (ex.: não assumir que existe só um profissional em lugares onde isso custar pouco), mas **sem criar telas, endpoints ou tabelas para isso** sem pedido explícito.

Também fora do MVP: convênios/TUSS como auditoria de rentabilidade, orçamentos em PDF, integração de pagamento.

---

## 3. Stack Tecnológica

| Camada | Tecnologia | Observação |
| :--- | :--- | :--- |
| Linguagem | **Java 21 (LTS)** | Obrigatório. O `pom.xml` usa `release 21`. |
| Framework | **Spring Boot 4.1.2** | Versão definida no `pom.xml` (parent). Não rebaixar. |
| Build | **Maven** (via `mvnw`) | Sempre use o wrapper. |
| Banco | **PostgreSQL 16** | Via `docker-compose.yml` em desenvolvimento. |
| Persistência | Spring Data JPA / Hibernate | Valores monetários em `NUMERIC` / `BigDecimal`. |
| Migrações | Flyway (dependência presente) | **Decisão pendente** — ver seção 12. |
| Segurança | Spring Security + JWT (`com.auth0:java-jwt`) | Stateless. |
| Validação | Bean Validation | `spring-boot-starter-validation`. |
| Excel | Apache POI (`poi-ooxml`) | Geração de `.xlsx` com fórmulas. |
| Boilerplate | Lombok | Annotation processor já configurado no `pom.xml`. |
| CI | GitHub Actions | `.github/workflows/ci.yml`. |
| Frontend | React + Vite + TypeScript + Tailwind | **Repositório separado**, não vive aqui. |

### Repositórios
O projeto usa **repositórios separados**: este (`precificaodonto-backend`, aberto no IntelliJ) e outro para o frontend React. **Não adicionar código de frontend neste repositório.**

---

## 4. Ambiente de Desenvolvimento (Windows)

- **JDK 21 é obrigatório.** A máquina do autor tem JDK 17 como padrão no terminal (`JAVA_HOME` aponta para `temurin-17`), mas o JDK 21 está em `C:\Users\andre\.jdks\temurin-21.0.11`.
  - Ao rodar Maven pelo terminal (PowerShell), defina antes:
    ```powershell
    $env:JAVA_HOME = "C:\Users\andre\.jdks\temurin-21.0.11"
    ```
  - Sem isso o build falha com `release version 21 not supported`.
  - No IntelliJ: *File → Project Structure → Project → SDK = 21*.
- Shell: **PowerShell**. Use `.\mvnw.cmd` (não `./mvnw`).
- **Docker Desktop** precisa estar aberto para subir o banco.

### Comandos úteis
```powershell
# Banco local
docker compose up -d
docker compose ps
docker compose down            # mantém os dados (volume)

# Build e testes
.\mvnw.cmd clean test
.\mvnw.cmd test-compile

# Rodar a aplicação
.\mvnw.cmd spring-boot:run
```

---

## 5. Arquitetura

Princípio central: **o motor financeiro é código Java puro e independe de banco, web e Spring.** Ele deve poder ser testado com JUnit sem subir contexto nenhum.

### Pacote base
`com.precificaodonto`

### Organização proposta
```
com.precificaodonto
├── PrecificaodontoBackendApplication.java
├── calculo/          # Motor financeiro: Java puro, BigDecimal, sem Spring/JPA
│   ├── CalculadoraHoraClinica
│   ├── CalculadoraPrecificacao
│   └── (records de entrada/saída)
├── domain/           # Entidades JPA e enums (Usuario, PlanoAssinatura, ...)
├── repository/       # Spring Data JPA
├── service/          # Orquestração: chama repository + calculo
├── controller/       # REST (/api/v1/...)
├── dto/              # Request/Response (records)
├── security/         # JWT, filtros, SecurityConfig
├── excel/            # Exportação .xlsx (Apache POI)
└── config/           # Configurações gerais, CORS
```

### Regras de dependência
- `calculo` **não importa** nada de `domain`, `repository`, `service`, `controller`, nem de Spring/JPA.
- `controller` fala só com `service` e `dto`. Nunca expõe entidade JPA diretamente.
- `service` é quem traduz entidade → entrada do `calculo` e saída do `calculo` → DTO.
- Regra de negócio **não** vive em controller nem em entidade.

---

## 6. Regras de Negócio e Fórmulas

> Estas fórmulas são a **fonte da verdade** do domínio. Qualquer mudança exige atualizar aqui, os testes e, se aplicável, o exportador de Excel.

### 6.1 Hora Clínica (dentista autônomo)

```
horasProdutivasMes = diasTrabalhoSemana × semanasPorMes × horasPorDia × (1 − ociosidade)

custoEstruturalMensal = Σ custosFixosAtivos + metaSalarialLiquida

horaClinica = custoSublocacaoPorHora + custoEstruturalMensal / horasProdutivasMes
```

- `semanasPorMes` padrão: **4,33** (52/12). Deve ser constante nomeada, não número solto.
- `ociosidade` é uma fração (0,20 = 20%) dentro do `calculo`; a entrada do usuário em % é convertida na borda.
- Se o local é sublocação por turno: `custoSublocacaoPorHora = custoTurno / horasDoTurno`.
- **A meta salarial é sempre parte do custo.** O sistema não pode calcular uma hora clínica que ignore a remuneração da dentista.
- `horasProdutivasMes <= 0` é erro de validação, nunca divisão por zero.

### 6.2 Custo direto do procedimento

```
custoDireto = (tempoMinutos / 60) × horaClinica
            + Σ (quantidadeDoses × custoPorDose)   // insumos + kit de biossegurança
            + custoProtetico
```

`custoPorDose = precoCompra / rendimentoDoses`.

### 6.3 Preço de venda — Markup Divisor ("margem por dentro")

```
deducoes = (impostos% + taxaCartao% + margemLucro%) / 100
precoSugerido = custoDireto / (1 − deducoes)
precoMinimo   = custoDireto / (1 − (impostos% + taxaCartao%) / 100)   // lucro zero, mas sem prejuízo
```

- Usar **Markup Divisor**, nunca multiplicador sobre o custo.
- Se `deducoes >= 1`, lançar erro de validação (cálculo inviável).
- `precoMinimo` é o *breakeven*: abaixo dele a dentista paga para trabalhar.

### 6.4 Decomposição do preço (gráfico donut)
Para um preço `P`, devolver as fatias: **tempo (hora clínica)**, **insumos/biossegurança**, **protético**, **impostos + taxa de cartão** e **lucro livre**. A soma das fatias deve ser exatamente igual a `P` (garantir fechamento com ajuste de centavos na maior fatia ou no lucro).

### 6.5 Semáforo de margem
Dado o `precoPraticado` informado pela dentista:
- 🔴 **Vermelho:** `precoPraticado < precoMinimo` (prejuízo).
- 🟡 **Amarelo:** entre `precoMinimo` e `precoSugerido` (cobre custos, abaixo da meta de margem).
- 🟢 **Verde:** `precoPraticado >= precoSugerido`.

### 6.6 Planos de assinatura
`PlanoAssinatura`: `LIFETIME`, `TRIAL`, `MENSAL`, `EXPIRADO`.
- `LIFETIME` → acesso **sempre** liberado, sem checagem de data.
- Demais → acesso liberado somente se `validadeAcesso` for posterior a hoje.
- Cobrança externa (Kiwify/Asaas/Stripe etc.) atualiza `validadeAcesso` via **webhook**. **Fase futura.** Não implementar checkout dentro do app.

---

## 7. Dinheiro e Precisão Numérica (regra crítica)

- **Nunca** use `double` ou `float` para dinheiro, percentual de cálculo, horas ou tempo fracionado.
- Use sempre **`BigDecimal`**. Construa com `new BigDecimal("0.20")` ou `BigDecimal.valueOf(...)`; **nunca** `new BigDecimal(double)`.
- Divisões sempre com escala e `RoundingMode` explícitos.
  - Cálculos intermediários: escala **4** ou mais.
  - Valor monetário final exibido/persistido: escala **2**, `RoundingMode.HALF_UP` (arredondamento comercial).
- Banco: `NUMERIC(12,2)` para valores finais; `NUMERIC(12,4)` para custos unitários fracionados (ex.: custo por dose).
- Comparar com `compareTo`, **nunca** `equals` (escala diferente quebra `equals`).

---

## 8. Convenções de Código

- **Idioma:** domínio e nomes de negócio em **português** (`horaClinica`, `custoDireto`, `Procedimento`), pois o vocabulário é odontológico/financeiro brasileiro. Termos técnicos de framework seguem em inglês (`Controller`, `Service`, `Repository`).
- **Sem acentos** em identificadores, nomes de arquivo e nomes de tabela/coluna.
- DTOs e entradas/saídas do motor de cálculo: preferir **`record`** (Java 21).
- Injeção de dependência **por construtor** (`@RequiredArgsConstructor` ok). Sem `@Autowired` em campo.
- Lombok: permitido em entidades e serviços. Evitar `@Data` em entidades JPA; preferir `@Getter`/`@Setter` pontuais.
- Validar entrada com Bean Validation nos DTOs (`@NotNull`, `@Positive`, `@DecimalMin`...).
- Erros de negócio: exceções próprias tratadas por um `@RestControllerAdvice` central, retornando JSON padronizado (sem stack trace).
- Entidades JPA **não** saem pela API.
- **Multi-tenant:** toda consulta a dados do usuário filtra por `usuario_id` do usuário autenticado. Nunca confiar em `usuario_id` vindo do corpo da requisição.
- Rotas versionadas: `/api/v1/...`.
- Comentários explicam o *porquê*, não o *o quê*. Preserve comentários existentes que não tenham relação com a sua mudança.

---

## 9. Testes

- **JUnit 5**. O motor `calculo` precisa de testes unitários **sem Spring** (rápidos, sem banco).
- Todo cálculo financeiro novo ou alterado deve ter teste com **valor esperado calculado à mão** e comentado no teste.
- Casos obrigatórios: divisão por zero (horas produtivas = 0), `deducoes >= 1`, arredondamento, decomposição do preço fechando exatamente em `P`.
- Usuário `LIFETIME` nunca é bloqueado por data.
- Testes de integração (`@SpringBootTest`, banco) só quando houver necessidade real; isolá-los dos testes unitários.
- Rode `.\mvnw.cmd clean test` antes de propor commit.

---

## 10. Git e Fluxo de Trabalho

- Desenvolvimento em **baby steps**: uma mudança pequena e coerente por commit.
- **Conventional Commits**, em português, no imperativo/descritivo curto:
  - `feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:`, `ci:`
  - Ex.: `feat(calculo): adiciona calculo da hora clinica com BigDecimal`
- Branch principal: `main`. O CI roda em `push` e `pull_request` para `main`.
- **Nunca commitar:** `.env`, senhas, chaves, `target/`, `.idea/`. O `.gitignore` já cobre isso — não enfraquecê-lo.
- O autor é quem executa `git commit` / `git push`. O agente pode **sugerir** a mensagem de commit.

---

## 11. Segurança

- Senhas com **BCrypt**. Nunca em texto puro, nunca em log.
- JWT *stateless*; o *secret* vem de variável de ambiente (`JWT_SECRET`), **sem valor real versionado**.
- Credenciais de produção **jamais** vão para o repositório. `application.properties` versionado contém apenas valores de desenvolvimento local.
- Atenção: com `spring-boot-starter-security` no classpath, **todos os endpoints ficam bloqueados por padrão (401)** até existir uma `SecurityConfig`. Isso é esperado, não é bug.
- LGPD: dados financeiros e de pacientes são sensíveis. Não logar dados pessoais.

---

## 12. Estado Atual do Projeto e Decisões em Aberto

### Concluído
- [x] Projeto Spring Boot 4.1.2 / Java 21 / Maven gerado no Spring Initializr.
- [x] Dependências: Web, JPA, PostgreSQL, Flyway, Security, Validation, Lombok, DevTools, `java-jwt`, Apache POI.
- [x] `docker-compose.yml` com PostgreSQL 16.
- [x] `.gitignore` revisado.
- [x] Workflow de CI (`.github/workflows/ci.yml`).
- [x] Build `test-compile` passando com JDK 21.

### Roadmap (baby steps, em ordem)
1. [ ] Subir o banco (`docker compose up -d`) e validar conexão.
2. [ ] **Motor financeiro em Java puro** (`calculo`): Hora Clínica e Precificação + testes JUnit.
3. [ ] Modelagem de dados e persistência (usuário, config, custos fixos, insumos, procedimentos, ficha técnica).
4. [ ] Autenticação JWT + conta `LIFETIME` da usuária zero.
5. [ ] Endpoints REST (onboarding, hora clínica, procedimentos).
6. [ ] Kit inicial pré-carregado.
7. [ ] Exportação `.xlsx` (Apache POI) com fórmulas vivas.
8. [ ] Integração com o frontend React (repo separado).
9. [ ] (Futuro) Webhook de pagamento, login social com Google OAuth2, hospedagem em nuvem, modo Clínica Consolidada.

### Decisões em aberto (perguntar ao autor antes de assumir)
- **Estratégia de schema:** `application.properties` está hoje com `spring.jpa.hibernate.ddl-auto=update` (conveniência de início). Há uma dependência de **Flyway** no `pom.xml`, mas **nenhuma migração existe** (as `V1`/`V2` foram removidas de propósito, a pedido do autor). Definir: manter `update` no início e migrar para Flyway depois, ou adotar Flyway já. Antes de produção, **`ddl-auto=update` não deve ser usado**.
- **Ambientes:** dev local (Docker) e prod em nuvem (Neon/Supabase/Railway/Render) via variáveis de ambiente e Spring Profiles. Ainda não configurado. GitLab foi descartado por ora (excessivo para equipe de 1).
- **Frontend:** estrutura e repositório do React ainda não criados.

### Armadilhas conhecidas
- **CI e PostgreSQL:** o teste padrão `contextLoads` sobe o contexto completo e exige PostgreSQL na porta 5432. O workflow `.github/workflows/ci.yml` já conta com o container de serviço `postgres:16-alpine` com healthcheck para que o CI na nuvem passe com sucesso.
- JDK 17 padrão no terminal vs. JDK 21 exigido (ver seção 4).
- Flyway sem pasta `db/migration` não derruba a aplicação por padrão, mas o aviso aparece nos logs.

---

## 13. Como o Agente de IA Deve Trabalhar Neste Projeto

1. **Faça apenas o que foi pedido.** O autor trabalha em *baby steps*. Não crie, remova ou reescreva arquivos fora do escopo da tarefa atual. Se algo adjacente parecer necessário, **sugira e pergunte**.
2. **Antes de mudar, leia.** Leia os arquivos relevantes e este `AGENTS.md`. Não assuma o estado do projeto.
3. **Seja honesto.** Se discordar de uma decisão do autor, diga e explique — ele pediu isso explicitamente. Se algo falhar ou não puder ser verificado (ex.: Docker fechado), diga claramente em vez de afirmar sucesso.
4. **Verifique antes de declarar pronto.** Compile e rode os testes (com JDK 21) e reporte o resultado real.
5. **Explique o porquê.** O autor está aprendendo e construindo um produto; prefira respostas curtas e objetivas, mas justifique decisões não óbvias.
6. **Não faça git por conta própria.** Sugira a mensagem de commit; quem comita é o autor.
7. **Não introduza dependências novas sem avisar** e justificar.
8. **Respeite o escopo:** foco no dentista autônomo. Modo clínica consolidada é melhoria futura.
9. **Mantenha este arquivo vivo:** ao concluir um passo do roadmap ou tomar uma decisão, atualize a seção 12.
10. **Idioma de conversa:** português do Brasil.
