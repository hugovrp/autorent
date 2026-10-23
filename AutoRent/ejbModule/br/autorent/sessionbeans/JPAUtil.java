package br.autorent.sessionbeans;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {
	private static final EntityManagerFactory FACTORY = Persistence.createEntityManagerFactory("auto-rent");

    private JPAUtil() { }

    static EntityManager createEntityManager() {
        return FACTORY.createEntityManager();
    }
}
