package org.dspace.app.xmlui.aspect.artifactbrowser;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.Serializable;
import java.io.File;
import java.sql.SQLException;
import java.lang.Integer;
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
import org.dspace.browse.BrowseInfo;
import org.dspace.content.Author;
import org.dspace.content.ItemRelacionado;
import org.dspace.content.dao.AuthorDAOPostgres;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.xml.sax.SAXException;

/**
 * Criar a pagina no qual apresentara informacoes do autor USP.
 * 
 * 
 * @author Dan Shinkai - 120817
 */
public class AuthorView extends AbstractDSpaceTransformer implements CacheableProcessingComponent
{
    /** Mensagens que deverao aparecer dependendo do idioma do browser */
    private static final Message T_title =
        message("xmlui.ArtifactBrowser.AuthorView.title");
    
    private static final Message T_dspace_home =
        message("xmlui.general.dspace_home");
    
    private static final Message T_trail = 
        message("xmlui.ArtifactBrowser.AuthorView.trail");

    private static final Message T_trail_item =
        message("xmlui.ArtifactBrowser.ItemViewer.trail");
    
    private static final Message T_head = 
        message("xmlui.ArtifactBrowser.AuthorView.head");
    
    private static final Message T_name =
        message("xmlui.ArtifactBrowser.AuthorView.name");

    private static final Message T_unitdepto =
        message("xmlui.ArtifactBrowser.AuthorView.unitdepto");

    private static final Message T_univesity =
        message("xmlui.ArtifactBrowser.AuthorView.university");
    
    private static final Message T_function =
        message("xmlui.ArtifactBrowser.AuthorView.function");
    
    private static final Message T_lattes = 
        message("xmlui.ArtifactBrowser.AuthorView.lattes");

    private static final Message T_bond =
        message("xmlui.ArtifactBrowser.AuthorView.entail");

    private static final Message T_email =
        message("xmlui.ArtifactBrowser.AuthorView.email");

    private static final Message T_return = 
        message("xmlui.general.return");

    private static final Message T_close =
        message("xmlui.general.close");    

    private static final Message T_author_not_found =
       message("xmlui.ArtifactBrowser.AuthorView.author_not_found");

	private static final Message T_author_not_citation =
       message("xmlui.ArtifactBrowser.AuthorView.author_not_citation");

	private static final Message T_author_not_item =
       message("xmlui.ArtifactBrowser.AuthorView.author_not_item");

    private static final Message T_itens_producao =
       message("xmlui.ArtifactBrowser.AuthorView.item_head");

    private static final Message T_titulo_sequencia =
       message("xmlui.ArtifactBrowser.AuthorView.col_title_sequence");

    private static final Message T_titulo_publicacao =
       message("xmlui.ArtifactBrowser.AuthorView.col_title_publication");

    private static final Message T_titulo_tipo =
       message("xmlui.ArtifactBrowser.AuthorView.col_title_type");

    private static final Message T_titulo_obra =
       message("xmlui.ArtifactBrowser.AuthorView.col_title_title"); 

    private static final Message T_titulo_citacao =
       message("xmlui.ArtifactBrowser.AuthorView.citation_title");

     private static final Message T_coauthor =
      message("xmlui.ArtifactBrowser.AuthorView.coauthor_link");
   
     private static final Message T_interdisplinar =
      message("xmlui.ArtifactBrowser.AuthorView.interdisciplinar_link");
	  
	 private static final Message T_visualiza_autor = 
	  message("xmlui.ArtifactBrowser.ConfigurableBrowse.trail.metadata.author");
    
    /** Parametro que armazenara o codpes da pessoa diretamente da pagina como um int */
    private int codpes;

    private String dspaceUrl = ConfigurationManager.getProperty("handle.canonical.prefix");
    private String handlePrefix = ConfigurationManager.getProperty("handle.prefix");
	private String dspaceDir = ConfigurationManager.getProperty("dspace.dir");

	private String theme = "BDPI";

    private String diretorioFoto = dspaceDir + "/webapps/xmlui/themes/" + theme + "/images/autorUSP/fotos/";
    private String diretorioFotoPag = "/themes/" + theme + "/images/autorUSP/fotos/";
    private String diretorioFotoPadrao = "/themes/" + theme + "/images/autorUSP/imagens/";
    private String diretorioFotoTemp = dspaceDir + "/webapps/xmlui/themes/" + theme + "/images/autorUSP/tmp/";
    private String diretorioFotoTempPag = "/themes/" + theme + "/images/autorUSP/tmp/";
    private String extensaoFoto = ".gif";	                              
    	                               
    private String itemIDStr;    
    private String codpesStr;

    private Author author = new Author();
    private AuthorDAOPostgres ap = new AuthorDAOPostgres();
	
	private BrowseInfo browseInfo;
    
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
		
		this.itemIDStr = parameters.getParameter("itemID","");
        this.codpesStr = parameters.getParameter("codpes","");

        /**converte para inteiro a String passada */
        setCodpesFromPage(this.codpesStr);
		
		Item item = null;

        /**recupera o objeto do autor a partir de seu codpes */
        this.author = this.ap.getAuthorByCodpes(this.codpes);
		
        if (!(dso instanceof Item))
        {
            item = Item.find(context, Integer.parseInt(this.itemIDStr));
        }
		
		else {
			item = (Item) dso;
		}

        if(this.author != null) {
          pageMeta.addMetadata("title").addContent(this.author.getSobrenome().toUpperCase());
        }
		
		else {
			pageMeta.addMetadata("title").addContent(T_author_not_found);
		}
		
    /** Trecho do codigo que monta o Trail da pagina */
        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
		
		/**
		 * 310713 - Dan Shinkai - Condicao implementada para verificar se o link oriundo e um item ou a lista de autores.
		 */
		if (item != null) {
			HandleUtil.buildHandleTrail(item,pageMeta,contextPath);
			pageMeta.addTrailLink(contextPath + "/handle/" + this.handlePrefix  + "/" + this.itemIDStr + "?show=full",T_trail_item);   
		}
		
		/**
		 * 310713 - Dan Shinkai - Condicao nao implementada, pois nao e possivel recuperar a URL anterior de uma busca.
		 *
		else {
			pageMeta.addTrailLink(urlAnterior,T_visualiza_autor);
		}*/
		
        pageMeta.addTrail().addContent(T_trail);
    }

    /**
     * Metodo pre-definido para a criacao de tags HTML dentro da tag <body>
     */
    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {

    /** Classe que cria uma tag <div> no qual os parametros passados sao o id e a class da <div> */
        Division geral = body.addDivision("id_body_geral", "class_body_autor");

    /** Recupera as informacoes passadas pelo sitemap.xmap contido no seguinte diretorio:
        /[dspace-src]/dspace-xmlui/dspace-xmlui-api/src/main/resources/aspects/ViewArtifacts
    */
        this.itemIDStr = parameters.getParameter("itemID","");
        this.codpesStr = parameters.getParameter("codpes","");


    /** Trecho do codigo que armazena a URL anterior para realizar a comparacao com a URL do item. */
      
        Request request = ObjectModelHelper.getRequest(objectModel);
        String urlItem = this.dspaceUrl + this.handlePrefix  + "/" + itemIDStr + "?show=full";
        String urlAnterior = request.getHeader("referer");
        
    /** Converte para inteiro a String passada */
        setCodpesFromPage(codpesStr);

    /** Recupera o objeto do autor a partir de seu codpes */
        this.author = this.ap.getAuthorByCodpes(this.codpes);

        String name = ConfigurationManager.getProperty("dspace.name");
        String diretorioFigura = "";
        
    /** Codigo que verifica a existencia de uma foto no arquivo. Caso seja encontrado, verifica se a foto esta nas dimensoes pre definidas.
        Caso negativo, realiza um resize na foto.       
    */
        boolean verificaFotoAutor = (new File(diretorioFoto + codpesStr + extensaoFoto)).exists();

	/** 130507 - Dan - Trecho que extrai o nome do usuario para arrumar no caminho da foto. 
     *     SOMENTE PARA A VERSAO TESTE. Caso o nome seja desnecessario, entao apagar a variavel "nome" contida nos caminhos do diretorioFigura. 
	**/
		StringTokenizer st = new StringTokenizer(dspaceDir, "/");
		st.nextToken();
		st.nextToken();
		String nome = "/" + st.nextToken();

	/** ============================= FIM ================================ **/

        if(verificaFotoAutor) {		

          try {
               int width = 96;
               int height = 140;

               BufferedImage foto = ImageIO.read(new File(diretorioFoto + codpesStr + extensaoFoto));

			   diretorioFigura = diretorioFoto + codpesStr + extensaoFoto;				  
               
               if(foto.getHeight() >= height || foto.getWidth() >= width) {                 
               
                  IMOperation op = new IMOperation();
                  op.addImage(diretorioFoto + codpesStr + extensaoFoto);
    	          op.resize(width,height);
                  op.gravity("center");
				  op.addImage(diretorioFotoTemp + codpesStr + extensaoFoto);

	              ConvertCmd cmd = new ConvertCmd();
	              cmd.run(op);
                  diretorioFigura = diretorioFotoTempPag + codpesStr + extensaoFoto;				  
              }

          } catch(InterruptedException ie) {
			ie.printStackTrace(System.out);

          } catch (IM4JavaException iem) {
			iem.printStackTrace(System.out);

          } catch (IOException io) {
			io.printStackTrace(System.out);
		  }
        }

        else {
          diretorioFigura = diretorioFotoPadrao + "logoSibiAutor.gif";

        }

        if(author != null) {

           Division informacoesImagem = geral.addDivision("id_informacoes_imagem","class_informacoes_imagem");
           Division fotoAutor = informacoesImagem.addDivision("id_autor_imagem","class_autor_imagem");
           //Division citacoesAutor = informacoesImagem.addDivision("id_citacoes_autor","class_citacoes_autor");
           Division informacao = geral.addDivision("id_autor_info","class_autor_info"); 
           //Division citacoesAutor = geral.addDivision("id_citacoes_autor","class_citacoes_autor");
           Division itensRelacionadosHead = geral.addDivision("id_itens_info_head","class_itens_info_head");
           Division itensRelacionados = geral.addDivision("id_itens_info","class_itens_info");

   /** Atribui um espaco na parte superior da div */
           
  /**
    * As listas sao utilizados para criar itens ordenados. Uma lista estara contido em uma determinada <div>. Portanto, a lista recebe
    * uma lista adicionado a <div> passando com parametros id, o tipo e a class. Os diversos tipos a serem escolhidos estao contidos na classe
    * List. Dependendo do tipo escolhido, a ordenacao e a apresentacao dos itens irao variar.
  */      
           List listaFigura = fotoAutor.addList("id_foto_autor", "gloss", "class_foto_autor");
           //List retorno = geral.addList("id_retorno", "gloss", "classListaInfo");           
      
   /** ====================================================================================================================================  */

   /** Define um header superior na pagina  */
           geral.setHead(author.getSobrenome().toUpperCase() + ", " + author.getNomeInicial());
           informacao.setHead(" ");
           
   /** A classe Figure define uma imagem no qual e passado como parametro o diretorio, um link externo e um rend */
           Figure figura = listaFigura.addItem().addFigureSemLink(diretorioFigura,"nolink");
           
   /** A classe Table cria uma tag <table> no qual e passado como parametros o id, numero de linhas, numero de colunas e o class */
           Table tabela = informacao.addTable("id_tblInfo_author", 5, 2, "class_tblInfo_author"); 

   /** A classe Row recebe uma linha adicionada de uma determinada tabela. Os parametros passados sao: id, o tipo (se e data ou header) e a class  */
           Row rowNome = tabela.addRow("id_row_autor", Row.ROLE_DATA, "class_row_autor");

   /** A classe Cell recebe uma celula de uma determinada linha. Os paramteros passados sao semelhantes a classe Row. */
           Cell cellNomeLabel = rowNome.addCell("id_cols_autor_label", Cell.ROLE_DATA, "class_cols_autor_label");
   /** Adiciona o conteudo na celula.  */
           cellNomeLabel.addContent(T_name); 
   /** Adiciona outro conteudo automaticamente na celula seguinte da mesma linha */
           Cell cellNomeField = rowNome.addCell("id_cols_autor_field", Cell.ROLE_DATA, "class_cols_autor_field");
           cellNomeField.addContent(author.getNome());

           Row rowDept = tabela.addRow("id_row_autor", Row.ROLE_DATA, "class_row_autor");
           Cell cellDeptLabel = rowDept.addCell("id_cols_autor_label", Cell.ROLE_DATA, "class_cols_autor_label");
           cellDeptLabel.addContent(T_unitdepto);
         
           StringBuilder unidadeDepto = new StringBuilder();
           
    /**
     * Condicao que verifica se a unidade e a sigla nao e null 
    */

	   if(author.getUnidade().length() > 0) {
             unidadeDepto.append(System.getProperty("line.separator"));
             unidadeDepto.append(author.getUnidade()); 
             if(author.getUnidadeSigla().length() > 0) { 
               unidadeDepto.append(" (");
               unidadeDepto.append(author.getUnidadeSigla().trim());
               unidadeDepto.append(")");
             }
           }
           else { 
              unidadeDepto.append(" - ");
           }

    /**
      * Condicao que verifica se o departamento e a sigla nao e null
    */
           if(author.getDepto().length() > 0) { 
             unidadeDepto.append(" / ");
             unidadeDepto.append(author.getDepto());
             if(author.getDeptoSigla().length() > 0) {
               unidadeDepto.append(" (");
               unidadeDepto.append(author.getDeptoSigla().trim());
               unidadeDepto.append(")");
             }
           }
           else { unidadeDepto.append(" / - ");
           }

           Cell cellDeptField = rowDept.addCell("id_cols_autor_field", Cell.ROLE_DATA, "class_cols_autor_field");
           cellDeptField.addContent(T_univesity); 
//           cellDeptField.addContent(unidadeDepto.toString());

           Row rowDeptContinuacao = tabela.addRow("id_row_autor", Row.ROLE_DATA, "class_row_autor");
           Cell cellDeptContinuacaoLabel = rowDeptContinuacao.addCell("id_cols_autor_label", Cell.ROLE_DATA, "class_cols_autor_label");
           cellDeptContinuacaoLabel.addContent("");
           Cell cellDeptContinuacaoField = rowDeptContinuacao.addCell("id_cols_autor_field", Cell.ROLE_DATA, "class_cols_autor_field");
           cellDeptContinuacaoField.addContent(unidadeDepto.toString());

           Row rowFuncao = tabela.addRow("id_row_autor", Row.ROLE_DATA, "class_row_autor");
           Cell cellFuncaoLabel = rowFuncao.addCell("id_cols_autor_label", Cell.ROLE_DATA, "class_cols_autor_label");
           cellFuncaoLabel.addContent(T_function);
           Cell cellFuncaoField = rowFuncao.addCell("id_cols_autor_field", Cell.ROLE_DATA, "class_cols_autor_field");
           cellFuncaoField.addContent(author.getFuncao());

           Row rowVinculo = tabela.addRow("id_row_autor", Row.ROLE_DATA, "class_row_autor");
           Cell cellVinculoLabel = rowVinculo.addCell("id_cols_autor_label", Cell.ROLE_DATA, "class_cols_autor_label");
           cellVinculoLabel.addContent(T_bond);
           Cell cellVinculoField = rowVinculo.addCell("id_cols_autor_field", Cell.ROLE_DATA, "class_cols_autor_field");
           cellVinculoField.addContent(author.getVinculo());

   /** Condicao que verifica se o lattes e maior que 22 caracteres para ser apresentado. O lattes e concatenado com 
    * o numero da base de dados e a url.
   */
           if(author.getLattes().length() > 22) {

             Row rowLattes = tabela.addRow("id_row_autor", Row.ROLE_DATA, "class_row_autor");
             Cell cellLattesLabel = rowLattes.addCell("id_cols_autor_label", Cell.ROLE_DATA, "class_cols_autor_label");
             cellLattesLabel.addContent(T_lattes);
             Cell cellLattesField = rowLattes.addCell("id_cols_autor_field", Cell.ROLE_DATA, "class_cols_autor_field");
//             cellLattesField.addXref("javascript: window.open('"+ author.getLattes() + "')",author.getLattes().substring(23),"_blank");
             cellLattesField.addXref(author.getLattes(),author.getLattes().substring(23),"_blank", "_blank");

           }
   /** Recupera todos as formas de citacoes de um mesmo autor */
           Division citacoesAutor = informacao.addDivision("id_citacoes_autor","class_citacoes_autor");

           ArrayList<String> listaCitacoesAutor = ap.getCitacoesAutor(codpesStr);
           int totalCitacoes = listaCitacoesAutor.size();

          if(totalCitacoes == 0) { totalCitacoes = 2; }

	   Table tabelaCitacoes = citacoesAutor.addTable("id_tblLista_citacao", totalCitacoes, 1, "class_tblLista_citacao");

           Row rowTituloCitacaoColunas = tabelaCitacoes.addRow("id_row_titulo_citacao_colunas", Row.ROLE_DATA, "class_row_titulo_citacao_colunas");
           Cell cellTituloCitacao = rowTituloCitacaoColunas.addCell("id_cols_titulo_citacao", Cell.ROLE_DATA, "class_cols_titulo_citacao");
           cellTituloCitacao.addContent(T_titulo_citacao);

	   if(listaCitacoesAutor.size() > 0) {
            
             String compara = listaCitacoesAutor.get(0);

             Row rowCitacaoInicial = tabelaCitacoes.addRow("id_row_citacao", Row.ROLE_DATA, "class_row_citacao");
             Cell cellCitacaoInicial = rowCitacaoInicial.addCell("id_cols_citacao_field", Cell.ROLE_DATA, "class_cols_citacao_field");
             cellCitacaoInicial.addContent(compara);

             for(int index = 1; index < listaCitacoesAutor.size(); index++) {

                 if(!compara.toUpperCase().equals(listaCitacoesAutor.get(index))) {

                    Row rowCitacao = tabelaCitacoes.addRow("id_row_citacao", Row.ROLE_DATA, "class_row_citacao");
                    Cell cellCitacao = rowCitacao.addCell("id_cols_citacao_field", Cell.ROLE_DATA, "class_cols_citacao_field");
                    cellCitacao.addContent(listaCitacoesAutor.get(index));

                    compara = listaCitacoesAutor.get(index);
                }
             }
           }

	   else {
	      Row rowCitacaoInicial = tabelaCitacoes.addRow("id_row_citacao", Row.ROLE_DATA, "class_row_citacao");
              Cell cellCitacaoInicial = rowCitacaoInicial.addCell("id_cols_citacao_field", Cell.ROLE_DATA, "class_cols_citacao_field");
              cellCitacaoInicial.addContent(T_author_not_citation);
	   }

           /**Row rowCoautoria = tabela.addRow("id_row_coautor", Row.ROLE_DATA, "class_row_coautor");
           Cell cellCoautoriaLabel = rowCoautoria.addCell("id_cols_autor_label", Cell.ROLE_DATA, "class_cols_autor_label");
           cellCoautoriaLabel.addContent(" ");
           Cell cellCoautoriaField = rowCoautoria.addCell("id_cols_autor_field", Cell.ROLE_DATA, "class_cols_autor_field");
           cellCoautoriaField.addXref(this.dspaceUrl + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/" + "coauthor",T_coauthor,"_blank", "_blank");*/

//           Division coautoriaLinkDivGeral = informacao.addDivision("id_coautoria_link_div","class_coautoria_link_div");

/** Codigo que cria o link para a pagina de coautoria */

           Division linkDivGeral = informacoesImagem.addDivision("id_link_div","class_link_div");

           Division coautoriaLinkDivGeral = linkDivGeral.addDivision("id_coautoria_link_div","class_coautoria_link_div");
           Division coautoriaLink = coautoriaLinkDivGeral.addDivision("id_coautoria_link","class_coautoria_link");
           Para coautoriaLinkPara = coautoriaLink.addPara("id_coautoria_link_para","class_coautoria_link_para");
           coautoriaLinkPara.addXref
              (contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/" + "coauthor",T_coauthor,"coautoria_link", "_blank");
              //(this.dspaceUrl + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/" + "coauthor",T_coauthor,"coautoria_link", "_blank");

           Division interdiscipLinkDivGeral = linkDivGeral.addDivision("id_interdis_link_div","class_interdis_link_div");
           Division interdiscipLink = interdiscipLinkDivGeral.addDivision("id_interdis_link","class_interdis_link");
           Para interdispLinkPara = interdiscipLink.addPara("id_interdis_link_para","class_interdis_link_para");
           interdispLinkPara.addXref
           (contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/" + "interunit",
                                                                              T_interdisplinar,"coautoria_link", "_blank");
           /**interdispLinkPara.addContent(T_interdisplinar); */

/**           Division coautoriaLinkForm = coautoriaLinkDivGeral.addInteractiveDivision
                     ("coautoria", this.dspaceUrl + this.handlePrefix  + "/" + itemIDStr + "/" + codpesStr + "/" + "coauthor", Division.METHOD_GET, "");

           List form = coautoriaLinkForm.addList("coautoria",List.TYPE_FORM);
           Button submit = form.addItem().addButton("submit", "");
           submit.setValue(T_coauthor);*/
           
           
   /** Cria o head para a lista de producao */
           Para head = itensRelacionadosHead.addPara("id_paragrafo_item_relacionado", "class_paragrafo_item_relacionado");
           head.addContent(T_itens_producao);
 
   /** Codigo que lista todos os itens relacionados com o autor  */
           ArrayList<ItemRelacionado> listaItens = ap.getItensRelacionados(codpesStr);
           ArrayList<ItemRelacionado> listaItensFinal = new ArrayList<ItemRelacionado>();
     
           int totalItens = 0;

           if(listaItens != null) { totalItens = listaItens.size(); }


           if(totalItens == 0) { totalItens = 2; }

           else {
               listaItensFinal.add(listaItens.get(0));
               int contadorLista = 0;

               for(ItemRelacionado itemFinal : listaItens) {
                  if(!listaItensFinal.get(contadorLista).getTitulo().equals(itemFinal.getTitulo())) {
                     listaItensFinal.add(itemFinal);
                     contadorLista++;
                  }
               }
               totalItens = listaItensFinal.size();
           }
 
           Table tabelaItens = itensRelacionados.addTable("id_tblLista_Itens", totalItens, 4, "class_tblLista_Itens");

           Row rowTituloColunas = tabelaItens.addRow("id_row_titulo_colunas", Row.ROLE_DATA, "class_row_titulo_colunas");
           Cell cellSeqTitulo = rowTituloColunas.addCell("id_cols_seq_titulos", Cell.ROLE_DATA, "class_cols_seq_titulos");
           cellSeqTitulo.addContent(T_titulo_sequencia);
           Cell cellPubTitulo = rowTituloColunas.addCell("id_cols_pub_titulos", Cell.ROLE_DATA, "class_cols_pub_titulos");
           cellPubTitulo.addContent(T_titulo_publicacao);
           Cell cellTipoTitulo = rowTituloColunas.addCell("id_cols_tipo_titulos", Cell.ROLE_DATA, "class_cols_tipo_titulos");
           cellTipoTitulo.addContent(T_titulo_tipo);
           Cell cellObraTitulo = rowTituloColunas.addCell("id_cols_obra_titulos", Cell.ROLE_DATA, "class_cols_obra_titulos");
           cellObraTitulo.addContent(T_titulo_obra);
            
           if((listaItens != null) && (listaItens.size() > 0)) { 
  
             int numeracao = 1;

             for(ItemRelacionado item : listaItensFinal) {

                 Row rowItem = tabelaItens.addRow("id_row_item", Row.ROLE_DATA, "class_row_item");
                 Cell cellNumeracao = rowItem.addCell("id_cols_numeracao_field", Cell.ROLE_DATA, "class_cols_numeracao_field");
                 cellNumeracao.addContent(numeracao);

                 Cell cellAnoPublicacao = rowItem.addCell("id_cols_ano_field", Cell.ROLE_DATA, "class_cols_ano_field");
                 cellAnoPublicacao.addContent(item.getAno());
  
                 Cell cellTipoPublicacao = rowItem.addCell("id_cols_tipo_field", Cell.ROLE_DATA, "class_cols_tipo_field");
                 cellTipoPublicacao.addContent(item.getTipo());

                 Cell cellTituloPublicacao = rowItem.addCell("id_cols_titulo_field", Cell.ROLE_DATA, "class_cols_titulo_field");
                 //cellTituloPublicacao.addXref("javascript: window.open('"+ dspaceUrl + item.getHandle() + "')", item.getTitulo(), "text-align: right");
                 cellTituloPublicacao.addXref(contextPath + "/handle/" + item.getHandle(), item.getTitulo(), "_blank", "_blank");

                 numeracao++;
             }
           }

	   else {
	      Row rowSemItem = tabelaItens.addRow("id_row_sem_item", Row.ROLE_DATA, "class_row_sem__item");
              Cell cellSemItem = rowSemItem.addCell("id_cols_sem_item", Cell.ROLE_DATA,1, 4, "class_cols_sem_item");
              cellSemItem.addContent(T_author_not_item);
		   }

          /** List retornar = itensRelacionados.addList("id_retornar", "gloss", "classListaInfo");
           List fechar = itensRelacionados.addList("id_fechar", "gloss", "classListaInfo");*/

          /** Para retornar = itensRelacionados.addPara("id_paragrafo_retornar", "class_paragrafo_retornar");*/
          /** Para fechar = itensRelacionados.addPara("id_paragrafo_fechar_central", "class_paragrafo_fechar_central"); */


   /** Codigo que cria o link de voltar e fechar a pagina. Dependendo da URL anterior, o link voltar sera visualizado ou nao */
           Para fechar = null;
   
           if(urlItem.equals(urlAnterior)) {
              fechar = itensRelacionados.addPara("id_paragrafo_fechar", "class_paragrafo_fechar");
              Para retornar = itensRelacionados.addPara("id_paragrafo_retornar", "class_paragrafo_retornar");
              retornar.addXref(contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "?show=full", T_return, "");
           }

           else { 
              fechar = itensRelacionados.addPara("id_paragrafo_fechar_central", "class_paragrafo_fechar_central");
           }

           fechar.addXref("#", T_close,"window.close()","window.close()");

        }
        else {
           
           geral.setHead(T_author_not_found);

           Para fechar = null;

           if(urlItem.equals(urlAnterior)) {
              fechar = geral.addPara("id_paragrafo_fechar", "class_paragrafo_fechar");
              Para retornar = geral.addPara("id_paragrafo_retornar", "class_paragrafo_retornar");
              retornar.addXref(contextPath + "/handle/" + this.handlePrefix  + "/" + itemIDStr + "?show=full", T_return, "");
           }

           else {
              fechar = geral.addPara("id_paragrafo_fechar_central", "class_paragrafo_fechar_central");
           }

           fechar.addXref("#", T_close,"window.close()","window.close()");

//           List retornar = geral.addList("id_retorno", "gloss", "classListaInfo");
//           retorno.addItem().addXref(this.dspaceUrl + this.handlePrefix  + "/" + itemIDStr + "?show=full", T_return, "text-align: right");
//           retornar.addItem().addXref("#", T_close,"window.close()","window.close()");
        }
        
    }


/** Metodo para conversao de uma String em um Integer */
    public void setCodpesFromPage(String codpes) {
       this.codpes = Integer.parseInt(codpes);
    }
}