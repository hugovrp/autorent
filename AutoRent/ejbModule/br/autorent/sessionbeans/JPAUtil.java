package br.autorent.sessionbeans;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Centraliza a criação de gerenciadores de entidades JPA.
 */
public class JPAUtil {
	private static final EntityManagerFactory FACTORY = Persistence.createEntityManagerFactory("auto-rent");

    private JPAUtil() { }
    
    /**
     * Cria um gerenciador de entidades para operações de persistência.
     * 
     * @return resultado da operação.
     */
    static EntityManager createEntityManager() {
        return FACTORY.createEntityManager();
    }
}
