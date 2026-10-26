package br.autorent.sessionbeans;

import java.util.Map;
/**
 * Define o contrato de operações de statusfrota disponibilizadas pelo backend.
 */
public interface IStatusFrota {
	/**
	 * Retorna a quantidade total de veículos.
	 * 
	 * @return valor calculado ou quantidade solicitada.
	 */
	long obterTotalVeiculos();
	
	/**
	 * Retorna a quantidade de veículos disponíveis.
	 * 
	 * @return valor calculado ou quantidade solicitada.
	 */
	long obterTotalDisponiveis();
	
	/**
	 * Retorna a quantidade de veículos alugados.
	 * 
	 * @return valor calculado ou quantidade solicitada.
	 */
	long obterTotalAlugados();
	
	/**
	 * Retorna a quantidade de veículos reservados.
	 * 
	 * @return valor calculado ou quantidade solicitada.
	 */
	long obterTotalReservados();
	
	/**
	 * Retorna a quantidade de veículos em manutenção.
	 * 
	 * @return valor calculado ou quantidade solicitada.
	 */
	long obterTotalEmManutencao();
	
	/**
	 * Retorna a quantidade de veículos disponíveis por agência.
	 * 
	 * @return mapa com os dados solicitados.
	 */
	Map<Long, Long> obterDisponiveisPorAgencia();
	
	/**
	 * Atualiza o cache dos indicadores da frota.
	 */
	void atualizarCache();
}