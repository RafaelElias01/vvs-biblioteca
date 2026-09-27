# Teste de Carga (k6)

Script: `k6/carga.js`. Aplicação rodando com `java -jar target/quarkus-app/quarkus-run.jar`.

## Cenário
- Até 20 usuários virtuais (subida de 10s, 30s estável, descida de 5s)
- Cada iteração: cadastra um livro, lista os livros e faz um empréstimo
- Metas (thresholds): menos de 1% de falhas e p(95) do tempo de resposta abaixo de 500 ms

## Resultado
| Métrica | Valor |
|---|---|
| Requisições | 2208 |
| Falhas | 0,00% |
| Tempo médio | 10,89 ms |
| p(95) | 27,27 ms |
| Máximo | 550 ms |
| Iterações | 736 |
| Checks aprovados | 100% |

As duas metas foram atingidas. Resumo completo em `docs/carga-resultado.json`.
