package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Veiculo;
import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.modelo.enums.StatusVeiculo;

public interface IVeiculo extends ICrud<Veiculo, Long> {

	/** Veículos de uma agência. */
	List<Veiculo> buscarPorAgencia(Long agenciaId);

	List<Veiculo> buscarPorStatus(StatusVeiculo status);

	List<Veiculo> buscarPorCategoria(CategoriaVeiculo categoria);

	/** Busca pela placa; devolve null se não existir. */
	Veiculo buscarPorPlaca(String placa);

	List<Veiculo> buscarDisponiveis(Long agenciaId, CategoriaVeiculo categoria);
	
	/** Veículos com status DISPONIVEL em uma agência (agência já carregada). */
	List<Veiculo> buscarDisponiveisPorAgencia(Long agenciaId);

	/** Veículos que estão alugados, isto é, com status LOCADO (agência já carregada). */
	List<Veiculo> buscarAlugados();
}