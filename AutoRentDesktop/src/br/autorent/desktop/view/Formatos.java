package br.autorent.desktop.view;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.modelo.enums.StatusLocacao;
import br.autorent.modelo.enums.StatusVeiculo;

/** 
 * Disponibiliza métodos de formatação para os dados exibidos nas telas. 
 */
final class Formatos {
	private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

	private Formatos() { }

	/** 
	 * Formata uma data para exibição.
	 * 
	 * @param data valor utilizado pela operação
	 * @return resultado da operação
	 */
	static String data(LocalDate data) {
		return data == null ? "-" : data.format(DATA);
	}

	/** 
	 * Formata um valor monetário para exibição.
	 * 
	 * @param valor valor utilizado pela operação
	 * @return resultado da operação
	 */
	static String moeda(Double valor) {
		return valor == null ? "-" : NumberFormat.getCurrencyInstance(PT_BR).format(valor);
	}

	/** 
	 * Formata um CPF para exibição.
	 * 
	 * @param cpf valor utilizado pela operação
	 * @return resultado da operação
	 */
	static String cpf(String cpf) {
		if (cpf == null || cpf.length() != 11) {
			return cpf;
		}
		return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
	}

	/** 
	 * Formata um telefone para exibição.
	 * 
	 * @param telefone valor utilizado pela operação
	 * @return resultado da operação
	 */
	static String telefone(String telefone) {
		if (telefone == null || telefone.isBlank()) {
			return "-";
		}
		if (telefone.length() == 11) {
			return "(" + telefone.substring(0, 2) + ") " + telefone.substring(2, 7) + "-" + telefone.substring(7);
		}
		if (telefone.length() == 10) {
			return "(" + telefone.substring(0, 2) + ") " + telefone.substring(2, 6) + "-" + telefone.substring(6);
		}
		return telefone;
	}

	/** 
	 * Retorna o texto de exibição da categoria.
	 * 
	 * @param categoria valor utilizado pela operação
	 * @return resultado da operação
	 */
	static String categoria(CategoriaVeiculo categoria) {
		switch (categoria) {
			case ECONOMICO: return "Econômico";
			case COMPACTO: return "Compacto";
			case SEDAN: return "Sedan";
			case SUV: return "SUV";
			case PREMIUM: return "Premium";
			default: return categoria.name();
		}
	}

	/** 
	 * Retorna o texto de exibição do status do veículo.
	 * 
	 * @param status valor utilizado pela operação
	 * @return resultado da operação
	 */
	static String statusVeiculo(StatusVeiculo status) {
		switch (status) {
			case DISPONIVEL: return "Disponível";
			case LOCADO: return "Locado";
			case MANUTENCAO: return "Em manutenção";
			default: return status.name();
		}
	}

	/** 
	 * Retorna o texto de exibição do status da locação.
	 * 
	 * @param status valor utilizado pela operação
	 * @return resultado da operação
	 */
	static String statusLocacao(StatusLocacao status) {
		switch (status) {
			case RESERVADA: return "Reservada";
			case ATIVA: return "Ativa";
			case FINALIZADA: return "Finalizada";
			case CANCELADA: return "Cancelada";
			default: return status.name();
		}
	}
}
