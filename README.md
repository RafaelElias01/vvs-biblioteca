# Biblioteca — Projeto de Verificação e Validação de Software

Sistema de biblioteca (cadastro de livros, empréstimo e devolução com multa) feito com **Quarkus 3.39 / Java 21**.

## Definição do projeto (Etapa 1)
- **Sistema:** Biblioteca — controle de livros, empréstimos e devoluções
- **Linguagem:** Java 21
- **Frameworks:** Quarkus (REST Jackson, Hibernate ORM Panache, Hibernate Validator), banco H2

## Pré-requisitos
JDK 21 e Maven 3.9+. Para os testes de sistema o Playwright baixa o Chromium sozinho na primeira execução (precisa de internet).

## Executando
```
mvn quarkus:dev          # http://localhost:8080  (interface web em /)
```
API: `GET/POST /livros`, `GET/DELETE /livros/{id}`, `GET/POST /emprestimos`, `PUT /emprestimos/{id}/devolucao`.

## Rubrica → onde está cada item
| Item | Onde | Como rodar |
|---|---|---|
| Plano de teste | [plano-de-teste.md](plano-de-teste.md) | — |
| Análise estática | PMD + Checkstyle no `pom.xml`, regras em `config/` | `mvn verify` → `target/pmd.xml`, `target/checkstyle-result.xml` |
| Testes unitários (JUnit 5 + Mockito) | `src/test/java/.../unit` | `mvn test` |
| Cobertura (JaCoCo) | — | `mvn verify` → `target/site/jacoco/index.html` |
| Testes de componente (RestAssured + H2) | `src/test/java/.../component` | `mvn verify` (Failsafe, classes `*IT`) |
| Testes de sistema (Playwright) | `src/test/java/.../system` | `mvn verify` (Failsafe, classes `*IT`); evidências em `target/evidencias/` |
| Carga (extra, k6) | `k6/carga.js` | `mvn quarkus:dev` e em outro terminal `k6 run k6/carga.js` |
| Segurança (extra) | `src/test/java/.../security`, [docs/seguranca.md](docs/seguranca.md) | `mvn verify` (classe `SegurancaApiIT`) |

Executar tudo: `mvn verify`. Só os unitários: `mvn test`.

## Resultado da última execução (`mvn verify`)
- 28 testes unitários e 32 de componente/sistema/segurança, todos passando
- Cobertura de linhas: 98,8% (JaCoCo)
- PMD: 0 violações; Checkstyle: 0 avisos
