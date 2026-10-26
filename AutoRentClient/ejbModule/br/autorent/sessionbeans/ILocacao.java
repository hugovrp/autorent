package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.dto.LocacaoDTO;
import br.autorent.modelo.Locacao;
import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.modelo.enums.StatusLocacao;

/**
 * Define o contrato de operações de locacao disponibilizadas pelo backend.
 */
public interface ILocacao extends ICrud<Locacao, Long> {

	/**
	 * Busca as locações de um cliente.
	 * 
	 * @param clienteId valor utilizado para clienteId.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Locacao> buscarPorCliente(Long clienteId);

	/**
	 * Busca as locações de um cliente com os dados relacionados carregados.
	 * 
	 * @param clienteId valor utilizado para clienteId.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Locacao> buscarPorClienteCompleto(Long clienteId);

	/**
	 * Registra a retirada do veículo.
	 * 
	 * @param locacaoId valor utilizado para locacaoId.
	 */
	void registrarRetirada(Long locacaoId);

	/**
	 * Registra a devolução do veículo.
	 * 
	 * @param locacaoId valor utilizado para locacaoId.
	 */
	void registrarDevolucao(Long locacaoId);

	/**
	 * Registra a retirada do veículo durante o atendimento.
	 * 
	 * @param locacaoId valor utilizado para locacaoId.
	 * @return objeto ou resultado correspondente à operação.
	 */
	LocacaoDTO registrarRetiradaAtendimento(Long locacaoId) throws Exception;

	/**
	 * Registra a devolução do veículo durante o atendimento.
	 * 
	 * @param locacaoId valor utilizado para locacaoId.
	 * @return objeto ou resultado correspondente à operação.
	 */
	LocacaoDTO registrarDevolucaoAtendimento(Long locacaoId) throws Exception;

	/**
	 * Cancela uma reserva.
	 * 
	 * @param locacaoId valor utilizado para locacaoId.
	 */
	void cancelar(Long locacaoId);

	/**
	 * Busca as locações associadas a um veículo.
	 * 
	 * @param veiculoId valor utilizado para veiculoId.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Locacao> buscarPorVeiculo(Long veiculoId);

	/**
	 * Busca locações pelo status.
	 * 
	 * @param status valor utilizado para status.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Locacao> buscarPorStatus(StatusLocacao status);

	/**
	 * Busca locações relacionadas a uma agência.
	 * 
	 * @param agenciaId valor utilizado para agenciaId.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Locacao> buscarPorAgencia(Long agenciaId);

	/**
	 * Busca registros conforme os critérios informados.
	 * 
	 * @param clienteId valor utilizado para clienteId.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Locacao> buscarAtivasPorCliente(Long clienteId);

	/**
	 * Busca registros conforme os critérios informados.
	 * 
	 * @param categoria valor utilizado para categoria.
	 * @param status valor utilizado para status.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Locacao> buscarPorCategoriaEStatus(CategoriaVeiculo categoria, StatusLocacao status);
}
