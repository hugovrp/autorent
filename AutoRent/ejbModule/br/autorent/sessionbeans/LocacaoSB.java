package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Locacao;
import br.autorent.modelo.enums.CategoriaVeiculo;
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
    public List<Locacao> buscarPorCliente(Long clienteId) {
        return executarConsultaPorId("select l from Locacao l where l.cliente.id = :id order by l.dataRetirada desc", clienteId);
    }
    
    @Override 
    public List<Locacao> buscarPorVeiculo(Long veiculoId) {
        return executarConsultaPorId("select l from Locacao l where l.veiculo.id = :id order by l.dataRetirada desc", veiculoId);
    }
    
    @Override 
    public List<Locacao> buscarPorAgencia(Long agenciaId) {
        return executarConsultaPorId("select l from Locacao l where l.agenciaRetirada.id = :id or l.agenciaDevolucao.id = :id order by l.dataRetirada desc", agenciaId);
    }
    
    @Override 
    public List<Locacao> buscarPorStatus(StatusLocacao status) {
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
    
    @Override
    public List<Locacao> buscarAtivasPorCliente(Long clienteId) {
        if (clienteId == null)
        	return List.of();

        EntityManager em = JPAUtil.createEntityManager();

        try {
        	return em.createQuery("select l from Locacao l "
        			+ "join fetch l.cliente c join fetch l.veiculo "
        			+ "join fetch l.agenciaRetirada join fetch l.agenciaDevolucao "
        			+ "where c.id = :clienteId and l.status = :status "
        			+ "order by l.dataRetirada desc", Locacao.class)
        			.setParameter("clienteId", clienteId).setParameter("status", StatusLocacao.ATIVA)
        			.getResultList();
        }
        finally {
        	em.close();
        }
    }

    @Override
    public List<Locacao> buscarPorCategoriaEStatus(CategoriaVeiculo categoria, StatusLocacao status) {
        if (categoria == null || status == null)
        	return List.of();

        EntityManager em = JPAUtil.createEntityManager();

        try {
        	// Relaciona Locacao -> Veiculo (filtro por categoria), Usuario e Agencia
        	return em.createQuery("select l from Locacao l "
        			+ "join fetch l.veiculo v join fetch l.cliente "
        			+ "join fetch l.agenciaRetirada join fetch l.agenciaDevolucao "
        			+ "where v.categoria = :categoria and l.status = :status "
        			+ "order by l.dataRetirada desc", Locacao.class)
        			.setParameter("categoria", categoria).setParameter("status", status)
        			.getResultList();
        }
        finally {
        	em.close();
        }
    }
    
    private List<Locacao> executarConsultaPorId(String jpql, Long id) {
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
