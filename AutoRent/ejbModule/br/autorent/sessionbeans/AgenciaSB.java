package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Agencia;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;

@Stateless
@Remote(IAgencia.class)
public class AgenciaSB extends CrudBean<Agencia, Long> implements IAgencia {
	
    public AgenciaSB() { 
    	super(Agencia.class); 
    }

    @Override
    public List<Agencia> findByCidade(String cidade) {
        if (cidade == null || cidade.isBlank()) 
        	return List.of();
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            return em.createQuery("select a from Agencia a where lower(a.cidade) = lower(:cidade) order by a.nome", Agencia.class)
                    .setParameter("cidade", cidade.trim()).getResultList();
        } 
        finally { 
        	em.close(); 
        }
    }

    @Override
    public List<Agencia> findByNome(String nome) {
        if (nome == null || nome.isBlank()) 
        	return List.of();
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            return em.createQuery("select a from Agencia a where lower(a.nome) like lower(:nome) order by a.nome", Agencia.class)
                    .setParameter("nome", "%" + nome.trim() + "%").getResultList();
        } 
        finally { 
        	em.close(); 
        }
    }
}
