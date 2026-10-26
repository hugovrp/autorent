package br.autorent.dto;

import java.io.Serializable;
import java.time.LocalDate;

import br.autorent.modelo.enums.StatusLocacao;

/**
 * Objeto de transferência de dados (LocacaoDTO) utilizado pela aplicação cliente.
 */
public class LocacaoDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Long id;
	private final String clienteNome;
	private final String clienteCpf;
	private final String veiculoMarca;
	private final String veiculoModelo;
	private final String veiculoPlaca;
	private final String agenciaRetiradaNome;
	private final String agenciaDevolucaoNome;
	private final LocalDate dataRetirada;
	private final LocalDate dataPrevDevolucao;
	private final LocalDate dataDevolucao;
	private final Double valorTotal;
	private final StatusLocacao status;
	private final boolean atrasada;

	public LocacaoDTO(Long id, String clienteNome, String clienteCpf, String veiculoMarca, String veiculoModelo,
			String veiculoPlaca, String agenciaRetiradaNome, String agenciaDevolucaoNome, LocalDate dataRetirada,
			LocalDate dataPrevDevolucao, LocalDate dataDevolucao, Double valorTotal, StatusLocacao status, boolean atrasada) {
		this.id = id;
		this.clienteNome = clienteNome;
		this.clienteCpf = clienteCpf;
		this.veiculoMarca = veiculoMarca;
		this.veiculoModelo = veiculoModelo;
		this.veiculoPlaca = veiculoPlaca;
		this.agenciaRetiradaNome = agenciaRetiradaNome;
		this.agenciaDevolucaoNome = agenciaDevolucaoNome;
		this.dataRetirada = dataRetirada;
		this.dataPrevDevolucao = dataPrevDevolucao;
		this.dataDevolucao = dataDevolucao;
		this.valorTotal = valorTotal;
		this.status = status;
		this.atrasada = atrasada;
	}

	public Long getId() {
		return id;
	}

	public String getClienteNome() {
		return clienteNome;
	}

	public String getClienteCpf() {
		return clienteCpf;
	}

	public String getVeiculoMarca() {
		return veiculoMarca;
	}

	public String getVeiculoModelo() {
		return veiculoModelo;
	}

	public String getVeiculoPlaca() {
		return veiculoPlaca;
	}

	public String getAgenciaRetiradaNome() {
		return agenciaRetiradaNome;
	}

	public String getAgenciaDevolucaoNome() {
		return agenciaDevolucaoNome;
	}

	public LocalDate getDataRetirada() {
		return dataRetirada;
	}

	public LocalDate getDataPrevDevolucao() {
		return dataPrevDevolucao;
	}

	public LocalDate getDataDevolucao() {
		return dataDevolucao;
	}

	public Double getValorTotal() {
		return valorTotal;
	}

	public StatusLocacao getStatus() {
		return status;
	}

	public boolean isAtrasada() {
		return atrasada;
	}
}