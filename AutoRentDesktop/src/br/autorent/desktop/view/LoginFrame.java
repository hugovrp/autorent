package br.autorent.desktop.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import br.autorent.desktop.AutenticacaoException;
import br.autorent.desktop.SessaoFuncionario;
import br.autorent.dto.UsuarioLogadoDTO;

/** 
 * Tela de autenticação do funcionário. 
 */
public class LoginFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private final JTextField campoCpf = new JTextField(18);
	private final JPasswordField campoSenha = new JPasswordField(18);
	private final JButton botaoEntrar = new JButton("Entrar");
	private final JButton botaoSair = new JButton("Sair");
	private final JTextArea mensagem = new JTextArea(3, 24);

	public LoginFrame() {
		super("AutoRent - Acesso do funcionário");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		montarTela();
		botaoEntrar.addActionListener(e -> entrar());
		botaoSair.addActionListener(e -> System.exit(0));
		getRootPane().setDefaultButton(botaoEntrar); // Enter confirma

		pack();
		setMinimumSize(new Dimension(380, getHeight()));
		setResizable(false);
		setLocationRelativeTo(null);
	}

	/** 
	 * Monta os componentes da interface.
	 */
	private void montarTela() {
		JPanel topo = new JPanel(new GridBagLayout());
		topo.setBackground(Tema.AZUL);
		topo.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
		JLabel titulo = new JLabel("AutoRent");
		titulo.setFont(Tema.TITULO);
		titulo.setForeground(Color.WHITE);
		JLabel subtitulo = new JLabel("Atendimento da locadora");
		subtitulo.setFont(Tema.SUBTITULO);
		subtitulo.setForeground(new Color(0xCBD5E1));
		GridBagConstraints t = new GridBagConstraints();
		t.gridx = 0;
		t.weightx = 1;
		t.fill = GridBagConstraints.HORIZONTAL;
		t.anchor = GridBagConstraints.WEST;
		topo.add(titulo, t);
		topo.add(subtitulo, t);

		JPanel form = new JPanel(new GridBagLayout());
		form.setBorder(BorderFactory.createEmptyBorder(20, 28, 8, 28));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(2, 0, 2, 0);

		form.add(rotulo("CPF"), c);
		form.add(campoCpf, c);
		c.insets = new Insets(12, 0, 2, 0);
		form.add(rotulo("Senha"), c);
		c.insets = new Insets(2, 0, 2, 0);
		form.add(campoSenha, c);

		c.insets = new Insets(14, 0, 0, 0);
		mensagem.setEditable(false);
		mensagem.setFocusable(false);
		mensagem.setOpaque(false);
		mensagem.setLineWrap(true);
		mensagem.setWrapStyleWord(true);
		mensagem.setFont(Tema.ROTULO);
		form.add(mensagem, c);

		JPanel botoes = new JPanel();
		botoes.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
		botoes.add(botaoEntrar);
		botoes.add(botaoSair);

		setLayout(new BorderLayout());
		add(topo, BorderLayout.NORTH);
		add(form, BorderLayout.CENTER);
		add(botoes, BorderLayout.SOUTH);
	}

	/** 
	 * Cria um rótulo para a interface.
	 * 
	 * @param texto valor utilizado pela operação
	 * @return resultado da operação
	 */
	private JLabel rotulo(String texto) {
		JLabel label = new JLabel(texto);
		label.setFont(Tema.ROTULO);
		return label;
	}

	/** Autentica o funcionário e inicia sua sessão.
	 */
	private void entrar() {
		String cpf = campoCpf.getText().trim();
		String senha = new String(campoSenha.getPassword());

		if (cpf.isEmpty()) {
			erro("Informe o CPF.");
			campoCpf.requestFocusInWindow();
			return;
		}
		if (cpf.replaceAll("\\D", "").length() != 11) {
			erro("O CPF deve conter 11 dígitos.");
			campoCpf.requestFocusInWindow();
			return;
		}
		if (senha.isEmpty()) {
			erro("Informe a senha.");
			campoSenha.requestFocusInWindow();
			return;
		}

		autenticar(cpf, senha);
	}

	/** 
	 * Realiza a autenticação do funcionário no backend.
	 * 
	 * @param cpf valor utilizado pela operação
	 * @param senha valor utilizado pela operação
	 */
	private void autenticar(String cpf, String senha) {
		habilitar(false);
		mensagem(Tema.NEUTRO, "Conectando ao servidor...");

		new SwingWorker<UsuarioLogadoDTO, Void>() {
			@Override
			/** 
			 * Executa a operação remota fora da thread da interface.
			 * 
			 * @return resultado da operação
			 */
			protected UsuarioLogadoDTO doInBackground() throws Exception {
				return SessaoFuncionario.getInstancia().entrar(cpf, senha);
			}

			@Override
			/** 
			 * Aplica o resultado da operação na interface.
			 */
			protected void done() {
				try {
					UsuarioLogadoDTO funcionario = get();
					mensagem(Tema.SUCESSO, "Login realizado com sucesso!");
					Timer timer = new Timer(600, e -> abrirJanelaPrincipal(funcionario));
					timer.setRepeats(false);
					timer.start();
				} 
				catch (ExecutionException e) {
					Throwable causa = e.getCause();
					if (!(causa instanceof AutenticacaoException)) {
						causa.printStackTrace();
					}
					erro(causa instanceof AutenticacaoException ? causa.getMessage()
							: "Erro inesperado ao autenticar: " + causa);
					campoSenha.setText("");
					habilitar(true);
					campoSenha.requestFocusInWindow();
				} 
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					habilitar(true);
				}
			}
		}.execute();
	}

	/** 
	 * Abre a janela principal após a autenticação.
	 * 
	 * @param funcionario valor utilizado pela operação
	 */
	private void abrirJanelaPrincipal(UsuarioLogadoDTO funcionario) {
		new MainFrame(funcionario).setVisible(true);
		dispose();
	}

	/** 
	 * Define se os controles da interface ficam habilitados.
	 * 
	 * @param habilitado valor utilizado pela operação
	 */
	private void habilitar(boolean habilitado) {
		campoCpf.setEnabled(habilitado);
		campoSenha.setEnabled(habilitado);
		botaoEntrar.setEnabled(habilitado);
	}

	/** 
	 * Exibe uma mensagem de erro e direciona o foco para o campo indicado.
	 * 
	 * @param texto valor utilizado pela operação
	 */
	private void erro(String texto) {
		mensagem(Tema.ERRO, texto);
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
