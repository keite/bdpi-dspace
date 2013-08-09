<?xml version="1.0" encoding="UTF-8"?>
<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->
<!--
    TODO: Describe this XSL file
    Author: Alexey Maslov

-->

<xsl:stylesheet xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
	xmlns:dri="http://di.tamu.edu/DRI/1.0/"
	xmlns:mets="http://www.loc.gov/METS/"
	xmlns:xlink="http://www.w3.org/TR/xlink/"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
	xmlns:xhtml="http://www.w3.org/1999/xhtml"
	xmlns:mods="http://www.loc.gov/mods/v3"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns="http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc">

    <xsl:import href="../dri2xhtml-alt/dri2xhtml.xsl"/>
    <xsl:import href="lib/xsl/core/global-variables.xsl"/>
    <xsl:import href="lib/xsl/core/page-structure.xsl"/>
    <xsl:import href="lib/xsl/core/navigation.xsl"/>
    <xsl:import href="lib/xsl/core/elements.xsl"/>
    <xsl:import href="lib/xsl/core/forms.xsl"/>
    <xsl:import href="lib/xsl/core/attribute-handlers.xsl"/>
    <xsl:import href="lib/xsl/core/utils.xsl"/>
    <xsl:import href="lib/xsl/aspect/general/choice-authority-control.xsl"/>
    <xsl:import href="lib/xsl/aspect/administrative/administrative.xsl"/>
    <xsl:import href="lib/xsl/aspect/artifactbrowser/item-list.xsl"/>
    <xsl:import href="lib/xsl/aspect/artifactbrowser/item-view.xsl"/>
    <xsl:import href="lib/xsl/aspect/artifactbrowser/community-list.xsl"/>
    <xsl:import href="lib/xsl/aspect/artifactbrowser/collection-list.xsl"/>
    <xsl:output indent="yes"/>

<!-- 130411 andre.assada@usp.br retirar busca da capa / pagina inicial (front page)  -->
<xsl:template name="disable_front-page-search" match="dri:div[@id='aspect.artifactbrowser.FrontPageSearch.div.front-page-search']">
</xsl:template>
<!-- FIM 130411 andre.assada@usp.br retirar busca da capa / pagina inicial (front page) FIM -->

<!-- 130808 - Dan Shinkai - Retirar busca da capa / pagina inicial (front page) gerado pelo Discovery  -->
<xsl:template name="disable_front-page-search_discovery" match="dri:document/dri:body/dri:div[@id='aspect.discovery.SiteViewer.div.front-page-search']">
</xsl:template>
<!-- FIM -->

<!-- 130411 andre.assada@usp.br retirar lista de comunidades (community list) da capa / pagina inicial (front page) -->
<!-- tive que inserir o div literalmente aqui; por que nao esta pegando do template? esse div eh necessario pra lista expansivel funcionar @TODO: fixme-->
<xsl:template name="disable_front-page-browse" match="dri:div[@id='aspect.artifactbrowser.CommunityBrowser.div.comunity-browser']">
    <xsl:if test="not(//dri:body/dri:div[@id='file.news.div.news'])">
        <div id="aspect_artifactbrowser_CommunityBrowser_div_comunity-browser" class="ds-static-div primary">
            <xsl:apply-templates/>
        </div>
    </xsl:if>
</xsl:template>
<!-- FIM 130411 andre.assada@usp.br retirar lista de comunidades (community list) da capa / pagina inicial (front page) FIM -->

<!-- 130419 - Dan - Codigo para possibilitar fechar uma aba, abrir uma pagina em outra aba, definir ancoras nas paginas e especificar uma class de um href --> 
<!-- a partir da palavra chave de seu @rend ou @n --> 
    <xsl:template match="dri:xref">
       <a>
          <xsl:if test="@target">
             <xsl:choose>
                <xsl:when test="@rend='aname'">
                   <xsl:attribute name="name">
                      <xsl:value-of select="@target"/>
                   </xsl:attribute>
                </xsl:when> 
                <xsl:otherwise>
                <xsl:attribute name="href">
                   <xsl:value-of select="@target"/>
                </xsl:attribute> </xsl:otherwise>
             </xsl:choose> 
          </xsl:if>
         
          <xsl:if test="@rend">
             <xsl:choose>
                <xsl:when test="@n='_blank'">
                   <xsl:attribute name="target">_blank</xsl:attribute>
                   <xsl:attribute name="class">
                      <xsl:value-of select="@rend"/>
                   </xsl:attribute>
                </xsl:when>
                <xsl:when test="@n='window.close()'">
                   <xsl:attribute name="onclick">window.close()</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                   <xsl:attribute name="class">
                      <xsl:value-of select="@rend"/>
                   </xsl:attribute>
                </xsl:otherwise>
             </xsl:choose>
          </xsl:if>

          <xsl:if test="@n">
             <xsl:attribute name="name">
                <xsl:value-of select="@n"/>
             </xsl:attribute>
          </xsl:if>
       <xsl:apply-templates />
       </a>
    </xsl:template>	
	
</xsl:stylesheet>
