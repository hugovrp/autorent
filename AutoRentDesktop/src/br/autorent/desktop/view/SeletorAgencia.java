package br.autorent.desktop.view;

import java.awt.Component;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

import br.autorent.dto.AgenciaDTO;

/** 
 * Seletor de agências utilizado nos filtros e formulários. 
 */
class SeletorAgencia extends JComboBox<AgenciaDTO> {
	private static final long serialVersionUID = 1L;

	private static final AgenciaDTO TODAS = new AgenciaDTO(null, "Todas as agências", "", "", "");

	private final boolean comTodas;

	SeletorAgencia() {
		this(true);
	}

	SeletorAgencia(boolean comTodas) {
		this.comTodas = comTodas;
		setModel(comTodas ? new DefaultComboBoxModel<>(new AgenciaDTO[] { TODAS }) : new DefaultComboBoxModel<>());
		setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object valor, int indice, boolean selecionado,
					boolean foco) {
				AgenciaDTO a = (AgenciaDTO) valor;
				String texto = a == null ? "" : a.getId() == null ? a.getNome() : a.getNome() + " - " + a.getCidade();
				return super.getListCellRendererComponent(list, texto, indice, selecionado, foco);
			}
		});
	}

	/** 
	 * Carrega as agências no seletor.
	 * 
	 * @param agencias valor utilizado pela operação
	 */
	void carregar(List<AgenciaDTO> agencias) {
		Long selecionada = getAgenciaId();
		DefaultComboBoxModel<AgenciaDTO> modelo = new DefaultComboBoxModel<>();
		AgenciaDTO manter = null;
		if (comTodas) {
			modelo.addElement(TODAS);
			manter = TODAS;
		}
		for (AgenciaDTO a : agencias) {
			modelo.addElement(a);
			if (a.getId().equals(selecionada)) {
				manter = a;
			}
		}
		setModel(modelo);
		if (manter != null) {
			setSelectedItem(manter);
		}
	}

	/** 
	 * Seleciona a agência correspondente ao identificador.
	 * 
	 * @param id valor utilizado pela operação
	 */
	void selecionar(Long id) {
		for (int i = 0; i < getItemCount(); i++) {
			if (id != null && id.equals(getItemAt(i).getId())) {
				setSelectedIndex(i);
				return;
			}
		}
	}

	Long getAgenciaId() {
		AgenciaDTO a = (AgenciaDTO) getSelectedItem();
		return a == null ? null : a.getId();
	}
}

