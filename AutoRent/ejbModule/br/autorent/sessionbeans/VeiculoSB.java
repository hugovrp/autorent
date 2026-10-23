package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Veiculo;
import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.modelo.enums.StatusVeiculo;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;

@Stateless
@Remote(IVeiculo.class)
public class VeiculoSB extends CrudBean<Veiculo, Long> implements IVeiculo {
	
    public VeiculoSB() { 
    	super(Veiculo.class); 
    }

    @Override 
    public List<Veiculo> buscarPorAgencia(Long agenciaId) {
        if (agenciaId == null) 
        	return List.of();
        
        return consultar("select v from Veiculo v where v.agencia.id = :valor order by v.modelo", "valor", agenciaId);
    }
    
    @Override 
    public List<Veiculo> buscarPorStatus(StatusVeiculo status) {
        if (status == null) 
        	return List.of();
        
        return consultar("select v from Veiculo v where v.status = :valor order by v.modelo", "valor", status);
    }
    
    @Override 
    public List<Veiculo> buscarPorCategoria(CategoriaVeiculo categoria) {
        if (categoria == null) 
        	return List.of();
        
        return consultar("select v from Veiculo v where v.categoria = :valor order by v.modelo", "valor", categoria);
    }
    
    @Override 
    public Veiculo buscarPorPlaca(String placa) {
        if (placa == null || placa.isBlank()) 
        	return null;
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            List<Veiculo> lista = em.createQuery("select v from Veiculo v where upper(v.placa) = upper(:placa)", Veiculo.class)
                    .setParameter("placa", placa.trim()).setMaxResults(1).getResultList();
            
            return lista.isEmpty() ? null : lista.get(0);
        } 
        finally { 
        	em.close(); 
        }
    }
    
    @Override 
    public List<Veiculo> buscarDisponiveis(Long agenciaId, CategoriaVeiculo categoria) {
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            String jpql = "select v from Veiculo v where v.status = :status";
            
            if (agenciaId != null) 
            	jpql += " and v.agencia.id = :agenciaId";
            
            if (categoria != null) 
            	jpql += " and v.categoria = :categoria";
            
            jpql += " order by v.modelo";
            var query = em.createQuery(jpql, Veiculo.class).setParameter("status", StatusVeiculo.DISPONIVEL);
            
            if (agenciaId != null) 
            	query.setParameter("agenciaId", agenciaId);
            
            if (categoria != null) 
            	query.setParameter("categoria", categoria);
            
            return query.getResultList();
        } 
        finally { 
        	em.close(); 
        }
    }
    
    @Override
    public List<Veiculo> buscarDisponiveisPorAgencia(Long agenciaId) {
        if (agenciaId == null)
        	return List.of();

        EntityManager em = JPAUtil.createEntityManager();

        try {
        	// JOIN FETCH traz a agência na mesma consulta (Veiculo.agencia é LAZY)
        	return em.createQuery("select v from Veiculo v join fetch v.agencia a "
        			+ "where a.id = :agenciaId and v.status = :status order by v.modelo", Veiculo.class)
        			.setParameter("agenciaId", agenciaId).setParameter("status", StatusVeiculo.DISPONIVEL)
        			.getResultList();
        }
        finally {
        	em.close();
        }
    }

    @Override
    public List<Veiculo> buscarAlugados() {
        EntityManager em = JPAUtil.createEntityManager();

        try {
        	return em.createQuery("select v from Veiculo v join fetch v.agencia "
        			+ "where v.status = :status order by v.modelo", Veiculo.class)
        			.setParameter("status", StatusVeiculo.LOCADO).getResultList();
        }
        finally {
        	em.close();
        }
    }
    
    private List<Veiculo> consultar(String jpql, String parametro, Object valor) {
        EntityManager em = JPAUtil.createEntityManager();
        
        try { 
        	return em.createQuery(jpql, Veiculo.class).setParameter(parametro, valor).getResultList(); 
        }
        finally { 
        	em.close(); 
        }
    }
}