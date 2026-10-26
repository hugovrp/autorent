package br.autorent.managedbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import br.autorent.modelo.Acessorio;
import br.autorent.modelo.Agencia;
import br.autorent.modelo.Veiculo;
import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.modelo.enums.StatusVeiculo;
import br.autorent.sessionbeans.IAcessorio;
import br.autorent.sessionbeans.IAgencia;
import br.autorent.sessionbeans.IVeiculo;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

/** 
 * Consulta veículos e gerencia a seleção para visualização e reserva. 
 */
@Named("veiculoMB")
@SessionScoped
public class VeiculoMB implements Serializable {
	private static final long serialVersionUID = 1L;

	// Filtros opcionais; valores nulos representam todos os valores.
	private CategoriaVeiculo categoria;
	private Long agenciaId;
	private StatusVeiculo disponibilidade = StatusVeiculo.DISPONIVEL;

	private List<Veiculo> veiculos = new ArrayList<>();
	private List<Agencia> agencias = new ArrayList<>();
	private Veiculo veiculoSelecionado;
	private List<Acessorio> acessorios = new ArrayList<>();

	private String mensagemErro;

	@EJB
	private IVeiculo iVeiculo;

	@EJB
	private IAgencia iAgencia;

	@EJB
	private IAcessorio iAcessorio;

	/** 
	 * Consulta veículos aplicando os filtros selecionados. 
	 */
	public void pesquisar() {
		mensagemErro = null;
		veiculos = new ArrayList<>();

		try {
			agencias = iAgencia.buscarTodos();

			if (disponibilidade == null || disponibilidade == StatusVeiculo.DISPONIVEL) {
				if (agenciaId != null) {
					veiculos.addAll(iVeiculo.buscarDisponiveisPorAgencia(agenciaId));
				} 
				else {
					for (Agencia agencia : agencias) {
						veiculos.addAll(iVeiculo.buscarDisponiveisPorAgencia(agencia.getId()));
					}
				}
			}

			if (disponibilidade == null || disponibilidade == StatusVeiculo.LOCADO) {
				for (Veiculo v : iVeiculo.buscarAlugados()) {
					if (agenciaId == null || agenciaId.equals(v.getAgencia().getId())) {
						veiculos.add(v);
					}
				}
			}

			if (categoria != null) {
				veiculos.removeIf(v -> v.getCategoria() != categoria);
			}

			veiculos.sort(Comparator.comparing(Veiculo::getMarca).thenComparing(Veiculo::getModelo));
		} 
		catch (Exception e) {
			veiculos = new ArrayList<>();
			mensagemErro = "Não foi possível consultar os veículos no momento. Tente novamente em instantes.";
		}
	}

	/** 
	 * Restaura os filtros para os valores padrão e refaz a pesquisa. 
	 */
	public void limpar() {
		categoria = null;
		agenciaId = null;
		disponibilidade = StatusVeiculo.DISPONIVEL;
		pesquisar();
	}

	/** 
	 * Seleciona um veículo e carrega seus acessórios para exibição.
	 * 
	 * @param veiculo veículo que será detalhado.
	 * @return caminho da página de detalhes do veículo.
	 */
	public String detalhar(Veiculo veiculo) {
		mensagemErro = null;
		veiculoSelecionado = veiculo;
		acessorios = new ArrayList<>();

		try {
			acessorios = iAcessorio.buscarPorVeiculo(veiculo.getId());
		} 
		catch (Exception e) {
			mensagemErro = "Não foi possível carregar os acessórios deste veículo.";
		}
		return "veiculo?faces-redirect=true";
	}

	/** 
	 * Seleciona o veículo para iniciar o processo de reserva.
	 * 
	 * @param veiculo veículo escolhido pelo cliente.
	 * @return caminho da página de reserva.
	 */
	public String reservar(Veiculo veiculo) {
		mensagemErro = null;
		veiculoSelecionado = veiculo;
		return "reserva?faces-redirect=true";
	}

	public CategoriaVeiculo[] getCategorias() {
		return CategoriaVeiculo.values();
	}

	public CategoriaVeiculo getCategoria() {
		return categoria;
	}

	public void setCategoria(CategoriaVeiculo categoria) {
		this.categoria = categoria;
	}

	public Long getAgenciaId() {
		return agenciaId;
	}

	public void setAgenciaId(Long agenciaId) {
		this.agenciaId = agenciaId;
	}

	public StatusVeiculo getDisponibilidade() {
		return disponibilidade;
	}

	public void setDisponibilidade(StatusVeiculo disponibilidade) {
		this.disponibilidade = disponibilidade;
	}

	public List<Veiculo> getVeiculos() {
		return veiculos;
	}

	public List<Agencia> getAgencias() {
		return agencias;
	}

	public Veiculo getVeiculoSelecionado() {
		return veiculoSelecionado;
	}

	public List<Acessorio> getAcessorios() {
		return acessorios;
	}

	public String getMensagemErro() {
		return mensagemErro;
	}
}
