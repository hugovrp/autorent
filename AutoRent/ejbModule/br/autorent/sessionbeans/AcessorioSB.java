package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Acessorio;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;

@Stateless
@Remote(IAcessorio.class)
public class AcessorioSB extends CrudBean<Acessorio, Long> implements IAcessorio {
	
	public AcessorioSB() { 
		super(Acessorio.class); 
	}

    @Override 
    public List<Acessorio> findByDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) 
        	return List.of();
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try { 
        	return em.createQuery("select a from Acessorio a where lower(a.descricao) like lower(:descricao) order by a.descricao", Acessorio.class)
        			.setParameter("descricao", "%" + descricao.trim() + "%").getResultList(); 
        }
        finally { 
        	em.close(); 
        }
    }
    
    @Override 
    public List<Acessorio> findByVeiculo(Long veiculoId) {
        if (veiculoId == null) 
        	return List.of();
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try { 
        	return em.createQuery("select a from Acessorio a join a.veiculos v where v.id = :id order by a.descricao", Acessorio.class)
                .setParameter("id", veiculoId).getResultList(); 
        }
        finally { 
        	em.close(); 
        }
    }
}
