package br.autorent.managedbeans;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import br.autorent.modelo.Locacao;
import br.autorent.modelo.enums.StatusLocacao;
import br.autorent.sessionbeans.ILocacao;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/** 
 * Consulta e filtra as reservas e locações do cliente autenticado. 
 */
@Named("minhasReservasMB")
@RequestScoped
public class MinhasReservasMB {
	private StatusLocacao status;
	private List<Locacao> locacoes;
	private String mensagemErro;

	@EJB
	private ILocacao iLocacao;

	@Inject
	private SessaoMB sessaoMB;

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
	
	public Map<StatusLocacao, String> getRotulos() {
		Map<StatusLocacao, String> rotulos = new EnumMap<>(StatusLocacao.class);
		
		rotulos.put(StatusLocacao.RESERVADA, "Reservada");
		rotulos.put(StatusLocacao.ATIVA, "Ativa");
		rotulos.put(StatusLocacao.FINALIZADA, "Finalizada");
		rotulos.put(StatusLocacao.CANCELADA, "Cancelada");
		
		return rotulos;
	}

	public StatusLocacao getStatus() {
		return status;
	}

	public void setStatus(StatusLocacao status) {
		this.status = status;
		this.locacoes = null; 
		this.mensagemErro = null;
	}

	public String getMensagemErro() {
		getLocacoes();
		return mensagemErro;
	}
}
