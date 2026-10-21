package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Locacao;
import br.autorent.modelo.enums.StatusLocacao;

public interface ILocacao extends ICrud<Locacao, Long> {

	/** Locações de um cliente. */
	List<Locacao> findByCliente(Long clienteId);

	/** Histórico de locações de um veículo. */
	List<Locacao> findByVeiculo(Long veiculoId);

	List<Locacao> findByStatus(StatusLocacao status);

	List<Locacao> findByAgencia(Long agenciaId);
}