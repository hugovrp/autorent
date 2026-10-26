package br.autorent.desktop.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import br.autorent.desktop.Mensagens;
import br.autorent.dto.AgenciaDTO;
import br.autorent.dto.VeiculoDTO;
import br.autorent.modelo.enums.CategoriaVeiculo;
import br.autorent.sessionbeans.IVeiculo;

/** 
 * Formulário de cadastro de veículos. 
 */
public class VeiculoFormDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final Callable<IVeiculo> servico;

	private final JTextField campoPlaca = new JTextField(24);
	private final JTextField campoMarca = new JTextField(24);
	private final JTextField campoModelo = new JTextField(24);
	private final JTextField campoAno = new JTextField(24);
	private final JComboBox<CategoriaVeiculo> comboCategoria = new JComboBox<>(CategoriaVeiculo.values());
	private final JTextField campoDiaria = new JTextField(24);
	private final SeletorAgencia comboAgencia = new SeletorAgencia(false);
	private final JTextArea mensagem = new JTextArea(3, 24);
	private final JButton botaoSalvar = new JButton("Salvar");
	private final JButton botaoCancelar = new JButton("Cancelar");

	private VeiculoDTO cadastrado;

	public VeiculoFormDialog(Frame dono, Callable<IVeiculo> servico, List<AgenciaDTO> agencias) {
		super(dono, "Novo veículo", true);
		this.servico = servico;
		comboAgencia.carregar(agencias);
		comboCategoria.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object valor, int indice, boolean selecionado,
					boolean foco) {
				return super.getListCellRendererComponent(list,
						valor == null ? "" : Formatos.categoria((CategoriaVeiculo) valor), indice, selecionado, foco);
			}
		});
		montarTela();

		botaoSalvar.addActionListener(e -> salvar());
		botaoCancelar.addActionListener(e -> dispose());
		getRootPane().setDefaultButton(botaoSalvar);

		pack();
		setResizable(false);
		setLocationRelativeTo(dono);
	}

	public VeiculoDTO getCadastrado() {
		return cadastrado;
	}

	/** 
	 * Monta os componentes da interface.
	 */
	private void montarTela() {
		JPanel form = new JPanel(new GridBagLayout());
		form.setBorder(BorderFactory.createEmptyBorder(18, 24, 8, 24));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;

		campo(form, c, "Placa (ex.: ABC1D23)", campoPlaca);
		campo(form, c, "Marca", campoMarca);
		campo(form, c, "Modelo", campoModelo);
		campo(form, c, "Ano", campoAno);
		campo(form, c, "Categoria", comboCategoria);
		campo(form, c, "Valor da diária (R$)", campoDiaria);
		campo(form, c, "Agência", comboAgencia);

		mensagem.setEditable(false);
		mensagem.setFocusable(false);
		mensagem.setOpaque(false);
		mensagem.setLineWrap(true);
		mensagem.setWrapStyleWord(true);
		mensagem.setFont(Tema.ROTULO);
		c.insets = new Insets(10, 0, 0, 0);
		form.add(mensagem, c);

		JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		botoes.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));
		botoes.add(botaoSalvar);
		botoes.add(botaoCancelar);

		setLayout(new BorderLayout());
		add(form, BorderLayout.CENTER);
		add(botoes, BorderLayout.SOUTH);
	}

	/** 
	 * Adiciona um campo identificado à área do formulário.
	 * 
	 * @param painel valor utilizado pela operação
	 * @param c valor utilizado pela operação
	 * @param titulo valor utilizado pela operação
	 * @param entrada valor utilizado pela operação
	 */
	private void campo(JPanel painel, GridBagConstraints c, String titulo, Component entrada) {
		JLabel rotulo = new JLabel(titulo);
		rotulo.setFont(Tema.ROTULO);
		c.insets = new Insets(8, 0, 2, 0);
		painel.add(rotulo, c);
		c.insets = new Insets(0, 0, 0, 0);
		painel.add(entrada, c);
	}

	/** 
	 * Valida os dados e inicia a operação de gravação.
	 */
	private void salvar() {
		String placa = campoPlaca.getText().trim();
		String marca = campoMarca.getText().trim();
		String modelo = campoModelo.getText().trim();
		String textoAno = campoAno.getText().trim();
		String textoDiaria = campoDiaria.getText().trim();

		if (placa.isEmpty()) {
			erro("Informe a placa.", campoPlaca);
			return;
		}
		if (marca.isEmpty()) {
			erro("Informe a marca.", campoMarca);
			return;
		}
		if (modelo.isEmpty()) {
			erro("Informe o modelo.", campoModelo);
			return;
		}

		Integer ano;
		try {
			ano = Integer.valueOf(textoAno);
		} 
		catch (NumberFormatException e) {
			erro("Informe o ano com 4 dígitos (somente números).", campoAno);
			return;
		}

		Double diaria;
		try {
			diaria = Double.valueOf(textoDiaria.replace("R$", "").trim().replace(".", "").replace(",", "."));
		} 
		catch (NumberFormatException e) {
			erro("Informe o valor da diária (ex.: 150,00).", campoDiaria);
			return;
		}

		if (comboAgencia.getAgenciaId() == null) {
			erro("Selecione a agência.", comboAgencia);
			return;
		}

		enviar(placa, marca, modelo, ano, (CategoriaVeiculo) comboCategoria.getSelectedItem(), diaria,
				comboAgencia.getAgenciaId());
	}

	private void enviar(String placa, String marca, String modelo, Integer ano, CategoriaVeiculo categoria, Double diaria,
			Long agenciaId) {
		habilitar(false);
		mensagem(Tema.NEUTRO, "Salvando...");

		new SwingWorker<VeiculoDTO, Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected VeiculoDTO doInBackground() throws Exception {
				return servico.call().cadastrarVeiculo(placa, marca, modelo, ano, categoria, diaria, agenciaId);
			}

			@Override
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				try {
					cadastrado = get();
					dispose();
				} 
				catch (Exception e) {
					mensagem(Tema.ERRO, Mensagens.de(e.getCause() != null ? e.getCause() : e));
					habilitar(true);
				}
			}
		}.execute();
	}

	/** 
	 * Define se os controles da interface ficam habilitados.
	 * 
	 * @param habilitado valor utilizado pela operação
	 */
	private void habilitar(boolean habilitado) {
		for (Component c : new Component[] { campoPlaca, campoMarca, campoModelo, campoAno, comboCategoria, campoDiaria,
				comboAgencia, botaoSalvar, botaoCancelar }) {
			c.setEnabled(habilitado);
		}
	}

	/** 
	 * Exibe uma mensagem de erro e direciona o foco para o campo indicado.
	 * 
	 * @param texto valor utilizado pela operação
	 * @param foco valor utilizado pela operação
	 */
	private void erro(String texto, Component foco) {
		mensagem(Tema.ERRO, texto);
		foco.requestFocusInWindow();
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
}
