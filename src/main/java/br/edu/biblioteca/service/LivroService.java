package br.edu.biblioteca.service;

import br.edu.biblioteca.exception.NaoEncontradoException;
import br.edu.biblioteca.exception.RegraDeNegocioException;
import br.edu.biblioteca.model.Livro;
import br.edu.biblioteca.repository.LivroRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class LivroService {

    @Inject
    LivroRepository repository;

    public List<Livro> listar() {
        return repository.listAll();
    }

    public Livro buscar(Long id) {
        Livro livro = repository.findById(id);
        if (livro == null) {
            throw new NaoEncontradoException("Livro " + id + " nao encontrado");
        }
        return livro;
    }

    @Transactional
    public Livro cadastrar(Livro livro) {
        if (repository.existePorIsbn(livro.isbn)) {
            throw new RegraDeNegocioException("ISBN ja cadastrado: " + livro.isbn);
        }
        livro.exemplaresDisponiveis = livro.exemplaresTotal;
        repository.persist(livro);
        return livro;
    }

    @Transactional
    public void remover(Long id) {
        Livro livro = buscar(id);
        if (livro.exemplaresDisponiveis < livro.exemplaresTotal) {
            throw new RegraDeNegocioException("Livro possui emprestimos ativos");
        }
        repository.delete(livro);
    }
}
