package br.autorent.managedbeans;

import java.util.ArrayList;
import java.util.List;

import br.autorent.modelo.Locacao;
import br.autorent.modelo.enums.StatusLocacao;
import br.autorent.sessionbeans.ILocacao;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Reservas/locações do cliente autenticado. O id do cliente vem sempre da sessão (SessaoMB).
 */
@Named("minhasReservasMB")
@RequestScoped
public class MinhasReservasMB {
	private StatusLocacao status; // filtro opcional (null = todos)
	private List<Locacao> locacoes;
	private String mensagemErro;

	@EJB
	private ILocacao iLocacao;

	@Inject
	private SessaoMB sessaoMB;

	/** Carrega na primeira leitura da requisição, já com o filtro de status aplicado. */
	public List<Locacao> getLocacoes() {
		if (locacoes == null) {
			locacoes = new ArrayList<>();
			if (!sessaoMB.isCliente()) {
				return locacoes;
			}
			try {
				for (Locacao l : iLocacao.buscarPorClienteCompleto(sessaoMB.getUsuario().getId())) {
					if (status == null || l.getStatus() == status) {
						locacoes.add(l);
					}
				}
			} 
			catch (Exception e) {
				mensagemErro = "Não foi possível consultar suas reservas no momento. Tente novamente em instantes.";
			}
		}
		return locacoes;
	}

	public StatusLocacao[] getStatusDisponiveis() {
		return StatusLocacao.values();
	}

	public String rotulo(StatusLocacao s) {
		switch (s) {
			case RESERVADA: 
				return "Reservada";
				
			case ATIVA: 
				return "Ativa";
				
			case FINALIZADA: 
				return "Finalizada";
				
			case CANCELADA: 
				return "Cancelada";
				
			default: 
				return s.name();
		}
	}

	public StatusLocacao getStatus() {
		return status;
	}

	public void setStatus(StatusLocacao status) {
		this.status = status;
	}

	public String getMensagemErro() {
		getLocacoes();
		return mensagemErro;
	}
}
