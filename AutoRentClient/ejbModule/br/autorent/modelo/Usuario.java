package br.autorent.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.autorent.modelo.enums.TipoUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;

/**
 * Representa um usuário do sistema, incluindo seus dados de acesso e perfil.
 */
@Entity
public class Usuario implements Serializable {
	
private static final long serialVersionUID = 1L;
	
	@Id
	@SequenceGenerator(
			name = "usuario_id", 
			sequenceName="usuario_seq",
			allocationSize=1
	)	
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="usuario_id")
	private Long id;
	
	@Column(nullable = false, length = 120)
	private String nome;
	
	@Column(nullable = false, unique = true, length = 11)
	private String cpf;
	
	@Column(nullable = false, unique = true, length = 150)
	private String email;
	
	@Column(name = "senha_hash", nullable = false, length = 255)
	private String senhaHash;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TipoUsuario tipo;
	
	// 1:N - um cliente pode realizar várias locações
	@OneToMany(mappedBy = "cliente")
	private List<Locacao> locacoes = new ArrayList<Locacao>();
	
	public Usuario() { }

    public Usuario(String nome, String cpf, String email, String senhaHash, TipoUsuario tipo) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.senhaHash = senhaHash;
        this.tipo = tipo;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenhaHash() {
		return senhaHash;
	}

	public void setSenhaHash(String senhaHash) {
		this.senhaHash = senhaHash;
	}

	public TipoUsuario getTipo() {
		return tipo;
	}

	public void setTipo(TipoUsuario tipo) {
		this.tipo = tipo;
	}

	public List<Locacao> getLocacoes() {
		return locacoes;
	}

	public void setLocacoes(List<Locacao> locacoes) {
		this.locacoes = locacoes;
	}
}