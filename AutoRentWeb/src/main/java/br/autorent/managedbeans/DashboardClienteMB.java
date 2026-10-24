package br.autorent.managedbeans;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import br.autorent.modelo.Locacao;
import br.autorent.modelo.enums.StatusLocacao;
import br.autorent.sessionbeans.ILocacao;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Dashboard do cliente autenticado. O id do cliente vem sempre da sessão (SessaoMB).
 */
@Named("dashboardClienteMB")
@RequestScoped
public class DashboardClienteMB {
	private List<Locacao> reservas = new ArrayList<>();
	private List<Locacao> locacoesAtivas = new ArrayList<>();
	private int totalFinalizadas;
	private int totalGeral;
	private String mensagemErro;

	@EJB
	private ILocacao iLocacao;

	@Inject
	private SessaoMB sessaoMB;

	@PostConstruct
	public void carregar() {
		if (!sessaoMB.isCliente()) 
			return;

		try {
			List<Locacao> todas = iLocacao.buscarPorClienteCompleto(sessaoMB.getUsuario().getId());
			totalGeral = todas.size();

			for (Locacao l : todas) {
				if (l.getStatus() == StatusLocacao.RESERVADA) {
					reservas.add(l);
				} 
				else if (l.getStatus() == StatusLocacao.ATIVA) {
					locacoesAtivas.add(l);
				} 
				else if (l.getStatus() == StatusLocacao.FINALIZADA) {
					totalFinalizadas++;
				}
			}
			
			reservas.sort(Comparator.comparing(Locacao::getDataRetirada));
			locacoesAtivas.sort(Comparator.comparing(Locacao::getDataPrevDevolucao));
		} 
		catch (Exception e) {
			mensagemErro = "Não foi possível carregar o seu painel no momento. Tente novamente em instantes.";
		}
	}

	/** Reserva com a retirada mais próxima (a lista já está ordenada), ou null. */
	public Locacao getProximaReserva() {
		return reservas.isEmpty() ? null : reservas.get(0);
	}

	public boolean isAtrasada(Locacao locacao) {
		return locacao.getDataPrevDevolucao().isBefore(LocalDate.now());
	}

	public int getTotalReservas() {
		return reservas.size();
	}

	public int getTotalLocacoesAtivas() {
		return locacoesAtivas.size();
	}

	public int getTotalFinalizadas() {
		return totalFinalizadas;
	}

	public int getTotalGeral() {
		return totalGeral;
	}

	public List<Locacao> getReservas() {
		return reservas;
	}

	public List<Locacao> getLocacoesAtivas() {
		return locacoesAtivas;
	}

	public String getMensagemErro() {
		return mensagemErro;
	}
}