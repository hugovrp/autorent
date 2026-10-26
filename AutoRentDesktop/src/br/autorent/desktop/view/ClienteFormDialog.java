package br.autorent.desktop.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import br.autorent.desktop.Mensagens;
import br.autorent.dto.ClienteDTO;
import br.autorent.sessionbeans.ICliente;

/** 
 * Formulário de cadastro de clientes. 
 */
public class ClienteFormDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final Callable<ICliente> servico;

	private final JTextField campoNome = new JTextField(24);
	private final JTextField campoCpf = new JTextField(24);
	private final JTextField campoEmail = new JTextField(24);
	private final JPasswordField campoSenha = new JPasswordField(24);
	private final JPasswordField campoConfirmacao = new JPasswordField(24);
	private final JTextArea mensagem = new JTextArea(3, 24);
	private final JButton botaoSalvar = new JButton("Salvar");
	private final JButton botaoCancelar = new JButton("Cancelar");

	private ClienteDTO cadastrado;

	public ClienteFormDialog(Frame dono, Callable<ICliente> servico) {
		super(dono, "Novo cliente", true);
		this.servico = servico;
		montarTela();

		botaoSalvar.addActionListener(e -> salvar());
		botaoCancelar.addActionListener(e -> dispose());
		getRootPane().setDefaultButton(botaoSalvar);

		pack();
		setResizable(false);
		setLocationRelativeTo(dono);
	}

	public ClienteDTO getCadastrado() {
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

		campo(form, c, "Nome completo", campoNome);
		campo(form, c, "CPF", campoCpf);
		campo(form, c, "E-mail", campoEmail);
		campo(form, c, "Senha (mínimo 6 caracteres)", campoSenha);
		campo(form, c, "Confirmar senha", campoConfirmacao);

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
	private void campo(JPanel painel, GridBagConstraints c, String titulo, java.awt.Component entrada) {
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
		String nome = campoNome.getText().trim();
		String cpf = campoCpf.getText().trim();
		String email = campoEmail.getText().trim();
		String senha = new String(campoSenha.getPassword());
		String confirmacao = new String(campoConfirmacao.getPassword());

		if (nome.isEmpty()) {
			erro("Informe o nome do cliente.", campoNome);
		} else if (cpf.isEmpty()) {
			erro("Informe o CPF.", campoCpf);
		} else if (email.isEmpty()) {
			erro("Informe o e-mail.", campoEmail);
		} else if (senha.isEmpty()) {
			erro("Informe a senha.", campoSenha);
		} else if (!senha.equals(confirmacao)) {
			erro("A confirmação de senha não confere.", campoConfirmacao);
		} else {
			enviar(nome, cpf, email, senha);
		}
	}

	/** 
	 * Envia os dados do formulário ao serviço remoto.
	 * 
	 * @param nome valor utilizado pela operação
	 * @param cpf valor utilizado pela operação
	 * @param email valor utilizado pela operação
	 * @param senha valor utilizado pela operação
	 */
	private void enviar(String nome, String cpf, String email, String senha) {
		habilitar(false);
		mensagem(Tema.NEUTRO, "Salvando...");

		new SwingWorker<ClienteDTO, Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected ClienteDTO doInBackground() throws Exception {
				return servico.call().cadastrarCliente(nome, cpf, email, senha);
			}

			@Override
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				try {
					cadastrado = get();
					dispose();
				} catch (Exception e) {
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
		for (java.awt.Component c : new java.awt.Component[] { campoNome, campoCpf, campoEmail, campoSenha,
				campoConfirmacao, botaoSalvar, botaoCancelar }) {
			c.setEnabled(habilitado);
		}
	}

	/** 
	 * Exibe uma mensagem de erro e direciona o foco para o campo indicado.
	 * 
	 * @param texto valor utilizado pela operação
	 * @param foco valor utilizado pela operação
	 */
	private void erro(String texto, java.awt.Component foco) {
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

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		return new Dimension(Math.max(d.width, 380), d.height);
	}
}
