package br.edu.biblioteca.resource;

import br.edu.biblioteca.model.Emprestimo;
import br.edu.biblioteca.service.EmprestimoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/emprestimos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmprestimoResource {

    public record NovoEmprestimo(Long livroId, String usuario) {
    }

    @Inject
    EmprestimoService service;

    @GET
    public List<Emprestimo> listar() {
        return service.listar();
    }

    @POST
    public Response emprestar(NovoEmprestimo dados) {
        Emprestimo e = service.emprestar(dados.livroId(), dados.usuario());
        return Response.status(Response.Status.CREATED).entity(e).build();
    }

    @PUT
    @Path("/{id}/devolucao")
    public Emprestimo devolver(@PathParam("id") Long id) {
        return service.devolver(id);
    }
}
