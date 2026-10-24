package br.autorent.modelo;

import java.io.Serializable;
import java.time.LocalDate;

import br.autorent.modelo.enums.StatusLocacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;

@Entity
public class Locacao implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@SequenceGenerator(
			name = "locacao_id", 
			sequenceName="locacao_seq",
			allocationSize=1
	)	
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="locacao_id")
	private Long id;
	
	// N:1 - um cliente realiza várias locações
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cliente_id", nullable = false)
	private Usuario cliente;
	
	// N:1 - cada locação está associada a um veículo
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "veiculo_id", nullable = false)
	private Veiculo veiculo;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "agencia_retirada_id", nullable = false)
	private Agencia agenciaRetirada;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "agencia_devolucao_id", nullable = false)
	private Agencia agenciaDevolucao;
	
	@Column(name = "data_retirada", nullable = false)
	private LocalDate dataRetirada;
	
	@Column(name = "data_prev_devolucao", nullable = false)
	private LocalDate dataPrevDevolucao;
	
	// Preenchida somente quando o veículo é efetivamente devolvido
	@Column(name = "data_devolucao")
	private LocalDate dataDevolucao;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusLocacao status = StatusLocacao.RESERVADA;
	
	@Column(name = "valor_total")
	private Double valorTotal;
	
	
	public Locacao() { }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Usuario getCliente() {
		return cliente;
	}

	public void setCliente(Usuario cliente) {
		this.cliente = cliente;
	}

	public Veiculo getVeiculo() {
		return veiculo;
	}

	public void setVeiculo(Veiculo veiculo) {
		this.veiculo = veiculo;
	}

	public Agencia getAgenciaRetirada() {
		return agenciaRetirada;
	}

	public void setAgenciaRetirada(Agencia agenciaRetirada) {
		this.agenciaRetirada = agenciaRetirada;
	}

	public Agencia getAgenciaDevolucao() {
		return agenciaDevolucao;
	}

	public void setAgenciaDevolucao(Agencia agenciaDevolucao) {
		this.agenciaDevolucao = agenciaDevolucao;
	}

	public LocalDate getDataRetirada() {
		return dataRetirada;
	}

	public void setDataRetirada(LocalDate dataRetirada) {
		this.dataRetirada = dataRetirada;
	}

	public LocalDate getDataPrevDevolucao() {
		return dataPrevDevolucao;
	}

	public void setDataPrevDevolucao(LocalDate dataPrevDevolucao) {
		this.dataPrevDevolucao = dataPrevDevolucao;
	}

	public LocalDate getDataDevolucao() {
		return dataDevolucao;
	}

	public void setDataDevolucao(LocalDate dataDevolucao) {
		this.dataDevolucao = dataDevolucao;
	}

	public StatusLocacao getStatus() {
		return status;
	}

	public void setStatus(StatusLocacao status) {
		this.status = status;
	}

	public Double getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(Double valorTotal) {
		this.valorTotal = valorTotal;
	}
}