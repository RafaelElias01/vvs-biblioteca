package br.edu.biblioteca.exception;

public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
