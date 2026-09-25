package br.edu.biblioteca.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Livro extends PanacheEntity {

    @NotBlank(message = "titulo e obrigatorio")
    public String titulo;

    @NotBlank(message = "autor e obrigatorio")
    public String autor;

    @NotBlank(message = "isbn e obrigatorio")
    @Column(unique = true)
    public String isbn;

    @Min(value = 1, message = "deve haver ao menos 1 exemplar")
    public int exemplaresTotal;

    public int exemplaresDisponiveis;

    public boolean temExemplarDisponivel() {
        return exemplaresDisponiveis > 0;
    }
}
