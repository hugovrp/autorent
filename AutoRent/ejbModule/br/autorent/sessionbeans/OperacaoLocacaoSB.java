package br.autorent.sessionbeans;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import br.autorent.modelo.Agencia;
import br.autorent.modelo.Locacao;
import br.autorent.modelo.Usuario;
import br.autorent.modelo.Veiculo;
import br.autorent.modelo.enums.StatusLocacao;
import br.autorent.modelo.enums.StatusVeiculo;
import br.autorent.modelo.enums.TipoUsuario;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;

@Stateful
@Remote(IOperacaoLocacao.class)
public class OperacaoLocacaoSB implements IOperacaoLocacao {
    private Usuario clienteLogado;
    private Agencia agenciaRetirada;
    private Agencia agenciaDevolucao;
    private Veiculo veiculoSelecionado;
    private LocalDate dataRetirada;
    private LocalDate dataDevolucao;

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
            if (usuario.getTipo() != TipoUsuario.CLIENTE || usuario.getSenhaHash() == null || !usuario.getSenhaHash().equals(senha)) 
            	return false;
            
            clienteLogado = usuario;
            return true;
        } 
        finally { 
        	em.close(); 
        }
    }

    @Override 
    public Usuario getLoggedClient() { 
    	return clienteLogado; 
    }

    @Override 
    public void selectPickupAgency(Long agenciaId) { 
    	agenciaRetirada = findAgency(agenciaId); 
    }
    
    @Override 
    public void selectReturnAgency(Long agenciaId) { 
    	agenciaDevolucao = findAgency(agenciaId); 
    }
    
    @Override public void selectVehicle(Long veiculoId) { 
    	veiculoSelecionado = findVehicle(veiculoId); 
    }

    @Override
    public void setRentalPeriod(LocalDate retirada, LocalDate devolucao) {
        if (retirada == null || devolucao == null || devolucao.isBefore(retirada) || devolucao.equals(retirada))
            throw new IllegalArgumentException("O período deve possuir datas válidas e a devolução deve ser posterior à retirada.");
        
        if (retirada.isBefore(LocalDate.now())) 
        	throw new IllegalArgumentException("A data de retirada não pode estar no passado.");
        
        dataRetirada = retirada;
        dataDevolucao = devolucao;
    }

    @Override
    public double calculateTotalValue() {
    	validateData();
        long diarias = ChronoUnit.DAYS.between(dataRetirada, dataDevolucao);
        return diarias * veiculoSelecionado.getValorDiaria();
    }

    @Override
    public Locacao confirmRental() {
    	validateData();
        
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
            
            // Ainda não houve devolução
            locacao.setDataDevolucao(null);
            locacao.setStatus(StatusLocacao.RESERVADA);
            locacao.setValorTotal(calculateTotalValue());
            
            em.persist(locacao);
            em.getTransaction().commit();
            
            clearOperation();
            return locacao;
        } 
        finally { 
        	em.close(); 
        }
    }

    @Override 
    public Agencia getPickupAgency() { 
    	return agenciaRetirada; 
    }
    
    @Override 
    public Agencia getReturnAgency() { 
    	return agenciaDevolucao; 
    }
    
    @Override 
    public Veiculo getSelectedVehicle() { 
    	return veiculoSelecionado; 
    }
    
    @Override 
    public LocalDate getPickupDate() { 
    	return dataRetirada; 
    }
    
    @Override 
    public LocalDate getReturnDate() { 
    	return dataDevolucao; 
    }

    private Agencia findAgency(Long id) {
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

    private Veiculo findVehicle(Long id) {
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

    private void validateData() {
        if (clienteLogado == null) 
        	throw new IllegalStateException("Faça login antes de realizar a locação.");
        
        if (agenciaRetirada == null || agenciaDevolucao == null || veiculoSelecionado == null || dataRetirada == null || dataDevolucao == null)
            throw new IllegalStateException("Selecione as agências, o veículo e o período antes de continuar.");
        
        if (veiculoSelecionado.getValorDiaria() == null || veiculoSelecionado.getValorDiaria() < 0)
            throw new IllegalStateException("O valor da diária do veículo é inválido.");
    }

    private void clearOperation() {
        agenciaRetirada = null;
        agenciaDevolucao = null;
        veiculoSelecionado = null;
        dataRetirada = null;
        dataDevolucao = null;
    }
}

