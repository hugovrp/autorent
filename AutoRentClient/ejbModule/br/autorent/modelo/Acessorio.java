package br.autorent.modelo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;

@Entity
public class Acessorio implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@SequenceGenerator(
			name = "acessorio_id", 
			sequenceName="acessorio_seq",
			allocationSize=1
	)	
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="acessorio_id")
	private Long id;
	
	@Column(nullable = false, length = 100)
	private String descricao;
	
	@Column(name = "valor_diaria", nullable = false)
	private Double valorDiaria;
	
	// N:M - lado inverso (o dono da relação é Veiculo)
	@ManyToMany(mappedBy = "acessorios")
	private Set<Veiculo> veiculos = new HashSet<>();
	
	public Acessorio() { }

    public Acessorio(String descricao, Double valorDiaria) {
        this.descricao = descricao;
        this.valorDiaria = valorDiaria;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Double getValorDiaria() {
		return valorDiaria;
	}

	public void setValorDiaria(Double valorDiaria) {
		this.valorDiaria = valorDiaria;
	}

	public Set<Veiculo> getVeiculos() {
		return veiculos;
	}

	public void setVeiculos(Set<Veiculo> veiculos) {
		this.veiculos = veiculos;
	}
}
