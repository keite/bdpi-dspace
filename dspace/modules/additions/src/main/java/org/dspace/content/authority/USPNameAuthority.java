/**
 * 
 */
package org.dspace.content.authority;

import com.sun.rowset.CachedRowSetImpl;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;

/**
 * USP Name Authority
 * 
 * @author Helves Domingues / Jan LL - alterado para uso da base oracle - 17.out.2013*
 * @version $Revision $
 */
public class USPNameAuthority implements ChoiceAuthority {
	private static Logger log = Logger.getLogger(USPNameAuthority.class);

	// Esquema e tabela onde os dados dos autores estao armazenados
        // codpes,nome, nomeinicial, sobrenome, unidade_sigla, depto_sigla,funcao
        private static final String DATABASE_TABLE = "(SELECT vinculopessoausp.codpes, vinculopessoausp.nompes nome, \n" +
        "regexp_substr(vinculopessoausp.nompes,'(.*)\\s.*',1,1,'i',1) nomeinicial,\n" +
        "nvl(regexp_substr(vinculopessoausp.nompes,'.*\\s(.*)',1,1,'i',1),vinculopessoausp.nompes) sobrenome,\n" +
        "unidade.sglund unidade_sigla,\n" +
        "setor.nomabvset depto_sigla,\n" +
        "vinculopessoausp.tipfnc funcao\n" +
        "FROM vinculopessoausp\n" +
        "left join unidade on (vinculopessoausp.codund = unidade.codund OR vinculopessoausp.codfusclgund = unidade.codund)\n" +
        "left join setor on (vinculopessoausp.codset = setor.codset))";
                
        private static Connection ocn = null;
        public static Connection getReplicaUspDBconnection() {
            try {
                if(ocn==null || ocn.isClosed()){
                        DriverManager.registerDriver((Driver) Class.forName(ConfigurationManager.getProperty("usp-authorities", "db.driver")).newInstance());

                        ocn = DriverManager.getConnection(ConfigurationManager.getProperty("usp-authorities", "db.url"),
                                                          ConfigurationManager.getProperty("usp-authorities", "db.username"),
                                                          ConfigurationManager.getProperty("usp-authorities", "db.password"));
                }
            }
            catch(ClassNotFoundException e){
                log.debug("[inicio - ClassNotFound] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
                e.printStackTrace(System.out);
                log.debug("[fim - ClassNotFound] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
            }
            catch(InstantiationException e){
                log.debug("[inicio - Instantiation] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
                e.printStackTrace(System.out);
                log.debug("[fim - Instantiation] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
            }
            catch(IllegalAccessException e){
                log.debug("[inicio - IllegalAccess] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
                e.printStackTrace(System.out);
                log.debug("[fim - IllegalAccess] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
            }
            catch(SQLException e){
                log.debug("[inicio - SQL] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
                e.printStackTrace(System.out);
                log.debug("[fim - SQL] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
            }
            return ocn;
        }        
        
	// Context context;
	
	// Construtor
	public USPNameAuthority() {
		
	}

	// Devolve as opcoes possiveis
        @Override
	public Choices getMatches(String field, String query, int collection,
			int start, int limit, String locale) {
		
		Choice v[] = null;
		PreparedStatement statement = null;
                CachedRowSetImpl rs = null;
                int MAX_AUTORES = ConfigurationManager.getIntProperty("xmlui.lookup.select.size", 10);
		
                
		try {
			log.debug(" ==1 Par‰metros == ");				
			log.debug(" == field == " + field);
			log.debug(" == query == " + query);			
			log.debug(" == collection == " + collection);
			log.debug(" == start == " + start);
			log.debug(" == limit == " + limit);
			log.debug(" == locale == " + locale);
			log.debug(" ================== ");
                        
			String nomes[];
			
			String sobrenome = "";
			String nome = "";
			String filtro = " where rownum <= ".concat(String.valueOf(MAX_AUTORES));

			nomes = query.split("\\,");
			if (nomes.length > 0)
				//sobrenome = new String(nomes[0].trim().getBytes("ISO-8859-1"),"UTF8");
				sobrenome = nomes[0].trim();
			if (nomes.length > 1)
				//nome = new String(nomes[1].trim().getBytes("ISO-8859-1"),"UTF8");
				nome = nomes[1].trim();
				
			String consulta = "SELECT DISTINCT codpes, nome, nomeinicial, sobrenome, unidade_sigla, depto_sigla, funcao from "
					+ DATABASE_TABLE;

			if (notEmpty(nome))
				filtro += " and translate(lower(nomeinicial),'áéíóúâêîôûàèìòùäëïöüãẽĩõũç','aeiouaeiouaeiouaeiouaeiouc') like translate(lower(?),'áéíóúâêîôûàèìòùäëïöüãẽĩõũç','aeiouaeiouaeiouaeiouaeiouc')";

			if (notEmpty(sobrenome))
                                filtro += " and translate(lower(sobrenome),'áéíóúâêîôûàèìòùäëïöüãẽĩõũç','aeiouaeiouaeiouaeiouaeiouc') like translate(lower(?),'áéíóúâêîôûàèìòùäëïöüãẽĩõũç','aeiouaeiouaeiouaeiouaeiouc')";

			consulta += filtro;

			consulta += " order by nome";

			log.debug(" consulta == " + consulta);
                        
                        statement = getReplicaUspDBconnection().prepareStatement(consulta);
                        
                        int pindex = 0;
			if (notEmpty(nome)) statement.setString(++pindex,"%" + nome + "%");
			if (notEmpty(sobrenome)) statement.setString(++pindex,"%" + sobrenome + "%");
                        
                        // rs = RowSetProvider.newFactory().createCachedRowSet();
                        rs = new CachedRowSetImpl();
                        rs.populate(statement.executeQuery());
                        rs.setReadOnly(true);
			
                        int MAX_SIZE = (rs.size() > MAX_AUTORES ? MAX_AUTORES: rs.size());
                        v = new Choice[MAX_SIZE];
			for (int i = 0; i < MAX_SIZE; i++) {
				log.debug(" contador == " + i);
                                if(rs.next()){

                                    // TableRow row = (TableRow) autores.get(i);
                                    
                                    log.debug(String.valueOf(rs.getInt("codpes"))
                                    + rs.getString("sobrenome") + " , "
                                    + rs.getString("nomeinicial") + ","
                                    + rs.getString("nome") + " ("
                                    + rs.getString("unidade_sigla") + "/" 
                                    + rs.getString("depto_sigla") + ")"
                                    + " [" + rs.getString("funcao") + "]");

                                    v[i] = new Choice(String.valueOf(rs.getInt("codpes")),
                                            rs.getString("sobrenome") + ", " + rs.getString("nomeinicial"),
                                            rs.getString("nome") + " ("
                                          + nvl(rs.getString("unidade_sigla"), rs.getString("unidade_sigla"), "- ")
                                          + nvl(rs.getString("depto_sigla"), "/ " + rs.getString("depto_sigla"),"/ -")
                                          + ")"
                                          + nvl(rs.getString("funcao")," [" + rs.getString("funcao") + "]",""));
                                }
                                else {
                                    break;
                                }
			}
                        statement.close();
                        log.debug(" FIM ");
                        return new Choices(v, 0, v.length , Choices.CF_ACCEPTED, true, 0);

		} catch (Exception e) {
                        e.printStackTrace(System.err);
		}
                return null;
	}

        @Override
	public Choices getBestMatch(String field, String text, int collection,
			String locale) {

		Choice v[] = new Choice[1];
		v[0] = new Choice("1", "Nao definido", "Nao definido");
		return new Choices(v, 0, v.length, Choices.CF_UNCERTAIN, false, 0);
	}

        @Override
	public String getLabel(String field, String key, String locale) {
		return "Nao definido";
	}

	public static boolean notEmpty(String s) {
		return (s != null && s.length() > 0);
	}
        
        public static String nvl(Object vi, String vo, String ve){
            if(vi==null){
                return ve;
            }
            else {
                return vo;
            }
        }

}
