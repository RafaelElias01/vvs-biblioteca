package br.edu.biblioteca.service;

import br.edu.biblioteca.exception.NaoEncontradoException;
import br.edu.biblioteca.exception.RegraDeNegocioException;
import br.edu.biblioteca.model.CalculadoraMulta;
import br.edu.biblioteca.model.Emprestimo;
import br.edu.biblioteca.model.Livro;
import br.edu.biblioteca.repository.EmprestimoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class EmprestimoService {

    public static final int LIMITE_EMPRESTIMOS_POR_USUARIO = 3;
    public static final int PRAZO_DIAS = 14;

    @Inject
    EmprestimoRepository emprestimoRepository;

    @Inject
    LivroService livroService;

    @Inject
    Clock clock;

    public List<Emprestimo> listar() {
        return emprestimoRepository.listAll();
    }

    @Transactional
    public Emprestimo emprestar(Long livroId, String usuario) {
        if (usuario == null || usuario.isBlank()) {
            throw new RegraDeNegocioException("usuario e obrigatorio");
        }
        Livro livro = livroService.buscar(livroId);
        if (!livro.temExemplarDisponivel()) {
            throw new RegraDeNegocioException("Nao ha exemplares disponiveis");
        }
        if (emprestimoRepository.contarAtivosDoUsuario(usuario) >= LIMITE_EMPRESTIMOS_POR_USUARIO) {
            throw new RegraDeNegocioException("Usuario atingiu o limite de emprestimos");
        }
        LocalDate hoje = LocalDate.now(clock);
        Emprestimo e = new Emprestimo();
        e.livro = livro;
        e.usuario = usuario;
        e.dataEmprestimo = hoje;
        e.dataPrevistaDevolucao = hoje.plusDays(PRAZO_DIAS);
        livro.exemplaresDisponiveis--;
        emprestimoRepository.persist(e);
        return e;
    }

    @Transactional
    public Emprestimo devolver(Long emprestimoId) {
        Emprestimo e = emprestimoRepository.findById(emprestimoId);
        if (e == null) {
            throw new NaoEncontradoException("Emprestimo " + emprestimoId + " nao encontrado");
        }
        if (!e.estaAtivo()) {
            throw new RegraDeNegocioException("Emprestimo ja devolvido");
        }
        e.dataDevolucao = LocalDate.now(clock);
        e.multa = CalculadoraMulta.calcular(e.dataPrevistaDevolucao, e.dataDevolucao);
        e.livro.exemplaresDisponiveis++;
        return e;
    }
}
