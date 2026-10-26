package br.autorent.dto;

import java.io.Serializable;

/**
 * Objeto de transferência de dados (ClienteDTO) utilizado pela aplicação cliente.
 */
public class ClienteDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Long id;
	private final String nome;
	private final String cpf;
	private final String email;

	public ClienteDTO(Long id, String nome, String cpf, String email) {
		this.id = id;
		this.nome = nome;
		this.cpf = cpf;
		this.email = email;
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	/** Somente dígitos (11). */
	public String getCpf() {
		return cpf;
	}

	public String getEmail() {
		return email;
	}
}