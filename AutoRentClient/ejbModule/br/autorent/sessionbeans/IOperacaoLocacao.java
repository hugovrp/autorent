package br.autorent.sessionbeans;

import java.time.LocalDate;

import br.autorent.modelo.Agencia;
import br.autorent.modelo.Locacao;
import br.autorent.modelo.Usuario;
import br.autorent.modelo.Veiculo;

public interface IOperacaoLocacao {
	boolean login(String cpf, String password);
	Usuario obterClienteLogado();
	void selecionarAgenciaRetirada(Long agencyId);
	void selecionarAgenciaDevolucao(Long agencyId);
	void selecionarVeiculo(Long vehicleId);
	void definirPeriodoLocacao(LocalDate pickupDate, LocalDate returnDate);
	double calcularValorTotal();
	Locacao confirmarLocacao();
	Agencia obterAgenciaRetirada();
	Agencia obterAgenciaDevolucao();
	Veiculo obterVeiculoSelecionado();
	LocalDate obterDataRetirada();
	LocalDate obterDataDevolucao();
}
