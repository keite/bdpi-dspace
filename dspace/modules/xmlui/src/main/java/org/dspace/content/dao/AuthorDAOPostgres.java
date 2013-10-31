package org.dspace.content.dao;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.dspace.content.Author;
import org.dspace.content.InterUnit;
import org.dspace.content.ItemRelacionado;
import org.dspace.core.Context;

import java.sql.SQLException;
import org.dspace.core.ConfigurationManager;

public class AuthorDAOPostgres extends AuthorDAO
{
    /** Constante para a busca de todos os parametros do autor USP a partir de seu codpes */
    private static final String selectAuthor = "SELECT vinculopessoausp.codpes, vinculopessoausp.nompes nome, \n" +
        "nvl(regexp_substr(vinculopessoausp.nompes,'.*\\s(.*)',1,1,'i',1),vinculopessoausp.nompes) sobrenome,\n" +
        "regexp_substr(vinculopessoausp.nompes,'(.*)\\s.*',1,1,'i',1) nomeinicial,\n" +
        "emailpessoa.codema email_1,\n" +
        "unidade.nomund unidade, unidade.sglund unidade_sigla,\n" +
        "setor.nomset depto, setor.nomabvset depto_sigla,\n" +
        "vinculopessoausp.tipvin vinculo, vinculopessoausp.tipfnc funcao, null lattes\n" +
        "FROM vinculopessoausp\n" +
        "left join unidade on (vinculopessoausp.codund = unidade.codund OR vinculopessoausp.codfusclgund = unidade.codund)\n" +
        "left join setor on (vinculopessoausp.codset = setor.codset)\n" +
        "left join colegiado\n" +
        "on ((vinculopessoausp.codclg = colegiado.codclg) AND (vinculopessoausp.sglclg = colegiado.sglclg))\n" +
        "left join emailpessoa on emailpessoa.codpes = vinculopessoausp.codpes\n" +
        "where emailpessoa.stamtr = 'S' and vinculopessoausp.codpes = ?\n" +
        "order by decode(lower(sitctousp),'ativado',0,'não ativado',1,'expirado',2,'suspenso',3,4), decode(substr(lower(tipvin),0,3),'ser',0,'alu',1,'pro',2,'bol',3,'est',4,'ext',5,6), decode(vinculopessoausp.sitatl,'A',1,'P',2,'D',3,'D')";
    
    /** Constante para a busca do ID do autor a partir do id referente ao 'usp' -> alterado para uso do campo authority contendo o numero USP */
    private static final String selectMetadataIdUSPAutor = "SELECT metadata_field_id FROM metadatafieldregistry INNER JOIN metadataschemaregistry ON (metadatafieldregistry.metadata_schema_id=metadataschemaregistry.metadata_schema_id AND metadataschemaregistry.short_id='dc')  WHERE metadatafieldregistry.element='contributor' AND metadatafieldregistry.qualifier='author'";

    /** Constante para a busca de todos os itens a partir dos seguintes parametros: tipo, data e titulo relacionados com o numero USP -> alterado para uso de authority contendo numero usp */
     private static final String selectHandleTitulos = "SELECT handle, TITLES.text_value AS title, TIPOS.text_value AS tipo, DTPUBS.text_value AS dtpub\n" +
"FROM handle \n" +
"INNER JOIN metadatavalue AUTHORS on (handle.resource_id = AUTHORS.item_id AND handle.resource_type_id=2)\n" +
"INNER JOIN metadatavalue TITLES on (handle.resource_id = TITLES.item_id AND handle.resource_type_id=2)\n" +
"INNER JOIN metadatavalue TIPOS on (handle.resource_id = TIPOS.item_id AND handle.resource_type_id=2)\n" +
"INNER JOIN metadatavalue DTPUBS on (handle.resource_id = DTPUBS.item_id AND handle.resource_type_id=2)\n" +
"INNER JOIN metadatafieldregistry MAUTHORS on (AUTHORS.metadata_field_id = MAUTHORS.metadata_field_id AND MAUTHORS.element='contributor' AND MAUTHORS.qualifier='author' AND AUTHORS.authority=?)\n" +
"INNER JOIN metadatafieldregistry MTITLES on (TITLES.metadata_field_id = MTITLES.metadata_field_id AND MTITLES.element='title' AND MTITLES.qualifier is null)\n" +
"INNER JOIN metadatafieldregistry MTIPOS on (TIPOS.metadata_field_id = MTIPOS.metadata_field_id AND MTIPOS.element='type' AND MTIPOS.qualifier is null)\n" +
"INNER JOIN metadatafieldregistry MDTPUBS on (DTPUBS.metadata_field_id = MDTPUBS.metadata_field_id AND MDTPUBS.element='date' AND MDTPUBS.qualifier = 'issued')\n" +
"INNER JOIN metadataschemaregistry on (MAUTHORS.metadata_schema_id = metadataschemaregistry.metadata_schema_id\n" +
"AND MTITLES.metadata_schema_id = metadataschemaregistry.metadata_schema_id\n" +
"AND MTIPOS.metadata_schema_id = metadataschemaregistry.metadata_schema_id\n" +
"AND MDTPUBS.metadata_schema_id = metadataschemaregistry.metadata_schema_id\n" +
"AND metadataschemaregistry.short_id='dc')";
    //      "AND SPLIT_PART(D.text_value,':',2)=?) order by B.text_value, C.text_value DESC, A.text_value";
    
    /** Constante para armazenar o numero de itens relacionados com um determinado numero USP -> alterado para uso de authority contendo numero usp */
    private static final String selectTotalTitulos = "SELECT COUNT(*) AS total FROM (" + selectHandleTitulos + ")";

    /** Constante para armazenar as diferentes formas de citacao de um mesmo autor a partir de seu numero USP e seu metadata_field_id  -> alterado para uso de authority contendo numero usp*/
    private static final String selectCitacaoAutor = "SELECT DISTINCT text_value AS citacao\n" +
"FROM metadatavalue\n" +
"INNER JOIN metadatafieldregistry on (metadatavalue.metadata_field_id=metadatafieldregistry.metadata_field_id\n" +
"AND metadatafieldregistry.element = 'contributor'\n" +
"AND metadatafieldregistry.qualifier = 'author')\n" +
"INNER JOIN metadataschemaregistry on (metadatafieldregistry.metadata_schema_id=metadataschemaregistry.metadata_schema_id\n" +
"AND metadataschemaregistry.short_id = 'dc')\n" +
"WHERE metadatavalue.authority=?\n" +
"order by citacao";

    /** Constante para armazenar as diferentes formas de citacao em maiusculas de um mesmo autor a partir de seu numero USP e seu metadata_field_id  -> alterado para uso de authority contendo numero usp*/
    private static final String selectCitacaoAutorUpper = "SELECT DISTINCT upper(text_value) AS citacao\n" +
"FROM metadatavalue\n" +
"INNER JOIN metadatafieldregistry on (metadatavalue.metadata_field_id=metadatafieldregistry.metadata_field_id\n" +
"AND metadatafieldregistry.element = 'contributor'\n" +
"AND metadatafieldregistry.qualifier = 'author')\n" +
"INNER JOIN metadataschemaregistry on (metadatafieldregistry.metadata_schema_id=metadataschemaregistry.metadata_schema_id\n" +
"AND metadataschemaregistry.short_id = 'dc')\n" +
"WHERE metadatavalue.authority=?\n" +
"order by citacao";

    /** Constante para criar a tabela com os coautores USP relacionados com um determinado autor USP CREATE TEMP TABLE cvcoautorusp AS*/
    private static final String selectCoautoresUSP = "SELECT COAUTORES.authority AS nusp, COAUTORES.text_value AS nomeusp, 'indisponível' AS unidade, count(*) AS ocorr\n" +
"FROM metadatavalue COAUTORES\n" +
"INNER JOIN metadatavalue AUTORES on (COAUTORES.item_id = AUTORES.item_id "
            + "AND COAUTORES.authority IS NOT NULL AND AUTORES.authority = ? "
            + "AND COAUTORES.authority != AUTORES.authority) " +
"INNER JOIN metadatafieldregistry on (AUTORES.metadata_field_id=metadatafieldregistry.metadata_field_id "
            + "AND COAUTORES.metadata_field_id=metadatafieldregistry.metadata_field_id "
            + "AND metadatafieldregistry.element = 'contributor' "
            + "AND metadatafieldregistry.qualifier = 'author') " +
"INNER JOIN metadataschemaregistry on (metadatafieldregistry.metadata_schema_id=metadataschemaregistry.metadata_schema_id " +
"AND metadataschemaregistry.short_id = 'dc') " +
"GROUP BY COAUTORES.authority, COAUTORES.text_value " +
"ORDER BY ocorr DESC, nomeusp";

    /** Constante sql para recuperar os diversos coautores externos usp a partir de um numero USP */
    private static final String selectCoautoresUSPExterno = "SELECT DISTINCT TUPLAE.text_value tuplax "
            + "FROM metadatavalue TUPLAE " +
"INNER JOIN metadatafieldregistry MFTE on (TUPLAE.metadata_field_id=MFTE.metadata_field_id " +
"            AND MFTE.element = 'autor' " +
"            AND MFTE.qualifier = 'externo') " +
"INNER JOIN metadataschemaregistry MSTE on (MFTE.metadata_schema_id=MSTE.metadata_schema_id " +
"                                       AND MSTE.short_id = 'usp') " +
"INNER JOIN metadatavalue COAUTORES on (TUPLAE.item_id = COAUTORES.item_id) " +
"INNER JOIN metadatafieldregistry MFAX on (COAUTORES.metadata_field_id=MFAX.metadata_field_id " +
"            AND MFAX.element = 'contributor' " +
"            AND MFAX.qualifier = 'author') " +
"INNER JOIN metadataschemaregistry MSAX on (MFAX.metadata_schema_id=MSAX.metadata_schema_id " +
"                                       AND MSAX.short_id = 'dc') " +
"WHERE COAUTORES.authority = ? " +
"ORDER BY tuplax";

    /** Constante sql para recuperar a interdisciplinariedade que um determinado autor possui com outras unidades USP */
    private static final String selectInterdisciplinarUSP = "SELECT SPLIT_PART(text_value,':',3) as unidade, count(*) as ocorr " +
           "FROM metadatavalue WHERE metadata_field_id=? AND item_id IN (SELECT item_id FROM metadatavalue WHERE metadata_field_id=? " +
           "AND SPLIT_PART(text_value,':',2)=?) AND split_part(text_value,':',2)!=? GROUP BY unidade ORDER BY ocorr DESC, unidade";

    public AuthorDAOPostgres() {
    }

    public AuthorDAOPostgres(Context ctx)
    {
        super(ctx);
        this.context = ctx;
    }

    /** Metodo que retorna o id do schema dc da tabela metadata_schema_registry */
    public int getTotalItensRelacionados(String codpes) throws SQLException {
        try {
            context = new Context();
            PreparedStatement statement = context.getDBConnection().prepareStatement(selectTotalTitulos);
            statement.setString(1,codpes);
            ResultSet rs = statement.executeQuery();
            int totalItens = -1;		 
            if(rs.next()) totalItens = rs.getInt("total");
            rs.close();
            statement.close();
            context.complete();
            return totalItens;
         } catch(SQLException sql) {
            System.out.println("Erro: no SQL ----" + sql.getMessage() );
            sql.printStackTrace(System.out);
            return -1;
          } 
     }

    /** Metodo que retorna o id do autor da tabela metadata_field_registry */
    private int getAutorId() throws SQLException  {
      try {
            context = new Context();
            PreparedStatement statement = context.getDBConnection().prepareStatement(selectMetadataIdUSPAutor);
           
           ResultSet rs = statement.executeQuery();
           
		   int id = -1;
		   
		   if(rs.next()) {

			id = rs.getInt("metadata_field_id");
			}

           rs.close();
           statement.close();
           context.complete();

           return id;

      } catch(SQLException sql) {

           System.out.println("Erro: no SQL ----" + sql.getMessage() );
           sql.printStackTrace(System.out);

           return -1;

      } 
    }

    /** Metodo que retorna todos as formas de citacao de um mesmo autor a partir de seu numero USP  */
    public ArrayList<String> getCitacoesAutor(String codpes) throws SQLException {
      ArrayList<String> listaCitacoes = new ArrayList<String>();
      try {
        context = new Context();
         PreparedStatement statement = context.getDBConnection().prepareStatement(selectCitacaoAutor);
         statement.setString(1,codpes);
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            String citacao = rs.getString("citacao");
            
            listaCitacoes.add(citacao);
         }

          rs.close();
          statement.close();
          context.complete();

          return listaCitacoes;

      } catch(SQLException sql) {
         System.out.println("Erro: no SQL ----" + sql.getMessage() );
         sql.printStackTrace(System.out);

         return null;

      }
    }

    /** Metodo que retorna todos as formas de citacao em MAIUSCULAS de um mesmo autor a partir de seu numero USP  */
    public ArrayList<String> getCitacoesAutorUpper(String codpes) throws SQLException {
      ArrayList<String> listaCitacoes = new ArrayList<String>();
      try {
        context = new Context();
         PreparedStatement statement = context.getDBConnection().prepareStatement(selectCitacaoAutorUpper);
         statement.setString(1,codpes);
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            String citacao = rs.getString("citacao");

            listaCitacoes.add(citacao);
         }

          rs.close();
          statement.close();
          context.complete();

          return listaCitacoes;

     } catch(SQLException sql) {
         System.out.println("Erro: no SQL ----" + sql.getMessage() );
         sql.printStackTrace(System.out);

         return null;

      }
    }
   
    /** Metodo que retorna todos os registros dos itens relacionados com um determinado numero USP  */
    public ArrayList<ItemRelacionado> getItensRelacionados(String codpes) throws SQLException {
      ArrayList<ItemRelacionado> listaItens = new ArrayList<ItemRelacionado>();
      try {
        context = new Context();
         PreparedStatement statement = context.getDBConnection().prepareStatement(selectHandleTitulos);
         statement.setString(1,codpes);
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            ItemRelacionado item = new ItemRelacionado();
            item.setAno(rs.getString("dtPub"));
            item.setTitulo(rs.getString("title"));
            item.setTipo(rs.getString("tipo"));
            item.setHandle(rs.getString("handle"));
 
            listaItens.add(item);
         }

          rs.close();
          statement.close();
          context.complete();

          return listaItens;

      } catch(SQLException sql) {
         System.out.println("Erro: no SQL ----" + sql.getMessage() );
         sql.printStackTrace(System.out);

         return null;

      } 
    }
    
    /** Metodo que retorna todos os coautores externos USP a partir do numero USP de um autor */
    public ArrayList<String> getCoautoresExternos(String codpes) throws SQLException {

      ArrayList<String> listaCoautores = new ArrayList<String>();

      try {
          context = new Context();
         PreparedStatement statement = context.getDBConnection().prepareStatement(selectCoautoresUSPExterno);
         statement.setString(1,codpes);
         ResultSet rs = statement.executeQuery();
     
         String ocorenciaAnterior = "";

         if(rs.next()) {
            ocorenciaAnterior = rs.getString("tuplax");
            listaCoautores.add(ocorenciaAnterior);
         }
         while(rs.next()) {
            String coautor = rs.getString("tuplax");

            if(!coautor.equals(ocorenciaAnterior)) {
               listaCoautores.add(coautor);
               ocorenciaAnterior = coautor;
            }
         }    

         rs.close();
         statement.close();
         context.complete();

         return listaCoautores;

      } catch(SQLException sql) {
         System.out.println("Erro: no SQL ----" + sql.getMessage() );
         sql.printStackTrace(System.out);

         return null;

      }
    }

    /** Metodo que retorna todos os coautores USP a partir do numero USP de um autor */
    public ArrayList<Author> getCoautoresUSP(String codpes) throws SQLException {

      
      ArrayList<Author> listaCoautores = new ArrayList<Author>();

      try {
          context = new Context();
         PreparedStatement statement = context.getDBConnection().prepareStatement(selectCoautoresUSP);
         statement.setString(1,codpes);
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {

            Author coautor = new Author();

            coautor.setCodpes(Integer.parseInt(rs.getString("nusp")));
            coautor.setNomeCompleto(rs.getString("nomeusp"));
            coautor.setUnidadeSigla(rs.getString("unidade"));
            coautor.setQntTrabalhos(rs.getInt("ocorr"));
            
            listaCoautores.add(coautor);
         }

         rs.close();
         statement.close();
         context.complete();

         return listaCoautores;

      } catch(SQLException sql) {
         System.out.println("Erro: no SQL ----" + sql.getMessage() );
         sql.printStackTrace(System.out);

         return null;

      }
    }
	
    /** Metodo que retorna todos as unidades interligadas com um determinado autores USP a partir do numero USP
     * @param codpes
     * @return  */
    public ArrayList<InterUnit> getInterUnitUSP(String codpes) throws SQLException {
      ArrayList<InterUnit> listaInterUnidades = new ArrayList<InterUnit>();
      try {
         // int metadataAutorId = getAutorId();
         int metadataAutorId = 81;
         context = new Context();
         PreparedStatement statement = context.getDBConnection().prepareStatement(selectInterdisciplinarUSP);
         statement.setInt(1,metadataAutorId);//SELECT metadata_field_id FROM metadatafieldregistry WHERE metadata_schema_id=(SELECT metadata_schema_id FROM metadataschemaregistry WHERE short_id='dc') AND element='contributor' AND qualifier='author'
         statement.setInt(2,metadataAutorId);//SELECT metadata_field_id FROM metadatafieldregistry WHERE metadata_schema_id=(SELECT metadata_schema_id FROM metadataschemaregistry WHERE short_id='dc') AND element='contributor' AND qualifier='author'
         statement.setString(3,codpes);
         statement.setString(4,codpes);
         ResultSet rs = statement.executeQuery();
         while(rs.next()) {
            InterUnit unidade = new InterUnit();
            //unidade.setCodpes(Integer.parseInt(codpes));
            unidade.setUnidadeSigla(rs.getString("unidade"));
            unidade.setQntTrabalhos(rs.getInt("ocorr"));
            listaInterUnidades.add(unidade);
         }
         rs.close();
         statement.close();
         context.complete();
         return listaInterUnidades;
      } catch(SQLException sql) {
         System.out.println("Erro: no SQL ----" + sql.getMessage() );
         sql.printStackTrace(System.out);
         return null;
      }
    }
    
    public Connection getReplicaUspDBconnection() {
        Connection ocn = null;
        try {
            DriverManager.registerDriver((Driver) Class.forName(ConfigurationManager.getProperty("usp-authorities", "db.driver")).newInstance());
            ocn = DriverManager.getConnection(ConfigurationManager.getProperty("usp-authorities", "db.url"),
                                              ConfigurationManager.getProperty("usp-authorities", "db.username"),
                                              ConfigurationManager.getProperty("usp-authorities", "db.password"));
        }
        catch(ClassNotFoundException e){
            System.out.println("[inicio - ClassNotFound] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
            e.printStackTrace(System.out);
            System.out.println("[fim - ClassNotFound] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
        }
        catch(InstantiationException e){
            System.out.println("[inicio - Instantiation] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
            e.printStackTrace(System.out);
            System.out.println("[fim - Instantiation] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
        }
        catch(IllegalAccessException e){
            System.out.println("[inicio - IllegalAccess] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
            e.printStackTrace(System.out);
            System.out.println("[fim - IllegalAccess] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
        }
        catch(SQLException e){
            System.out.println("[inicio - SQL] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
            e.printStackTrace(System.out);
            System.out.println("[fim - SQL] erro em AuthorDAOPostgres.getReplicaUspDBconnection");
        }
        return ocn;
    }

    /** Metodo que retorna um objeto do tipo Autor a partir de seu numero USP
     * @param codpes
     * @return  */
    public Author getAuthorByCodpes(int codpes) throws SQLException
    {
      try {
            Connection cabc = getReplicaUspDBconnection();
            PreparedStatement statement = cabc.prepareStatement(selectAuthor);
            statement.setInt(1,codpes);
            ResultSet rs = statement.executeQuery();
            Author author = null;
            if(rs.next()) {
                    author = new Author();
                    author.setCodpes(rs.getInt("codpes"));
                    author.setNome(rs.getString("nome"));
                    author.setEmail_1(rs.getString("email_1"));
                    author.setSobrenome(rs.getString("sobrenome"));
                    author.setNomeInicial(rs.getString("nomeinicial"));
                    author.setUnidade(rs.getString("unidade"));
                    author.setUnidadeSigla(rs.getString("unidade_sigla"));
                    author.setDepto(rs.getString("depto"));
                    author.setDeptoSigla(rs.getString("depto_sigla"));
                    author.setVinculo(rs.getString("vinculo"));
                    author.setFuncao(rs.getString("funcao"));
                    author.setLattes(rs.getString("lattes"));
            }
            rs.close();
            statement.close();
            cabc.commit();
            cabc.close();
            return author;
         } catch(SQLException sql) {
           System.out.println("Erro: no SQL ----" + sql.getMessage() );
           sql.printStackTrace(System.out);
         }
         return null;
    }
    //Metodo criado para efeitos de testes na conexao com o database
    /**public static void main (String args[]) throws SQLException {

       Scanner input = new Scanner(System.in);

       System.out.print("Informe o numero USP: ");
       int codpes = input.nextInt();

       Author author = new Author();
   
       AuthorDAOPostgres ap = new AuthorDAOPostgres();

       author =  ap.getAuthorByCodpes(codpes);

       System.out.println("Nome: " + author.getNome());
       System.out.println("Sobrenome: " + author.getSobrenome());
       System.out.println("Departamento: " + author.getDepto());
       System.out.println("Funcao: " + author.getFuncao());
       System.out.println("Vinculo: " + author.getVinculo());
       System.out.println("Lattes: " + author.getLattes());

    }*/
}

