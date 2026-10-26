package br.autorent.sessionbeans;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.EntityManager;

/**
 * Fornece operações genéricas de persistência para entidades.
 */
abstract class CrudBean<T, ID extends Serializable> {
	private final Class<T> entityClass;
	
	protected CrudBean(Class<T> entityCLass) {
		this.entityClass = entityCLass;
	}
	
	/**
	 * Cria e persiste um novo registro.
	 * 
	 * @param entidade parâmetro utilizado na operação.
	 * @return resultado da operação.
	 */
	public T criar(T entidade) {
        if (entidade == null) 
        	throw new IllegalArgumentException("A entidade não pode ser nula.");
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
        	em.getTransaction().begin();
        	em.persist(entidade);
        	em.getTransaction().commit();
             
             return entidade;
        }
        finally {
        	em.close();
        }
    }
	
    /**
     * Atualiza os dados de um registro existente.
     * 
     * @param entidade parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    public T atualizar(T entidade) {
        if (entidade == null) 
        	throw new IllegalArgumentException("A entidade não pode ser nula.");
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
        	em.getTransaction().begin(); 
            T novaEntidade = em.merge(entidade);
            em.getTransaction().commit();
            
            return novaEntidade;
        }
        finally {
        	em.close();
        }
    }
    
    /**
     * Remove o registro informado.
     * 
     * @param id parâmetro utilizado na operação.
     */
    public void excluir(ID id) {
        if (id == null) 
        	throw new IllegalArgumentException("O identificador não pode ser nulo.");
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
        	em.getTransaction().begin();
	        T entidade = em.find(entityClass, id);
	        
	        if (entidade != null) 
	        	em.remove(entidade);
	        
	        em.getTransaction().commit();
        }
        finally {
        	em.close();
        }
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param id parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    public T buscarPorId(ID id) {
        if (id == null) 
        	return null;
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            return em.find(entityClass, id);
        } 
        finally {
        	em.close();
        }
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @return resultado da operação.
     */
    public List<T> buscarTodos() {
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            return em.createQuery("select e from " + entityClass.getSimpleName() + " e", entityClass).getResultList();
        } 
        finally {
            em.close();
        }
    }
}
