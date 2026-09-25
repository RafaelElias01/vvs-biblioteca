package br.edu.biblioteca.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.edu.biblioteca.exception.NaoEncontradoException;
import br.edu.biblioteca.exception.RegraDeNegocioException;
import br.edu.biblioteca.model.Emprestimo;
import br.edu.biblioteca.model.Livro;
import br.edu.biblioteca.repository.EmprestimoRepository;
import br.edu.biblioteca.service.EmprestimoService;
import br.edu.biblioteca.service.LivroService;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmprestimoServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 5, 1);

    @Mock
    EmprestimoRepository emprestimoRepository;

    @Mock
    LivroService livroService;

    @InjectMocks
    EmprestimoService service;

    private Livro livro;

    @BeforeEach
    void preparar() throws Exception {
        setClock(HOJE);
        livro = new Livro();
        livro.titulo = "DDD";
        livro.exemplaresTotal = 2;
        livro.exemplaresDisponiveis = 2;
    }

    private void setClock(LocalDate data) throws Exception {
        Field f = EmprestimoService.class.getDeclaredField("clock");
        f.setAccessible(true);
        Instant instante = data.atStartOfDay().toInstant(ZoneOffset.UTC);
        f.set(service, Clock.fixed(instante, ZoneOffset.UTC));
    }

    @Test
    void emprestarBaixaEstoqueEDefinePrazoDe14Dias() {
        when(livroService.buscar(1L)).thenReturn(livro);
        when(emprestimoRepository.contarAtivosDoUsuario("ana")).thenReturn(0L);

        Emprestimo e = service.emprestar(1L, "ana");

        assertEquals(1, livro.exemplaresDisponiveis);
        assertEquals(HOJE, e.dataEmprestimo);
        assertEquals(HOJE.plusDays(14), e.dataPrevistaDevolucao);
        assertTrue(e.estaAtivo());
        verify(emprestimoRepository).persist(e);
    }

    @Test
    void emprestarSemExemplarDisponivelLancaExcecao() {
        livro.exemplaresDisponiveis = 0;
        when(livroService.buscar(1L)).thenReturn(livro);

        assertThrows(RegraDeNegocioException.class, () -> service.emprestar(1L, "ana"));
        verify(emprestimoRepository, never()).persist(any(Emprestimo.class));
    }

    @Test
    void emprestarAcimaDoLimiteDeTresLancaExcecao() {
        when(livroService.buscar(1L)).thenReturn(livro);
        when(emprestimoRepository.contarAtivosDoUsuario("ana")).thenReturn(3L);

        assertThrows(RegraDeNegocioException.class, () -> service.emprestar(1L, "ana"));
        assertEquals(2, livro.exemplaresDisponiveis);
    }

    @Test
    void emprestarComTresAtivosAindaNaoPassouDoLimiteComDois() {
        when(livroService.buscar(1L)).thenReturn(livro);
        when(emprestimoRepository.contarAtivosDoUsuario("ana")).thenReturn(2L);

        Emprestimo e = service.emprestar(1L, "ana");

        assertEquals("ana", e.usuario);
    }

    @Test
    void emprestarSemUsuarioLancaExcecao() {
        assertThrows(RegraDeNegocioException.class, () -> service.emprestar(1L, " "));
        assertThrows(RegraDeNegocioException.class, () -> service.emprestar(1L, null));
    }

    @Test
    void emprestarLivroInexistentePropagaNaoEncontrado() {
        when(livroService.buscar(9L)).thenThrow(new NaoEncontradoException("nao existe"));

        assertThrows(NaoEncontradoException.class, () -> service.emprestar(9L, "ana"));
    }

    @Test
    void devolverNoPrazoRepoeEstoqueSemMulta() {
        Emprestimo e = emprestimoAtivo(HOJE.plusDays(3));
        livro.exemplaresDisponiveis = 1;
        when(emprestimoRepository.findById(5L)).thenReturn(e);

        Emprestimo devolvido = service.devolver(5L);

        assertFalse(devolvido.estaAtivo());
        assertEquals(BigDecimal.ZERO, devolvido.multa);
        assertEquals(2, livro.exemplaresDisponiveis);
    }

    @Test
    void devolverComAtrasoCobraMulta() {
        Emprestimo e = emprestimoAtivo(HOJE.minusDays(5));
        when(emprestimoRepository.findById(5L)).thenReturn(e);

        Emprestimo devolvido = service.devolver(5L);

        assertEquals(new BigDecimal("10.00"), devolvido.multa);
    }

    @Test
    void devolverEmprestimoJaDevolvidoLancaExcecao() {
        Emprestimo e = emprestimoAtivo(HOJE);
        e.dataDevolucao = HOJE;
        when(emprestimoRepository.findById(5L)).thenReturn(e);

        assertThrows(RegraDeNegocioException.class, () -> service.devolver(5L));
    }

    @Test
    void devolverEmprestimoInexistenteLancaNaoEncontrado() {
        when(emprestimoRepository.findById(5L)).thenReturn(null);

        assertThrows(NaoEncontradoException.class, () -> service.devolver(5L));
    }

    private Emprestimo emprestimoAtivo(LocalDate prevista) {
        Emprestimo e = new Emprestimo();
        e.livro = livro;
        e.usuario = "ana";
        e.dataEmprestimo = prevista.minusDays(14);
        e.dataPrevistaDevolucao = prevista;
        return e;
    }
}
