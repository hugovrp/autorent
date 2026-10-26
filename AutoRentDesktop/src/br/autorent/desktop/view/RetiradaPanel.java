package br.autorent.desktop.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

import br.autorent.desktop.ConexaoEJB;
import br.autorent.desktop.Mensagens;
import br.autorent.dto.AgenciaDTO;
import br.autorent.dto.LocacaoDTO;
import br.autorent.modelo.enums.StatusLocacao;
import br.autorent.sessionbeans.IConsultaFrota;
import br.autorent.sessionbeans.ILocacao;

/** 
 * Tela de consulta de reservas e registro da retirada de veículos. 
 */
public class RetiradaPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final Callable<IConsultaFrota> servicoConsulta;
	private final Callable<ILocacao> servicoLocacao;

	private final SeletorAgencia comboAgencia = new SeletorAgencia();
	private final JTextField campoBusca = new JTextField(20);
	private final JButton botaoConsultar = new JButton("Consultar");
	private final JButton botaoRetirada = new JButton("Registrar retirada");
	private final JLabel mensagem = new JLabel(" ");
	private final ReservasTableModel modelo = new ReservasTableModel();
	private final JTable tabela = new JTable(modelo);

	private IConsultaFrota iConsulta;
	private ILocacao iLocacao;
	private boolean ocupado;

	public RetiradaPanel() {
		this(ConexaoEJB::getConsultaFrotaService, ConexaoEJB::getLocacaoService);
	}

	public RetiradaPanel(Callable<IConsultaFrota> servicoConsulta, Callable<ILocacao> servicoLocacao) {
		this.servicoConsulta = servicoConsulta;
		this.servicoLocacao = servicoLocacao;
		montarTela();

		botaoConsultar.addActionListener(e -> consultar(null, null));
		campoBusca.addActionListener(e -> aplicarFiltro(true));
		botaoRetirada.addActionListener(e -> registrarRetirada());
		tabela.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				atualizarBotoes();
			}
		});
	}

	/** 
	 * Atualiza os dados quando a tela é exibida.
	 */
	public void aoExibir() {
		consultar(null, null);
	}

	/** 
	 * Monta os componentes da interface.
	 */
	private void montarTela() {
		setLayout(new BorderLayout(0, 10));
		setBackground(Tema.FUNDO);
		setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

		JLabel titulo = new JLabel("Retirada de veículo");
		titulo.setFont(Tema.TITULO.deriveFont(20f));

		JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		filtros.setOpaque(false);
		filtros.add(new JLabel("Agência:"));
		filtros.add(comboAgencia);
		filtros.add(new JLabel("Cliente, CPF, placa ou nº:"));
		filtros.add(campoBusca);
		filtros.add(botaoConsultar);
		filtros.add(botaoRetirada);

		JPanel topo = new JPanel(new BorderLayout(0, 12));
		topo.setOpaque(false);
		topo.add(titulo, BorderLayout.NORTH);
		topo.add(filtros, BorderLayout.CENTER);

		tabela.setRowHeight(24);
		tabela.setFillsViewportHeight(true);
		tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabela.getTableHeader().setReorderingAllowed(false);
		tabela.getColumnModel().getColumn(0).setMaxWidth(50);
		tabela.getColumnModel().getColumn(3).setPreferredWidth(260);

		mensagem.setFont(Tema.ROTULO);
		botaoRetirada.setEnabled(false);

		add(topo, BorderLayout.NORTH);
		add(new JScrollPane(tabela), BorderLayout.CENTER);
		add(mensagem, BorderLayout.SOUTH);
	}

	/** 
	 * Executa a operação de consulta.
	 * 
	 * @return resultado da operação
	 */
	private synchronized IConsultaFrota consulta() throws Exception {
		if (iConsulta == null) {
			iConsulta = servicoConsulta.call();
		}
		return iConsulta;
	}

	/** 
	 * Executa a operação de locacoes.
	 * 
	 * @return resultado da operação
	 */
	private synchronized ILocacao locacoes() throws Exception {
		if (iLocacao == null) {
			iLocacao = servicoLocacao.call();
		}
		return iLocacao;
	}

	/** 
	 * Consulta os registros que atendem aos critérios atuais.
	 * 
	 * @param texto valor utilizado pela operação
	 * @param cor valor utilizado pela operação
	 */
	private void consultar(String texto, Color cor) {
		Long agenciaId = comboAgencia.getAgenciaId();
		ocupar(true);
		if (texto == null) {
			mensagem(Tema.NEUTRO, "Consultando...");
		}

		new SwingWorker<Object[], Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected Object[] doInBackground() throws Exception {
				IConsultaFrota s = consulta();
				return new Object[] { s.listarAgencias(), s.consultarLocacoes(StatusLocacao.RESERVADA, agenciaId) };
			}

			@Override
			@SuppressWarnings("unchecked")
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				try {
					Object[] resultado = get();
					comboAgencia.carregar((List<AgenciaDTO>) resultado[0]);
					modelo.setTodas((List<LocacaoDTO>) resultado[1]);
					aplicarFiltro(texto == null);
					if (texto != null) {
						mensagem(cor, texto);
					}
				} catch (Exception e) {
					synchronized (RetiradaPanel.this) {
						iConsulta = null; // refaz a referência na próxima tentativa
					}
					modelo.setTodas(new ArrayList<>());
					mensagem(Tema.ERRO, Mensagens.de(e.getCause() != null ? e.getCause() : e));
				}
				ocupar(false);
			}
		}.execute();
	}

	/** 
	 * Aplica o filtro selecionado à lista de registros.
	 * 
	 * @param informar valor utilizado pela operação
	 */
	private void aplicarFiltro(boolean informar) {
		modelo.filtrar(campoBusca.getText());
		if (informar) {
			mensagem(Tema.NEUTRO, modelo.getRowCount() == 0 ? "Nenhuma reserva aguardando retirada encontrada."
					: modelo.getRowCount() + " reserva(s) aguardando retirada. Selecione uma para registrar a retirada.");
		}
		atualizarBotoes();
	}

	/**
	 * Registra a retirada do veículo selecionado.
	 */
	private void registrarRetirada() {
		int linha = tabela.getSelectedRow();
		if (linha < 0) {
			mensagem(Tema.ERRO, "Selecione a locação para registrar a retirada.");
			return;
		}
		LocacaoDTO l = modelo.get(tabela.convertRowIndexToModel(linha));

		String resumo = "<html>Locação nº <b>" + l.getId() + "</b><br>Cliente: <b>" + l.getClienteNome() + "</b> (CPF "
				+ Formatos.cpf(l.getClienteCpf()) + ")<br>Veículo: <b>" + l.getVeiculoMarca() + " " + l.getVeiculoModelo()
				+ " (" + l.getVeiculoPlaca() + ")</b><br>Retirada prevista: <b>" + Formatos.data(l.getDataRetirada())
				+ "</b> em " + l.getAgenciaRetiradaNome() + "<br>Devolução prevista: <b>"
				+ Formatos.data(l.getDataPrevDevolucao()) + "</b> em " + l.getAgenciaDevolucaoNome()
				+ "<br><br>Confirmar a retirada do veículo?</html>";
		if (JOptionPane.showConfirmDialog(this, resumo, "Confirmar retirada", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
			return;
		}

		ocupar(true);
		mensagem(Tema.NEUTRO, "Registrando retirada...");

		new SwingWorker<LocacaoDTO, Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected LocacaoDTO doInBackground() throws Exception {
				return locacoes().registrarRetiradaAtendimento(l.getId());
			}

			@Override
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				try {
					LocacaoDTO r = get();
					consultar("Retirada da locação nº " + r.getId() + " registrada com sucesso: " + r.getVeiculoMarca() + " "
							+ r.getVeiculoModelo() + " (" + r.getVeiculoPlaca() + ") entregue a " + r.getClienteNome()
							+ ". A locação está ativa e o veículo, locado.", Tema.SUCESSO);
				} catch (Exception e) {
					synchronized (RetiradaPanel.this) {
						iLocacao = null;
					}

					consultar(Mensagens.de(e.getCause() != null ? e.getCause() : e), Tema.ERRO);
				}
			}
		}.execute();
	}

	/** 
	 * Define se os controles de operação ficam ocupados.
	 * 
	 * @param ocupado valor utilizado pela operação
	 */
	private void ocupar(boolean ocupado) {
		this.ocupado = ocupado;
		botaoConsultar.setEnabled(!ocupado);
		comboAgencia.setEnabled(!ocupado);
		atualizarBotoes();
	}

	/** 
	 * Atualiza a disponibilidade dos botões de ação.
	 */
	private void atualizarBotoes() {
		botaoRetirada.setEnabled(!ocupado && tabela.getSelectedRow() >= 0);
	}

	/** 
	 * Exibe uma mensagem com a cor informada.
	 * 
	 * @param cor valor utilizado pela operação
	 * @param texto valor utilizado pela operação
	 */
	private void mensagem(Color cor, String texto) {
		mensagem.setForeground(cor);
		mensagem.setText(texto);
	}

	private static class ReservasTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private static final String[] COLUNAS = { "Nº", "Cliente", "CPF", "Veículo", "Retirada", "Devolução prevista",
				"Retirar em", "Devolver em", "Valor" };

		private List<LocacaoDTO> todas = new ArrayList<>();
		private List<LocacaoDTO> exibidas = new ArrayList<>();

		void setTodas(List<LocacaoDTO> todas) {
			this.todas = todas;
			this.exibidas = new ArrayList<>(todas);
			fireTableDataChanged();
		}

		/** 
		 * Filtra os registros pelo texto informado.
		 * 
		 * @param texto valor utilizado pela operação
		 */
		void filtrar(String texto) {
			String termo = normalizar(texto);
			String digitos = texto == null ? "" : texto.replaceAll("\\D", "");
			exibidas = new ArrayList<>();
			for (LocacaoDTO l : todas) {
				String alvo = normalizar(l.getClienteNome() + " " + l.getVeiculoPlaca() + " " + l.getVeiculoMarca() + " "
						+ l.getVeiculoModelo());
				boolean confere = termo.isEmpty() || alvo.contains(termo)
						|| (!digitos.isEmpty() && (l.getClienteCpf().contains(digitos) || String.valueOf(l.getId()).equals(digitos)));
				if (confere) {
					exibidas.add(l);
				}
			}
			fireTableDataChanged();
		}

		/** 
		 * Normaliza o texto para facilitar a comparação.
		 * 
		 * @param texto valor utilizado pela operação
		 * @return resultado da operação
		 */
		private static String normalizar(String texto) {
			String base = texto == null ? "" : texto.trim().toLowerCase();
			return Normalizer.normalize(base, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		}

		LocacaoDTO get(int linha) {
			return exibidas.get(linha);
		}

		@Override
		public int getRowCount() {
			return exibidas.size();
		}

		@Override
		public int getColumnCount() {
			return COLUNAS.length;
		}

		@Override
		public String getColumnName(int coluna) {
			return COLUNAS[coluna];
		}

		@Override
		public Object getValueAt(int linha, int coluna) {
			LocacaoDTO l = exibidas.get(linha);
			switch (coluna) {
				case 0: return l.getId();
				case 1: return l.getClienteNome();
				case 2: return Formatos.cpf(l.getClienteCpf());
				case 3: return l.getVeiculoMarca() + " " + l.getVeiculoModelo() + " (" + l.getVeiculoPlaca() + ")";
				case 4: return Formatos.data(l.getDataRetirada());
				case 5: return Formatos.data(l.getDataPrevDevolucao());
				case 6: return l.getAgenciaRetiradaNome();
				case 7: return l.getAgenciaDevolucaoNome();
				default: return Formatos.moeda(l.getValorTotal());
			}
		}
	}
}
