package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Acessorio;

/**
 * Define o contrato de operações de acessorio disponibilizadas pelo backend.
 */
public interface IAcessorio extends ICrud<Acessorio, Long> {
	/**
	 * Busca acessórios pela descrição.
	 * 
	 * @param descricao valor utilizado para descricao.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Acessorio> buscarPorDescricao(String descricao);

	/**
	 * Busca as locações associadas a um veículo.
	 * 
	 * @param veiculoId valor utilizado para veiculoId.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Acessorio> buscarPorVeiculo(Long veiculoId);
}
