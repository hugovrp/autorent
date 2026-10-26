package br.autorent.managedbeans;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.autorent.modelo.Agencia;
import br.autorent.modelo.Locacao;
import br.autorent.modelo.Veiculo;
import br.autorent.sessionbeans.IAgencia;
import br.autorent.sessionbeans.IOperacaoLocacao;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/** 
 * Gerencia o processo de criação de reservas de veículos. 
 */
@Named("reservaMB")
@RequestScoped
public class ReservaMB {
	private Long agenciaRetiradaId;
	private Long agenciaDevolucaoId;
	private LocalDate dataRetirada;
	private LocalDate dataDevolucao;
	private String senha;

	private List<Agencia> agencias = new ArrayList<>();
	private Locacao reserva;
	private String mensagemSucesso;
	private String mensagemErro;

	@EJB
	private IOperacaoLocacao iOperacaoLocacao;

	@EJB
	private IAgencia iAgencia;

	@Inject
	private SessaoMB sessaoMB;

	@Inject
	private VeiculoMB veiculoMB;

	/** 
	 * Carrega as agências e define valores iniciais para a reserva. 
	 */
	@PostConstruct
	public void iniciar() {
		try {
			agencias = iAgencia.buscarTodos();
		} catch (Exception e) {
			mensagemErro = "Não foi possível carregar as agências no momento.";
		}

		// Usa inicialmente a agência do veículo, permitindo alteração pelo cliente.
		Veiculo veiculo = veiculoMB.getVeiculoSelecionado();
		if (veiculo != null && veiculo.getAgencia() != null) {
			agenciaRetiradaId = veiculo.getAgencia().getId();
			agenciaDevolucaoId = veiculo.getAgencia().getId();
		}
	}

	/** 
	 * Valida os dados e solicita a criação da reserva ao serviço de locação.
	 * 
	 * @return caminho de navegação após a operação, ou {@code null} em caso de falha.
	 */
	public String reservar() {
		mensagemSucesso = null;
		mensagemErro = null;

		if (!sessaoMB.isCliente()) {
			mensagemErro = "Somente clientes podem realizar reservas.";
			return null;
		}

		Veiculo veiculo = veiculoMB.getVeiculoSelecionado();
		if (veiculo == null) {
			mensagemErro = "Nenhum veículo selecionado. Escolha um veículo na consulta.";
			return null;
		}

		try {
			if (!iOperacaoLocacao.login(sessaoMB.getUsuario().getCpf(), senha)) {
				mensagemErro = "Senha incorreta. Confirme sua senha para finalizar a reserva.";
				return null;
			}

			iOperacaoLocacao.selecionarVeiculo(veiculo.getId());
			iOperacaoLocacao.selecionarAgenciaRetirada(agenciaRetiradaId);
			iOperacaoLocacao.selecionarAgenciaDevolucao(agenciaDevolucaoId);
			iOperacaoLocacao.definirPeriodoLocacao(dataRetirada, dataDevolucao);

			reserva = iOperacaoLocacao.confirmarLocacao();
			mensagemSucesso = "Reserva realizada com sucesso!";
		} 
		catch (Exception e) {
			mensagemErro = mensagem(e);
		}
		senha = null;
		return null;
	}

	/** 
	 * Extrai uma mensagem de validação ou retorna uma mensagem genérica.
	 * 
	 * @param erro exceção ocorrida durante a reserva.
	 * @return mensagem adequada para exibição ao usuário.
	 */
	private String mensagem(Throwable erro) {
		for (Throwable t = erro; t != null; t = t.getCause()) {
			if ((t instanceof IllegalArgumentException || t instanceof IllegalStateException) && t.getMessage() != null) {
				return t.getMessage();
			}
		}
		return "Não foi possível realizar a reserva no momento. Tente novamente em instantes.";
	}

	public Long getAgenciaRetiradaId() {
		return agenciaRetiradaId;
	}

	public void setAgenciaRetiradaId(Long agenciaRetiradaId) {
		this.agenciaRetiradaId = agenciaRetiradaId;
	}

	public Long getAgenciaDevolucaoId() {
		return agenciaDevolucaoId;
	}

	public void setAgenciaDevolucaoId(Long agenciaDevolucaoId) {
		this.agenciaDevolucaoId = agenciaDevolucaoId;
	}

	public LocalDate getDataRetirada() {
		return dataRetirada;
	}

	public void setDataRetirada(LocalDate dataRetirada) {
		this.dataRetirada = dataRetirada;
	}

	public LocalDate getDataDevolucao() {
		return dataDevolucao;
	}

	public void setDataDevolucao(LocalDate dataDevolucao) {
		this.dataDevolucao = dataDevolucao;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public List<Agencia> getAgencias() {
		return agencias;
	}

	public Locacao getReserva() {
		return reserva;
	}

	public String getMensagemSucesso() {
		return mensagemSucesso;
	}

	public String getMensagemErro() {
		return mensagemErro;
	}
}

