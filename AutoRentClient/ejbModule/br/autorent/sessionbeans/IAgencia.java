package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Agencia;
import br.autorent.modelo.enums.CategoriaVeiculo;

public interface IAgencia extends ICrud<Agencia, Long> {
	/** Agências de uma cidade. */
	List<Agencia> buscarPorCidade(String cidade);

	/** Agências cujo nome contém o texto informado. */
	List<Agencia> buscarPorNome(String nome);
	
	/** Agências que possuem ao menos um veículo DISPONIVEL da categoria informada. */
	List<Agencia> buscarVeiculosDisponiveis(CategoriaVeiculo categoria);
}
