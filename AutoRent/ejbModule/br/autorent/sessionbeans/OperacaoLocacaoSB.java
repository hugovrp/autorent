package br.autorent.sessionbeans;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import br.autorent.dto.LocacaoDTO;
import br.autorent.modelo.Agencia;
import br.autorent.modelo.Locacao;
import br.autorent.modelo.Usuario;
import br.autorent.modelo.Veiculo;
import br.autorent.modelo.enums.StatusLocacao;
import br.autorent.modelo.enums.StatusVeiculo;
import br.autorent.modelo.enums.TipoUsuario;
import jakarta.ejb.Remote;
import jakarta.ejb.Remove;
import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;

/**
 * Coordena as etapas necessárias para realizar uma reserva ou locação.
 */
@Stateful
@Remote(IOperacaoLocacao.class)
public class OperacaoLocacaoSB implements IOperacaoLocacao {
    private Usuario clienteLogado;
    private Agencia agenciaRetirada;
    private Agencia agenciaDevolucao;
    private Veiculo veiculoSelecionado;
    private LocalDate dataRetirada;
    private LocalDate dataDevolucao;
    
    /**
     * Autentica o usuário com as credenciais informadas.
     * 
     * @param cpf parâmetro utilizado na operação.
     * @param senha parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    @Override
    public boolean login(String cpf, String senha) {
        clienteLogado = null;
        if (cpf == null || cpf.isBlank() || senha == null) 
        	return false;
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            List<Usuario> usuarios = em.createQuery("select u from Usuario u where u.cpf = :cpf", Usuario.class)
                    .setParameter("cpf", cpf.replaceAll("\\D", "")).setMaxResults(1).getResultList();
        
            if (usuarios.isEmpty()) 
            	return false;
            
            Usuario usuario = usuarios.get(0);
            if (usuario.getTipo() != TipoUsuario.CLIENTE || !SenhaUtil.conferirEMigrar(em, usuario, senha)) 
            	return false;
            
            clienteLogado = usuario;
            return true;
        } 
        finally { 
        	em.close(); 
        }
    }
    
    /**
     * Obtém as informações solicitadas.
     * 
     * @return resultado da operação.
     */
    @Override 
    public Usuario obterClienteLogado() { 
    	return clienteLogado; 
    }
    
    /**
     * Define a opção selecionada para a operação atual.
     * 
     * @param agenciaId parâmetro utilizado na operação.
     */
    @Override 
    public void selecionarAgenciaRetirada(Long agenciaId) { 
    	agenciaRetirada = procuraAgencia(agenciaId); 
    }
    
    /**
     * Define a opção selecionada para a operação atual.
     * 
     * @param agenciaId parâmetro utilizado na operação.
     */
    @Override 
    public void selecionarAgenciaDevolucao(Long agenciaId) { 
    	agenciaDevolucao = procuraAgencia(agenciaId); 
    }
    
    @Override public void selecionarVeiculo(Long veiculoId) { 
    	veiculoSelecionado = buscarVeiculo(veiculoId); 
    }
    
    /**
     * Define os dados necessários para a operação.
     * 
     * @param retirada parâmetro utilizado na operação.
     * @param devolucao parâmetro utilizado na operação.
     */
    @Override
    public void definirPeriodoLocacao(LocalDate retirada, LocalDate devolucao) {
        if (retirada == null || devolucao == null || devolucao.isBefore(retirada) || devolucao.equals(retirada))
            throw new IllegalArgumentException("O período deve possuir datas válidas e a devolução deve ser posterior à retirada.");
        
        if (retirada.isBefore(LocalDate.now())) 
        	throw new IllegalArgumentException("A data de retirada não pode estar no passado.");
        
        dataRetirada = retirada;
        dataDevolucao = devolucao;
    }
    
    /**
     * Calcula o valor com base nos dados informados.
     * 
     * @return resultado da operação.
     */
    @Override
    public double calcularValorTotal() {
    	validarDados();
        long diarias = ChronoUnit.DAYS.between(dataRetirada, dataDevolucao);
        return diarias * veiculoSelecionado.getValorDiaria();
    }
    
    /**
     * Valida e confirma a operação solicitada.
     * 
     * @return resultado da operação.
     */
    @Override
    public Locacao confirmarLocacao() {
    	validarDados();
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            em.getTransaction().begin();
            
            Usuario cliente = em.find(Usuario.class, clienteLogado.getId());
            Agencia retirada = em.find(Agencia.class, agenciaRetirada.getId());
            Agencia devolucao = em.find(Agencia.class, agenciaDevolucao.getId());
            Veiculo veiculo = em.find(Veiculo.class, veiculoSelecionado.getId());
            
            if (cliente == null || cliente.getTipo() != TipoUsuario.CLIENTE || retirada == null || devolucao == null || veiculo == null)
                throw new IllegalStateException("Cliente, agência ou veículo não encontrado.");
            
            if (veiculo.getStatus() != StatusVeiculo.DISPONIVEL)
                throw new IllegalStateException("O veículo não está disponível.");

            Long conflitos = em.createQuery("select count(l) from Locacao l where l.veiculo.id = :veiculoId " +
                    "and l.status in :status and l.dataRetirada < :devolucao and l.dataPrevDevolucao > :retirada", Long.class)
                    .setParameter("veiculoId", veiculo.getId()).setParameter("status", List.of(StatusLocacao.RESERVADA, StatusLocacao.ATIVA))
                    .setParameter("retirada", dataRetirada).setParameter("devolucao", dataDevolucao).getSingleResult();
            
            if (conflitos > 0) 
            	throw new IllegalStateException("O veículo já possui uma locação ou reserva nesse período.");

            Locacao locacao = new Locacao();
            locacao.setCliente(cliente);
            locacao.setAgenciaRetirada(retirada);
            locacao.setAgenciaDevolucao(devolucao);
            locacao.setVeiculo(veiculo);
            locacao.setDataRetirada(dataRetirada);
            locacao.setDataPrevDevolucao(dataDevolucao);
            
            locacao.setDataDevolucao(null);
            locacao.setStatus(StatusLocacao.RESERVADA);
            locacao.setValorTotal(calcularValorTotal());
            
            em.persist(locacao);
            em.getTransaction().commit();
            
            limparOperacao();
            return locacao;
        } 
        finally { 
        	em.close(); 
        }
    }

    @Override
    public double prepararLocacaoAtendimento(Long clienteId, Long agenciaRetiradaId, Long agenciaDevolucaoId, Long veiculoId, LocalDate retirada, LocalDate devolucao) throws Exception {
        try {
            clienteLogado = buscarClienteParaAtendimento(clienteId);
            selecionarAgenciaRetirada(agenciaRetiradaId);
            selecionarAgenciaDevolucao(agenciaDevolucaoId);
            selecionarVeiculo(veiculoId);
            definirPeriodoLocacao(retirada, devolucao);
            return calcularValorTotal();
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            clienteLogado = null;
            limparOperacao();
            throw new Exception(e.getMessage());
        }
    }
    
    /**
     * Valida e confirma a operação solicitada.
     * 
     * @return resultado da operação.
     */
    @Override
    public LocacaoDTO confirmarLocacaoAtendimento() throws Exception {
        try {
            Locacao l = confirmarLocacao();
            return new LocacaoDTO(l.getId(), l.getCliente().getNome(), l.getCliente().getCpf(), l.getVeiculo().getMarca(),
                    l.getVeiculo().getModelo(), l.getVeiculo().getPlaca(), l.getAgenciaRetirada().getNome(),
                    l.getAgenciaDevolucao().getNome(), l.getDataRetirada(), l.getDataPrevDevolucao(), l.getDataDevolucao(),
                    l.getValorTotal(), l.getStatus(), false);
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            throw new Exception(e.getMessage());
        }
    }
    
    /**
     * Executa a operação correspondente aos dados informados.
     */
    @Override
    @Remove
    public void encerrar() {
        clienteLogado = null;
        limparOperacao();
    }
    
    /**
     * Obtém as informações solicitadas.
     * 
     * @return resultado da operação.
     */
    @Override 
    public Agencia obterAgenciaRetirada() { 
    	return agenciaRetirada; 
    }
    
    /**
     * Obtém as informações solicitadas.
     * 
     * @return resultado da operação.
     */
    @Override 
    public Agencia obterAgenciaDevolucao() { 
    	return agenciaDevolucao; 
    }
    
    /**
     * Obtém as informações solicitadas.
     * 
     * @return resultado da operação.
     */
    @Override 
    public Veiculo obterVeiculoSelecionado() { 
    	return veiculoSelecionado; 
    }
    
    /**
     * Obtém as informações solicitadas.
     * 
     * @return resultado da operação.
     */
    @Override 
    public LocalDate obterDataRetirada() { 
    	return dataRetirada; 
    }
    
    /**
     * Obtém as informações solicitadas.
     * 
     * @return resultado da operação.
     */
    @Override 
    public LocalDate obterDataDevolucao() { 
    	return dataDevolucao; 
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param id parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    private Usuario buscarClienteParaAtendimento(Long id) {
        if (id == null)
        	throw new IllegalArgumentException("Selecione o cliente.");

        EntityManager em = JPAUtil.createEntityManager();

        try {
            Usuario cliente = em.find(Usuario.class, id);

            if (cliente == null || cliente.getTipo() != TipoUsuario.CLIENTE)
            	throw new IllegalArgumentException("Cliente não encontrado.");

            return cliente;
        }
        finally {
        	em.close();
        }
    }
    
    /**
     * Executa a operação correspondente aos dados informados.
     * 
     * @param id parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    private Agencia procuraAgencia(Long id) {
        if (id == null) 
        	throw new IllegalArgumentException("Informe uma agência.");
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            Agencia agencia = em.find(Agencia.class, id);
            
            if (agencia == null) 
            	throw new IllegalArgumentException("Agência não encontrada: " + id);
            
            return agencia;
        } 
        finally { 
        	em.close(); 
        }
    }
    
    /**
     * Busca registros que correspondam aos critérios informados.
     * 
     * @param id parâmetro utilizado na operação.
     * @return resultado da operação.
     */
    private Veiculo buscarVeiculo(Long id) {
        if (id == null) 
        	throw new IllegalArgumentException("Informe um veículo.");
        
        EntityManager em = JPAUtil.createEntityManager();
        
        try {
            Veiculo veiculo = em.find(Veiculo.class, id);
            
            if (veiculo == null) 
            	throw new IllegalArgumentException("Veículo não encontrado: " + id);
            
            if (veiculo.getStatus() != StatusVeiculo.DISPONIVEL) 
            	throw new IllegalStateException("O veículo não está disponível.");
            
            return veiculo;
        } 
        finally { 
        	em.close(); 
        }
    }
    
    /**
     * Valida os dados informados.
     */
    private void validarDados() {
        if (clienteLogado == null) 
        	throw new IllegalStateException("Faça login antes de realizar a locação.");
        
        if (agenciaRetirada == null || agenciaDevolucao == null || veiculoSelecionado == null || dataRetirada == null || dataDevolucao == null)
            throw new IllegalStateException("Selecione as agências, o veículo e o período antes de continuar.");
        
        if (veiculoSelecionado.getValorDiaria() == null || veiculoSelecionado.getValorDiaria() < 0)
            throw new IllegalStateException("O valor da diária do veículo é inválido.");
    }
    
    /**
     * Limpa os dados da operação atual.
     */
    private void limparOperacao() {
        agenciaRetirada = null;
        agenciaDevolucao = null;
        veiculoSelecionado = null;
        dataRetirada = null;
        dataDevolucao = null;
    }
}
