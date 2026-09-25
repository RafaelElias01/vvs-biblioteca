# Avaliação de Segurança (Extra)

Testes automatizados em `src/test/java/br/edu/biblioteca/security/` (`mvn verify`, classe `SegurancaApiIT`).

| # | Vulnerabilidade | Teste | Resultado | Evidência / Explicação |
|---|---|---|---|---|
| 1 | SQL Injection no campo `usuario` | `sqlInjectionNoUsuario...` | **Não vulnerável** | Consultas Panache usam parâmetros (`?1`); o texto malicioso é gravado literalmente |
| 2 | SQL Injection no id da URL | `sqlInjectionNoIdDaUrl...` | **Não vulnerável** | id é convertido para `Long`; valor inválido não chega ao banco (404) |
| 3 | SQL Injection no ISBN (DROP TABLE) | `sqlInjectionNoIsbn...` | **Não vulnerável** | Registro salvo como texto e a tabela continua existindo |
| 4 | Mass assignment | `massAssignmentDeEstoque...` | **Não vulnerável** | `exemplaresDisponiveis` enviado pelo cliente é sobrescrito pelo serviço |
| 5 | XSS armazenado | análise do código da tela (`esc()`) | **Não vulnerável** | A interface escapa o texto com `esc()` antes de exibir |
| 6 | Vazamento de stack trace | `jsonMalformado...`, `erroDeNegocio...` | **Não vulnerável** | Respostas de erro não contêm classes, SQL ou stack trace |
| 7 | Autenticação/autorização | análise manual | **VULNERÁVEL (achado)** | Nenhum endpoint exige login: qualquer um cadastra, remove livros e devolve empréstimos |
| 8 | Identificação do usuário do empréstimo | análise manual | **Achado** | `usuario` é texto livre; é possível emprestar em nome de outra pessoa e burlar o limite de 3 trocando o nome |
| 9 | Limite de tamanho de campos | análise manual | **Achado (baixo)** | Não há `@Size` em título/autor/ISBN; entradas enormes são aceitas |

## Recomendações
1. Adicionar autenticação (Quarkus Security + JWT/OIDC) e papéis (bibliotecário × leitor).
2. Ligar o empréstimo a um usuário autenticado em vez de um texto livre.
3. Adicionar `@Size(max=...)` nos campos de texto.
4. Publicar a aplicação apenas atrás de HTTPS.

Os achados 7–9 não foram corrigidos por estarem fora do escopo da disciplina (o sistema é didático); ficam
documentados como risco aceito.
