package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Agencia;
import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.modelo.enums.StatusVeiculo;
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
    public List<Agencia> buscarPorCidade(String cidade) {
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
    public List<Agencia> buscarPorNome(String nome) {
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
    
    @Override
    public List<Agencia> buscarVeiculosDisponiveis(CategoriaVeiculo categoria) {
        if (categoria == null)
        	return List.of();

        EntityManager em = JPAUtil.createEntityManager();

        try {
        	return em.createQuery("select distinct a from Agencia a join a.veiculos v "
        			+ "where v.status = :status and v.categoria = :categoria order by a.nome", Agencia.class)
        			.setParameter("status", StatusVeiculo.DISPONIVEL).setParameter("categoria", categoria)
        			.getResultList();
        }
        finally {
        	em.close();
        }
    }
}
