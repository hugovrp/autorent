package br.autorent.sessionbeans;

import java.time.LocalDate;

import br.autorent.modelo.Agencia;
import br.autorent.modelo.Locacao;
import br.autorent.modelo.Usuario;
import br.autorent.modelo.Veiculo;

public interface IOperacaoLocacao {
	boolean login(String cpf, String password);
	Usuario getLoggedClient();
	void selectPickupAgency(Long agencyId);
	void selectReturnAgency(Long agencyId);
	void selectVehicle(Long vehicleId);
	void setRentalPeriod(LocalDate pickupDate, LocalDate returnDate);
	double calculateTotalValue();
	Locacao confirmRental();
	Agencia getPickupAgency();
	Agencia getReturnAgency();
	Veiculo getSelectedVehicle();
	LocalDate getPickupDate();
	LocalDate getReturnDate();
}
