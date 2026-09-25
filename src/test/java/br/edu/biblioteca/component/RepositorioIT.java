package br.edu.biblioteca.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.edu.biblioteca.model.Emprestimo;
import br.edu.biblioteca.model.Livro;
import br.edu.biblioteca.repository.EmprestimoRepository;
import br.edu.biblioteca.repository.LivroRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Teste de componente da camada de persistencia (rollback automatico com @TestTransaction). */
@QuarkusTest
class RepositorioIT {

    @Inject
    LivroRepository livros;

    @Inject
    EmprestimoRepository emprestimos;

    private Livro persistirLivro(String isbn) {
        Livro l = new Livro();
        l.titulo = "T";
        l.autor = "A";
        l.isbn = isbn;
        l.exemplaresTotal = 1;
        l.exemplaresDisponiveis = 1;
        livros.persist(l);
        return l;
    }

    private void persistirEmprestimo(Livro livro, String usuario, LocalDate devolucao) {
        Emprestimo e = new Emprestimo();
        e.livro = livro;
        e.usuario = usuario;
        e.dataEmprestimo = LocalDate.now();
        e.dataPrevistaDevolucao = LocalDate.now().plusDays(14);
        e.dataDevolucao = devolucao;
        emprestimos.persist(e);
    }

    @Test
    @TestTransaction
    void existePorIsbnEncontraLivroPersistido() {
        persistirLivro("REPO-1");

        assertTrue(livros.existePorIsbn("REPO-1"));
        assertFalse(livros.existePorIsbn("REPO-INEXISTENTE"));
    }

    @Test
    @TestTransaction
    void contarAtivosIgnoraEmprestimosDevolvidosEDeOutrosUsuarios() {
        Livro livro = persistirLivro("REPO-2");
        persistirEmprestimo(livro, "joao", null);
        persistirEmprestimo(livro, "joao", null);
        persistirEmprestimo(livro, "joao", LocalDate.now());
        persistirEmprestimo(livro, "maria", null);

        assertEquals(2, emprestimos.contarAtivosDoUsuario("joao"));
        assertEquals(1, emprestimos.contarAtivosDoUsuario("maria"));
        assertEquals(0, emprestimos.contarAtivosDoUsuario("ninguem"));
    }
}
