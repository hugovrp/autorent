package br.autorent.managedbeans;

import br.autorent.modelo.Usuario;
import br.autorent.sessionbeans.ICliente;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named("cadastroMB")
@RequestScoped
public class CadastroMB {
	private Usuario cliente = new Usuario();
	private String senha;
	private String confirmarSenha;

	private String mensagemSucesso;
	private String mensagemErro;

	@EJB
	private ICliente iCliente;

	public String cadastrar() {
		mensagemSucesso = null;
		mensagemErro = null;

		if (!senha.equals(confirmarSenha)) {
			mensagemErro = "A confirmação de senha não confere com a senha informada.";
			return null;
		}

		String cpf = cliente.getCpf().replaceAll("\\D", "");
		String email = cliente.getEmail().trim();

		try {
			if (iCliente.buscarPorCpf(cpf) != null) {
				mensagemErro = "Já existe um cliente cadastrado com este CPF.";
				return null;
			}
			if (iCliente.buscarPorEmail(email) != null) {
				mensagemErro = "Já existe um cliente cadastrado com este e-mail.";
				return null;
			}

			cliente.setNome(cliente.getNome().trim());
			cliente.setCpf(cpf);
			cliente.setEmail(email);
			cliente.setSenhaHash(senha);

			iCliente.criar(cliente);

			mensagemSucesso = "Cadastro realizado com sucesso! Você já pode entrar no sistema.";
			cliente = new Usuario();
		} 
		catch (Exception e) {
			mensagemErro = "Não foi possível concluir o cadastro. Verifique os dados e tente novamente.";
		}
		
		senha = null;
		confirmarSenha = null;
		
		return null;
	}

	public Usuario getCliente() {
		return cliente;
	}

	public void setCliente(Usuario cliente) {
		this.cliente = cliente;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getConfirmarSenha() {
		return confirmarSenha;
	}

	public void setConfirmarSenha(String confirmarSenha) {
		this.confirmarSenha = confirmarSenha;
	}

	public String getMensagemSucesso() {
		return mensagemSucesso;
	}

	public String getMensagemErro() {
		return mensagemErro;
	}
}
