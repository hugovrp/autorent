package br.autorent.sessionbeans;

import java.util.List;

import br.autorent.dto.AgenciaDTO;
import br.autorent.dto.LocacaoDTO;
import br.autorent.dto.VeiculoDTO;
import br.autorent.modelo.enums.StatusLocacao;
import br.autorent.modelo.enums.StatusVeiculo;

/**
 * Define o contrato de operações de consultafrota disponibilizadas pelo backend.
 */
public interface IConsultaFrota {

	/**
	 * Lista as agências cadastradas.
	 * 
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<AgenciaDTO> listarAgencias();

	/**
	 * Consulta os veículos da frota conforme os filtros informados.
	 * 
	 * @param agenciaId valor utilizado para agenciaId.
	 * @param status valor utilizado para status.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<VeiculoDTO> consultarFrota(Long agenciaId, StatusVeiculo status);

	/**
	 * Consulta as locações conforme os filtros informados.
	 * 
	 * @param status valor utilizado para status.
	 * @param agenciaId valor utilizado para agenciaId.
	 * @return lista de resultados correspondentes aos critérios informados.
	 */
	List<LocacaoDTO> consultarLocacoes(StatusLocacao status, Long agenciaId);
}