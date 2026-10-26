package br.autorent.dto;

import java.io.Serializable;
import java.time.LocalDate;

import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.modelo.enums.StatusVeiculo;

/**
 * Objeto de transferência de dados (VeiculoDTO) utilizado pela aplicação cliente.
 */
public class VeiculoDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Long id;
	private final String placa;
	private final String marca;
	private final String modelo;
	private final Integer ano;
	private final CategoriaVeiculo categoria;
	private final Double valorDiaria;
	private final StatusVeiculo status;
	private final Long agenciaId;
	private final String agenciaNome;
	private final boolean disponivelParaLocacao;
	private final LocalDate devolucaoPrevista;

	public VeiculoDTO(Long id, String placa, String marca, String modelo, Integer ano, CategoriaVeiculo categoria,
			Double valorDiaria, StatusVeiculo status, Long agenciaId, String agenciaNome,
			boolean disponivelParaLocacao, LocalDate devolucaoPrevista) {
		this.id = id;
		this.placa = placa;
		this.marca = marca;
		this.modelo = modelo;
		this.ano = ano;
		this.categoria = categoria;
		this.valorDiaria = valorDiaria;
		this.status = status;
		this.agenciaId = agenciaId;
		this.agenciaNome = agenciaNome;
		this.disponivelParaLocacao = disponivelParaLocacao;
		this.devolucaoPrevista = devolucaoPrevista;
	}

	public Long getId() {
		return id;
	}

	public String getPlaca() {
		return placa;
	}

	public String getMarca() {
		return marca;
	}

	public String getModelo() {
		return modelo;
	}

	public Integer getAno() {
		return ano;
	}

	public CategoriaVeiculo getCategoria() {
		return categoria;
	}

	public Double getValorDiaria() {
		return valorDiaria;
	}

	public StatusVeiculo getStatus() {
		return status;
	}

	public Long getAgenciaId() {
		return agenciaId;
	}

	public String getAgenciaNome() {
		return agenciaNome;
	}

	public boolean isDisponivelParaLocacao() {
		return disponivelParaLocacao;
	}

	public LocalDate getDevolucaoPrevista() {
		return devolucaoPrevista;
	}
}