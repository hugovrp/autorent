package br.autorent.desktop.view;

import java.awt.Color;
import java.awt.Font;

/** 
 * Define cores e fontes compartilhadas pela interface. 
 */
final class Tema {
	static final Color AZUL = new Color(0x1E3A5F);
	static final Color FUNDO = new Color(0xF3F4F6);
	static final Color ERRO = new Color(0xB00020);
	static final Color SUCESSO = new Color(0x1E6B34);
	static final Color NEUTRO = new Color(0x555555);

	static final Font TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 24);
	static final Font SUBTITULO = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
	static final Font ROTULO = new Font(Font.SANS_SERIF, Font.BOLD, 12);

	private Tema() { }
}