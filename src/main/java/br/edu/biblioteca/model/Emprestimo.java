package br.edu.biblioteca.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Emprestimo extends PanacheEntity {

    @ManyToOne
    public Livro livro;

    public String usuario;

    public LocalDate dataEmprestimo;

    public LocalDate dataPrevistaDevolucao;

    public LocalDate dataDevolucao;

    public BigDecimal multa = BigDecimal.ZERO;

    public boolean estaAtivo() {
        return dataDevolucao == null;
    }
}
