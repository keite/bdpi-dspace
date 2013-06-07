/**
 * 
 */
package org.dspace.content.authority;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;
import org.dspace.content.DCPersonName;

import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

/**
 * USP Name Authority
 * 
 * @author Helves Domingues
 * @version $Revision $
 */
public class USPNameAuthority implements ChoiceAuthority {
	private static Logger log = Logger.getLogger(USPNameAuthority.class);

	// Esquema e tabela onde os dados dos autores est‹o armazenados
	private static final String DATABASE_TABLE = "usp.viewauthoritycontrol";

	Context context;
	
	// Construtor
	public USPNameAuthority() {
		
	}

	// Devolve as op›es poss’veis
	public Choices getMatches(String field, String query, int collection,
			int start, int limit, String locale) {
		
		Choice v[] = null;
		int MAX_AUTORES = ConfigurationManager.getIntProperty("xmlui.lookup.select.size", 10);
		
		try {


			System.out.println(" ==1 Par‰metros == ");				
			System.out.println(" == field == " + field);
			System.out.println(" == query == " + query);			
			System.out.println(" == collection == " + collection);
			System.out.println(" == start == " + start);
			System.out.println(" == limit == " + limit);
			System.out.println(" == locale == " + locale);
			System.out.println(" ================== ");

			String nomes[] = null;

			
			String sobrenome = "";
			String nome = "";
			String filtro = "";

			nomes = query.split("\\,");
			if (nomes.length > 0)
				//sobrenome = new String(nomes[0].trim().getBytes("ISO-8859-1"),"UTF8");
				sobrenome = nomes[0].trim();
			if (nomes.length > 1)
				//nome = new String(nomes[1].trim().getBytes("ISO-8859-1"),"UTF8");
				nome = nomes[1].trim();
				
			String consulta = "SELECT codpes,nome, nomeinicial, sobrenome, unidade_sigla, depto_sigla,funcao from "
					+ DATABASE_TABLE + " ";

			if (notEmpty(nome))
				filtro = "upper(sem_acentos(nomeinicial)) like upper(sem_acentos('%"
						+ nome + "%'))";

			if (notEmpty(sobrenome))
				if (notEmpty(filtro))
					filtro = filtro
							+ "and upper(sem_acentos(sobrenome)) like upper(sem_acentos('%"
							+ sobrenome + "%'))";
				else
					filtro = "upper(sem_acentos(sobrenome)) like upper(sem_acentos('%"
							+ sobrenome + "%'))";

			if (notEmpty(filtro))
				consulta = consulta + " where " + filtro;

			consulta = consulta + " order by nome";

			System.out.println(" consulta == " + consulta);

			// Obtem o contexto inicial
			context = new Context();
			TableRowIterator tri = DatabaseManager.query(context, consulta);

			List<TableRow> autores = tri.toList();
			
			int MAX_SIZE = (autores.size() > MAX_AUTORES ? MAX_AUTORES: autores.size());
			v = new Choice[MAX_SIZE];
			for (int i = 0; i < MAX_SIZE; i++) {
				System.out.println(" contador == " + i);

				TableRow row = (TableRow) autores.get(i);

				v[i] = new Choice(String.valueOf(row.getIntColumn("codpes")),
								  row.getStringColumn("sobrenome") + " , " + row.getStringColumn("nomeinicial"),
								  row.getStringColumn("nome") + " (" + row.getStringColumn("unidade_sigla") + "/" 
															  + row.getStringColumn("depto_sigla") + ")"
															  + " [" + row.getStringColumn("funcao") + "]");

			}

		} catch (Exception e) {
			System.err.println("Exception occurred:" + e);
			e.printStackTrace();

			if (context != null) {
				context.abort();
			}

			System.exit(1);
		}

		System.out.println(" FIM ");

		// Choice values[], int start, int total, int confidence, boolean more, int defaultSelected
		return new Choices(v, 0, v.length , Choices.CF_ACCEPTED, true, 0);
	}

	public Choices getBestMatch(String field, String text, int collection,
			String locale) {

		Choice v[] = new Choice[1];
		v[0] = new Choice("1", "Nao definido", "Nao definido");
		return new Choices(v, 0, v.length, Choices.CF_UNCERTAIN, false, 0);
	}

	public String getLabel(String field, String key, String locale) {
		return "Nao definido";
	}

	public static boolean notEmpty(String s) {
		return (s != null && s.length() > 0);
	}

}
