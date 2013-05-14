package org.dspace.content;

import java.util.Comparator;


/**
   Classe criado para indicar os atributos de interdisciplinaridade.
   120927 - Dan Shinkai
*/

public class InterUnit implements Comparator<InterUnit>, Comparable<InterUnit> {

        private int codpes;
        private int qntTrabalhos;
        private String unidadeSigla;

        public InterUnit(){}
    
	public InterUnit(int codpes, int qntTrabalhos, String unidadeSigla) {
		super();
		this.codpes = codpes;
                this.qntTrabalhos = qntTrabalhos;
		this.unidadeSigla = unidadeSigla;
        }
	
	public int getCodpes() {
		return codpes;
	}
	public void setCodpes(int codpes) {
		this.codpes = codpes;
	}
	public String getUnidadeSigla() {
		return unidadeSigla;
	}
	public void setUnidadeSigla(String unidadeSigla) {
		this.unidadeSigla = unidadeSigla;
	}
        public int getQntTrabalhos() {
		return qntTrabalhos;
	}

	public void setQntTrabalhos(int qntTrabalhos) {
		this.qntTrabalhos = qntTrabalhos;
	}
       	public int compareTo(InterUnit o) {
		
    	   return unidadeSigla.compareTo(o.getUnidadeSigla());
	}

        /** 1 = o1 is greater than o2
            0 = o1 equals to o2
           -1 = o1 is less than o2*/
	@Override
        public int compare(InterUnit interUnit, InterUnit interUnitComp) {
	   
           if(interUnit.qntTrabalhos < interUnitComp.qntTrabalhos) { return 1; }

            else if(interUnit.qntTrabalhos == interUnitComp.qntTrabalhos) {
               return interUnit.unidadeSigla.compareTo(interUnitComp.getUnidadeSigla());
            }

            else { return -1; }
        }
}
