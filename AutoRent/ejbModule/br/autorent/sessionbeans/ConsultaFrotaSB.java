package br.autorent.sessionbeans;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.autorent.dto.AgenciaDTO;
import br.autorent.dto.LocacaoDTO;
import br.autorent.dto.VeiculoDTO;
import br.autorent.modelo.Agencia;
import br.autorent.modelo.Locacao;
import br.autorent.modelo.Veiculo;
import br.autorent.modelo.enums.StatusLocacao;
import br.autorent.modelo.enums.StatusVeiculo;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;

/**
 * Disponibiliza consultas consolidadas sobre a frota e as locações.
 */
@Stateless
@Remote(IConsultaFrota.class)
public class ConsultaFrotaSB implements IConsultaFrota {
	
    /**
     * Lista os registros disponíveis.
     * 
     * @return resultado da operação.
     */
    @Override
    public List<AgenciaDTO> listarAgencias() {
        EntityManager em = JPAUtil.createEntityManager();

        try {
            List<AgenciaDTO> lista = new ArrayList<>();

            for (Agencia a : em.createQuery("select a from Agencia a order by a.nome", Agencia.class).getResultList())
            	lista.add(new AgenciaDTO(a.getId(), a.getNome(), a.getCidade(), a.getEndereco(), a.getTelefone()));

            return lista;
        }
        finally {
        	em.close();
        }
    }
    
    /**
     * Consulta informações relacionadas aos critérios informados.
     * 
     * @param agenciaId parâmetro utilizado na operação.
     * @param status parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public List<VeiculoDTO> consultarFrota(Long agenciaId, StatusVeiculo status) {
        EntityManager em = JPAUtil.createEntityManager();

        try {
            String jpql = "select v from Veiculo v join fetch v.agencia a where 1 = 1";

            if (agenciaId != null)
            	jpql += " and a.id = :agenciaId";

            if (status != null)
            	jpql += " and v.status = :status";

            var consulta = em.createQuery(jpql + " order by a.nome, v.marca, v.modelo", Veiculo.class);

            if (agenciaId != null)
            	consulta.setParameter("agenciaId", agenciaId);

            if (status != null)
            	consulta.setParameter("status", status);

            // data prevista de devolução do veículo que está em locação ativa
            Map<Long, LocalDate> devolucoes = new HashMap<>();
            for (Object[] linha : em.createQuery("select l.veiculo.id, l.dataPrevDevolucao from Locacao l where l.status = :status", Object[].class)
                    .setParameter("status", StatusLocacao.ATIVA).getResultList())
            	devolucoes.put((Long) linha[0], (LocalDate) linha[1]);

            List<VeiculoDTO> frota = new ArrayList<>();

            for (Veiculo v : consulta.getResultList()) {
            	boolean disponivel = v.getStatus() == StatusVeiculo.DISPONIVEL;
            	frota.add(new VeiculoDTO(v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(), v.getAno(), v.getCategoria(),
            			v.getValorDiaria(), v.getStatus(), v.getAgencia().getId(), v.getAgencia().getNome(), disponivel,
            			devolucoes.get(v.getId())));
            }
            return frota;
        }
        finally {
        	em.close();
        }
    }
    
    /**
     * Consulta informações relacionadas aos critérios informados.
     * 
     * @param status parâmetro utilizado na operação.
     * @param agenciaId parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public List<LocacaoDTO> consultarLocacoes(StatusLocacao status, Long agenciaId) {
        EntityManager em = JPAUtil.createEntityManager();

        try {
            String jpql = "select l from Locacao l join fetch l.cliente join fetch l.veiculo "
                    + "join fetch l.agenciaRetirada ar join fetch l.agenciaDevolucao ad where 1 = 1";

            if (status != null)
            	jpql += " and l.status = :status";

            if (agenciaId != null)
            	jpql += " and (ar.id = :agenciaId or ad.id = :agenciaId)";

            var consulta = em.createQuery(jpql + " order by l.dataPrevDevolucao, l.id", Locacao.class);

            if (status != null)
            	consulta.setParameter("status", status);

            if (agenciaId != null)
            	consulta.setParameter("agenciaId", agenciaId);

            LocalDate hoje = LocalDate.now();
            List<LocacaoDTO> lista = new ArrayList<>();

            for (Locacao l : consulta.getResultList()) {
            	boolean atrasada = l.getStatus() == StatusLocacao.ATIVA && l.getDataPrevDevolucao().isBefore(hoje);
            	lista.add(new LocacaoDTO(l.getId(), l.getCliente().getNome(), l.getCliente().getCpf(), l.getVeiculo().getMarca(),
            			l.getVeiculo().getModelo(), l.getVeiculo().getPlaca(), l.getAgenciaRetirada().getNome(),
            			l.getAgenciaDevolucao().getNome(), l.getDataRetirada(), l.getDataPrevDevolucao(), l.getDataDevolucao(),
            			l.getValorTotal(), l.getStatus(), atrasada));
            }
            return lista;
        }
        finally {
        	em.close();
        }
    }
}
