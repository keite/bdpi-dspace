package org.dspace.app.xmlui.utils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringEscapeUtils;


/**
 * Utilities that are needed in XSL transformations.
 *
 * @author Dan Shinkai
 */
public class USPXSLUtils {

    private static final String FAPESP_URL_ABRE_TAG = "<a href=\"http://www.bv.fapesp.br/pt/pesquisa/?q=";
    private static final String FAPESP_URL_FECHA_TAG = "\" target=\"_blank\">";
    private static final String FECHA_URL_TAG = "</a>";

   /*
    * Metodo que recebe uma String referente a FAPESP e cria um campo no qual HTML tags estarao presentes para criar um link no numero do projeto Fapesp
   */
   public static String constroiLinkFapesp (String fapespCode) {
		
   	int campoIndexInicial = 0;
	int campoIndexFinal = 0;
		
	String codeFapesp = "";
	String campo = fapespCode;
		
	ArrayList<String> listaCaracters = new ArrayList<String>();
		
	listaCaracters.add(",");
	listaCaracters.add(";");
	listaCaracters.add(".");
	listaCaracters.add("]");
	listaCaracters.add(")");
	listaCaracters.add("[");
	listaCaracters.add("(");
	listaCaracters.add("#");
		
	/*
	 * Expressoes Regulares
	 * \s -> Com espacos;
	 * \S -> Sem espacos;
	 * \d -> digitos de [0-9];
	 * \D -> Nenhum digito de [0-9];
	 * \w -> um caractere [a-z_A-Z];
	 * \W -> sem nenhum caractere [a-z_A-Z];
	 *  $ -> Fim da expressao;
	 */
		
	String regex = "(#|\\(|\\s|\\[)\\d{2}(/|-)\\d{5}-\\d(#|\\]|\\)|\\s|$)|(#|\\s|\\[)\\d{4}(/|-)\\d{5}-\\d(#|\\]|\\)|\\s|$)|" +
     		       "(#|\\(|\\s|\\[)\\d{2}(/|-)\\d{5}(#|\\]|\\)|\\s|$)|(#|\\s|\\[)\\d{4}(/|-)\\d{5}(#|\\]|\\)|\\s|$)|" +
		       "(#|\\(|\\s|\\[)\\d{2}(/|-)\\d{5}-\\d(#|\\]|\\)|\\s|,)|(#|\\s|\\[)\\d{4}(/|-)\\d{5}-\\d(#|\\]|\\)|\\s|,)|" +
		       "(#|\\(|\\s|\\[)\\d{2}(/|-)\\d{5}(#|\\]|\\)|\\s|,)|(#|\\s|\\[)\\d{4}(/|-)\\d{5}(#|\\]|\\)|\\s|,)|" +
		       "(#|\\(|\\s|\\[)\\d{2}(/|-)\\d{5}-\\d(#|\\]|\\)|\\s|;)|(#|\\s|\\[)\\d{4}(/|-)\\d{5}-\\d(#|\\]|\\)|\\s|;)|" +
		       "(#|\\(|\\s|\\[)\\d{2}(/|-)\\d{5}(#|\\]|\\)|\\s|;)|(#|\\s|\\[)\\d{4}(/|-)\\d{5}(#|\\]|\\)|\\s|;)";
	        
        // Compila a expressao regular e encapsula em um objeto do tipo Matcher a String atual
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(fapespCode);
            
        // Verifica se a determinada expressao esta na String atual
        while (m.find()) {
            	
        	campoIndexInicial = m.start();
        	campoIndexFinal = m.end();
        	
    		codeFapesp = fapespCode.substring(campoIndexInicial, campoIndexFinal);
    		
    		//Elimina todos os espacos possiveis.
    		codeFapesp = codeFapesp.trim();
    		
    		//Realiza a iteracao da lista contendo os caracteres.
    		Iterator<String> iterator = listaCaracters.iterator();
    		
    		//Retira todos os caracteres e troca por "".
    		while(iterator.hasNext()){ 
    			
    			String caractere = iterator.next();
    			
    			if(codeFapesp.contains(caractere)) {
        		 codeFapesp = codeFapesp.replace(caractere, "");
    			}
        	}
    		    		
    		
		campo = campo.replace(codeFapesp, FAPESP_URL_ABRE_TAG + codeFapesp + FAPESP_URL_FECHA_TAG + codeFapesp + FECHA_URL_TAG);
        }            	
        return campo;
    }

   /*
   * Metodo que verifica se o dc.contributor.author eh o mesmo que o usp.autor retornando true ou false. 
   */
   public static boolean verificaUSPAutor(String autor, String comparaAutor) {
		
      Collator col = Collator.getInstance(new Locale("pt", "BR"));
      col.setStrength(Collator.PRIMARY);
		
      if(col.compare(autor, comparaAutor) == 0) { return true; }
		
      return false;
   }

   /* 130419 - Dan - Funcao que verifica se a palavra "handle" esta contido na URL passada como parametro.
   *  Esta funcao sera utilizada para criar os links no icone USP para o redirecionamento do CV na lista de itens.
   *  Caso a palavra "handle" exista, e o numero de token seja maior que 3, entao retorna 2. Caso contrario, retorna 1. 
   *  Caso nao exista "handle" na url, retorne 0.
   */ 
   public static int contemHandleURL (String url) {
      if(url.contains("handle")) {
         StringTokenizer st = new StringTokenizer(url, "/"); 
         int contador = st.countTokens(); 
         
        if(contador > 3) { return 2; }
       return 1;
      }
      return 0;
   } 
   
   /* 130524 - Dan - Funcao que retira todos os espacos contidos em uma determinada String. */
   public static String retiraEspacos(String string) {
		return string.replace(" ", "");
   }
}
