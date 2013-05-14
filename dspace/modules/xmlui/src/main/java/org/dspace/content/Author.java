package org.dspace.content;

import java.util.Comparator;


/**
   Classe criado para indicar os atributos do autor.
   120817 - Dan Shinkai

   120911 - Dan - implementado a interface Comparable para ordenar por nomeCompleto
*/

public class Author implements Comparator<Author>, Comparable<Author> {

	private int codpes;
        private int qntTrabalhos;
        private String nome;
        private String email_1;
        private String sobrenome;
        private String nomeCompleto;
        private String nomeInicial;
        private String unidade;
        private String unidadeSigla;
        private String depto;
        private String deptoSigla;
        private String vinculo;
        private String funcao;
        private String lattes;

        public Author(){}
    
	public Author(int codpes, int qntTrabalhos, String nome, String email_1, String sobrenome,
			String nomeCompleto, String nomeInicial, String unidade, String unidadeSigla,
			String depto, String deptoSigla, String vinculo, String funcao,
			String lattes) {
		super();
		this.codpes = codpes;
                this.qntTrabalhos = qntTrabalhos;
		this.nome = nome;
		this.email_1 = email_1;
		this.sobrenome = sobrenome;
                this.nomeCompleto = nomeCompleto;
		this.nomeInicial = nomeInicial;
		this.unidade = unidade;
		this.unidadeSigla = unidadeSigla;
		this.depto = depto;
		this.deptoSigla = deptoSigla;
		this.vinculo = vinculo;
		this.funcao = funcao;
		this.lattes = lattes;
	}
	
	public int getCodpes() {
		return codpes;
	}
	public void setCodpes(int codpes) {
		this.codpes = codpes;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getEmail_1() {
		return email_1;
	}
	public void setEmail_1(String email_1) {
		this.email_1 = email_1;
	}
	public String getSobrenome() {
		return sobrenome;
	}
	public void setSobrenome(String sobrenome) {
		this.sobrenome = sobrenome;
	}
        public String getNomeCompleto() {
                return nomeCompleto;
        }
        public void setNomeCompleto(String nomeCompleto) {
                this.nomeCompleto = nomeCompleto;
        }
	public String getNomeInicial() {
		return nomeInicial;
	}
	public void setNomeInicial(String nomeInicial) {
		this.nomeInicial = nomeInicial;
	}
	public String getUnidade() {
		return unidade;
	}
	public void setUnidade(String unidade) {
		this.unidade = unidade;
	}
	public String getUnidadeSigla() {
		return unidadeSigla;
	}
	public void setUnidadeSigla(String unidadeSigla) {
		this.unidadeSigla = unidadeSigla;
	}
	public String getDepto() {
		return depto;
	}
	public void setDepto(String depto) {
		this.depto = depto;
	}
	public String getDeptoSigla() {
		return deptoSigla;
	}
	public void setDeptoSigla(String deptoSigla) {
		this.deptoSigla = deptoSigla;
	}
	public String getVinculo() {
		return vinculo;
	}
	public void setVinculo(String vinculo) {
		this.vinculo = vinculo;
	}
	public String getFuncao() {
		return funcao;
	}
	public void setFuncao(String funcao) {
		this.funcao = funcao;
	}
	public String getLattes() {
		return lattes;
	}
	public void setLattes(String lattes) {
                if(lattes != null) {
		  this.lattes = "http:/"+"/lattes.cnpq.br/" + lattes;
                }
	}
        public int getQntTrabalhos() {
		return qntTrabalhos;
	}

	public void setQntTrabalhos(int qntTrabalhos) {
		this.qntTrabalhos = qntTrabalhos;
	}
       	public int compareTo(Author o) {
		
    	   return nomeCompleto.compareTo(o.getNomeCompleto());
	}

        /** 1 = o1 is greater than o2
            0 = o1 equals to o2
           -1 = o1 is less than o2*/
	@Override
        public int compare(Author author, Author authorComp) {
	   
           if(author.qntTrabalhos < authorComp.qntTrabalhos) { return 1; }

            else if(author.qntTrabalhos == authorComp.qntTrabalhos) {
               return author.nomeCompleto.compareTo(authorComp.nomeCompleto);
            }

            else { return -1; }
        }
}
