package br.autorent.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.modelo.enums.StatusVeiculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;

@Entity
public class Veiculo implements Serializable {

private static final long serialVersionUID = 1L;
	
	@Id
	@SequenceGenerator(
			name = "veiculo_id", 
			sequenceName="veiculo_seq",
			allocationSize=1
	)	
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="veiculo_id")
	private Long id;
	
	@Column(nullable = false, unique = true, length = 8)
	private String placa;
	
	@Column(nullable = false, length = 50)
	private String marca;
	
	@Column(nullable = false, length = 80)
	private String modelo;
	
	@Column(nullable = false)
	private Integer ano;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CategoriaVeiculo categoria;
	
	@Column(name = "valor_diaria", nullable = false)
	private Double valorDiaria;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusVeiculo status = StatusVeiculo.DISPONIVEL;
	
	// N:1 - vários veículos pertencem a uma agência
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "agencia_id", nullable = false)
	private Agencia agencia;
	
	// N:M - dono da relação (gera a tabela veiculo_acessorio)
	@ManyToMany
	@JoinTable(
		name = "veiculo_acessorio",
		joinColumns = @JoinColumn(name = "veiculo_id"),
		inverseJoinColumns = @JoinColumn(name = "acessorio_id")
	)
	private Set<Acessorio> acessorios = new HashSet<>();
	
	// 1:N - um veículo pode ter várias locações ao longo do tempo
	@OneToMany(mappedBy = "veiculo")
	private List<Locacao> locacoes = new ArrayList<Locacao>();
	
	public Veiculo() { }

    public Veiculo(String placa, String marca, String modelo, Integer ano, CategoriaVeiculo categoria, Double valorDiaria, Agencia agencia) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.categoria = categoria;
        this.valorDiaria = valorDiaria;
        this.agencia = agencia;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPlaca() {
		return placa;
	}

	public void setPlaca(String placa) {
		this.placa = placa;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public CategoriaVeiculo getCategoria() {
		return categoria;
	}

	public void setCategoria(CategoriaVeiculo categoria) {
		this.categoria = categoria;
	}

	public Double getValorDiaria() {
		return valorDiaria;
	}

	public void setValorDiaria(Double valorDiaria) {
		this.valorDiaria = valorDiaria;
	}

	public StatusVeiculo getStatus() {
		return status;
	}

	public void setStatus(StatusVeiculo status) {
		this.status = status;
	}

	public Agencia getAgencia() {
		return agencia;
	}

	public void setAgencia(Agencia agencia) {
		this.agencia = agencia;
	}

	public Set<Acessorio> getAcessorios() {
		return acessorios;
	}

	public void setAcessorios(Set<Acessorio> acessorios) {
		this.acessorios = acessorios;
	}

	public List<Locacao> getLocacoes() {
		return locacoes;
	}

	public void setLocacoes(List<Locacao> locacoes) {
		this.locacoes = locacoes;
	}
}