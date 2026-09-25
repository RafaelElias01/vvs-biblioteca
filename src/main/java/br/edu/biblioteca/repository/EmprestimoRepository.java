package br.edu.biblioteca.repository;

import br.edu.biblioteca.model.Emprestimo;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmprestimoRepository implements PanacheRepository<Emprestimo> {

    public long contarAtivosDoUsuario(String usuario) {
        return count("usuario = ?1 and dataDevolucao is null", usuario);
    }
}
