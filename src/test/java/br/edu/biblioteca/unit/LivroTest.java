package br.edu.biblioteca.unit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.biblioteca.model.Emprestimo;
import br.edu.biblioteca.model.Livro;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LivroTest {

    @Test
    void temExemplarDisponivelQuandoHaEstoque() {
        Livro l = new Livro();
        l.exemplaresDisponiveis = 1;
        assertTrue(l.temExemplarDisponivel());
    }

    @Test
    void naoTemExemplarDisponivelQuandoEstoqueZerado() {
        assertFalse(new Livro().temExemplarDisponivel());
    }

    @Test
    void emprestimoSemDataDeDevolucaoEstaAtivo() {
        Emprestimo e = new Emprestimo();
        assertTrue(e.estaAtivo());
        e.dataDevolucao = LocalDate.now();
        assertFalse(e.estaAtivo());
    }
}
