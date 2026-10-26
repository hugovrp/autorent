package br.autorent.sessionbeans;

import java.time.LocalDate;

import br.autorent.dto.LocacaoDTO;
import br.autorent.modelo.Agencia;
import br.autorent.modelo.Locacao;
import br.autorent.modelo.Usuario;
import br.autorent.modelo.Veiculo;

/**
 * Define o contrato de operações de operacaolocacao disponibilizadas pelo backend.
 */
public interface IOperacaoLocacao {

	/**
	 * Autentica o usuário com as credenciais informadas.
	 * 
	 * @param cpf valor utilizado para cpf.
	 * @param password valor utilizado para password.
	 * @return true se a operação for bem-sucedida; caso contrário, false.
	 */
	boolean login(String cpf, String password);

	/**
	 * Retorna o cliente atualmente autenticado.
	 * 
	 * @return objeto ou resultado correspondente à operação.
	 */
	Usuario obterClienteLogado();

	/**
	 * Seleciona a agência de retirada do veículo.
	 * 
	 * @param agencyId valor utilizado para agencyId.
	 */
	void selecionarAgenciaRetirada(Long agencyId);

	/**
	 * Seleciona a agência de devolução do veículo.
	 * 
	 * @param agencyId valor utilizado para agencyId.
	 */
	void selecionarAgenciaDevolucao(Long agencyId);

	/**
	 * Seleciona o veículo para a locação.
	 * 
	 * @param vehicleId valor utilizado para vehicleId.
	 */
	void selecionarVeiculo(Long vehicleId);

	/**
	 * Define o período da locação.
	 * 
	 * @param pickupDate valor utilizado para pickupDate.
	 * @param returnDate valor utilizado para returnDate.
	 */
	void definirPeriodoLocacao(LocalDate pickupDate, LocalDate returnDate);

	/**
	 * Calcula o valor total da locação.
	 * 
	 * @return valor calculado ou quantidade solicitada.
	 */
	double calcularValorTotal();

	/**
	 * Confirma a locação preparada.
	 * 
	 * @return objeto ou resultado correspondente à operação.
	 */
	Locacao confirmarLocacao();

	/**
	 * Retorna a agência selecionada para retirada.
	 * 
	 * @return objeto ou resultado correspondente à operação.
	 */
	Agencia obterAgenciaRetirada();

	/**
	 * Retorna a agência selecionada para devolução.
	 * 
	 * @return objeto ou resultado correspondente à operação.
	 */
	Agencia obterAgenciaDevolucao();

	/**
	 * Retorna o veículo selecionado para a locação.
	 * 
	 * @return objeto ou resultado correspondente à operação.
	 */
	Veiculo obterVeiculoSelecionado();

	/**
	 * Retorna a data de retirada definida para a locação.
	 * 
	 * @return objeto ou resultado correspondente à operação.
	 */
	LocalDate obterDataRetirada();

	/**
	 * Retorna a data de devolução definida para a locação.
	 * 
	 * @return objeto ou resultado correspondente à operação.
	 */
	LocalDate obterDataDevolucao();

	/**
	 * Prepara uma locação para atendimento.
	 * 
	 * @param clienteId valor utilizado para clienteId.
	 * @param agenciaRetiradaId valor utilizado para agenciaRetiradaId.
	 * @param agenciaDevolucaoId valor utilizado para agenciaDevolucaoId.
	 * @param veiculoId valor utilizado para veiculoId.
	 * @param retirada valor utilizado para retirada.
	 * @param devolucao valor utilizado para devolucao.
	 * @return valor calculado ou quantidade solicitada.
	 */
	double prepararLocacaoAtendimento(Long clienteId, Long agenciaRetiradaId, Long agenciaDevolucaoId, Long veiculoId,
			LocalDate retirada, LocalDate devolucao) throws Exception;

	/**
	 * Confirma a locação preparada para atendimento.
	 * 
	 * @return objeto ou resultado correspondente à operação.
	 */
	LocacaoDTO confirmarLocacaoAtendimento() throws Exception;

	/**
	 * Encerra a sessão de operação.
	 */
	void encerrar();
}
