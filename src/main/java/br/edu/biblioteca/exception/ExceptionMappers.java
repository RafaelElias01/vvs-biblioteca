package br.edu.biblioteca.exception;

import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMappers {

    @ServerExceptionMapper
    public Response naoEncontrado(NaoEncontradoException e) {
        return Response.status(Response.Status.NOT_FOUND).entity(Map.of("erro", e.getMessage())).build();
    }

    @ServerExceptionMapper
    public Response regraDeNegocio(RegraDeNegocioException e) {
        return Response.status(Response.Status.CONFLICT).entity(Map.of("erro", e.getMessage())).build();
    }
}
