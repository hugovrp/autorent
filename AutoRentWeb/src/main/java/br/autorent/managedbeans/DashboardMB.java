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
 * Dashboard administrativo.
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

	public void registrarRetirada(Long locacaoId) {
		executar(() -> iLocacao.registrarRetirada(locacaoId), "Retirada registrada com sucesso.");
	}

	public void registrarDevolucao(Long locacaoId) {
		executar(() -> iLocacao.registrarDevolucao(locacaoId), "Devolução registrada com sucesso.");
	}

	public void cancelarReserva(Long locacaoId) {
		executar(() -> iLocacao.cancelar(locacaoId), "Reserva cancelada com sucesso.");
	}

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
		String erroOperacao = mensagemErro;
		carregar();
		
		if (erroOperacao != null) 
			mensagemErro = erroOperacao;
	}

	private String mensagem(Throwable erro) {
		for (Throwable t = erro; t != null; t = t.getCause()) {
			if ((t instanceof IllegalArgumentException || t instanceof IllegalStateException) && t.getMessage() != null) {
				return t.getMessage();
			}
		}
		return "Não foi possível concluir a operação no momento. Tente novamente em instantes.";
	}

	/** Reúne as locações do status informado de todas as categorias, da retirada mais próxima à mais distante. */
	private List<Locacao> buscar(StatusLocacao status) {
		List<Locacao> lista = new ArrayList<>();
		for (CategoriaVeiculo categoria : CategoriaVeiculo.values()) {
			lista.addAll(iLocacao.buscarPorCategoriaEStatus(categoria, status));
		}
		lista.sort(Comparator.comparing(Locacao::getDataRetirada));
		return lista;
	}

	/** Locação ativa do veículo alugado (para mostrar cliente e data de devolução), ou null. */
	public Locacao locacaoAtivaDo(Veiculo veiculo) {
		for (Locacao l : locacoesAtivas) {
			if (l.getVeiculo().getId().equals(veiculo.getId())) {
				return l;
			}
		}
		return null;
	}

	public long disponiveisDa(Agencia agencia) {
		Long total = disponiveisPorAgencia.get(agencia.getId());
		return total == null ? 0 : total;
	}

	public boolean isAtrasada(Locacao locacao) {
		return locacao.getDataPrevDevolucao().isBefore(LocalDate.now());
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
