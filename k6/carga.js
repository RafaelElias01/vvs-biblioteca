// Teste de carga com k6.  Uso:  k6 run k6/carga.js   (aplicacao rodando em localhost:8080)
// Cenario: 20 usuarios virtuais por 30s, cada iteracao cadastra um livro, lista e faz um emprestimo.
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 20 },
    { duration: '30s', target: 20 },
    { duration: '5s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const JSON_HEADERS = { headers: { 'Content-Type': 'application/json' } };

export default function () {
  const isbn = `K6-${__VU}-${__ITER}-${Date.now()}`;
  const livro = http.post(`${BASE}/livros`, JSON.stringify({
    titulo: 'Livro de carga', autor: 'k6', isbn, exemplaresTotal: 5,
  }), JSON_HEADERS);
  check(livro, { 'livro criado (201)': (r) => r.status === 201 });

  const lista = http.get(`${BASE}/livros`);
  check(lista, { 'lista ok (200)': (r) => r.status === 200 });

  const emprestimo = http.post(`${BASE}/emprestimos`, JSON.stringify({
    livroId: livro.json('id'), usuario: `vu-${__VU}-${__ITER}`,
  }), JSON_HEADERS);
  check(emprestimo, { 'emprestimo criado (201)': (r) => r.status === 201 });

  sleep(1);
}
