package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Veiculo;
import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.modelo.enums.StatusVeiculo;

public interface IVeiculo extends ICrud<Veiculo, Long> {

	/** Veículos de uma agência. */
	List<Veiculo> findByAgencia(Long agenciaId);

	List<Veiculo> findByStatus(StatusVeiculo status);

	List<Veiculo> findByCategoria(CategoriaVeiculo categoria);

	/** Busca pela placa; devolve null se não existir. */
	Veiculo findByPlaca(String placa);

	List<Veiculo> findDisponiveis(Long agenciaId, CategoriaVeiculo categoria);
}