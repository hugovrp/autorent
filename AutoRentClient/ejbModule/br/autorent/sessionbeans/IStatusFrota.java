package br.autorent.sessionbeans;

import java.util.Map;

public interface IStatusFrota {
	long obterTotalVeiculos();
	long obterTotalDisponiveis();
	long obterTotalAlugados();
	long obterTotalReservados();
	long obterTotalEmManutencao();
	Map<Long, Long> obterDisponiveisPorAgencia();
	void atualizarCache();
}
