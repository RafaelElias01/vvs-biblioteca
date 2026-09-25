package br.edu.biblioteca.service;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.time.Clock;

@ApplicationScoped
public class ClockProducer {

    @Produces
    @DefaultBean
    @ApplicationScoped
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
