package br.autorent.sessionbeans;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;

import br.autorent.dto.LocacaoDTO;
import br.autorent.modelo.Locacao;
import br.autorent.modelo.Veiculo;
import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.modelo.enums.StatusLocacao;
import br.autorent.modelo.enums.StatusVeiculo;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;

/**
 * Gerencia locações, retiradas, devoluções e cancelamentos.
 */
@Stateless
@Remote(ILocacao.class)
public class LocacaoSB extends CrudBean<Locacao, Long> implements ILocacao {
	
    public LocacaoSB() { 
    	super(Locacao.class); 
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param clienteId parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override 
    public List<Locacao> buscarPorCliente(Long clienteId) {
    	return executarConsultaPorId("select l from Locacao l where l.cliente.id = :id order by l.dataRetirada desc", clienteId);
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param clienteId parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public List<Locacao> buscarPorClienteCompleto(Long clienteId) {
    	if (clienteId == null)
        	return List.of();

        EntityManager em = JPAUtil.createEntityManager();

        try {
        	return em.createQuery("select l from Locacao l join fetch l.veiculo join fetch l.agenciaRetirada join fetch l.agenciaDevolucao "
        			+ "where l.cliente.id = :clienteId order by l.dataRetirada desc", Locacao.class)
        			.setParameter("clienteId", clienteId).getResultList();
        }
        finally {
        	em.close();
        }
    }
    
    /**
     * Registra a operação informada.
     * 
     * @param locacaoId parâmetro utilizado na operação.
     */
    @Override
    public void registrarRetirada(Long locacaoId) {
        alterar(locacaoId, StatusLocacao.RESERVADA, (l, v) -> {
            if (v.getStatus() != StatusVeiculo.DISPONIVEL)
                throw new IllegalStateException("O veículo não está disponível para retirada.");
            l.setStatus(StatusLocacao.ATIVA);
            v.setStatus(StatusVeiculo.LOCADO);
        });
    }
    
    /**
     * Registra a operação informada.
     * 
     * @param locacaoId parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public LocacaoDTO registrarRetiradaAtendimento(Long locacaoId) throws Exception {
        try {
            registrarRetirada(locacaoId);
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            throw new Exception(e.getMessage());
        }

        EntityManager em = JPAUtil.createEntityManager();

        try {
            Locacao l = em.createQuery("select l from Locacao l join fetch l.cliente join fetch l.veiculo "
                    + "join fetch l.agenciaRetirada join fetch l.agenciaDevolucao where l.id = :id", Locacao.class)
                    .setParameter("id", locacaoId).getSingleResult();

            return new LocacaoDTO(l.getId(), l.getCliente().getNome(), l.getCliente().getCpf(), l.getVeiculo().getMarca(),
                    l.getVeiculo().getModelo(), l.getVeiculo().getPlaca(), l.getAgenciaRetirada().getNome(),
                    l.getAgenciaDevolucao().getNome(), l.getDataRetirada(), l.getDataPrevDevolucao(), l.getDataDevolucao(),
                    l.getValorTotal(), l.getStatus(), false);
        }
        finally {
        	em.close();
        }
    }
    
    /**
     * Registra a operação informada.
     * 
     * @param locacaoId parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public LocacaoDTO registrarDevolucaoAtendimento(Long locacaoId) throws Exception {
        try {
            registrarDevolucao(locacaoId);
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            throw new Exception(e.getMessage());
        }

        return carregarDTO(locacaoId);
    }
    
    /**
     * Converte os dados da locação para o objeto de transferência correspondente.
     * 
     * @param locacaoId parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    private LocacaoDTO carregarDTO(Long locacaoId) {
        EntityManager em = JPAUtil.createEntityManager();

        try {
            Locacao l = em.createQuery("select l from Locacao l join fetch l.cliente join fetch l.veiculo "
                    + "join fetch l.agenciaRetirada join fetch l.agenciaDevolucao where l.id = :id", Locacao.class)
                    .setParameter("id", locacaoId).getSingleResult();

            return new LocacaoDTO(l.getId(), l.getCliente().getNome(), l.getCliente().getCpf(), l.getVeiculo().getMarca(),
                    l.getVeiculo().getModelo(), l.getVeiculo().getPlaca(), l.getAgenciaRetirada().getNome(),
                    l.getAgenciaDevolucao().getNome(), l.getDataRetirada(), l.getDataPrevDevolucao(), l.getDataDevolucao(),
                    l.getValorTotal(), l.getStatus(), false);
        }
        finally {
        	em.close();
        }
    }
    
    /**
     * Registra a operação informada.
     * 
     * @param locacaoId parâmetro utilizado na operação.
     */
    @Override
    public void registrarDevolucao(Long locacaoId) {
        alterar(locacaoId, StatusLocacao.ATIVA, (l, v) -> {
            l.setStatus(StatusLocacao.FINALIZADA);
            l.setDataDevolucao(LocalDate.now());
            v.setStatus(StatusVeiculo.DISPONIVEL);
            v.setAgencia(l.getAgenciaDevolucao()); 
        });
    }
    
    /**
     * Cancela a operação informada.
     * 
     * @param locacaoId parâmetro utilizado na operação.
     */
    @Override
    public void cancelar(Long locacaoId) {
        alterar(locacaoId, StatusLocacao.RESERVADA, (l, v) -> l.setStatus(StatusLocacao.CANCELADA));
    }
    
    /**
     * Altera os dados ou o estado do registro informado.
     * 
     * @param id parâmetro utilizado na operação.
     * @param statusEsperado parâmetro utilizado na operação.
     * @param acao parâmetro utilizado na operação.
     */
    private void alterar(Long id, StatusLocacao statusEsperado, BiConsumer<Locacao, Veiculo> acao) {
        if (id == null)
        	throw new IllegalArgumentException("Informe a locação.");

        EntityManager em = JPAUtil.createEntityManager();

        try {
        	em.getTransaction().begin();
        	Locacao l = em.find(Locacao.class, id);

        	if (l == null)
        		throw new IllegalArgumentException("Locação não encontrada.");

        	if (l.getStatus() != statusEsperado)
        		throw new IllegalStateException("A locação precisa estar com status " + statusEsperado + " para esta operação.");

        	acao.accept(l, l.getVeiculo());
        	em.getTransaction().commit();
        }
        finally {
        	if (em.getTransaction().isActive())
        		em.getTransaction().rollback();
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
    public List<Locacao> buscarPorVeiculo(Long veiculoId) {
        return executarConsultaPorId("select l from Locacao l where l.veiculo.id = :id order by l.dataRetirada desc", veiculoId);
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param agenciaId parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override 
    public List<Locacao> buscarPorAgencia(Long agenciaId) {
        return executarConsultaPorId("select l from Locacao l where l.agenciaRetirada.id = :id or l.agenciaDevolucao.id = :id order by l.dataRetirada desc", agenciaId);
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param status parâmetro utilizado na operação.
     * @return resultado da operação.
     */
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
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param clienteId parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public List<Locacao> buscarAtivasPorCliente(Long clienteId) {
        if (clienteId == null)
        	return List.of();

        EntityManager em = JPAUtil.createEntityManager();

        try {
        	return em.createQuery("select l from Locacao l join fetch l.cliente c join fetch l.veiculo "
        			+ "join fetch l.agenciaRetirada join fetch l.agenciaDevolucao where c.id = :clienteId and l.status = :status "
        			+ "order by l.dataRetirada desc", Locacao.class)
        			.setParameter("clienteId", clienteId).setParameter("status", StatusLocacao.ATIVA).getResultList();
        }
        finally {
        	em.close();
        }
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param categoria parâmetro utilizado na operação.
     * @param status parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public List<Locacao> buscarPorCategoriaEStatus(CategoriaVeiculo categoria, StatusLocacao status) {
        if (categoria == null || status == null)
        	return List.of();

        EntityManager em = JPAUtil.createEntityManager();

        try {
        	return em.createQuery("select l from Locacao l join fetch l.veiculo v join fetch l.cliente "
        			+ "join fetch l.agenciaRetirada join fetch l.agenciaDevolucao where v.categoria = :categoria and l.status = :status "
        			+ "order by l.dataRetirada desc", Locacao.class)
        			.setParameter("categoria", categoria).setParameter("status", status).getResultList();
        }
        finally {
        	em.close();
        }
    }
    
    /**
     * Executa a consulta da locação identificada pelo código informado.
     * 
     * @param jpql parâmetro utilizado na operação.
     * @param id parâmetro utilizado na operação.
     * @return resultado da operação.
     */
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
