package br.autorent.sessionbeans;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.EntityManager;

abstract class CrudBean<T, ID extends Serializable> {
	private final Class<T> entityClass;
	
	protected CrudBean(Class<T> entityCLass) {
		this.entityClass = entityCLass;
	}
	
	public T create(T entidade) {
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

    public T update(T entidade) {
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

    public void delete(ID id) {
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

    public T findById(ID id) {
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

    public List<T> findAll() {
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            return em.createQuery("select e from " + entityClass.getSimpleName() + " e", entityClass).getResultList();
        } 
        finally {
            em.close();
        }
    }
}
