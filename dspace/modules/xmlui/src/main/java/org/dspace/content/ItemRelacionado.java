package org.dspace.content;

public class ItemRelacionado {
	
	private String ano;
	private String tipo;
	private String titulo;
	private String handle;

	public ItemRelacionado(){}
	
	public ItemRelacionado(String ano, String tipo, String titulo, String handle) {
		super();
		this.ano = ano;
		this.tipo = tipo;
		this.titulo = titulo;
		this.handle = handle;
	}
	
	public String getAno() {
		return ano;
	}
	
	public void setAno(String ano) {
		this.ano = ano;
	}
	
	public String getTipo() {
		return tipo;
	}
	
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	public String getTitulo() {
		return titulo;
	}
	
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getHandle() {
			return handle;
	}

	public void setHandle(String handle) {
			this.handle = handle;
	}
}

