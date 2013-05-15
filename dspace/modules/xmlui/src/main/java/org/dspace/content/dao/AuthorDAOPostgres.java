package org.dspace.content.dao;

import java.util.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.dspace.content.Author;
import org.dspace.content.InterUnit;
import org.dspace.content.ItemRelacionado;
import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;

import java.sql.SQLException;

public class AuthorDAOPostgres extends AuthorDAO
{
    /** Constante para a busca de todos os parametros do autor USP a partir de seu codpes */
    private static final String selectAuthor = "SELECT * FROM usp.viewauthoritycontrol " +
            "WHERE codpes=?";

    /** Constante para a busca do ID do schema dc referente ao 'dc' */
    private static final String selectDCSchemaId = "SELECT metadata_schema_id FROM metadataschemaregistry WHERE short_id='dc'";

    /** Constante para a busca do ID do schema usp referente ao 'usp' */
    private static final String selectUSPSchemaId = "SELECT metadata_schema_id FROM metadataschemaregistry WHERE short_id='usp'";  

    /** Constante para a busca do ID do titulo a partir do id referente ao 'dc' */
    private static final String selectMetadataIdDCTitle = "SELECT metadata_field_id FROM metadatafieldregistry " + 
            "WHERE metadata_schema_id=? AND element='title' AND qualifier is null";

    /** Constante para a busca do ID do tipo a partir do id referente ao 'dc' */
    private static final String selectMetadataIdDCType = "SELECT metadata_field_id FROM metadatafieldregistry " + 
            "WHERE metadata_schema_id=? AND element='type' AND qualifier is null";

    /** Constante para a busca do ID do autor a partir do id referente ao 'usp' */
    private static final String selectMetadataIdUSPAutor = "SELECT metadata_field_id FROM metadatafieldregistry " + 
            "WHERE metadata_schema_id=? AND element='autor' AND qualifier is null";

    /** Constante para a busca do ID do autor externo a partir do id referente ao 'usp' */
    private static final String selectMetadataIdUSPAutorExterno = "SELECT metadata_field_id FROM metadatafieldregistry " +
            "WHERE metadata_schema_id=? AND element='autor' AND qualifier='externo'";

    /** Constante para a busca do ID da data de publicacao a partir do id referente ao 'dc' */
    private static final String selectMetadataIdDataPublicacao = "SELECT metadata_field_id FROM metadatafieldregistry " +
            "WHERE metadata_schema_id=? AND element='date' AND qualifier='issued'";

    /** Constante para a busca de todos os itens a partir dos seguintes parametros: tipo, data e titulo relacionados com o numero USP */
    private static final String selectHandleTitulos = "SELECT handle, A.text_value AS title, B.text_value AS tipo, C.text_value AS dtPub FROM handle " +
            "JOIN metadatavalue A ON (handle.resource_id = A.item_id AND handle.resource_type_id=2 AND A.metadata_field_id=?) " + 
            "JOIN metadatavalue B ON (handle.resource_id = B.item_id AND handle.resource_type_id=2 AND B.metadata_field_id=?) " +
            "JOIN metadatavalue C ON (handle.resource_id = C.item_id AND handle.resource_type_id=2 AND C.metadata_field_id=?) " +
            "JOIN metadatavalue D ON (handle.resource_id = D.item_id AND handle.resource_type_id=2 AND D.metadata_field_id=? " +
            "AND SPLIT_PART(D.text_value,':',2)=?) order by B.text_value, C.text_value DESC, A.text_value";

    /** Constante para armazenar o numero de itens relacionados com um determinado numero USP */
    private static final String selectTotalTitulos = "SELECT COUNT(*) AS total FROM " +
            "(SELECT handle, A.text_value AS title, B.text_value AS tipo, C.text_value AS dtPub FROM handle " +
            "JOIN metadatavalue A ON (handle.resource_id = A.item_id AND handle.resource_type_id=2 AND A.metadata_field_id=?) " +
            "JOIN metadatavalue B ON (handle.resource_id = B.item_id AND handle.resource_type_id=2 AND B.metadata_field_id=?) " +
            "JOIN metadatavalue C ON (handle.resource_id = C.item_id AND handle.resource_type_id=2 AND C.metadata_field_id=?) " +
            "JOIN metadatavalue D ON (handle.resource_id = D.item_id AND handle.resource_type_id=2 AND D.metadata_field_id=? " +
            "AND SPLIT_PART(D.text_value,':',2)=?)) AS contador";

    /** Constante para armazenar as diferentes formas de citacao de um mesmo autor a partir de seu numero USP e seu metadata_field_id */
    private static final String selectCitacaoAutor = "SELECT DISTINCT TRIM(SPLIT_PART(text_value,':',1)) AS citacao FROM metadatavalue " + 
            "WHERE metadata_field_id=? AND  SPLIT_PART(text_value,':',2)=? order by citacao";

    /** Constante para armazenar as diferentes formas de citacao em maiusculas de um mesmo autor a partir de seu numero USP e seu metadata_field_id */
    private static final String selectCitacaoAutorUpper = "SELECT DISTINCT TRIM(UPPER(SPLIT_PART(text_value,':',1))) AS citacao FROM metadatavalue " +
            "WHERE metadata_field_id=? AND  SPLIT_PART(text_value,':',2)=?";

    /** Constante para criar a tabela com os coautores USP relacionados com um determinado autor USP CREATE TEMP TABLE cvcoautorusp AS*/
    private static final String selectCoautoresUSP = "SELECT SPLIT_PART(text_value,':',2) AS nusp, " +
            "SPLIT_PART(text_value,':',1) AS nomeusp, SPLIT_PART(text_value,':',3) AS unidade, COUNT(*) AS ocorr from metadatavalue " +
            "WHERE metadata_field_id=? AND item_id IN (SELECT item_id FROM metadatavalue WHERE metadata_field_id=? " +
            "AND SPLIT_PART(text_value,':',2)=?) AND SPLIT_PART(text_value,':',2)!=? GROUP BY nusp, nomeusp, unidade ORDER BY nusp";

    /** Constante sql para recuperar os diversos coautores externos usp a partir de um numero USP */
    private static final String selectCoautoresUSPExterno = "SELECT text_value FROM metadatavalue WHERE metadata_field_id=? and item_id in " +
           "(SELECT item_id FROM metadatavalue WHERE metadata_field_id=? and SPLIT_PART(text_value,':',2)=?) order by text_value;";

    /** Constante sql para recuperar a interdisciplinariedade que um determinado autor possui com outras unidades USP */
    private static final String selectInterdisciplinarUSP = "SELECT SPLIT_PART(text_value,':',3) as unidade, count(*) as ocorr " +
           "FROM metadatavalue WHERE metadata_field_id=? AND item_id IN (SELECT item_id FROM metadatavalue WHERE metadata_field_id=? " +
           "AND SPLIT_PART(text_value,':',2)=?) AND split_part(text_value,':',2)!=? GROUP BY unidade ORDER BY ocorr DESC, unidade;";

    /** Constante para armazenar os coautores USP relacionados com um determinado autor USP 
    private static final String selectCoautoresUSP = "SELECT * FROM cvcoautorusp";

    /** Constante que apara a tabela de coautores USP criado temporariamente 
    private static final String dropTabelaCvcoautorUSP = "DROP TABLE cvcoautorusp";*/

    private static final int[] VETOR_ID = new int[6]; 

    private Context context;

    public AuthorDAOPostgres(){}

    public AuthorDAOPostgres(Context ctx)
    {
        super(ctx);
        this.context = ctx;
    }

    /** Metodo que retorna o id do schema dc da tabela metadata_schema_registry */
    public int getTotalItensRelacionados(String codpes) throws SQLException {

     Context context = new Context();

     try {
         setVetorId();
         PreparedStatement statement = context.getDBConnection().prepareStatement(selectTotalTitulos);
         statement.setInt(1,this.VETOR_ID[4]);
         statement.setInt(2,this.VETOR_ID[3]);
         statement.setInt(3,this.VETOR_ID[2]);
         statement.setInt(4,this.VETOR_ID[5]);
         statement.setString(5,codpes);
         ResultSet rs = statement.executeQuery();
         rs.next();

         int totalItens = rs.getInt("total");        

         rs.close();
         statement.close();
         context.complete();

         return totalItens;

      } catch(SQLException sql) {

          System.out.println("Erro: no SQL ----" + sql.getMessage() );
          sql.printStackTrace();

       return -1;

       } 
     }


    /** Metodo que retorna o id do schema dc da tabela metadata_schema_registry */
    public int getSchemaDcId() throws SQLException {

      Context context = new Context();

     try {
          PreparedStatement statement = context.getDBConnection().prepareStatement(selectDCSchemaId);
          ResultSet rs = statement.executeQuery();
          rs.next();

          int id = rs.getInt("metadata_schema_id");

          rs.close();
          statement.close();
          context.complete();
           
          return id;

      } catch(SQLException sql) {

          System.out.println("Erro: no SQL ----" + sql.getMessage() );
          sql.printStackTrace();
       
       return -1;

      }
    }

    /** Metodo que retorna o id do schema usp da tabela metadata_schema_registry*/
    public int getSchemaUspId() throws SQLException {

     Context context = new Context();

     try {
          PreparedStatement statement = context.getDBConnection().prepareStatement(selectUSPSchemaId);
          ResultSet rs = statement.executeQuery();
          rs.next();

          int id = rs.getInt("metadata_schema_id");

          rs.close();
          statement.close();
          context.complete();

          return id;

      } catch(SQLException sql) {

          System.out.println("Erro: no SQL ----" + sql.getMessage() );
          sql.printStackTrace();

       return -1;

      } 
    }

    /** Metodo que retorna o id do titulo da tabela metadata_field_registry */
    public int getTituloId() throws SQLException {
  
     Context context = new Context();

     try {
          int schemaDcId = getSchemaDcId(); 
          PreparedStatement statement = context.getDBConnection().prepareStatement(selectMetadataIdDCTitle);
          statement.setInt(1,schemaDcId);
          ResultSet rs = statement.executeQuery();
          rs.next();
          
          int id = rs.getInt("metadata_field_id");

          rs.close();
          statement.close();
          context.complete();

          return id;

      } catch(SQLException sql) {

           System.out.println("Erro: no SQL ----" + sql.getMessage() );
           sql.printStackTrace();

       return -1;

      } 
    }

    /** Metodo que retorna o id do tipo da tabela metadata_field_registry */
    public int getTipoId() throws SQLException {
     
      Context context = new Context();

      try {
           int schemaDcId = getSchemaDcId();
           PreparedStatement statement = context.getDBConnection().prepareStatement(selectMetadataIdDCType);
           statement.setInt(1,schemaDcId);
           ResultSet rs = statement.executeQuery();
           rs.next();

           int id = rs.getInt("metadata_field_id");

           rs.close();
           statement.close();
           context.complete();

           return id;

      } catch(SQLException sql) {

           System.out.println("Erro: no SQL ----" + sql.getMessage() );
           sql.printStackTrace();

           return -1;
      } 
    }

    /** Metodo que retorna o id do autor da tabela metadata_field_registry */
    public int getAutorId() throws SQLException  {

      Context context = new Context();

      try {
           int schemaUspId = getSchemaUspId();
           PreparedStatement statement = context.getDBConnection().prepareStatement(selectMetadataIdUSPAutor);
           statement.setInt(1,schemaUspId);
           ResultSet rs = statement.executeQuery();
           rs.next();

           int id = rs.getInt("metadata_field_id");

           rs.close();
           statement.close();
           context.complete();

          return id;

      } catch(SQLException sql) {

           System.out.println("Erro: no SQL ----" + sql.getMessage() );
           sql.printStackTrace();

           return -1;

      } 
    }
   
    /** Metodo que retorna o id do autor externo da tabela metadata_field_registry */
    public int getAutorExternoId() throws SQLException  {

      Context context = new Context();

      try {
           int schemaUspId = getSchemaUspId();
           PreparedStatement statement = context.getDBConnection().prepareStatement(selectMetadataIdUSPAutorExterno);
           statement.setInt(1,schemaUspId);
           ResultSet rs = statement.executeQuery();
           rs.next();

           int id = rs.getInt("metadata_field_id");

           rs.close();
           statement.close();
           context.complete();

          return id;

      } catch(SQLException sql) {

           System.out.println("Erro: no SQL ----" + sql.getMessage() );
           sql.printStackTrace();

           return -1;

      }
    }
 
    /** Metodo que retorna o id da data da tabela metadata_field_registry */
    public int getDataId() throws SQLException {

      Context context = new Context();

      try {
           int schemaDcId = getSchemaDcId();
           PreparedStatement statement = context.getDBConnection().prepareStatement(selectMetadataIdDataPublicacao);
           statement.setInt(1,schemaDcId);
           ResultSet rs = statement.executeQuery();
           rs.next();

           int id = rs.getInt("metadata_field_id");

           rs.close();
           statement.close();
           context.complete();

           return id;

      } catch(SQLException sql) {

           System.out.println("Erro: no SQL ----" + sql.getMessage() );
           sql.printStackTrace();

           return -1;

      } 
    }

    /** Metodo que armazena ids na seguinte ordem: schema dc, schema usp, data, tipo, titulo e autor */
    public int[] setVetorId() throws SQLException {

      this.VETOR_ID[0] = getSchemaDcId();
      this.VETOR_ID[1] = getSchemaUspId();
      this.VETOR_ID[2] = getDataId();
      this.VETOR_ID[3] = getTipoId();
      this.VETOR_ID[4] = getTituloId();
      this.VETOR_ID[5] = getAutorId();

      return this.VETOR_ID;      
    }

    /** Metodo que retorna todos as formas de citacao de um mesmo autor a partir de seu numero USP  */
    public ArrayList<String> getCitacoesAutor(String codpes) throws SQLException {

      Context context = new Context();
      ArrayList<String> listaCitacoes = new ArrayList<String>();

      try {
         int metadataAutorId = getAutorId();
         PreparedStatement statement = context.getDBConnection().prepareStatement(selectCitacaoAutor);
         statement.setInt(1,metadataAutorId);
         statement.setString(2,codpes);
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
         sql.printStackTrace();

         return null;

      }
    }

    /** Metodo que retorna todos as formas de citacao em MAIUSCULAS de um mesmo autor a partir de seu numero USP  */
    public ArrayList<String> getCitacoesAutorUpper(String codpes) throws SQLException {

      Context context = new Context();
      ArrayList<String> listaCitacoes = new ArrayList<String>();

      try {
         int metadataAutorId = getAutorId();
         PreparedStatement statement = context.getDBConnection().prepareStatement(selectCitacaoAutorUpper);
         statement.setInt(1,metadataAutorId);
         statement.setString(2,codpes);
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
         sql.printStackTrace();

         return null;

      }
    }
   
    /** Metodo que retorna todos os registros dos itens relacionados com um determinado numero USP  */
    public ArrayList<ItemRelacionado> getItensRelacionados(String codpes) throws SQLException {
 
      Context context = new Context();
      ArrayList<ItemRelacionado> listaItens = new ArrayList<ItemRelacionado>();

      try {
         setVetorId();
         PreparedStatement statement = context.getDBConnection().prepareStatement(selectHandleTitulos);
         statement.setInt(1,this.VETOR_ID[4]);
         statement.setInt(2,this.VETOR_ID[3]);
         statement.setInt(3,this.VETOR_ID[2]);
         statement.setInt(4,this.VETOR_ID[5]);
         statement.setString(5,codpes);
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            ItemRelacionado item = new ItemRelacionado();
            item.setAno(rs.getInt("dtPub"));
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
         sql.printStackTrace();

         return null;

      } 
    }
    
    /** Metodo que retorna todos os coautores externos USP a partir do numero USP de um autor */
    public ArrayList<String> getCoautoresExternos(String codpes) throws SQLException {

      Context context = new Context();
      ArrayList<String> listaCoautores = new ArrayList<String>();

      try {

         int metadataFieldUspAutorId = getAutorId();
         int metadataFieldUspAutorExternoId = getAutorExternoId();
         PreparedStatement statement = context.getDBConnection().prepareStatement(selectCoautoresUSPExterno);
         statement.setInt(1,metadataFieldUspAutorExternoId);
         statement.setInt(2,metadataFieldUspAutorId);
         statement.setString(3,codpes);
         ResultSet rs = statement.executeQuery();
     
         String ocorenciaAnterior = "";

         if(rs.next()) {
            ocorenciaAnterior = rs.getString("text_value");
            listaCoautores.add(ocorenciaAnterior);
         }
         while(rs.next()) {
            String coautor = rs.getString("text_value");

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
         sql.printStackTrace();

         return null;

      }
    }

    /** Metodo que retorna todos os coautores USP a partir do numero USP de um autor */
    public ArrayList<Author> getCoautoresUSP(String codpes) throws SQLException {

      Context context = new Context();
      ArrayList<Author> listaCoautores = new ArrayList<Author>();

      try {

         int metadataAutorId = getAutorId();
         PreparedStatement statement = context.getDBConnection().prepareStatement(selectCoautoresUSP);
         statement.setInt(1,metadataAutorId);
         statement.setInt(2,metadataAutorId);
         statement.setString(3,codpes);
         statement.setString(4,codpes);
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
         sql.printStackTrace();

         return null;

      }
    }
	
	/** Metodo que retorna todos as unidades interligadas com um determinado autores USP a partir do numero USP */
    public ArrayList<InterUnit> getInterUnitUSP(String codpes) throws SQLException {

      Context context = new Context();
      ArrayList<InterUnit> listaInterUnidades = new ArrayList<InterUnit>();

      try {

         int metadataAutorId = getAutorId();
         PreparedStatement statement = context.getDBConnection().prepareStatement(selectInterdisciplinarUSP);
         statement.setInt(1,metadataAutorId);
         statement.setInt(2,metadataAutorId);
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
         sql.printStackTrace();

         return null;

      }
    }

    /** Metodo que retorna todos os coautores USP a partir do numero USP de um autor 
    public Map<Integer, Author> getCoautoresUSP(String codpes) throws SQLException {

      Context context = new Context();
      Map<Integer, Author> mapCoautores = new HashMap<Integer, Author>();

      int contador = 1;

      try {
         int metadataAutorId = getAutorId();
         PreparedStatement statement = context.getDBConnection().prepareStatement(selectCoautoresUSP);
         statement.setInt(1,metadataAutorId);
         statement.setInt(2,metadataAutorId);
         statement.setString(3,codpes);
         statement.setString(4,codpes);
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {

            Author coautor = new Author();
  
            coautor.setCodpes(Integer.parseInt(rs.getString("nusp")));
            coautor.setNome(rs.getString("nomeusp"));
            coautor.setUnidadeSigla(rs.getString("unidade"));
            coautor.setQntTrabalhos(rs.getInt("ocorr"));

            mapCoautores.put(contador, coautor);
            contador++;
         }

//          PreparedStatement statementDrop = context.getDBConnection().prepareStatement(dropTabelaCvcoautorUSP);
//          statementDrop.executeQuery();

          rs.close();
          statement.close();
//          statementSelect.close();
  //        statementDrop.close();
          context.complete();

          return mapCoautores;

      } catch(SQLException sql) {
         System.out.println("Erro: no SQL ----" + sql.getMessage() );
         sql.printStackTrace();

         return new HashMap<Integer, Author>();

      }
    }*/

    /** Metodo que retorna um objeto do tipo Autor a partir de seu numero USP */
    public Author getAuthorByCodpes(int codpes) throws SQLException
    {
      Context context = new Context();
      try {
           //Context context = new Context();
           PreparedStatement statement = context.getDBConnection().prepareStatement(selectAuthor);
           statement.setInt(1,codpes);
           ResultSet rs = statement.executeQuery();
           rs.next();

           Author author = new Author();

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
          
           rs.close();
           statement.close();
           context.complete();

           return author;

         } catch(SQLException sql) {

           System.out.println("Erro: no SQL ----" + sql.getMessage() );
           sql.printStackTrace();

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

