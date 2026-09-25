# Plano de Teste — Sistema de Biblioteca

## 1. Introdução
Sistema web de biblioteca (API REST + interface HTML) em **Java 21 + Quarkus 3.39**. Permite cadastrar livros,
emprestar e devolver, com regras de negócio: prazo de 14 dias, limite de 3 empréstimos ativos por usuário,
multa de R$ 2,00 por dia de atraso e controle de estoque de exemplares.

## 2. Escopo

### Dentro do escopo
| Funcionalidade | Regra testada |
|---|---|
| Cadastro de livro | campos obrigatórios, mínimo 1 exemplar, ISBN único, estoque inicial = total |
| Consulta de livros | listar e buscar por id (404 quando não existe) |
| Remoção de livro | só se não houver empréstimo ativo |
| Empréstimo | livro existente, exemplar disponível, usuário informado, limite de 3 ativos, prazo de 14 dias |
| Devolução | repõe estoque, calcula multa, impede devolução dupla |
| Interface web | fluxos de cadastrar, emprestar e devolver e mensagens de erro |

### Fora do escopo
Autenticação/autorização, persistência em banco externo (usa H2 em memória), deploy, acessibilidade.

## 3. Estratégia de teste
| Nível | Ferramentas | Onde | O que valida |
|---|---|---|---|
| Análise estática | PMD 7 + Checkstyle (Maven) | `config/` | qualidade e boas práticas do código |
| Unitário | JUnit 5 + Mockito | `src/test/.../unit` | regras de negócio isoladas (repositórios mockados, relógio fixo) |
| Componente | `@QuarkusTest` + RestAssured + H2 (Maven Failsafe, classes `*IT`) | `src/test/.../component` | API REST + serviço + persistência integrados, com cenários de sucesso e erro |
| Sistema | Playwright (`@UsePlaywright`, Chromium headless) | `src/test/.../system` | fluxos do usuário pela interface, com screenshots de evidência |
| Cobertura | JaCoCo | `target/site/jacoco` | linhas/branches cobertos |
| Carga (extra) | k6 | `k6/carga.js` | comportamento com 20 usuários virtuais |
| Segurança (extra) | JUnit/RestAssured | `src/test/.../security` + `docs/seguranca.md` | injeção, XSS, mass assignment, vazamento de erro |

Técnicas: partição de equivalência, análise de valor limite (ex.: 2 e 3 empréstimos ativos, 0 e 1 dia de atraso),
testes de caminho de erro e de exceção.

## 4. Critérios de entrada
- Código compila (`mvn compile`) e o serviço sobe com `mvn quarkus:dev`.
- Requisitos das regras de negócio definidos (seção 2).
- Ambiente com JDK 21 e Maven instalados.

## 5. Critérios de saída
- 100% dos testes automatizados passando (`mvn verify`).
- Cobertura de linhas ≥ 90% nas classes de domínio, serviço e recursos.
- Zero violações PMD; avisos do Checkstyle corrigidos ou justificados.
- Todos os fluxos principais da interface cobertos por teste de sistema.
- Nenhum defeito crítico aberto.

## 6. Riscos e mitigação
| Risco | Prob. | Impacto | Mitigação |
|---|---|---|---|
| Testes dependentes da data atual quebram | Média | Alto | `Clock` injetado e fixado nos testes |
| Testes de componente interferem entre si (banco compartilhado) | Média | Médio | ISBNs/usuários únicos por teste; `@TestTransaction` nos de repositório |
| Testes de UI instáveis (lentos/assíncronos) | Média | Médio | asserções auto-wait do Playwright, sem `sleep` |
| Download do Chromium falhar na 1ª execução | Baixa | Médio | documentado no README; `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"` |
| Falta de tempo | Média | Alto | priorizar unitário e componente (peso maior), depois sistema |

## 7. Cronograma
| Semana | Atividade |
|---|---|
| 1 | Escolha do projeto, definição de escopo, plano de teste |
| 2 | Configuração de PMD/Checkstyle e correção dos apontamentos |
| 3 | Testes unitários + JaCoCo |
| 4 | Testes de componente (API e repositório) |
| 5 | Testes de sistema (Playwright) |
| 6 | Extras (k6 e segurança), revisão e entrega |

## 8. Ambiente e ferramentas
- Windows 11, JDK 21 (Temurin), Maven 3.9, Quarkus 3.39.5, banco H2 em memória.
- JUnit 5, Mockito, RestAssured, Playwright 1.49, JaCoCo 0.8.13, PMD 7.7, Checkstyle, k6.

## 9. Como executar
```
mvn test      # somente testes unitários (Surefire)
mvn verify    # tudo: unitários, componente e sistema (Failsafe), cobertura e análise estática
```
Relatórios: `target/surefire-reports`, `target/site/jacoco/index.html`, `target/pmd.xml`,
`target/checkstyle-result.xml`, `target/evidencias/*.png`.
