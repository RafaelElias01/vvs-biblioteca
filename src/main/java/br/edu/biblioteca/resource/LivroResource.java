package br.edu.biblioteca.resource;

import br.edu.biblioteca.model.Livro;
import br.edu.biblioteca.service.LivroService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/livros")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LivroResource {

    @Inject
    LivroService service;

    @GET
    public List<Livro> listar() {
        return service.listar();
    }

    @GET
    @Path("/{id}")
    public Livro buscar(@PathParam("id") Long id) {
        return service.buscar(id);
    }

    @POST
    public Response cadastrar(@Valid Livro livro) {
        Livro criado = service.cadastrar(livro);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @DELETE
    @Path("/{id}")
    public Response remover(@PathParam("id") Long id) {
        service.remover(id);
        return Response.noContent().build();
    }
}
