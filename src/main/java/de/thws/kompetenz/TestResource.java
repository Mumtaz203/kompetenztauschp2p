package de.thws.kompetenz;



import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/test-db")
public class TestResource {

    @Inject
    EntityManager em;

    @GET
    @Transactional
    public String test() {
        em.createNativeQuery("SELECT 1").getSingleResult();
        return "DB connected";
    }
}