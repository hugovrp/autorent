package br.autorent.managedbeans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("loginMB")
@RequestScoped
public class LoginMB {
	private String cpf;
	private String senha;
	private String mensagemErro;

	@Inject
	private SessaoMB sessaoMB;

	public String entrar() {
		try {
			if (sessaoMB.login(cpf, senha)) {
				return sessaoMB.isAdmin() ? "/admin/dashboard?faces-redirect=true" : "/dashboard?faces-redirect=true";
			}
			mensagemErro = "CPF ou senha inválidos.";
		} 
		catch (Exception e) {
			mensagemErro = "Não foi possível realizar o login. Tente novamente em instantes.";
		}
		
		senha = null;
		return null;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getMensagemErro() {
		return mensagemErro;
	}
}