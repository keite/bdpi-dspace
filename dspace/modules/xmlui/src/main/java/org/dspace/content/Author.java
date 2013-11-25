package org.dspace.content;

import java.util.Comparator;


/**
   Classe criado para indicar os atributos do autor.
   120817 - Dan Shinkai

   120911 - Dan - implementado a interface Comparable para ordenar por nomeCompleto
   04out2013 - Jan - contornando oracle, que considera string vazia igual a null
*/

public final class Author implements Comparator<Author>, Comparable<Author> {

	private int codpes = 0;
        private int qntTrabalhos = 0;
        private String nome = "";
        private String email_1 = "";
        private String sobrenome = "";
        private String nomeCompleto = "";
        private String nomeInicial = "";
        private String unidade = "";
        private String unidadeSigla = "";
        private String depto = "";
        private String deptoSigla = "";
        private String vinculo = "";
        private String funcao = "";
        private String lattes = "";

        public Author(){}
    
	/**
     *
     * @param codpes
     * @param qntTrabalhos
     * @param nome
     * @param email_1
     * @param sobrenome
     * @param nomeCompleto
     * @param nomeInicial
     * @param unidade
     * @param unidadeSigla
     * @param depto
     * @param deptoSigla
     * @param vinculo
     * @param funcao
     * @param lattes
     */
    public Author(int codpes, int qntTrabalhos, String nome, String email_1, String sobrenome,
			String nomeCompleto, String nomeInicial, String unidade, String unidadeSigla,
			String depto, String deptoSigla, String vinculo, String funcao,
			String lattes) {
		super();
                this.setCodpes(codpes);
                this.setQntTrabalhos(qntTrabalhos);
		this.setNome(nome);
		this.setEmail_1(email_1);
		this.setSobrenome(sobrenome);
                this.setNomeCompleto(nomeCompleto);
		this.setNomeInicial(nomeInicial);
		this.setUnidade(unidade);
		this.setUnidadeSigla(unidadeSigla);
		this.setDepto(depto);
		this.setDeptoSigla(deptoSigla);
		this.setVinculo(vinculo);
		this.setFuncao(funcao);
		this.setLattes(lattes);
                
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
		this.nome = nome==null?"":nome;
	}
	public String getEmail_1() {
		return email_1;
	}
	public void setEmail_1(String email_1) {
		this.email_1 = email_1==null?"":email_1;
	}
	public String getSobrenome() {
		return sobrenome;
	}
	public void setSobrenome(String sobrenome) {
		this.sobrenome = sobrenome==null?"":sobrenome;
	}
        public String getNomeCompleto() {
                return nomeCompleto;
        }
        public void setNomeCompleto(String nomeCompleto) {
                this.nomeCompleto = nomeCompleto==null?"":nomeCompleto;
        }
	public String getNomeInicial() {
		return nomeInicial;
	}
	public void setNomeInicial(String nomeInicial) {
		this.nomeInicial = nomeInicial==null?"":nomeInicial;
	}
	public String getUnidade() {
		return unidade;
	}
	public void setUnidade(String unidade) {
		this.unidade = unidade==null?"":unidade;
	}
	public String getUnidadeSigla() {
		return unidadeSigla;
	}
	public void setUnidadeSigla(String unidadeSigla) {
		this.unidadeSigla = unidadeSigla==null?"":unidadeSigla;
	}
	public String getDepto() {
		return depto;
	}
	public void setDepto(String depto) {
		this.depto = depto==null?"":depto;
	}
	public String getDeptoSigla() {
		return deptoSigla;
	}
	public void setDeptoSigla(String deptoSigla) {
		this.deptoSigla = deptoSigla==null?"":deptoSigla;
	}
	public String getVinculo() {
		return vinculo;
	}
	public void setVinculo(String vinculo) {
		this.vinculo = vinculo==null?"":vinculo;
	}
	public String getFuncao() {
		return funcao;
	}
	public void setFuncao(String funcao) {
		this.funcao = funcao==null?"":funcao;
	}
	public String getLattes() {
		return lattes;
	}
        public void setLattes(){
                this.lattes = "https://uspdigital.usp.br/tycho/curriculoLattesMostrar?codpes=".concat(String.valueOf(codpes));
        }
        public void setLattes(String lattes) {
                if(lattes != null) {
		  this.lattes = "http://lattes.cnpq.br/" + lattes;
                }
                else {
                  setLattes();
                }
	}
        public int getQntTrabalhos() {
		return qntTrabalhos;
	}

	public void setQntTrabalhos(int qntTrabalhos) {
		this.qntTrabalhos = qntTrabalhos;
	}
        @Override
       	public int compareTo(Author o) {
    	   return nomeCompleto.compareTo(o.getNomeCompleto());
	}

        /** 1 = o1 is greater than o2
            0 = o1 equals to o2
           -1 = o1 is less than o
     * @param author
     * @param authorComp */
	@Override
        public int compare(Author author, Author authorComp) {
	   
           if(author.qntTrabalhos < authorComp.qntTrabalhos) { return 1; }

            else if(author.qntTrabalhos == authorComp.qntTrabalhos) {
               return author.nomeCompleto.compareTo(authorComp.nomeCompleto);
            }

            else { return -1; }
        }
}
