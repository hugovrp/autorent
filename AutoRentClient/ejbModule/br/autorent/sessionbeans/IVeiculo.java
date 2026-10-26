package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.dto.VeiculoDTO;
import br.autorent.modelo.Veiculo;
import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.modelo.enums.StatusVeiculo;

/**
 * Define o contrato de operações de veiculo disponibilizadas pelo backend.
 */
public interface IVeiculo extends ICrud<Veiculo, Long> {

	/**
	 * Busca locações relacionadas a uma agência.
	 * 
	 * @param agenciaId valor utilizado para agenciaId.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Veiculo> buscarPorAgencia(Long agenciaId);

	/**
	 * Busca locações pelo status.
	 * 
	 * @param status valor utilizado para status.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Veiculo> buscarPorStatus(StatusVeiculo status);

	/**
	 * Busca veículos pela categoria.
	 * 
	 * @param categoria valor utilizado para categoria.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Veiculo> buscarPorCategoria(CategoriaVeiculo categoria);

	/**
	 * Busca um veículo pela placa.
	 * 
	 * @param placa valor utilizado para placa.
	 * @return objeto ou resultado correspondente à operação.
	 */
	Veiculo buscarPorPlaca(String placa);

	/**
	 * Busca veículos disponíveis conforme os filtros informados.
	 * 
	 * @param agenciaId valor utilizado para agenciaId.
	 * @param categoria valor utilizado para categoria.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Veiculo> buscarDisponiveis(Long agenciaId, CategoriaVeiculo categoria);

	/**
	 * Busca veículos disponíveis em uma agência.
	 * 
	 * @param agenciaId valor utilizado para agenciaId.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Veiculo> buscarDisponiveisPorAgencia(Long agenciaId);

	/**
	 * Lista os veículos atualmente alugados.
	 * 
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<Veiculo> buscarAlugados();

	/**
	 * Valida e cadastra um veículo na frota.
	 * 
	 * @param placa valor utilizado para placa.
	 * @param marca valor utilizado para marca.
	 * @param modelo valor utilizado para modelo.
	 * @param ano valor utilizado para ano.
	 * @param categoria valor utilizado para categoria.
	 * @param valorDiaria valor utilizado para valorDiaria.
	 * @param agenciaId valor utilizado para agenciaId.
	 * @return objeto ou resultado correspondente à operação.
	 */
	VeiculoDTO cadastrarVeiculo(String placa, String marca, String modelo, Integer ano, CategoriaVeiculo categoria, Double valorDiaria, Long agenciaId) throws Exception;
}