package br.edu.biblioteca.component;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Teste de componente dos emprestimos; so o relogio e substituido para controlar datas. */
@QuarkusTest
class EmprestimoApiIT {

    @InjectMock
    Clock clock;

    @BeforeEach
    void relogioEm(){
        fixarData("2026-05-01T10:00:00Z");
    }

    private void fixarData(String instante) {
        when(clock.instant()).thenReturn(Instant.parse(instante));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    private int emprestar(int livroId, String usuario) {
        return given().contentType(ContentType.JSON).body(Map.of("livroId", livroId, "usuario", usuario))
            .when().post("/emprestimos")
            .then().statusCode(201).extract().path("id");
    }

    @Test
    void emprestaLivroEDefinePrazo() {
        int livro = LivroApiIT.criarLivro(2);

        given().contentType(ContentType.JSON).body(Map.of("livroId", livro, "usuario", "ana"))
            .when().post("/emprestimos")
            .then().statusCode(201)
            .body("usuario", equalTo("ana"))
            .body("dataEmprestimo", equalTo("2026-05-01"))
            .body("dataPrevistaDevolucao", equalTo("2026-05-15"))
            .body("dataDevolucao", equalTo(null));

        given().get("/livros/" + livro).then().body("exemplaresDisponiveis", equalTo(1));
    }

    @Test
    void livroInexistenteRetorna404() {
        given().contentType(ContentType.JSON).body(Map.of("livroId", 999999, "usuario", "ana"))
            .when().post("/emprestimos")
            .then().statusCode(404);
    }

    @Test
    void semExemplaresRetorna409() {
        int livro = LivroApiIT.criarLivro(1);
        emprestar(livro, "ana");

        given().contentType(ContentType.JSON).body(Map.of("livroId", livro, "usuario", "bia"))
            .when().post("/emprestimos")
            .then().statusCode(409)
            .body("erro", containsString("exemplares"));
    }

    @Test
    void usuarioNaoPodeTerMaisDeTresEmprestimosAtivos() {
        String usuario = "limite-" + System.nanoTime();
        for (int i = 0; i < 3; i++) {
            emprestar(LivroApiIT.criarLivro(1), usuario);
        }
        int quarto = LivroApiIT.criarLivro(1);

        given().contentType(ContentType.JSON).body(Map.of("livroId", quarto, "usuario", usuario))
            .when().post("/emprestimos")
            .then().statusCode(409)
            .body("erro", containsString("limite"));
    }

    @Test
    void usuarioEmBrancoRetorna409() {
        int livro = LivroApiIT.criarLivro(1);

        given().contentType(ContentType.JSON).body(Map.of("livroId", livro, "usuario", ""))
            .when().post("/emprestimos")
            .then().statusCode(409);
    }

    @Test
    void devolucaoNoPrazoNaoCobraMultaERepoeEstoque() {
        int livro = LivroApiIT.criarLivro(1);
        int emprestimo = emprestar(livro, "ana");
        fixarData("2026-05-10T10:00:00Z");

        given().when().put("/emprestimos/" + emprestimo + "/devolucao")
            .then().statusCode(200)
            .body("dataDevolucao", equalTo("2026-05-10"))
            .body("multa", equalTo(0));

        given().get("/livros/" + livro).then().body("exemplaresDisponiveis", equalTo(1));
    }

    @Test
    void devolucaoComAtrasoCobraMulta() {
        int livro = LivroApiIT.criarLivro(1);
        int emprestimo = emprestar(livro, "ana");
        fixarData("2026-05-20T10:00:00Z");

        given().when().put("/emprestimos/" + emprestimo + "/devolucao")
            .then().statusCode(200)
            .body("multa", equalTo(10.0f));
    }

    @Test
    void devolverDuasVezesRetorna409() {
        int livro = LivroApiIT.criarLivro(1);
        int emprestimo = emprestar(livro, "ana");
        given().put("/emprestimos/" + emprestimo + "/devolucao").then().statusCode(200);

        given().put("/emprestimos/" + emprestimo + "/devolucao").then().statusCode(409);
    }

    @Test
    void devolverEmprestimoInexistenteRetorna404() {
        given().put("/emprestimos/999999/devolucao").then().statusCode(404);
    }

    @Test
    void listaEmprestimos() {
        emprestar(LivroApiIT.criarLivro(1), "lista-" + System.nanoTime());

        given().get("/emprestimos").then().statusCode(200).body("[0].id", notNullValue());
    }
}
