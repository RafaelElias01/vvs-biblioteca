package br.edu.biblioteca.security;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Testes de seguranca basicos (extra): injecao, mass assignment, XSS armazenado, exposicao de dados. */
@QuarkusTest
@Tag("security")
class SegurancaApiIT {

    private static String isbn() {
        return "SEC-" + System.nanoTime();
    }

    @Test
    void sqlInjectionNoUsuarioNaoQuebraNemBurlaOLimite() {
        int livro = given().contentType(ContentType.JSON)
            .body(Map.of("titulo", "T", "autor", "A", "isbn", isbn(), "exemplaresTotal", 1))
            .post("/livros").then().statusCode(201).extract().path("id");

        given().contentType(ContentType.JSON)
            .body(Map.of("livroId", livro, "usuario", "x' OR '1'='1"))
            .when().post("/emprestimos")
            .then().statusCode(201)
            .body("usuario", equalTo("x' OR '1'='1"));
    }

    @Test
    void sqlInjectionNoIdDaUrlRetornaErroDeCliente() {
        given().when().get("/livros/1%20OR%201=1")
            .then().statusCode(404);
    }

    @Test
    void sqlInjectionNoIsbnEArmazenadoComoTextoLiteral() {
        String malicioso = "'; DROP TABLE Livro; --" + System.nanoTime();
        given().contentType(ContentType.JSON)
            .body(Map.of("titulo", "T", "autor", "A", "isbn", malicioso, "exemplaresTotal", 1))
            .post("/livros").then().statusCode(201);

        given().get("/livros").then().statusCode(200).body("isbn", org.hamcrest.Matchers.hasItem(malicioso));
    }

    @Test
    void massAssignmentDeEstoqueDisponivelEIgnorado() {
        given().contentType(ContentType.JSON)
            .body(Map.of("titulo", "T", "autor", "A", "isbn", isbn(),
                "exemplaresTotal", 1, "exemplaresDisponiveis", 999))
            .post("/livros")
            .then().statusCode(201)
            .body("exemplaresDisponiveis", equalTo(1));
    }

    @Test
    void jsonMalformadoRetornaErroDeClienteSemVazarStackTrace() {
        given().contentType(ContentType.JSON).body("{isso nao e json")
            .post("/livros")
            .then().statusCode(400)
            .body(not(containsString("at br.edu")))
            .body(not(containsString("Exception")));
    }

    @Test
    void erroDeNegocioNaoExpoeDetalhesInternos() {
        given().get("/livros/999999")
            .then().statusCode(404)
            .body(not(containsString("hibernate")))
            .body(not(containsString("SQL")));
    }

    @Test
    void scriptNoTituloEArmazenadoSemExecucaoNoServidor() {
        String xss = "<script>alert(1)</script>";
        given().contentType(ContentType.JSON)
            .body(Map.of("titulo", xss, "autor", "A", "isbn", isbn(), "exemplaresTotal", 1))
            .post("/livros")
            .then().statusCode(201)
            .body("titulo", equalTo(xss));
    }
}
