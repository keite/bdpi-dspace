/**
 * 
 */
package org.dspace.content.authority;

// import com.sun.rowset.CachedRowSetImpl;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;

/**
 * USP Name Authority
 * 
 * @author Helves Domingues / Jan LL - alterado para uso da base oracle - 17.out.2013*
 * @version $Revision $
 */
public class USPNameAuthority implements ChoiceAuthority {
	private static final Logger log = Logger.getLogger(USPNameAuthority.class);

	// Esquema e tabela onde os dados dos autores estao armazenados
        // codpes,nome, nomeinicial, sobrenome, unidade_sigla, depto_sigla,funcao
        private static final String DATABASE_TABLE = "(SELECT vinculopessoausp.codpes, vinculopessoausp.nompes nome, \n" +
        "regexp_substr(vinculopessoausp.nompes,'(.*)\\s.*',1,1,'i',1) nomeinicial,\n" +
        "nvl(regexp_substr(vinculopessoausp.nompes,'.*\\s(.*)',1,1,'i',1),vinculopessoausp.nompes) sobrenome,\n" +
        "unidade.sglund unidade_sigla,\n" +
        "setor.nomabvset depto_sigla,\n" +
        "vinculopessoausp.tipfnc funcao,\n" +
        "nvl(resuservhistfuncional.dtainisitfun,vinculopessoausp.dtainivin) dtaini,\n" +
        "nvl(resuservhistfuncional.dtafimsitfun,vinculopessoausp.dtafimvin) dtafim\n" +
        "FROM vinculopessoausp\n" +
        "left join resuservhistfuncional on (resuservhistfuncional.codpes = vinculopessoausp.codpes AND vinculopessoausp.tipvin = 'SERVIDOR')\n" +
        "left join unidade on (vinculopessoausp.codund = unidade.codund OR vinculopessoausp.codfusclgund = unidade.codund)\n" +
        "left join setor on (vinculopessoausp.codset = setor.codset))";
                
        public Connection getReplicaUspDBconnection() {
            Connection ocn = null;
            try {
                DriverManager.registerDriver((Driver) Class.forName(ConfigurationManager.getProperty("usp-authorities", "db.driver")).newInstance());

                ocn = DriverManager.getConnection(ConfigurationManager.getProperty("usp-authorities", "db.url"),
                                                  ConfigurationManager.getProperty("usp-authorities", "db.username"),
                                                  ConfigurationManager.getProperty("usp-authorities", "db.password"));
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
		
		// Choice v[] = null;
		PreparedStatement statement = null;
                // CachedRowSetImpl rs = null;
                ResultSet rs = null;
                int MAX_AUTORES = ConfigurationManager.getIntProperty("xmlui.lookup.select.size", 10);
                
		try {
			log.debug(" ==1 Parametros == ");				
			log.debug(" == field == " + field);
			log.debug(" == query == " + query);			
			log.debug(" == collection == " + collection);
			log.debug(" == start == " + start);
			log.debug(" == limit == " + limit);
			log.debug(" == locale == " + locale);
			log.debug(" ================== ");
                        
			String nomes[];
			
			// String sobrenome = "";
			// String nome = "";
                        
			// String filtro = " where rownum <= ".concat(String.valueOf(MAX_AUTORES));
                        
                        HashMap<Integer,String[]> filtro = new HashMap<Integer,String[]>();
                        
                        // filtro.add(" where rownum <= ".concat(String.valueOf(MAX_AUTORES)));

			nomes = query.toLowerCase().split("[^0-9a-záéíóúâêîôûàèìòùäëïöüãõç]+");
                        
                        int pindex = 1;
                        for (String nome : nomes) {
                            try {
                                if (Integer.parseInt(nome) > 0) {
                                    filtro.put(pindex++, new String[]{"codpes = ?",
                                                                      nome,
                                                                      "int"});
                                }
                            } catch (NumberFormatException e) {
                                filtro.put(pindex++, new String[]{"translate(lower(nome),'áéíóúâêîôûàèìòùäëïöüãõç','aeiouaeiouaeiouaeiouaoc') like lower(?)",
                                                                  "%".concat(nome).concat("%"),
                                                                  "string"});
                            }
                        }

                        StringBuilder consulta = new StringBuilder();
                        
                        consulta.append("SELECT DISTINCT codpes, nome, nomeinicial, sobrenome, unidade_sigla, depto_sigla, funcao, dtaini, dtafim from ");
                        consulta.append(DATABASE_TABLE);
                        
                        consulta.append(" WHERE ");
                        if(filtro.isEmpty()) consulta.append("rownum < 0");
                        else {
                            consulta.append("rownum <= ".concat(String.valueOf(MAX_AUTORES)));
                            for(int i = 1; i < pindex; i++){
                                consulta.append(" AND ");
                                consulta.append(filtro.get(i)[0]);
                            }
                            consulta.append(" order by nome");
                        }
                        log.debug(" consulta == " + consulta.toString());
                        Connection caut = getReplicaUspDBconnection();
                        statement = caut.prepareStatement(consulta.toString());
                        for(int i = 1; i < pindex; i++){
                            if(filtro.get(i)[2].equals("int")){
                                statement.setInt(i, Integer.valueOf(filtro.get(i)[1]));
                            }
                            else if(filtro.get(i)[2].equals("string")){
                                statement.setString(i, filtro.get(i)[1]);
                            }
                        }
                        
                        // rs = new CachedRowSetImpl();
                        // rs.populate(statement.executeQuery());
                        // rs.setReadOnly(true);
			
                        rs = statement.executeQuery();
                        
                        // int MAX_SIZE = (rs.size() > MAX_AUTORES ? MAX_AUTORES: rs.size());
                        // v = new Choice[MAX_SIZE];
                        ArrayList<Choice> v = new ArrayList<Choice>();
                        while(rs.next()){                            
                            v.add(new Choice(String.valueOf(rs.getInt("codpes")),
                                    rs.getString("sobrenome") + ", "
                                  + rs.getString("nomeinicial"),
                                    rs.getString("nome")
                                  + " - "
                                  + String.valueOf(rs.getInt("codpes")) + " ("
                                  + nvl(rs.getString("unidade_sigla"),trims(rs.getString("unidade_sigla")), "- ")
                                  + nvl(rs.getString("depto_sigla"), "/ " + trims(rs.getString("depto_sigla")),"/ -")
                                  + ")"
                                  + nvl(rs.getString("funcao")," [" + trims(rs.getString("funcao")) + "]"," ")
                                  + nvl(rs.getDate("dtaini"),"[" + sdfnew(rs.getDate("dtaini")),"[")
                                  + nvl(rs.getDate("dtafim")," a " + sdfnew(rs.getDate("dtafim")) + "]","]")));
                        }
                        statement.close();
                        caut.commit();
                        caut.close();
                        log.debug(" FIM ");
                        return new Choices(v.toArray(new Choice[v.size()]), 0, v.size() , Choices.CF_ACCEPTED, true, 0);

                    } catch (NumberFormatException e) {
                        e.printStackTrace(System.out);
                    } catch (SQLException e) {
                        e.printStackTrace(System.out);
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
        
        public static String sdfnew(java.sql.Date sdfx){
            if(sdfx == null){
                return "";
            }
            else {
                return new SimpleDateFormat("dd/MM/yyyy").format(sdfx);
            }
        }
        
        public static String trims(String x){
            if(x == null){
                return "";
            }
            else {
                return x.trim();
            }
        }

}
