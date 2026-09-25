package br.edu.biblioteca.component;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** Teste de componente: API REST + servico + repositorio + H2, sem mocks. */
@QuarkusTest
class LivroApiIT {

    private static final AtomicInteger SEQ = new AtomicInteger();

    static String isbnUnico() {
        return "LIVRO-" + SEQ.incrementAndGet() + "-" + System.nanoTime();
    }

    static Map<String, Object> livro(String isbn, int exemplares) {
        return Map.of("titulo", "Java Efetivo", "autor", "Joshua Bloch", "isbn", isbn, "exemplaresTotal", exemplares);
    }

    static int criarLivro(int exemplares) {
        return given().contentType(ContentType.JSON).body(livro(isbnUnico(), exemplares))
            .when().post("/livros")
            .then().statusCode(201).extract().path("id");
    }

    @Test
    void cadastraLivroEDevolveComEstoqueCheio() {
        given().contentType(ContentType.JSON).body(livro(isbnUnico(), 3))
            .when().post("/livros")
            .then().statusCode(201)
            .body("id", greaterThan(0))
            .body("exemplaresDisponiveis", equalTo(3))
            .body("titulo", equalTo("Java Efetivo"));
    }

    @Test
    void listaContemLivroCadastrado() {
        String isbn = isbnUnico();
        given().contentType(ContentType.JSON).body(livro(isbn, 1)).post("/livros").then().statusCode(201);

        given().when().get("/livros")
            .then().statusCode(200)
            .body("isbn", hasItem(isbn));
    }

    @Test
    void buscaLivroPorId() {
        int id = criarLivro(2);

        given().when().get("/livros/" + id)
            .then().statusCode(200)
            .body("id", equalTo(id));
    }

    @Test
    void buscaLivroInexistenteRetorna404() {
        given().when().get("/livros/999999")
            .then().statusCode(404)
            .body("erro", containsString("nao encontrado"));
    }

    @Test
    void isbnDuplicadoRetorna409() {
        String isbn = isbnUnico();
        given().contentType(ContentType.JSON).body(livro(isbn, 1)).post("/livros").then().statusCode(201);

        given().contentType(ContentType.JSON).body(livro(isbn, 1))
            .when().post("/livros")
            .then().statusCode(409)
            .body("erro", containsString("ISBN"));
    }

    @Test
    void tituloEmBrancoRetorna400() {
        Map<String, Object> invalido = Map.of("titulo", "", "autor", "X", "isbn", isbnUnico(), "exemplaresTotal", 1);

        given().contentType(ContentType.JSON).body(invalido)
            .when().post("/livros")
            .then().statusCode(400);
    }

    @Test
    void zeroExemplaresRetorna400() {
        given().contentType(ContentType.JSON).body(livro(isbnUnico(), 0))
            .when().post("/livros")
            .then().statusCode(400);
    }

    @Test
    void removeLivroSemEmprestimos() {
        int id = criarLivro(1);

        given().when().delete("/livros/" + id).then().statusCode(204);
        given().when().get("/livros/" + id).then().statusCode(404);
    }

    @Test
    void naoRemoveLivroComEmprestimoAtivo() {
        int id = criarLivro(1);
        given().contentType(ContentType.JSON).body(Map.of("livroId", id, "usuario", "carla"))
            .post("/emprestimos").then().statusCode(201);

        given().when().delete("/livros/" + id).then().statusCode(409);
    }
}
