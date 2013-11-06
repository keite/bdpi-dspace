package org.dspace.app.xmlui.aspect.artifactbrowser;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.lang.Integer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.imageio.ImageIO;

import de.toolforge.googlechartwrapper.Dimension;
import de.toolforge.googlechartwrapper.PieChart;
import de.toolforge.googlechartwrapper.data.PieChartSlice;

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
import org.dspace.content.InterUnit;
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
public class InterUnitAuthorView extends AbstractDSpaceTransformer implements CacheableProcessingComponent 
{
    /** Mensagens que deverao aparecer dependendo do idioma do browser */
    private static final Message T_dspace_home =
        message("xmlui.general.dspace_home");

     private static final Message T_return =
        message("xmlui.general.return");

    private static final Message T_close =
        message("xmlui.general.close");

    private static final Message T_title =
        message("xmlui.ArtifactBrowser.InterUnitAuthorView.title_page");

    private static final Message T_title_initial =
        message("xmlui.ArtifactBrowser.InterUnitAuthorView.title_initial");

    private static final Message T_coauthor =
        message("xmlui.ArtifactBrowser.InterUnitAuthorView.link_coauthor");

    private static final Message T_trail = 
        message("xmlui.ArtifactBrowser.AuthorView.trail");

    private static final Message T_trail_item =
        message("xmlui.ArtifactBrowser.ItemViewer.trail");

    private static final Message T_trail_interunit =
        message("xmlui.ArtifactBrowser.InterUnitAuthorView.trail");

    private static final Message T_interunit_not_found =
        message("xmlui.ArtifactBrowser.InterUnitAuthorView.page_not_found");

    private static final Message T_title_seq =
        message("xmlui.ArtifactBrowser.InterUnitAuthorView.title_sequence");

    private static final Message T_title_unit =
        message("xmlui.ArtifactBrowser.InterUnitAuthorView.title_unit");

    private static final Message T_title_work =
        message("xmlui.ArtifactBrowser.InterUnitAuthorView.title_work");

    private static final Message T_title_total =
        message("xmlui.ArtifactBrowser.InterUnitAuthorView.title_total");

    private static final Message T_interunit_head = 
        message("xmlui.ArtifactBrowser.InterUnitAuthorView.interunit_head");

    /** Parametro que armazenara o codpes da pessoa diretamente da pagina como um int */
    private int codpes;

    private String dspaceUrl = ConfigurationManager.getProperty("handle.canonical.prefix");
    private String handlePrefix = ConfigurationManager.getProperty("handle.prefix");
	private String dspaceDir = ConfigurationManager.getProperty("dspace.dir");

	private String theme = "BDPI";

    private AuthorDAOPostgres ap = new AuthorDAOPostgres();

    private String diretorioGraficoTemp = dspaceDir + "/webapps/xmlui/themes/" + theme + "/images/autorUSP/tmp/";
	private String diretorioGrafico = "/themes/"+ theme + "/images/autorUSP/tmp/";
    
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
		
		Item item = null;
		
		String itemIDStr = parameters.getParameter("itemID","");
        String codpesStr = parameters.getParameter("codpes","");

   /** Converte para inteiro a String passada */
        setCodpesFromPage(codpesStr);

   /** Recupera o objeto do autor a partir de seu codpes */
        Author author = this.ap.getAuthorByCodpes(this.codpes);
		
        if (!(dso instanceof Item))
        {
            item = Item.find(context, Integer.parseInt(itemIDStr));
        }
		
		else  {
			item = (Item) dso;
		}
		
        if(author != null) {
          pageMeta.addMetadata("title").addContent(T_title);
        }
		
		else {
			pageMeta.addMetadata("title").addContent(T_interunit_not_found);
		}
 
   /** Codigo que cria a parte dos links do Trail */
       pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
       HandleUtil.buildHandleTrail(item,pageMeta,contextPath);
       pageMeta.addTrailLink(contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "?show=full",T_trail_item);
       pageMeta.addTrailLink(contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/author",T_trail);
       pageMeta.addTrail().addContent(T_trail_interunit);
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

   /** Recupera a lista de coautores */
      ArrayList<InterUnit> listaInterUnidades = this.ap.getInterUnitUSP(codpesStr);

      /** Adiciona uma <div> na <div> geral */
       Division informacoesInterUnidadeUSP = geral.addDivision("id_informacoes_interUnid","class_informacoes_interUnid");

      if(listaInterUnidades != null && listaInterUnidades.size() > 0) {

   /** Recupera o objeto Autor */
        Author autor = this.ap.getAuthorByCodpes(Integer.parseInt(codpesStr));

   /** Define o titulo localizado na parte superior da pagina */
        Head headGeral = geral.setHead();
        headGeral.addContent(T_title_initial);
        headGeral.addContent(" " + autor.getSobrenome().toUpperCase());
             
        int contadorLista = 0;

   /** Adiciona as divs para a criacao do titulo acima da tabela e a propria tabela */
       Division informacoesInterUnidadeUSPHead = informacoesInterUnidadeUSP.addDivision("id_informacoes_interUnid_head","class_informacoes_interUnid_head");
       Division grafico = informacoesInterUnidadeUSP.addDivision("id_grafico_interUnid","class_grafico_interUnid");
       Division informacoesTabelaInterUnidadeUSP = informacoesInterUnidadeUSP.addDivision
	                                   ("id_informacoes_tabela_interUnid","class_informacoes_tabela_interUnid");
       Division informacoesTabelaLinks = informacoesInterUnidadeUSP.addDivision("id_tabela_links","class_tabela_links");

   /** Adiciona o titulo para a lista de Interunidade USP */
       Para head = informacoesInterUnidadeUSPHead.addPara("id_paragrafo_interUnid_head", "class_paragrafo_interUnid_head");
       head.addContent(T_interunit_head);
      
       Collections.sort(listaInterUnidades, new InterUnit());
    
       Table tabelaInterUnidade = informacoesTabelaInterUnidadeUSP.addTable("id_tblLista_interUnidUSP", listaInterUnidades.size(), 3, "class_tblLista_interUnidUSP");

       Row rowTituloColuna = tabelaInterUnidade.addRow("id_row_titulos_interUnid_colunas", Row.ROLE_DATA, "class_row_titulos_interUnid_colunas");

       Cell cellSeq = rowTituloColuna.addCell("id_cols_seq_interUnid", Cell.ROLE_DATA, "class_cols_seq_interUnid");
       cellSeq.addContent(T_title_seq);       

       Cell cellUnidadeSigla = rowTituloColuna.addCell("id_cols_unidSigla_interUnid", Cell.ROLE_DATA, "class_cols_unidSigla_interUnid");
       cellUnidadeSigla.addContent(T_title_unit);

       Cell cellTrabalhos = rowTituloColuna.addCell("id_cols_trabalhos_interUnid", Cell.ROLE_DATA, "class_cols_trabalhos_interUnid");
       cellTrabalhos.addContent(T_title_work);

       contadorLista = 1;
//       int totalTrabalhos = 0;
     
       //PieChart pieChart = new PieChart(new Dimension(450,300));

       for(InterUnit unidade : listaInterUnidades) {

	      if(unidade != null) {
             
	     String unidadeSigla = unidade.getUnidadeSigla();
             int trabalhos = unidade.getQntTrabalhos();
  //           totalTrabalhos = totalTrabalhos + trabalhos;
             //int codpes = unidade.getCodpes();

             Row rowUnidadeField = tabelaInterUnidade.addRow("id_row_interUnid", Row.ROLE_DATA, "class_row_interUnid");
                     
             Cell cellSeqField = rowUnidadeField.addCell("id_cols_seq_interUnid_field" + contadorLista, Cell.ROLE_DATA, "class_cols_seq_interUnid_field");
             cellSeqField.addContent(contadorLista);

             Cell cellUnidadeField = rowUnidadeField.addCell("id_cols_unid_interUnid_field" + contadorLista, Cell.ROLE_DATA, "class_cols_unid_interUnid_field");
             cellUnidadeField.addContent(unidadeSigla);
             
 	     Cell cellTrabalhosField = rowUnidadeField.addCell("id_cols_trabalhos_interUnid_field" + contadorLista, Cell.ROLE_DATA, "class_cols_trabalhos_interUnid_field");
             cellTrabalhosField.addContent(trabalhos);

             contadorLista++;

            // pieChart.addPieChartSlice(new PieChartSlice.PieChartSliceBuilder(trabalhos).label(unidadeSigla).build());
             }
	}

       /** URL url;
 	   try {
	      url = new URL(pieChart.getUrl());
	      InputStream is = url.openStream();
	      OutputStream os = new FileOutputStream(diretorioGraficoTemp + codpesStr + "gfc.gif");

	      byte[] b = new byte[2048];
	      int length;

	      while ((length = is.read(b)) != -1) {
	         os.write(b, 0, length);
	      }

	      is.close();
	      os.close();

	      } catch (MalformedURLException e) {
		e.printStackTrace();

	      } catch (IOException e) {
		e.printStackTrace();
	    }
		**/
		/** 130507 - Dan - Trecho que extrai o nome do usuario para arrumar no caminho da foto. 
         *     SOMENTE PARA A VERSAO TESTE. Caso o nome seja desnecessario, entao apagar a variavel "nome" contida nos caminhos do diretorioFigura. 
	    **/
		/**StringTokenizer st = new StringTokenizer(dspaceDir, "/");
		st.nextToken();
		st.nextToken();
		String nome = "/" + st.nextToken();**/

	    /** ============================= FIM ================================ **/

        List listaFigura = grafico.addList("id_grafico_interUnidLista", "gloss", "class_grafico_interUnidLista");
        //Figure figura = listaFigura.addItem().addFigureSemLink(diretorioGrafico + codpesStr + "gfc.gif","nolink");

        /* Row rowTotalTrabField = tabelaInterUnidade.addRow("id_row_interUnid_total", Row.ROLE_DATA, "class_row_interUnid_total");

         Cell cellTotalTrab = rowTotalTrabField.addCell("id_cols_interUnid_total_label", Cell.ROLE_DATA, 1, 2, "class_cols_interUnid_total_label");
         cellTotalTrab.addContent(T_title_total);

         Cell cellTotalTrabField = rowTotalTrabField.addCell("id_cols_interUnid_total_field", Cell.ROLE_DATA, "class_cols_interUnid_total_field");
         cellTotalTrabField.addContent(totalTrabalhos);*/

         /*Para coautoria = informacoesInterUnidadeUSP.addPara("id_paragrafo_coautoria", "class_paragrafo_coautoria");
         Para retornar = informacoesInterUnidadeUSP.addPara("id_paragrafo_retornar", "class_paragrafo_retornar");
         Para fechar = informacoesInterUnidadeUSP.addPara("id_paragrafo_fechar", "class_paragrafo_fechar");*/

         Table tabelaLinks = informacoesTabelaLinks.addTable("id_tbl_links", 1, 3, "class_tbl_links");

         Row rowLinksColuna = tabelaLinks.addRow("id_row_links_colunas", Row.ROLE_DATA, "class_row_links_colunas");

         Cell coautoriaLink = rowLinksColuna.addCell("id_cols_coautoria_link", Cell.ROLE_DATA, "class_cols_coautoria_link");
         coautoriaLink.addXref
                    (contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/coauthor", T_coauthor, "", "_self");
/*
         Cell retornarLink = rowLinksColuna.addCell("id_cols_retornar_link", Cell.ROLE_DATA, "class_cols_retornar_link");
         retornarLink.addXref
                    (contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/author", T_return);

         Cell fecharLink = rowLinksColuna.addCell("id_cols_fechar_link", Cell.ROLE_DATA, "class_cols_fechar_link");
         fecharLink.addXref
                    ("#", T_close,"window.close()","window.close()");
*/
         /**coautoria.addXref(contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/coauthor", T_coauthor, "", "_self");
         retornar.addXref(contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/author", T_return);
         fechar.addXref("#", T_close,"window.close()","window.close()"); */

        }
        else {
           geral.setHead(T_interunit_not_found);
/*
           Para retornar = informacoesInterUnidadeUSP.addPara("id_paragrafo_retornar_error", "class_paragrafo_retornar_error");
           Para fechar = informacoesInterUnidadeUSP.addPara("id_paragrafo_fechar_error", "class_paragrafo_fechar_error");

           retornar.addXref(contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/author", T_return, "");
           fechar.addXref("#", T_close,"window.close()","window.close()");
*/
        }
    }

    public void setCodpesFromPage(String codpes) {
       this.codpes = Integer.parseInt(codpes);
    }
}