package br.autorent.sessionbeans;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.autorent.modelo.enums.StatusLocacao;
import br.autorent.modelo.enums.StatusVeiculo;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Remote;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;

@Singleton
@Startup
@Remote(IStatusFrota.class)
public class StatusFrotaSB implements IStatusFrota {
    private long totalVeiculos;
    private long totalDisponiveis;
    private long totalAlugados;
    private long totalReservados;
    private long totalEmManutencao;
    private Map<Long, Long> disponiveisPorAgencia = new HashMap<>();

    @PostConstruct
    private void initialize() { 
    	refreshCache(); 
    }

    @Override 
    @Lock(LockType.READ) 
    public long getTotalVehicles() { 
    	return totalVeiculos; 
    }
    
    @Override
    @Lock(LockType.READ) 
    public long getTotalAvailable() { 
    	return totalDisponiveis; 
    }
    
    @Override 
    @Lock(LockType.READ) 
    public long getTotalRented() { 
    	return totalAlugados; 
    }
    
    @Override 
    @Lock(LockType.READ) 
    public long getTotalReserved() { 
    	return totalReservados; 
    }
    
    @Override 
    @Lock(LockType.READ) 
    public long getTotalInMaintenance() { 
    	return totalEmManutencao; 
    }
    
    @Override 
    @Lock(LockType.READ) 
    public Map<Long, Long> getAvailableByAgency() {
        return Collections.unmodifiableMap(new HashMap<>(disponiveisPorAgencia));
    }

    @Override 
    @Lock(LockType.WRITE)
    public void refreshCache() {
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            totalVeiculos = em.createQuery("select count(v) from Veiculo v", Long.class).getSingleResult();
            totalDisponiveis = countVehicles(em, StatusVeiculo.DISPONIVEL);
            totalAlugados = countVehicles(em, StatusVeiculo.LOCADO);
            totalEmManutencao = countVehicles(em, StatusVeiculo.MANUTENCAO);
            totalReservados = em.createQuery("select count(l) from Locacao l where l.status = :status", Long.class).setParameter("status", StatusLocacao.RESERVADA).getSingleResult();
            Map<Long, Long> novoMapa = new HashMap<>();
            
            String jpql = "select a.id, count(v) from Agencia a left join a.veiculos v on v.status = :status group by a.id";
            List<Object[]> resultados = em.createQuery(jpql, Object[].class).setParameter("status", StatusVeiculo.DISPONIVEL).getResultList();

            for (Object[] linha : resultados) {
                Long idAgencia = (Long) linha[0];
                Long totalVeiculos = (Long) linha[1];
                novoMapa.put(idAgencia, totalVeiculos);
            }
            disponiveisPorAgencia = novoMapa;
        } 
        finally { 
        	em.close(); 
        }
    }

    private long countVehicles(EntityManager em, StatusVeiculo status) {
        return em.createQuery("select count(v) from Veiculo v where v.status = :status", Long.class)
                .setParameter("status", status).getSingleResult();
    }
}
