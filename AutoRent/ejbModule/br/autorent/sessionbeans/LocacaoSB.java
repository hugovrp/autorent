package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Locacao;
import br.autorent.modelo.enums.StatusLocacao;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;

@Stateless
@Remote(ILocacao.class)
public class LocacaoSB extends CrudBean<Locacao, Long> implements ILocacao {
	
    public LocacaoSB() { 
    	super(Locacao.class); 
    }

    @Override 
    public List<Locacao> findByCliente(Long clienteId) {
        return executeQueryWithId("select l from Locacao l where l.cliente.id = :id order by l.dataRetirada desc", clienteId);
    }
    
    @Override 
    public List<Locacao> findByVeiculo(Long veiculoId) {
        return executeQueryWithId("select l from Locacao l where l.veiculo.id = :id order by l.dataRetirada desc", veiculoId);
    }
    
    @Override 
    public List<Locacao> findByAgencia(Long agenciaId) {
        return executeQueryWithId("select l from Locacao l where l.agenciaRetirada.id = :id or l.agenciaDevolucao.id = :id order by l.dataRetirada desc", agenciaId);
    }
    
    @Override 
    public List<Locacao> findByStatus(StatusLocacao status) {
        if (status == null) 
        	return List.of();
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try { 
        	return em.createQuery("select l from Locacao l where l.status = :status order by l.dataRetirada desc", Locacao.class)
                .setParameter("status", status).getResultList(); 
        }
        finally { 
        	em.close(); 
        }
    }
    
    private List<Locacao> executeQueryWithId(String jpql, Long id) {
        if (id == null) 
        	return List.of();
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try { 
        	return em.createQuery(jpql, Locacao.class).setParameter("id", id).getResultList(); 
        }
        finally { 
        	em.close(); 
        }
    }
}
