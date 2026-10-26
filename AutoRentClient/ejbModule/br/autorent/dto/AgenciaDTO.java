package br.autorent.dto;

import java.io.Serializable;

/**
 * Objeto de transferência de dados (AgenciaDTO) utilizado pela aplicação cliente.
 */
public class AgenciaDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Long id;
	private final String nome;
	private final String cidade;
	private final String endereco;
	private final String telefone;

	public AgenciaDTO(Long id, String nome, String cidade, String endereco, String telefone) {
		this.id = id;
		this.nome = nome;
		this.cidade = cidade;
		this.endereco = endereco;
		this.telefone = telefone;
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getCidade() {
		return cidade;
	}

	public String getEndereco() {
		return endereco;
	}

	public String getTelefone() {
		return telefone;
	}
}