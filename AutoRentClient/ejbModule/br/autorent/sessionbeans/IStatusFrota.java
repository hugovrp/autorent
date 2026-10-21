package br.autorent.sessionbeans;

import java.util.Map;

public interface IStatusFrota {
	long getTotalVehicles();
	long getTotalAvailable();
	long getTotalRented();
	long getTotalReserved();
	long getTotalInMaintenance();
	Map<Long, Long> getAvailableByAgency();
	void refreshCache();
}
