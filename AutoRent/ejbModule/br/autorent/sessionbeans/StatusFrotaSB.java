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

/**
 * Mantém estatísticas e contagens sobre o estado da frota.
 */
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
    
    /**
     * Executa a operação correspondente aos dados informados.
     */
    @PostConstruct
    private void initialize() { 
    	atualizarCache(); 
    }
    
    /**
     * Obtém as informações solicitadas.
     * 
     * @return resultado da operação.
     */
    @Override 
    @Lock(LockType.READ) 
    public long obterTotalVeiculos() { 
    	return totalVeiculos; 
    }
    
    /**
     * Obtém as informações solicitadas.
     * 
     * @return resultado da operação.
     */
    @Override
    @Lock(LockType.READ) 
    public long obterTotalDisponiveis() { 
    	return totalDisponiveis; 
    }
    
    /**
     * Obtém as informações solicitadas.
     * 
     * @return resultado da operação.
     */
    @Override 
    @Lock(LockType.READ) 
    public long obterTotalAlugados() { 
    	return totalAlugados; 
    }
    
    /**
     * Obtém as informações solicitadas.
     * 
     * @return resultado da operação.
     */
    @Override 
    @Lock(LockType.READ) 
    public long obterTotalReservados() { 
    	return totalReservados; 
    }
    
    /**
     * Obtém as informações solicitadas.
     * 
     * @return resultado da operação.
     */
    @Override 
    @Lock(LockType.READ) 
    public long obterTotalEmManutencao() { 
    	return totalEmManutencao; 
    }
    
    /**
     * Obtém as informações solicitadas.
     * 
     * @return resultado da operação.
     */
    @Override 
    @Lock(LockType.READ) 
    public Map<Long, Long> obterDisponiveisPorAgencia() {
        return Collections.unmodifiableMap(new HashMap<>(disponiveisPorAgencia));
    }
    
    /**
     * Atualiza os dados de um registro existente.
     */
    @Override 
    @Lock(LockType.WRITE)
    public void atualizarCache() {
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            totalVeiculos = em.createQuery("select count(v) from Veiculo v", Long.class).getSingleResult();
            totalDisponiveis = contarVeiculos(em, StatusVeiculo.DISPONIVEL);
            totalAlugados = contarVeiculos(em, StatusVeiculo.LOCADO);
            totalEmManutencao = contarVeiculos(em, StatusVeiculo.MANUTENCAO);
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
    
    /**
     * Conta os veículos que atendem aos critérios informados.
     * 
     * @param em parâmetro utilizado na operação.
     * @param status parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    private long contarVeiculos(EntityManager em, StatusVeiculo status) {
        return em.createQuery("select count(v) from Veiculo v where v.status = :status", Long.class)
                .setParameter("status", status).getSingleResult();
    }
}
