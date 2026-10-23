package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.modelo.Locacao;
import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.modelo.enums.StatusLocacao;

public interface ILocacao extends ICrud<Locacao, Long> {

	/** Locações de um cliente. */
	List<Locacao> buscarPorCliente(Long clienteId);

	/** Histórico de locações de um veículo. */
	List<Locacao> buscarPorVeiculo(Long veiculoId);

	List<Locacao> buscarPorStatus(StatusLocacao status);

	List<Locacao> buscarPorAgencia(Long agenciaId);
	
	/** Locações com status ATIVA de um cliente (veículo e agências já carregados). */
	List<Locacao> buscarAtivasPorCliente(Long clienteId);

	/**
	 * Consulta envolvendo Locacao, Veiculo, Usuario e Agencia: locações com o status
	 * informado cujo veículo é da categoria informada, com tudo carregado.
	 */
	List<Locacao> buscarPorCategoriaEStatus(CategoriaVeiculo categoria, StatusLocacao status);
}