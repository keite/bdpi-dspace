package org.dspace.app.xmlui.aspect.artifactbrowser;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import java.sql.SQLException;
import java.lang.Integer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.imageio.ImageIO;

import org.im4java.core.IMOperation;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.process.ProcessStarter;

import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Figure;
import org.dspace.app.xmlui.wing.element.Table;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Cell;
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.app.xmlui.wing.element.Head;
import org.dspace.content.Author;
import org.dspace.content.ItemRelacionado;
import org.dspace.content.dao.AuthorDAOPostgres;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.xml.sax.SAXException;

/**
 * Cria a pagina no qual apresentara informacoes de coautores.
 * 
 * 
 * @author Dan Shinkai - 120905
 */
public class CoauthorView extends AbstractDSpaceTransformer implements CacheableProcessingComponent
{
    /** Mensagens que deverao aparecer dependendo do idioma do browser */
    private static final Message T_title =
        message("xmlui.ArtifactBrowser.CoauthorView.title_page");
    
    private static final Message T_dspace_home =
        message("xmlui.general.dspace_home");

    private static final Message T_trail = 
        message("xmlui.ArtifactBrowser.AuthorView.trail");

    private static final Message T_trail_item =
        message("xmlui.ArtifactBrowser.ItemViewer.trail");

    private static final Message T_trail_coauthor =
        message("xmlui.ArtifactBrowser.CoauthorView.trail");

    private static final Message T_title_bdpi =
        message("xmlui.ArtifactBrowser.CoauthorView.title_bdpi");

    private static final Message T_return = 
        message("xmlui.general.return");

    private static final Message T_close =
        message("xmlui.general.close");

    private static final Message T_coautor_nao_encontrado =
        message("xmlui.ArtifactBrowser.CoauthorView.coauthor_not_found");

    private static final Message T_nao_coautorUSP =
        message("xmlui.ArtifactBrowser.CoauthorView.not_coauthorUSP");

    private static final Message T_nao_coautorExt =
        message("xmlui.ArtifactBrowser.CoauthorView.not_coauthorExt");

    private static final Message T_titulo_seq =
        message("xmlui.ArtifactBrowser.CoauthorView.title_sequence");

    private static final Message T_titulo_nome =
        message("xmlui.ArtifactBrowser.CoauthorView.title_name");

    private static final Message T_titulo_unidade =
        message("xmlui.ArtifactBrowser.CoauthorView.title_unit");

     private static final Message T_titulo_trabalhos =
        message("xmlui.ArtifactBrowser.CoauthorView.title_count");

    private static final Message T_coauthorUSP_head = 
        message("xmlui.ArtifactBrowser.CoauthorView.coauthorUSP_head");

     private static final Message T_coauthorExterno_head =
        message("xmlui.ArtifactBrowser.CoauthorView.coauthor_external_head");

    private static final Message T_titulo_nome_coautor =
        message("xmlui.ArtifactBrowser.CoauthorView.coauthor_external_title_name");    

    private static final Message T_aname_top = 
        message("xmlui.ArtifactBrowser.CoauthorView.aname_top");

    private static final Message T_aname_coauthorUSP =
        message("xmlui.ArtifactBrowser.CoauthorView.aname_coauthorUSP");

    private static final Message T_aname_coauthorExt =
        message("xmlui.ArtifactBrowser.CoauthorView.aname_coauthor_external");

    private static final Message T_aname_fim =
        message("xmlui.ArtifactBrowser.CoauthorView.aname_end");

    /** Parametro que armazenara o codpes da pessoa diretamente da pagina como um int */
    private int codpes;

    private String dspaceUrl = ConfigurationManager.getProperty("handle.canonical.prefix");
    private String handlePrefix = ConfigurationManager.getProperty("handle.prefix");

    private AuthorDAOPostgres ap = new AuthorDAOPostgres();
    
    /**
     * 
     * This key must be unique inside the space of this component.
     */
    public Serializable getKey() 
    {
       return "1";
    }

    /**
     * Generate the cache validity object.
     */
    public SourceValidity getValidity() 
    {
        return NOPValidity.SHARED_INSTANCE;
    }
    
    
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (!(dso instanceof Item))
        {
            return;
        }

        Item item = (Item) dso;

        String itemIDStr = parameters.getParameter("itemID","");
        String codpesStr = parameters.getParameter("codpes","");

   /** Converte para inteiro a String passada */
        setCodpesFromPage(codpesStr);

   /** Recupera o objeto do autor a partir de seu codpes */
        Author author = this.ap.getAuthorByCodpes(this.codpes);

        if(author != null) {

          pageMeta.addMetadata("title").addContent(T_title);

        }
 
   /** Codigo que cria a parte dos links do Trail */
       pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
       HandleUtil.buildHandleTrail(item,pageMeta,contextPath);
       pageMeta.addTrailLink(contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "?show=full",T_trail_item);
       pageMeta.addTrailLink(contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/author",T_trail);
       pageMeta.addTrail().addContent(T_trail_coauthor);
    }

   /**
    * Metodo pre-definido para a criacao de tags HTML dentro da tag <body>
   */
    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {

   /** Classe que cria uma tag <div> no qual os parametros passados sao o id e a class da <div> */
        Division geral = body.addDivision("id_body_geral", "class_body_autor");

   /** Armazena o codpes e o numero do item recuperadas da URL */
        String itemIDStr = parameters.getParameter("itemID","");
        String codpesStr = parameters.getParameter("codpes","");

   /** Armazena o nome do DSpace para ser utilizado no caminho do diretorio */
        String name = ConfigurationManager.getProperty("dspace.name");

   /** Esta variavel sera usada como parametro no @rend para criar a tag <a name> */
        String aName = "aname";

   /** Estas variaveis serao utilizadas para interligar cada a name */
        String topo = "topo";
        String coautoriaUSP = "coautoriaUSP";
        String coautoriaExt = "coautoriaExt";
        String fim = "fim";

   /** Estas variaveis irao definir a localizacao de cada a name */
        Para topoAname = null;
        Para coautoriaUSPAname = null;
        Para coautoriaExtAname = null;
        Para fimAname = null;

        /*Request request = ObjectModelHelper.getRequest(objectModel);
        String urlAnterior = request.getHeader("referer");*/

//        String urlAutor = this.dspaceUrl + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/" + "author";

   /** Armazena a URL do autor */
        String urlAutor = this.dspaceUrl + this.handlePrefix  + "/" + itemIDStr + "/";

   /** Recupera o objeto Autor */
        Author autor = this.ap.getAuthorByCodpes(Integer.parseInt(codpesStr));

        //String tituloBDPI = (String) T_title_bdpi;

        //geral.setHead(tituloBDPI + autor.getSobrenome().toUpperCase());
    if(autor != null) {
   /** Define o titulo localizado na parte superior da pagina */
        Head headGeral = geral.setHead();
        headGeral.addContent(T_title_bdpi);
        headGeral.addContent(" " + autor.getSobrenome().toUpperCase());

   /** Recupera a lista de coautores */
        ArrayList<Author> listaCoautores = this.ap.getCoautoresUSP(codpesStr);
        ArrayList<String> listaCoautoresExternos = ap.getCoautoresExternos(codpesStr);

           if((listaCoautores != null && listaCoautores.size() > 0)||(listaCoautoresExternos != null && listaCoautoresExternos.size() > 0)) {
             
   /** Instancia uma lista que sera utilizada como lista final */
              ArrayList<Author> listaCoautoresTemp = new ArrayList<Author>();

   /** Variaveis que armazenarao o numero USP para comparacao e um contador de lista para mostrar no campo de sequencia */
              int numeroUSP = 0;
              int contadorLista = 0;

   /** Adiciona uma <div> na <div> geral */
              Division informacoesCoautoresUSP = geral.addDivision("id_informacoes_coautores","class_informacoes_coautores");

   /** Adiciona uma <div> para definir a localizacao do <a name> */
              Division aNameCoautoresUSP = informacoesCoautoresUSP.addDivision("id_aname_coautoresUSP","class_aname_coautoresUSP");

   /** Adiciona as divs para a criacao do titulo acima da tabela e a propria tabela */
              Division informacoesCoautoresUSPHead = informacoesCoautoresUSP.addDivision("id_informacoes_coautoresUSP_head","class_informacoes_coautoresUSP_head");
              Division informacoesTabelaCoautoresUSP = informacoesCoautoresUSP.addDivision("id_informacoes_tabela_coautoresUSP","class_informacoes_tabela_coautoresUSP");

   /** A ideia descrita acima tambem e seguida nas proximas divs abaixo */
              Division informacoesCoautoresExternos = geral.addDivision("id_informacoes_coautores_externos","class_informacoes_coautores_externos");
              Division aNameCoautoreExternos = informacoesCoautoresExternos.addDivision("id_aname_coautoresExt","class_aname_coautoresExt");
              Division informacoesCoautoresExternosHead = informacoesCoautoresExternos.addDivision
                                                                          ("id_informacoes_coautores_externos_head","class_informacoes_coautores_externos_head");
              Division informacoesTabelaCoautoresExternos = informacoesCoautoresExternos.addDivision
                                                     ("id_informacoes_tabela_coautores_externos","class_informacoes_tabela_coautores_externos");

   /** Adiciona um <p> para poder criar o <a name> e mostrar o link. */ 
              topoAname = aNameCoautoresUSP.addPara("id_paragrafo_aname_topoPara", "class_paragrafo_aname_topoPara");
              coautoriaUSPAname = aNameCoautoresUSP.addPara("id_paragrafo_aname_coautoriaUSPPara", "class_paragrafo_aname_coautoriaUSPPara");
              //coautoriaExtAname = aNameCoautoresUSP.addPara("id_paragrafo_aname_coautoriaExtPara", "class_paragrafo_aname_coautoriaExtPara");

              topoAname.addXref(topo,"",aName);
              coautoriaUSPAname.addXref(coautoriaUSP,"",aName);
              //coautoriaExtAname.addXref(coautoriaExt,"",aName);

   /** Cria uma tabela com os links dos <a name>  */
              Table tabelaAnameCoautoresUSP = aNameCoautoresUSP.addTable("id_tblAName_coautoresUSP", 1, 2, "class_tblAName_coautoresUSP");

              Row rowAname = tabelaAnameCoautoresUSP.addRow("id_row_aname_coautoresUSP", Row.ROLE_DATA, "class_row_aname_coautoresUSP");

              Cell cellAnameCoautoriaUSPExt = rowAname.addCell("id_cols_aname_coautoriaExt_field", Cell.ROLE_DATA, "class_cols_aname_coautoriaExt_field");
              cellAnameCoautoriaUSPExt.addXref("#" + coautoriaExt,T_aname_coauthorExt,"id_aname_coautorExt_link");

              Cell cellAnameTopo = rowAname.addCell("id_cols_aname_topo_field", Cell.ROLE_DATA, "class_cols_aname_topo_field");
              cellAnameTopo.addXref("#" + fim,T_aname_fim,"id_aname_topo_link");

              /**Cell cellAnameCoautoriaUSP = rowAname.addCell("id_cols_aname_coautoriaUSP_field", Cell.ROLE_DATA, "class_cols_aname_coautoriaUSP_field");
              cellAnameCoautoriaUSP.addXref("#" + coautoriaUSP,T_aname_coauthorUSP,"id_aname_coautorUSP_link");*/

              /**Cell cellAnameCoautoriaUSPExt = rowAname.addCell("id_cols_aname_coautoriaExt_field", Cell.ROLE_DATA, "class_cols_aname_coautoriaExt_field");
              cellAnameCoautoriaUSPExt.addXref("#" + coautoriaExt,T_aname_coauthorExt,"id_aname_coautorExt_link");*/

   /** Adiciona o titulo para a lista de coautores USP */
              Para head = informacoesCoautoresUSPHead.addPara("id_paragrafo_coautoresUSP_head", "class_paragrafo_coautoresUSP_head");
              head.addContent(T_coauthorUSP_head);

   /** Armazena o primeiro item da lista original para a lista final para poder realizar as comparacoes com o restante dos coautores */
              
   	      if(listaCoautores.size() > 0) {
    	        listaCoautoresTemp.add(listaCoautores.get(0));

   /** O primeiro Autor e seu numero USP sao recuperados  */
                Author coautorTemp = (Author) listaCoautoresTemp.get(0);
                numeroUSP = coautorTemp.getCodpes();
	      }

   /** Para ficar claro exatamente o index da lista, nao foi utilizado o for aprimorado */
              for(int contador = 1; contador < listaCoautores.size(); contador++) {
              
   /** Recupera o Autor da lista original comecando do primeiro. Como o SQl devolve os coautores em ordem de numero USP, entao
       realiza a comparacao com o numero USP armazenado anteriormente. 
  */
                 Author coautor = (Author) listaCoautores.get(contador);

                 if(coautor != null) {
         
                    if(coautor.getCodpes() != numeroUSP) {
                       contadorLista++;
                       listaCoautoresTemp.add(coautor);
                       numeroUSP = coautor.getCodpes();
                    }

                    else {
                    
                     //  if(contador != 0) {                 
                         int qntTrabalhos = coautor.getQntTrabalhos();
                         coautor = (Author) listaCoautoresTemp.get(contadorLista);
                         qntTrabalhos = qntTrabalhos + coautor.getQntTrabalhos();
                         coautor.setQntTrabalhos(qntTrabalhos);
                         listaCoautoresTemp.set(contadorLista,coautor);
                     // }
                    }
                 }
              }

              int qntCoautores = listaCoautoresTemp.size();
			  
	      if(qntCoautores == 0) { qntCoautores = 2; }

              Collections.sort(listaCoautoresTemp, new Author());
    
              Table tabelaCoautoresUSP = informacoesTabelaCoautoresUSP.addTable("id_tblLista_coautoresUSP", qntCoautores, 4, "class_tblLista_coautoresUSP");

              Row rowTituloCoautorUSPColuna = tabelaCoautoresUSP.addRow("id_row_nome_coautor_colunas", Row.ROLE_DATA, "class_row_nome_coautor_colunas");

              Cell cellSeqCoautorUSP = rowTituloCoautorUSPColuna.addCell("id_cols_seq_coautor", Cell.ROLE_DATA, "class_cols_seq_coautor");
              cellSeqCoautorUSP.addContent(T_titulo_seq);       

              Cell cellNomeCoautorUSP = rowTituloCoautorUSPColuna.addCell("id_cols_nome_coautor", Cell.ROLE_DATA, "class_cols_nome_coautor");
              cellNomeCoautorUSP.addContent(T_titulo_nome);

              Cell cellUnidadeCoautorUSP = rowTituloCoautorUSPColuna.addCell("id_cols_unidade_coautor", Cell.ROLE_DATA, "class_cols_unidade_coautor");
              cellUnidadeCoautorUSP.addContent(T_titulo_unidade);

              Cell cellTrabalhosCoautorUSP = rowTituloCoautorUSPColuna.addCell("id_cols_trabalhos_coautor", Cell.ROLE_DATA, "class_cols_trabalhos_coautor");
              cellTrabalhosCoautorUSP.addContent(T_titulo_trabalhos);

              contadorLista = 1;
			  
    	      if(listaCoautores.size() > 0) {

                for(Author coautor : listaCoautoresTemp) {

                   if(coautor!= null) {

                      String nome = coautor.getNomeCompleto();
                      String unidade = coautor.getUnidadeSigla();
                      int trabalhos = coautor.getQntTrabalhos();
                      int codpes = coautor.getCodpes();

                      Row rowCoautor = tabelaCoautoresUSP.addRow("id_row_coautor", Row.ROLE_DATA, "class_row_coautor");
                     
                      Cell cellSeqCoautor = rowCoautor.addCell("id_cols_seq_field", Cell.ROLE_DATA, "class_cols_seq_field");
                      cellSeqCoautor.addContent(contadorLista);

                      Cell cellNomeCoautor = rowCoautor.addCell("id_cols_nome_field", Cell.ROLE_DATA, "class_cols_nome_field");
//                    cellNomeCoautor.addContent(nome);
                      cellNomeCoautor.addXref
                              (contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "/" + String.valueOf(codpes) + "/" + 
                               "author", nome, "_blank", "_blank");

                      Cell cellUnidadeCoautor = rowCoautor.addCell("id_cols_unidade_field", Cell.ROLE_DATA, "class_cols_unidade_field");
                      cellUnidadeCoautor.addContent(unidade);

                      Cell cellTrabalhosCoautor = rowCoautor.addCell("id_cols_trabalhos_field", Cell.ROLE_DATA, "class_cols_trabalhos_field");
                      cellTrabalhosCoautor.addContent(trabalhos);

                      contadorLista++;
                   }
				 }
			   }
			   
			   else {
			     Row rowCoautor = tabelaCoautoresUSP.addRow("id_row_NaoCoautoriaUSP", Row.ROLE_DATA, "class_row_NaoCoautoriaUSP");
                     
                   Cell cellNaoHaCoautor = rowCoautor.addCell("id_cols_NaoCoautoriaUSP_field", Cell.ROLE_DATA, 1, 4, "class_cols_NaoCoautoriaUSP_field");
                   cellNaoHaCoautor.addContent(T_nao_coautorUSP);
			   }
			   
/** O codigo que resolve o problema com o aname. Por alguma razao, sem uma forma de manter um espaco entra a div de cima e a div que contem
    o aname, o mesmo nao funciona.
**/
              Division funcionaAname = informacoesCoautoresUSP.addDivision("id_div_funciona_aname","class_div_funciona_aname");
              funcionaAname.addSimpleHTMLFragment(false, " ");
            
               
//              ArrayList<String> listaCoautoresExternos = ap.getCoautoresExternos(codpesStr);
              int totalCoautoresExternos = listaCoautoresExternos.size();
			  
			  if(totalCoautoresExternos == 0) { totalCoautoresExternos = 2; }
              
                Table tabelaAnameCoautoresExt = aNameCoautoreExternos.addTable("id_tblAName_coautoresExt", 1, 3, "class_tblAName_coautoresExt");

                Row rowAnameCoautoresExt = tabelaAnameCoautoresExt.addRow("id_row_aname_coautoresUSP", Row.ROLE_DATA, "class_row_aname_coautoresUSP");

                Cell cellAnameCoautoriaExtUSP = rowAnameCoautoresExt.addCell
                                      ("id_cols_aname_coautoriaUSP_field", Cell.ROLE_DATA, "class_cols_aname_coautoriaUSP_field");
                cellAnameCoautoriaExtUSP.addXref("#" + coautoriaUSP,T_aname_coauthorUSP,"id_aname_coautorUSP_link");

                Cell cellAnameFimExt = rowAnameCoautoresExt.addCell("id_cols_aname_fim_field", Cell.ROLE_DATA, "class_cols_aname_fim_field");
                cellAnameFimExt.addXref("#" + fim,T_aname_fim,"id_aname_fim_link");

                Cell cellAnameTopoExt = rowAnameCoautoresExt.addCell("id_cols_aname_topo_field", Cell.ROLE_DATA, "class_cols_aname_topo_field");
                cellAnameTopoExt.addXref("#" + topo,T_aname_top,"id_aname_topo_link");

                /**Cell cellAnameCoautoriaExtUSP = rowAnameCoautoresExt.addCell
                                      ("id_cols_aname_coautoriaUSP_field", Cell.ROLE_DATA, "class_cols_aname_coautoriaUSP_field");
                cellAnameCoautoriaExtUSP.addXref("#" + coautoriaUSP,T_aname_coauthorUSP,"id_aname_coautorUSP_link");

                Cell cellAnameCoautoriaExt = rowAnameCoautoresExt.addCell
                                      ("id_cols_aname_coautoriaExt_field", Cell.ROLE_DATA, "class_cols_aname_coautoriaExt_field");
                cellAnameCoautoriaExt.addXref("#" + coautoriaExt,T_aname_coauthorExt,"id_aname_coautorExt_link");*/

//                coautoriaExtAname = aNameCoautoreExternos.addPara("id_paragrafo_aname_coautoriaExtPara", "class_paragrafo_aname_coautoriaExtPara"); 
//                coautoriaExtAname.addXref(coautoriaExt,"",aName);

                Para headExterno = informacoesCoautoresExternosHead.addPara("id_paragrafo_coautores_externos_head", "class_paragrafo_coautores_externos_head");
                headExterno.addContent(T_coauthorExterno_head);
          
                coautoriaExtAname = aNameCoautoreExternos.addPara("id_paragrafo_aname_coautoriaExtPara", "class_paragrafo_aname_coautoriaExtPara");
                coautoriaExtAname.addXref(coautoriaExt,"",aName);

                Table tabelaCoautoresExternos = informacoesTabelaCoautoresExternos.addTable
                                       ("id_tblLista_coautores_externos", totalCoautoresExternos, 2, "class_tblLista_coautores_externos");
                				
                Row rowTituloColunas = tabelaCoautoresExternos.addRow("id_row_sequencia_colunas_externo", Row.ROLE_DATA, "class_row_sequencia_colunas_externo");

                Cell cellSeqTitulo = rowTituloColunas.addCell("id_cols_sequencia_externo", Cell.ROLE_DATA, "class_cols_sequencia_externo");
                cellSeqTitulo.addContent(T_titulo_seq);

                Cell cellNomeCoautor = rowTituloColunas.addCell("id_cols_nome_coautor_externo", Cell.ROLE_DATA, "class_cols_nome_coautor_externo");
                cellNomeCoautor.addContent(T_titulo_nome_coautor);

                contadorLista = 1;

                if(totalCoautoresExternos > 0) {                
                  for(String coautorExterno : listaCoautoresExternos) {

                      Row rowCoautorExterno = tabelaCoautoresExternos.addRow("id_row_coautores_externos_colunas", Row.ROLE_DATA, "class_row_coautores_externos_colunas");

                      Cell cellSeqTituloExterno = rowCoautorExterno.addCell("id_cols_sequencia", Cell.ROLE_DATA, "class_cols_sequencia");
                      cellSeqTituloExterno.addContent(contadorLista);

                      Cell cellNomeCoautorExterno = rowCoautorExterno.addCell("id_cols_nome_coautor", Cell.ROLE_DATA, "class_cols_nome_coautor");
                      cellNomeCoautorExterno.addContent(coautorExterno);
                      contadorLista++;
                   }
               }
			   
	       else {
	           Row rowCoautorExterno = tabelaCoautoresExternos.addRow("id_row_coautores_externos_colunas", Row.ROLE_DATA, "class_row_coautores_externos_colunas");
                     
                   Cell cellNaoHaCoautor = rowCoautorExterno.addCell("id_cols_NaoCoautoriaUSP_field", Cell.ROLE_DATA, 1, 2, "class_cols_NaoCoautoriaUSP_field");
                   cellNaoHaCoautor.addContent(T_nao_coautorExt);
	      }

             Division aNameCoautoresFim = informacoesCoautoresExternos.addDivision("id_aname_coautores_fim","class_aname_coautores_fim");

             Table tabelaAnameCoautoresFim = aNameCoautoresFim.addTable("id_tblAName_coautores_fim", 1, 3, "class_tblAName_coautores_fim");

             Row rowAnameCoautoresFim = tabelaAnameCoautoresFim.addRow("id_row_aname_coautoresUSP", Row.ROLE_DATA, "class_row_aname_coautoresUSP");

             Cell cellAnameCoautoriaFimExt = rowAnameCoautoresFim.addCell
                                   ("id_cols_aname_coautoriaExt_field", Cell.ROLE_DATA, "class_cols_aname_coautoriaExt_field");
             cellAnameCoautoriaFimExt.addXref("#" + coautoriaExt,T_aname_coauthorExt,"id_aname_coautorExt_link");

             Cell cellAnameCoautoriaFimUSP = rowAnameCoautoresFim.addCell
                                   ("id_cols_aname_coautoriaUSP_field", Cell.ROLE_DATA, "class_cols_aname_coautoriaUSP_field");
             cellAnameCoautoriaFimUSP.addXref("#" + coautoriaUSP,T_aname_coauthorUSP,"id_aname_coautorUSP_link");

             Cell cellAnameTopoFim = rowAnameCoautoresFim.addCell("id_cols_aname_topo_field", Cell.ROLE_DATA, "class_cols_aname_topo_field");
             cellAnameTopoFim.addXref("#" + topo,T_aname_top,"id_aname_topo_link");

   /** Define o <a name> para o fim da pagina */
             fimAname = aNameCoautoresFim.addPara("id_paragrafo_aname_fimPara", "class_paragrafo_aname_fimPara");
             fimAname.addXref(fim,"",aName);

   /** Define os links para voltar ou fechar a pagina */
             Para retornar = geral.addPara("id_paragrafo_retornar", "class_paragrafo_retornar");
             Para fechar = geral.addPara("id_paragrafo_fechar", "class_paragrafo_fechar");
 
             //retornar.addXref(this.dspaceUrl + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/" + "author", T_return, "");
			 retornar.addXref(contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/author", T_return);
             fechar.addXref("#", T_close,"window.close()","window.close()");

//             List retorno = informacoesCoautoresExternos.addList("id_retornar_page_autor", "gloss", "classListaInfo");
//             retorno.addItem().addXref(this.dspaceUrl + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/" + "author", T_return, "text-align: right");
           }
		   
		   else {
           geral.setHead(T_coautor_nao_encontrado);

           Para retornar = geral.addPara("id_paragrafo_retornar", "class_paragrafo_retornar");
           Para fechar = geral.addPara("id_paragrafo_fechar", "class_paragrafo_fechar");

           retornar.addXref(contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "?show=full", T_return, "");		                   
           fechar.addXref("#", T_close,"window.close()","window.close()");
        }
		
        }
        else {
           geral.setHead(T_coautor_nao_encontrado);

           Para retornar = geral.addPara("id_paragrafo_retornar", "class_paragrafo_retornar");
           Para fechar = geral.addPara("id_paragrafo_fechar", "class_paragrafo_fechar");

           retornar.addXref(contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "?show=full", T_return, "");
           fechar.addXref("#", T_close,"window.close()","window.close()");
        }
    }

    public void setCodpesFromPage(String codpes) {
       this.codpes = Integer.parseInt(codpes);
    }
}
