package br.edu.biblioteca.repository;

import br.edu.biblioteca.model.Livro;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LivroRepository implements PanacheRepository<Livro> {

    public boolean existePorIsbn(String isbn) {
        return count("isbn", isbn) > 0;
    }
}
