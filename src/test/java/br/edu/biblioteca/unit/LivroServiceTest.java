package br.edu.biblioteca.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.biblioteca.exception.NaoEncontradoException;
import br.edu.biblioteca.exception.RegraDeNegocioException;
import br.edu.biblioteca.model.Livro;
import br.edu.biblioteca.repository.LivroRepository;
import br.edu.biblioteca.service.LivroService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {

    @Mock
    LivroRepository repository;

    @InjectMocks
    LivroService service;

    private Livro novoLivro(String isbn, int total) {
        Livro l = new Livro();
        l.titulo = "Clean Code";
        l.autor = "Robert Martin";
        l.isbn = isbn;
        l.exemplaresTotal = total;
        return l;
    }

    @Test
    void cadastrarDefineDisponiveisIgualAoTotal() {
        Livro livro = novoLivro("111", 4);
        when(repository.existePorIsbn("111")).thenReturn(false);

        Livro salvo = service.cadastrar(livro);

        assertEquals(4, salvo.exemplaresDisponiveis);
        verify(repository).persist(livro);
    }

    @Test
    void cadastrarComIsbnDuplicadoLancaExcecao() {
        when(repository.existePorIsbn("111")).thenReturn(true);

        assertThrows(RegraDeNegocioException.class, () -> service.cadastrar(novoLivro("111", 1)));
        verify(repository, never()).persist(org.mockito.ArgumentMatchers.any(Livro.class));
    }

    @Test
    void buscarExistenteRetornaLivro() {
        Livro livro = novoLivro("222", 1);
        when(repository.findById(7L)).thenReturn(livro);

        assertSame(livro, service.buscar(7L));
    }

    @Test
    void buscarInexistenteLancaNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(null);

        assertThrows(NaoEncontradoException.class, () -> service.buscar(99L));
    }

    @Test
    void listarRetornaTodosOsLivros() {
        List<Livro> livros = List.of(novoLivro("1", 1), novoLivro("2", 1));
        when(repository.listAll()).thenReturn(livros);

        assertEquals(2, service.listar().size());
    }

    @Test
    void removerLivroSemEmprestimosApagaDoRepositorio() {
        Livro livro = novoLivro("333", 2);
        livro.exemplaresDisponiveis = 2;
        when(repository.findById(1L)).thenReturn(livro);

        service.remover(1L);

        verify(repository).delete(livro);
    }

    @Test
    void removerLivroComEmprestimoAtivoLancaExcecao() {
        Livro livro = novoLivro("444", 2);
        livro.exemplaresDisponiveis = 1;
        when(repository.findById(1L)).thenReturn(livro);

        assertThrows(RegraDeNegocioException.class, () -> service.remover(1L));
        verify(repository, never()).delete(livro);
    }
}
