package br.autorent.dto;

import java.io.Serializable;

import br.autorent.modelo.enums.TipoUsuario;

/**
 * Objeto de transferência de dados (UsuarioLogadoDTO) utilizado pela aplicação cliente.
 */
public class UsuarioLogadoDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Long id;
	private final String nome;
	private final TipoUsuario tipo;

	public UsuarioLogadoDTO(Long id, String nome, TipoUsuario tipo) {
		this.id = id;
		this.nome = nome;
		this.tipo = tipo;
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public TipoUsuario getTipo() {
		return tipo;
	}
}