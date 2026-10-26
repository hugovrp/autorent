package br.autorent.managedbeans;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import br.autorent.modelo.Agencia;
import br.autorent.modelo.Locacao;
import br.autorent.modelo.Veiculo;
import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.modelo.enums.StatusLocacao;
import br.autorent.sessionbeans.IAgencia;
import br.autorent.sessionbeans.ILocacao;
import br.autorent.sessionbeans.IStatusFrota;
import br.autorent.sessionbeans.IVeiculo;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/** 
 * Gerencia os dados e as operações do painel administrativo. 
 */
@Named("dashboardMB")
@RequestScoped
public class DashboardMB {
	private long totalVeiculos;
	private long totalDisponiveis;
	private long totalAlugados;
	private long totalEmManutencao;

	private List<Locacao> reservas = new ArrayList<>();
	private List<Locacao> locacoesAtivas = new ArrayList<>();
	private List<Veiculo> veiculosAlugados = new ArrayList<>();
	private List<Agencia> agencias = new ArrayList<>();
	private java.util.Map<Long, Locacao> locacaoAtivaPorVeiculo; // montado sob demanda
	private java.util.Map<Long, Long> disponiveisPorAgencia = new java.util.HashMap<>();

	private String mensagemSucesso;
	private String mensagemErro;

	@Inject
	private SessaoMB sessaoMB;

	@EJB
	private IStatusFrota iStatusFrota;

	@EJB
	private ILocacao iLocacao;

	@EJB
	private IVeiculo iVeiculo;

	@EJB
	private IAgencia iAgencia;

	/** 
	 * Carrega os indicadores da frota e as locações do painel administrativo. 
	 */
	@PostConstruct
	public void carregar() {
		if (!sessaoMB.isAdmin()) 
			return;

		try {
			iStatusFrota.atualizarCache();
			totalVeiculos = iStatusFrota.obterTotalVeiculos();
			totalDisponiveis = iStatusFrota.obterTotalDisponiveis();
			totalAlugados = iStatusFrota.obterTotalAlugados();
			totalEmManutencao = iStatusFrota.obterTotalEmManutencao();
			disponiveisPorAgencia = iStatusFrota.obterDisponiveisPorAgencia();

			agencias = iAgencia.buscarTodos();
			veiculosAlugados = iVeiculo.buscarAlugados();
			reservas = buscar(StatusLocacao.RESERVADA);
			locacoesAtivas = buscar(StatusLocacao.ATIVA);
		} 
		catch (Exception e) {
			mensagemErro = "Não foi possível carregar os dados do painel no momento. Tente novamente em instantes.";
		}
	}

	/** 
	 * Registra a retirada do veículo associado à locação.
	 * 
	 * @param locacaoId identificador da locação.
	 */
	public void registrarRetirada(Long locacaoId) {
		executar(() -> iLocacao.registrarRetirada(locacaoId), "Retirada registrada com sucesso.");
	}

	/** 
	 * Registra a devolução do veículo associado à locação.
	 * 
	 * @param locacaoId identificador da locação.
	 */
	public void registrarDevolucao(Long locacaoId) {
		executar(() -> iLocacao.registrarDevolucao(locacaoId), "Devolução registrada com sucesso.");
	}

	/** 
	 * Cancela uma reserva existente.
	 * 
	 * @param locacaoId identificador da locação a cancelar.
	 */
	public void cancelarReserva(Long locacaoId) {
		executar(() -> iLocacao.cancelar(locacaoId), "Reserva cancelada com sucesso.");
	}

	/** 
	 * Executa uma operação administrativa e atualiza as informações do painel.
	 * 
	 * @param operacao ação a ser executada.
	 * @param sucesso mensagem exibida quando a operação é concluída.
	 */
	private void executar(Runnable operacao, String sucesso) {
		mensagemSucesso = null;
		mensagemErro = null;

		if (!sessaoMB.isAdmin()) {
			mensagemErro = "Operação permitida somente para administradores.";
			return;
		}

		try {
			operacao.run();
			mensagemSucesso = sucesso;
		} 
		catch (Exception e) {
			mensagemErro = mensagem(e);
		}
		
		reservas = new ArrayList<>();
		locacoesAtivas = new ArrayList<>();
		locacaoAtivaPorVeiculo = null;
		String erroOperacao = mensagemErro;
		carregar();
		
		if (erroOperacao != null) 
			mensagemErro = erroOperacao;
	}

	/** 
	 * Obtém uma mensagem adequada para apresentar em caso de falha.
	 * 
	 * @param erro exceção ocorrida durante a operação.
	 * @return mensagem de erro compreensível para o usuário.
	 */
	private String mensagem(Throwable erro) {
		for (Throwable t = erro; t != null; t = t.getCause()) {
			if ((t instanceof IllegalArgumentException || t instanceof IllegalStateException) && t.getMessage() != null) {
				return t.getMessage();
			}
		}
		return "Não foi possível concluir a operação no momento. Tente novamente em instantes.";
	}

	/** 
	 * Busca e ordena as locações de todas as categorias com o status informado.
	 * 
	 * @param status situação das locações desejadas.
	 * @return lista de locações ordenada pela data de retirada.
	 */
	private List<Locacao> buscar(StatusLocacao status) {
		List<Locacao> lista = new ArrayList<>();
		for (CategoriaVeiculo categoria : CategoriaVeiculo.values()) {
			lista.addAll(iLocacao.buscarPorCategoriaEStatus(categoria, status));
		}
		lista.sort(Comparator.comparing(Locacao::getDataRetirada));
		return lista;
	}
	
	public java.util.Map<Long, Locacao> getLocacaoAtivaPorVeiculo() {
		if (locacaoAtivaPorVeiculo == null) {
			locacaoAtivaPorVeiculo = new java.util.HashMap<>();
			for (Locacao l : locacoesAtivas) {
				locacaoAtivaPorVeiculo.put(l.getVeiculo().getId(), l);
			}
		}
		return locacaoAtivaPorVeiculo;
	}

	/** 
	 * Retorna a quantidade de veículos disponíveis em uma agência.
	 * 
	 * @param agencia agência consultada.
	 * @return quantidade de veículos disponíveis, ou zero se não houver registro.
	 */
	public long disponiveisDa(Agencia agencia) {
		Long total = disponiveisPorAgencia.get(agencia.getId());
		return total == null ? 0 : total;
	}

	public LocalDate getHoje() {
		return LocalDate.now();
	}

	public long getTotalVeiculos() {
		return totalVeiculos;
	}

	public long getTotalDisponiveis() {
		return totalDisponiveis;
	}

	public long getTotalAlugados() {
		return totalAlugados;
	}

	public long getTotalEmManutencao() {
		return totalEmManutencao;
	}

	public int getTotalReservas() {
		return reservas.size();
	}

	public int getTotalLocacoesAtivas() {
		return locacoesAtivas.size();
	}

	public List<Locacao> getReservas() {
		return reservas;
	}

	public List<Locacao> getLocacoesAtivas() {
		return locacoesAtivas;
	}

	public List<Veiculo> getVeiculosAlugados() {
		return veiculosAlugados;
	}

	public List<Agencia> getAgencias() {
		return agencias;
	}

	public String getMensagemSucesso() {
		return mensagemSucesso;
	}

	public String getMensagemErro() {
		return mensagemErro;
	}
}
