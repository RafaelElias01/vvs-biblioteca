package br.edu.biblioteca.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URL;
import java.nio.file.Paths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Testes de sistema pela interface web (Playwright). Screenshots ficam em target/evidencias. */
@QuarkusTest
@UsePlaywright
class BibliotecaSystemIT {

    @TestHTTPResource("/")
    URL baseUrl;

    private void cadastrar(Page page, String titulo, String isbn) {
        page.getByPlaceholder("Titulo").fill(titulo);
        page.getByPlaceholder("Autor").fill("Autor Teste");
        page.getByPlaceholder("ISBN").fill(isbn);
        page.getByPlaceholder("Exemplares").fill("1");
        page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Cadastrar")).click();
    }

    private Locator livro(Page page, String titulo) {
        return page.locator("li.livro", new Page.LocatorOptions().setHasText(titulo));
    }

    @Test
    @DisplayName("Fluxo principal: cadastrar, emprestar e devolver um livro")
    void shouldRegisterBorrowAndReturnBook(Page page) {
        String titulo = "Fluxo " + System.nanoTime();
        page.navigate(baseUrl.toString());

        cadastrar(page, titulo, "SYS-" + System.nanoTime());
        assertThat(livro(page, titulo)).isVisible();

        livro(page, titulo).getByPlaceholder("Seu nome").fill("aluno");
        livro(page, titulo).getByText("Emprestar").click();
        assertThat(livro(page, titulo).locator(".disponiveis")).hasText("0");

        Locator emprestimo = page.locator("li.emprestimo", new Page.LocatorOptions().setHasText(titulo));
        assertThat(emprestimo).containsText("ativo");
        emprestimo.getByText("Devolver").click();

        assertThat(livro(page, titulo).locator(".disponiveis")).hasText("1");
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/evidencias/fluxo-principal.png")));
    }

    @Test
    @DisplayName("Fluxo alternativo: ISBN duplicado mostra erro")
    void shouldShowErrorWhenIsbnIsDuplicated(Page page) {
        String isbn = "DUP-" + System.nanoTime();
        page.navigate(baseUrl.toString());
        cadastrar(page, "Primeiro " + isbn, isbn);
        assertThat(livro(page, "Primeiro " + isbn)).isVisible();

        cadastrar(page, "Segundo " + isbn, isbn);

        assertThat(page.locator("#mensagem")).containsText("ISBN ja cadastrado");
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/evidencias/isbn-duplicado.png")));
    }

    @Test
    @DisplayName("Fluxo alternativo: titulo em branco mostra erro de validacao")
    void shouldShowErrorWhenTitleIsBlank(Page page) {
        page.navigate(baseUrl.toString());

        cadastrar(page, "", "INV-" + System.nanoTime());

        assertThat(page.locator("#mensagem")).containsText("titulo e obrigatorio");
    }

    @Test
    @DisplayName("Fluxo alternativo: emprestimo sem nome mostra erro")
    void shouldShowErrorWhenBorrowerNameIsMissing(Page page) {
        String titulo = "SemNome " + System.nanoTime();
        page.navigate(baseUrl.toString());
        cadastrar(page, titulo, "SN-" + System.nanoTime());

        livro(page, titulo).getByText("Emprestar").click();

        assertThat(page.locator("#mensagem")).containsText("usuario e obrigatorio");
    }
}
