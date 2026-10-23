package br.autorent.sessionbeans;

import br.autorent.modelo.Usuario;

public interface IUsuario {
	boolean login(String cpf, String senha);
    void logout();
    Usuario obterClienteLogado();
}
