package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Acessorio;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;

/**
 * Serviço de acesso e consulta aos acessórios dos veículos.
 */
@Stateless
@Remote(IAcessorio.class)
public class AcessorioSB extends CrudBean<Acessorio, Long> implements IAcessorio {
	
	public AcessorioSB() { 
		super(Acessorio.class); 
	}
	
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param descricao parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override 
    public List<Acessorio> buscarPorDescricao(String descricao) {
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
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param veiculoId parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override 
    public List<Acessorio> buscarPorVeiculo(Long veiculoId) {
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
