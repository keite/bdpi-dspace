<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->
<!--
    Rendering specific to the item display page.

    Author: art.lowel at atmire.com
    Author: lieven.droogmans at atmire.com
    Author: ben at atmire.com
    Author: Alexey Maslov

-->

<xsl:stylesheet
    xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
    xmlns:dri="http://di.tamu.edu/DRI/1.0/"
    xmlns:mets="http://www.loc.gov/METS/"
    xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
    xmlns:xlink="http://www.w3.org/TR/xlink/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:atom="http://www.w3.org/2005/Atom"
    xmlns:ore="http://www.openarchives.org/ore/terms/"
    xmlns:oreatom="http://www.openarchives.org/ore/atom/"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:encoder="xalan://java.net.URLEncoder"
    xmlns:util="org.dspace.app.xmlui.utils.XSLUtils"
    xmlns:jstring="java.lang.String"
    xmlns:rights="http://cosimo.stanford.edu/sdr/metsrights/"
    exclude-result-prefixes="xalan encoder i18n dri mets dim xlink xsl util jstring rights">

    <xsl:output indent="yes"/>

    <xsl:template name="itemSummaryView-DIM">
        <!-- Generate the info about the item from the metadata section -->
        <xsl:apply-templates select="./mets:dmdSec/mets:mdWrap[@OTHERMDTYPE='DIM']/mets:xmlData/dim:dim"
        mode="itemSummaryView-DIM"/>

        <xsl:copy-of select="$SFXLink" />
        <!-- Generate the bitstream information from the file section -->
        <xsl:choose>
            <xsl:when test="./mets:fileSec/mets:fileGrp[@USE='CONTENT' or @USE='ORIGINAL']/mets:file">
                <xsl:apply-templates select="./mets:fileSec/mets:fileGrp[@USE='CONTENT' or @USE='ORIGINAL']">
                    <xsl:with-param name="context" select="."/>
                    <xsl:with-param name="primaryBitstream" select="./mets:structMap[@TYPE='LOGICAL']/mets:div[@TYPE='DSpace Item']/mets:fptr/@FILEID"/>
                </xsl:apply-templates>
            </xsl:when>
            <!-- Special case for handling ORE resource maps stored as DSpace bitstreams -->
            <xsl:when test="./mets:fileSec/mets:fileGrp[@USE='ORE']">
                <xsl:apply-templates select="./mets:fileSec/mets:fileGrp[@USE='ORE']"/>
            </xsl:when>
            <xsl:otherwise>
                <h2><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-head</i18n:text></h2>
                <table class="ds-table file-list">
                    <tr class="ds-table-header-row">
                        <th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-file</i18n:text></th>
                        <th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-size</i18n:text></th>
                        <th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-format</i18n:text></th>
                        <th><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-view</i18n:text></th>
                    </tr>
                    <tr>
                        <td colspan="4">
                            <p><i18n:text>xmlui.dri2xhtml.METS-1.0.item-no-files</i18n:text></p>
                        </td>
                    </tr>
                </table>
            </xsl:otherwise>
        </xsl:choose>

        <!-- Generate the Creative Commons license information from the file section (DSpace deposit license hidden by default)-->
        <xsl:apply-templates select="./mets:fileSec/mets:fileGrp[@USE='CC-LICENSE']"/>

    </xsl:template>


    <xsl:template match="dim:dim" mode="itemSummaryView-DIM">
        <div class="item-summary-view-metadata">
            <xsl:call-template name="itemSummaryView-DIM-fields"/>
        </div>
    </xsl:template>

    <xsl:template name="itemSummaryView-DIM-fields">
      <xsl:param name="clause" select="'1'"/>
      <xsl:param name="phase" select="'even'"/>
      <xsl:variable name="otherPhase">
            <xsl:choose>
              <xsl:when test="$phase = 'even'">
                <xsl:text>odd</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>even</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
      </xsl:variable>

      <xsl:choose>
          <!-- Title row -->
          <xsl:when test="$clause = 1">

              <xsl:choose>
                  <xsl:when test="count(dim:field[@element='title'][not(@qualifier)]) &gt; 1">
                      <!-- display first title as h1 -->
                      <h1>
                          <xsl:value-of select="dim:field[@element='title'][not(@qualifier)][1]/node()"/>
                      </h1>
                      <div class="simple-item-view-other">
                          <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-title</i18n:text>:</span>
                          <span>
                              <xsl:for-each select="dim:field[@element='title'][not(@qualifier)]">
                                  <xsl:value-of select="./node()"/>
                                  <xsl:if test="count(following-sibling::dim:field[@element='title'][not(@qualifier)]) != 0">
                                      <xsl:text>; </xsl:text>
                                      <br/>
                                  </xsl:if>
                              </xsl:for-each>
                          </span>
                      </div>
                  </xsl:when>
                  <xsl:when test="count(dim:field[@element='title'][not(@qualifier)]) = 1">
                      <h1>
                          <xsl:value-of select="dim:field[@element='title'][not(@qualifier)][1]/node()"/>
                      </h1>
                  </xsl:when>
                  <xsl:otherwise>
                      <h1>
                          <i18n:text>xmlui.dri2xhtml.METS-1.0.no-title</i18n:text>
                      </h1>
                  </xsl:otherwise>
              </xsl:choose>
            <xsl:call-template name="itemSummaryView-DIM-fields">
              <xsl:with-param name="clause" select="($clause + 1)"/>
              <xsl:with-param name="phase" select="$otherPhase"/>
            </xsl:call-template>
          </xsl:when>

          <!-- Author(s) row -->
          <xsl:when test="$clause = 2 and (dim:field[@element='contributor'][@qualifier='author'] or dim:field[@element='creator'] or dim:field[@element='contributor'])">
                    <div class="simple-item-view-authors">
	                    <xsl:choose>
	                        <xsl:when test="dim:field[@element='contributor'][@qualifier='author']">
	                            <xsl:for-each select="dim:field[@element='contributor'][@qualifier='author']">
                                        <span>
                                          <xsl:if test="@authority">
                                            <xsl:attribute name="class"><xsl:text>ds-dc_contributor_author-authority</xsl:text></xsl:attribute>
                                          </xsl:if>
	                                <xsl:copy-of select="node()"/>
                                        </span>
	                                <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='author']) != 0">
	                                    <xsl:text>; </xsl:text>
	                                </xsl:if>
	                            </xsl:for-each>
	                        </xsl:when>
	                        <xsl:when test="dim:field[@element='creator']">
	                            <xsl:for-each select="dim:field[@element='creator']">
	                                <xsl:copy-of select="node()"/>
	                                <xsl:if test="count(following-sibling::dim:field[@element='creator']) != 0">
	                                    <xsl:text>; </xsl:text>
	                                </xsl:if>
	                            </xsl:for-each>
	                        </xsl:when>
	                        <xsl:when test="dim:field[@element='contributor']">
	                            <xsl:for-each select="dim:field[@element='contributor']">
	                                <xsl:copy-of select="node()"/>
	                                <xsl:if test="count(following-sibling::dim:field[@element='contributor']) != 0">
	                                    <xsl:text>; </xsl:text>
	                                </xsl:if>
	                            </xsl:for-each>
	                        </xsl:when>
	                        <xsl:otherwise>
	                            <i18n:text>xmlui.dri2xhtml.METS-1.0.no-author</i18n:text>
	                        </xsl:otherwise>
	                    </xsl:choose>
	            </div>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                <xsl:with-param name="clause" select="($clause + 1)"/>
                <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when>

          <!-- identifier.uri row -->
          <xsl:when test="$clause = 3 and (dim:field[@element='identifier' and @qualifier='uri'])">
                    <div class="simple-item-view-other">
	                <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-uri</i18n:text>:</span>
	                <span>
	                	<xsl:for-each select="dim:field[@element='identifier' and @qualifier='uri']">
		                    <a>
		                        <xsl:attribute name="href">
		                            <xsl:copy-of select="./node()"/>
		                        </xsl:attribute>
		                        <xsl:copy-of select="./node()"/>
		                    </a>
		                    <xsl:if test="count(following-sibling::dim:field[@element='identifier' and @qualifier='uri']) != 0">
		                    	<br/>
		                    </xsl:if>
	                    </xsl:for-each>
	                </span>
	            </div>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                <xsl:with-param name="clause" select="($clause + 1)"/>
                <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when>

          <!-- date.issued row -->
          <xsl:when test="$clause = 4 and (dim:field[@element='date' and @qualifier='issued'])">
                    <div class="simple-item-view-other">
	                <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-date</i18n:text>:</span>
	                <span>
		                <xsl:for-each select="dim:field[@element='date' and @qualifier='issued']">
		                	<xsl:copy-of select="substring(./node(),1,10)"/>
		                	 <xsl:if test="count(following-sibling::dim:field[@element='date' and @qualifier='issued']) != 0">
	                    	<br/>
	                    </xsl:if>
		                </xsl:for-each>
	                </span>
	            </div>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                <xsl:with-param name="clause" select="($clause + 1)"/>
                <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when>

          <!-- Abstract row -->
          <xsl:when test="$clause = 5 and (dim:field[@element='description' and @qualifier='abstract' and descendant::text()])">
                    <div class="simple-item-view-description">
	                <h3><i18n:text>xmlui.dri2xhtml.METS-1.0.item-abstract</i18n:text>:</h3>
	                <div>
	                <xsl:if test="count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1">
	                	<div class="spacer">&#160;</div>
	                </xsl:if>
	                <xsl:for-each select="dim:field[@element='description' and @qualifier='abstract']">
                        <xsl:choose>
                            <xsl:when test="node()">
                                <xsl:copy-of select="node()"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>&#160;</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:if test="count(following-sibling::dim:field[@element='description' and @qualifier='abstract']) != 0">
                            <div class="spacer">&#160;</div>
	                    </xsl:if>
	              	</xsl:for-each>
	              	<xsl:if test="count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1">
                          <div class="spacer">&#160;</div>
	                </xsl:if>
	                </div>
	            </div>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                <xsl:with-param name="clause" select="($clause + 1)"/>
                <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when>

          <!-- Description row -->
          <xsl:when test="$clause = 6 and (dim:field[@element='description' and not(@qualifier)])">
                <div class="simple-item-view-description">
	                <h3 class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-description</i18n:text>:</h3>
	                <div>
	                <xsl:if test="count(dim:field[@element='description' and not(@qualifier)]) &gt; 1 and not(count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1)">
                        <div class="spacer">&#160;</div>
	                </xsl:if>
	                <xsl:for-each select="dim:field[@element='description' and not(@qualifier)]">
		                <xsl:copy-of select="./node()"/>
		                <xsl:if test="count(following-sibling::dim:field[@element='description' and not(@qualifier)]) != 0">
                            <div class="spacer">&#160;</div>
	                    </xsl:if>
	               	</xsl:for-each>
	               	<xsl:if test="count(dim:field[@element='description' and not(@qualifier)]) &gt; 1">
                           <div class="spacer">&#160;</div>
	                </xsl:if>
	                </div>
	            </div>
              <xsl:call-template name="itemSummaryView-DIM-fields">
                <xsl:with-param name="clause" select="($clause + 1)"/>
                <xsl:with-param name="phase" select="$otherPhase"/>
              </xsl:call-template>
          </xsl:when>

          <xsl:when test="$clause = 7 and $ds_item_view_toggle_url != ''">
              <p class="ds-paragraph item-view-toggle item-view-toggle-bottom">
                  <a>
                      <xsl:attribute name="href"><xsl:value-of select="$ds_item_view_toggle_url"/></xsl:attribute>
                      <i18n:text>xmlui.ArtifactBrowser.ItemViewer.show_full</i18n:text>
                  </a>
              </p>
          </xsl:when>

          <!-- recurse without changing phase if we didn't output anything -->
          <xsl:otherwise>
            <!-- IMPORTANT: This test should be updated if clauses are added! -->
            <xsl:if test="$clause &lt; 8">
              <xsl:call-template name="itemSummaryView-DIM-fields">
                <xsl:with-param name="clause" select="($clause + 1)"/>
                <xsl:with-param name="phase" select="$phase"/>
              </xsl:call-template>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>

         <!-- Generate the Creative Commons license information from the file section (DSpace deposit license hidden by default) -->
        <xsl:apply-templates select="mets:fileSec/mets:fileGrp[@USE='CC-LICENSE']"/>
    </xsl:template>


    <xsl:template match="dim:dim" mode="itemDetailView-DIM">
        <table class="ds-includeSet-table detailtable">
		    <xsl:apply-templates mode="itemDetailView-DIM"/>
		</table>
        <span class="Z3988">
            <xsl:attribute name="title">
                 <xsl:call-template name="renderCOinS"/>
            </xsl:attribute>
            &#xFEFF; <!-- non-breaking space to force separating the end tag -->
        </span>
        <xsl:copy-of select="$SFXLink" />
    </xsl:template>

    <xsl:template match="dim:field" mode="itemDetailView-DIM">
            <!-- =============================================================================================== -->
            <!-- 130328 agrupar metadados dentro de um TD, conforme determinacoes da profa. sueli (outubro 2010) -->
            <!-- @author Dan Shinkai (SI/EACH/USP) -->
            <!-- =============================================================================================== -->
            <xsl:choose>
                <!-- agrupar os metadados dc.subject em uma celula. -->
                <xsl:when test="@element='subject' and @mdschema='dc' and not(@qualifier)">
                    <xsl:variable name="contSubject" select="count(following-sibling::dim:field[@element='subject'][@mdschema='dc'][not(@qualifier)])"/>
                    <xsl:if test="$contSubject = 0">
                        <tr>
                            <xsl:attribute name="class">
                                <xsl:text>ds-table-row </xsl:text>
                                <xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>
                                <xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>
                            </xsl:attribute>
                            <td class="label-cell">
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                <i18n:text>
                                    <xsl:text>metadataTrad.</xsl:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                                    <xsl:value-of select="./@mdschema"/>
                                    <xsl:text>.</xsl:text>
                                    <xsl:value-of select="./@element"/>
                                    <xsl:if test="./@qualifier">
                                        <xsl:text>.</xsl:text>
                                        <xsl:value-of select="./@qualifier"/>
                                    </xsl:if>
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                </i18n:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                            </td>
                            <td>
                                <xsl:if test="$contSubject = 0">
                                    <xsl:for-each select="../dim:field[@element='subject'][@mdschema='dc'][not(@qualifier)]">
                                        <xsl:value-of select="current()"/>
                                        <xsl:text>;</xsl:text>
                                        <xsl:text> </xsl:text>
                                    </xsl:for-each>
                                </xsl:if>
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:when>

                <!-- agrupar os metadados dc.subject.vcusp em uma celula. -->
                <xsl:when test="@element='subject' and @mdschema='dc' and @qualifier='vcusp'">
                    <xsl:variable name="contSubject" select="count(following-sibling::dim:field[@element='subject'][@mdschema='dc'][@qualifier='vcusp'])"/>
                    <xsl:if test="$contSubject = 0">
                        <tr>
                            <xsl:attribute name="class">
                                <xsl:text>ds-table-row </xsl:text>
                                <xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>
                                <xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>
                            </xsl:attribute>
                            <td class="label-cell">
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                <i18n:text>
                                     <xsl:text>metadataTrad.</xsl:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                                    <xsl:value-of select="./@mdschema"/>
                                    <xsl:text>.</xsl:text>
                                    <xsl:value-of select="./@element"/>
                                    <xsl:if test="./@qualifier">
                                        <xsl:text>.</xsl:text>
                                        <xsl:value-of select="./@qualifier"/>
                                    </xsl:if>
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                </i18n:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                            </td>
                            <td>
                                <xsl:if test="$contSubject = 0">
                                    <xsl:for-each select="../dim:field[@element='subject'][@mdschema='dc'][@qualifier='vcusp']">
                                        <xsl:value-of select="current()"/>
                                        <xsl:text>;</xsl:text>
                                        <xsl:text> </xsl:text>
                                    </xsl:for-each>
                                </xsl:if>
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:when>

                <!-- agrupar os metadados dc.subject.wos em uma celula. -->
                <xsl:when test="@element='subject' and @mdschema='dc' and @qualifier='wos'">
                    <xsl:variable name="contSubject" select="count(following-sibling::dim:field[@element='subject'][@mdschema='dc'][@qualifier='wos'])"/>
                    <xsl:if test="$contSubject = 0">
                        <tr>
                            <xsl:attribute name="class">
                                <xsl:text>ds-table-row </xsl:text>
                                <xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>
                                <xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>
                            </xsl:attribute>
                            <td class="label-cell">
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                <i18n:text>
                                    <xsl:text>metadataTrad.</xsl:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                                    <xsl:value-of select="./@mdschema"/>
                                    <xsl:text>.</xsl:text>
                                    <xsl:value-of select="./@element"/>
                                    <xsl:if test="./@qualifier">
                                        <xsl:text>.</xsl:text>
                                        <xsl:value-of select="./@qualifier"/>
                                    </xsl:if>
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                </i18n:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                            </td>
                            <td>
                                <xsl:if test="$contSubject = 0">
                                    <xsl:for-each select="../dim:field[@element='subject'][@mdschema='dc'][@qualifier='wos']">
                                        <xsl:value-of select="current()"/>
                                        <xsl:text>;</xsl:text>
                                        <xsl:text> </xsl:text>
                                    </xsl:for-each>
                                </xsl:if>
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:when>                                                                                    

                <!-- agrupar os metadados dc.title.alternative em uma celula. -->
                <xsl:when test="@element='title' and @mdschema='dc' and @qualifier='alternative'">
                    <xsl:variable name="contAlternative" select="count(following-sibling::dim:field[@element='title'][@mdschema='dc'][@qualifier='alternative'])"/>
                    <xsl:if test="$contAlternative = 0">
                        <tr>
                            <xsl:attribute name="class">
                                <xsl:text>ds-table-row </xsl:text>
                                <xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>
                                <xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>
                            </xsl:attribute>
                            <td class="label-cell">
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                <i18n:text>
                                    <xsl:text>metadataTrad.</xsl:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                                    <xsl:value-of select="./@mdschema"/>
                                    <xsl:text>.</xsl:text>
                                    <xsl:value-of select="./@element"/>
                                    <xsl:if test="./@qualifier">
                                    <xsl:text>.</xsl:text>
                                        <xsl:value-of select="./@qualifier"/>
                                    </xsl:if>
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                </i18n:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                            </td>
                            <td>
                                <xsl:if test="$contAlternative = 0">
                                    <xsl:for-each select="../dim:field[@element='title'][@mdschema='dc'][@qualifier='alternative']">
                                        <xsl:value-of select="current()"/>
                                        <xsl:variable name="contAlternativeAtual" select="count(following-sibling::dim:field[@element='title'][@mdschema='dc'][@qualifier='alternative'])"/>
                                        <xsl:if test="$contAlternative != $contAlternativeAtual">
                                            <br/>
                                            <hr/>
                                        </xsl:if>
                                    </xsl:for-each>
                                </xsl:if>
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:when>

                <!-- agrupar os metadados dc.description.abstract em uma celula. -->
                <xsl:when test="@element='description' and @mdschema='dc' and @qualifier='abstract'">
                    <xsl:variable name="contAbstract" select="count(following-sibling::dim:field[@element='description'][@mdschema='dc'][@qualifier='abstract'])"/>
                    <xsl:if test="$contAbstract = 0">
                        <tr>
                            <xsl:attribute name="class">
                                <xsl:text>ds-table-row </xsl:text>
                                <xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>
                                <xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>
                            </xsl:attribute>
                            <td class="label-cell">
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                <i18n:text>
                                    <xsl:text>metadataTrad.</xsl:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                                    <xsl:value-of select="./@mdschema"/>
                                    <xsl:text>.</xsl:text>
                                    <xsl:value-of select="./@element"/>
                                    <xsl:if test="./@qualifier">
                                        <xsl:text>.</xsl:text>
                                        <xsl:value-of select="./@qualifier"/>
                                    </xsl:if>
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                </i18n:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                            </td>
                            <td>
                                <xsl:if test="$contAbstract = 0">
                                    <xsl:for-each select="../dim:field[@element='description'][@mdschema='dc'][@qualifier='abstract']">
                                        <xsl:value-of select="current()"/>
                                        <xsl:variable name="contAbstractAtual" select="count(following-sibling::dim:field[@element='description'][@mdschema='dc'][@qualifier='abstract'])"/>
                                        <xsl:if test="$contAbstract != $contAbstractAtual">
                                            <br/>
                                            <hr/>
                                        </xsl:if>
                                    </xsl:for-each>
                                </xsl:if>
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:when>

                <!-- agrupar os metadados usp.autor em uma celula. -->
                <xsl:when test="@element='autor' and @mdschema='usp' and not(@qualifier)">
                    <xsl:variable name="contAutor" select="count(following-sibling::dim:field[@element='autor'][@mdschema='usp'][not(@qualifier)])"/>
                    <xsl:if test="$contAutor = 0">
                        <tr>
                            <xsl:attribute name="class">
                                <xsl:text>ds-table-row </xsl:text>
                                <xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>
                                <xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>
                            </xsl:attribute>
                            <td class="label-cell">
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                <i18n:text>
                                    <xsl:text>metadataTrad.</xsl:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                                    <xsl:value-of select="./@mdschema"/>
                                    <xsl:text>.</xsl:text>
                                    <xsl:value-of select="./@element"/>
                                    <xsl:if test="./@qualifier">
                                        <xsl:text>.</xsl:text>
                                        <xsl:value-of select="./@qualifier"/>
                                    </xsl:if>
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                </i18n:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                            </td>
                            <td class="espacamento_metadados">
                                <xsl:variable name="contAutor" select="count(following-sibling::dim:field[@element='autor'][@mdschema='usp'][not(@qualifier)])"/>
                                <xsl:if test="$contAutor = 0">
                                    <xsl:for-each select="../dim:field[@element='autor'][@mdschema='usp'][not(@qualifier)]">
                                    <xsl:value-of select="current()"/>
                                        <xsl:variable name="contAutorAtual" select="count(following-sibling::dim:field[@element='autor'][@mdschema='usp'][not(@qualifier)])"/>
                                        <xsl:if test="$contAutor != $contAutorAtual">
                                            <br/>
                                            <br/>
                                        </xsl:if>
                                    </xsl:for-each>
                                </xsl:if>
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:when>

                <!-- agrupar os metadados usp.autor.externo em uma celula. -->
                <xsl:when test="@element='autor' and @mdschema='usp' and @qualifier='externo'">
                    <xsl:variable name="contAutorEx" select="count(following-sibling::dim:field[@element='autor'][@mdschema='usp'][@qualifier='externo'])"/>
                    <xsl:if test="$contAutorEx = 0">
                        <tr>
                            <xsl:attribute name="class">
                                <xsl:text>ds-table-row </xsl:text>
                                <xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>
                                <xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>
                            </xsl:attribute>
                            <td class="label-cell">
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                <i18n:text>
                                    <xsl:text>metadataTrad.</xsl:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                                    <xsl:value-of select="./@mdschema"/>
                                    <xsl:text>.</xsl:text>
                                    <xsl:value-of select="./@element"/>
                                    <xsl:if test="./@qualifier">
                                        <xsl:text>.</xsl:text>
                                        <xsl:value-of select="./@qualifier"/>
                                    </xsl:if>
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                </i18n:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                            </td>
                            <td>
                                <xsl:if test="$contAutorEx = 0">
                                    <xsl:for-each select="../dim:field[@element='autor'][@mdschema='usp'][@qualifier='externo']">
                                        <xsl:value-of select="current()"/>
                                        <xsl:variable name="contAutorExAtual"  select="count(following-sibling::dim:field[@element='autor'][@mdschema='usp'][@qualifier='externo'])"/>
                                        <xsl:if test="$contAutorEx != $contAutorExAtual">
                                            <br/>
                                            <br/>
                                        </xsl:if>
                                    </xsl:for-each>
                                </xsl:if>
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:when>
                
                <!-- agrupar os metadados usp.relation.references em uma celula. -->
                <xsl:when test="@element='relation' and @mdschema='usp' and @qualifier='references'">
                    <xsl:variable name="contReferences" select="count(following-sibling::dim:field[@element='relation'][@mdschema='usp'][@qualifier='references'])"/>
                    <xsl:if test="$contReferences = 0">
                        <tr>
                            <xsl:attribute name="class">
                                <xsl:text>ds-table-row </xsl:text>
                                <xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>
                                <xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>
                            </xsl:attribute>
                            <td class="label-cell">
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                <i18n:text>
                                    <xsl:text>metadataTrad.</xsl:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                                    <xsl:value-of select="./@mdschema"/>
                                    <xsl:text>.</xsl:text>
                                    <xsl:value-of select="./@element"/>
                                    <xsl:if test="./@qualifier">
                                        <xsl:text>.</xsl:text>
                                        <xsl:value-of select="./@qualifier"/>
                                    </xsl:if>
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                </i18n:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                            </td>
                            <td>
                                <xsl:if test="$contReferences = 0">
                                    <xsl:for-each select="../dim:field[@element='relation'][@mdschema='usp'][@qualifier='references']">
                                        <xsl:value-of select="current()"/>
                                        <xsl:variable name="contReferencesAtual" select="count(following-sibling::dim:field[@element='relation'][@mdschema='usp'][@qualifier='references'])"/>
                                        <xsl:if test="$contReferences != $contReferencesAtual">
                                            <br/>
                                            <hr/>
                                        </xsl:if>
                                    </xsl:for-each>
                                </xsl:if>
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:when>
                
                
                <!-- agrupar os metadados dc.description.sponsorship em uma celula. -->
                <!-- codigo utiliza uma classe java para criar um link nos numeros da Fapesp (em qualquer case) em um campo. 
                A classe java retorna o campo com as tags HTML necessarias para criar um link. -->

<!-- 130328 andre.assada@usp.br desabilitado por enquanto, para conseguir fazer a migracao por etapas para o dspace 3
                <xsl:when test="@element='description' and @mdschema='dc' and @qualifier='sponsorship'">
                <xsl:variable name="contSponsor" select="count(following-sibling::dim:field[@element='description'][@mdschema='dc'][@qualifier='sponsorship'])"/>
                <xsl:if test="$contSponsor = 0">
                <tr>
                <xsl:attribute name="class">
                <xsl:text>ds-table-row </xsl:text>
                <xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>
                <xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>
                </xsl:attribute>
                <td class="label-cell">
                <xsl:value-of select="./@mdschema"/>
                <xsl:text>.</xsl:text>
                <xsl:value-of select="./@element"/>
                <xsl:if test="./@qualifier">
                <xsl:text>.</xsl:text>
                <xsl:value-of select="./@qualifier"/>
                </xsl:if>
                </td>
                <td>
                <xsl:if test="$contSponsor = 0">
                <xsl:for-each select="../dim:field[@element='description'][@mdschema='dc'][@qualifier='sponsorship']">
                <xsl:variable name="copiaNode" select="translate(current(),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>                          
                <xsl:choose>
                <xsl:when test="contains($copiaNode, 'FAPESP')">
                <xsl:variable name="fapesp" select="utilUSP:constroiLinkFapesp(current())"/>
-->
                <!-- parametro "disable-output-escaping="yes"" permite utilizar tags HTML quando passadas pelo programa JAVA. -->
<!--
                <xsl:value-of select="$fapesp" disable-output-escaping="yes" />
                </xsl:when>
                <xsl:otherwise>
                <xsl:value-of select="current()"/>
                </xsl:otherwise>
                </xsl:choose>
                <xsl:variable name="contSponsorAtual"select="count(following-sibling::dim:field[@element='description'][@mdschema='dc'][@qualifier='sponsorship'])"/>
                <xsl:if test="$contSponsor != $contSponsorAtual">
                <br/>
                </xsl:if>
                </xsl:for-each>
                </xsl:if>
                </td>
                </tr>
                </xsl:if>
                </xsl:when> 
-->
<!-- 130328 andre.assada@usp.br agrupar dc.description.sponsorship sem usputils (temporario) -->
                <xsl:when test="@element='description' and @mdschema='dc' and @qualifier='sponsorship'">
                    <xsl:variable name="contSponsor" select="count(following-sibling::dim:field[@element='description'][@mdschema='dc'][@qualifier='sponsorship'])"/>
                    <xsl:if test="$contSponsor = 0">
                        <tr>
                            <xsl:attribute name="class">
                                <xsl:text>ds-table-row </xsl:text>
                                <xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>
                                <xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>
                            </xsl:attribute>
                            <td class="label-cell">
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                <i18n:text>
                                    <xsl:text>metadataTrad.</xsl:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                                    <xsl:value-of select="./@mdschema"/>
                                    <xsl:text>.</xsl:text>
                                    <xsl:value-of select="./@element"/>
                                    <xsl:if test="./@qualifier">
                                        <xsl:text>.</xsl:text>
                                        <xsl:value-of select="./@qualifier"/>
                                    </xsl:if>
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                </i18n:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                            </td>
                            <td>
                                <xsl:if test="$contSponsor = 0">
                                    <xsl:for-each select="../dim:field[@element='description'][@mdschema='dc'][@qualifier='sponsorship']">
                                        <xsl:value-of select="current()"/>
                                        <xsl:variable name="contSponsorAtual" select="count(following-sibling::dim:field[@element='description'][@mdschema='dc'][@qualifier='sponsorship'])"/>
                                        <xsl:if test="$contSponsor != $contSponsorAtual">
                                            <br/>
                                            <hr/>
                                        </xsl:if>
                                    </xsl:for-each>
                                </xsl:if>
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:when>
<!-- FIM 130328 andre.assada@usp.br agrupar dc.description.sponsorship sem usputils (temporario) FIM -->


                <!-- agrupar os metadados dc.identifier.url em uma celula. -->
                <xsl:when test="@element='identifier' and @mdschema='dc' and @qualifier='url'">
                    <xsl:variable name="contUrl" select="count(following-sibling::dim:field[@element='identifier'][@mdschema='dc'][@qualifier='url'])"/>
                    <xsl:if test="$contUrl = 0">
                        <tr>
                            <xsl:attribute name="class">
                                <xsl:text>ds-table-row </xsl:text>
                                <xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>
                                <xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>
                            </xsl:attribute>
                            <td class="label-cell">
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                <i18n:text>
                                    <xsl:text>metadataTrad.</xsl:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                                    <xsl:value-of select="./@mdschema"/>
                                    <xsl:text>.</xsl:text>
                                    <xsl:value-of select="./@element"/>
                                    <xsl:if test="./@qualifier">
                                        <xsl:text>.</xsl:text>
                                        <xsl:value-of select="./@qualifier"/>
                                    </xsl:if>
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                </i18n:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                            </td>
                            <td class="espacamento_metadados">
                                <xsl:if test="$contUrl = 0">
                                    <xsl:for-each select="../dim:field[@element='identifier'][@mdschema='dc'][@qualifier='url']">
                                        <a>
                                            <xsl:attribute name="href">
                                                <xsl:value-of select="current()"/>
                                            </xsl:attribute>
                                            <xsl:attribute name="target">_blank</xsl:attribute>
                                            <!-- Usa-se o current() para recuperar o nivel atual, pois o copy-of copia todos os niveis e -->
                                            <!-- realiza a postagem dos mesmos evitando uma etapa de verificacao -->
                                            <xsl:value-of select="current()"/>
                                        </a>
                                        <xsl:variable name="contUrlAtual" select="count(following-sibling::dim:field[@element='identifier'][@mdschema='dc'][@qualifier='url'])"/>
                                        <xsl:if test="$contUrl != $contUrlAtual">
                                            <br/>
                                            <br/>
                                        </xsl:if>
                                    </xsl:for-each>
                                </xsl:if>
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:when>
                
                <!-- agrupar os metadados dc.contributor.author em uma celula. -->

<!-- 130328 andre.assada@usp.br desabilitado por enquanto para possibilitar migracao para DSpace3 por etapas controladas

                <xsl:when test="@element='contributor' and @mdschema='dc' and @qualifier='author'">
                    <xsl:variable name="contContributor" select="count(following-sibling::dim:field[@element='contributor'][@mdschema='dc'][@qualifier='author'])"/>
                    <xsl:if test="$contContributor = 0">
                        <tr>
                            <xsl:attribute name="class">
                                <xsl:text>ds-table-row </xsl:text>
                                <xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>
                                <xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>
                            </xsl:attribute>
                            <td class="label-cell">
                                <xsl:value-of select="./@mdschema"/>
                                <xsl:text>.</xsl:text>
                                <xsl:value-of select="./@element"/>
                                <xsl:if test="./@qualifier">
                                    <xsl:text>.</xsl:text>
                                    <xsl:value-of select="./@qualifier"/>
                                </xsl:if>
                            </td>
                            <td>
                                <xsl:if test="$contContributor = 0">
                                    <xsl:for-each select="../dim:field[@element='contributor'][@mdschema='dc'][@qualifier='author']">
                                        <xsl:value-of select="current()"/>
                                        <xsl:variable name="nodeSemAcento" select="translate(./node(), $lowerCase, $upperCase)"/>

                                        <xsl:for-each select="../dim:field[@mdschema='usp'][@element='autor'][not(@qualifier)]">
                                            <xsl:variable name="uspAutor" select="substring-before(./node(),':')"/>
                                            <xsl:variable name="uspAutorSemAcento" select="translate($uspAutor,$lowerCase,$upperCase)"/>
                                            <xsl:if test="$nodeSemAcento=$uspAutorSemAcento">
                                                <xsl:text> </xsl:text>
-->
                                                <!-- recuperar somente o codpes do autor  -->
<!--
                                                <xsl:variable name="uspAutorInfo" select="substring-after(./node(),':')"/>
                                                <xsl:variable name="codpes" select="substring-before($uspAutorInfo,':')"/>
-->
                                                <!-- recuperar somente o itemID -->
<!--
                                                <xsl:variable name="url" select="../dim:field[@element='identifier'][@qualifier='uri'][@mdschema='dc']"/>
                                                <xsl:variable name="urlSub" select="substring-after($url,'handle/')"/>
                                                <xsl:variable name="itemID" select="substring-after($urlSub,'/')"/>
-->
                                                <!-- insere o itemID e o codpes na URL -->
<!--
                                                <a href="{$itemID}/{$codpes}/author" target="_blank" class="removeLinkUSP">
                                                    <img alt="Icon" src="{concat($theme-path, '/images/ehUSP.png')}"/>
                                                </a>
                                            </xsl:if>
                                        </xsl:for-each>

                                        <xsl:variable name="contContributorAtual" select="count(following- sibling::dim:field[@element='contributor'][@mdschema='dc'][@qualifier='author'])"/>
                                        <xsl:if test="$contContributor != $contContributorAtual">
                                            <br/>
                                        </xsl:if>
                                    </xsl:for-each>
                                </xsl:if>
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:when>
-->
<!-- 130328 andre.assada@usp.br agrupar dc.description.sponsorship sem usputils (temporario) -->
                <xsl:when test="@element='contributor' and @mdschema='dc' and @qualifier='author'">
                    <xsl:variable name="contAutor" select="count(following-sibling::dim:field[@element='contributor'][@mdschema='dc'][@qualifier='author'])"/>
                    <xsl:if test="$contAutor = 0">
                        <tr>
                            <xsl:attribute name="class">
                                <xsl:text>ds-table-row </xsl:text>
                                <xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>
                                <xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>
                            </xsl:attribute>
                            <td class="label-cell">
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                <i18n:text>
                                    <xsl:text>metadataTrad.</xsl:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                                    <xsl:value-of select="./@mdschema"/>
                                    <xsl:text>.</xsl:text>
                                    <xsl:value-of select="./@element"/>
                                    <xsl:if test="./@qualifier">
                                        <xsl:text>.</xsl:text>
                                        <xsl:value-of select="./@qualifier"/>
                                    </xsl:if>
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                                </i18n:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                            </td>
                            <td>
                                <xsl:if test="$contAutor = 0">
                                    <xsl:for-each select="../dim:field[@element='contributor'][@mdschema='dc'][@qualifier='author']">
                                        <xsl:value-of select="current()"/>
                                        <xsl:variable name="contAutorAtual" select="count(following-sibling::dim:field[@element='contributor'][@mdschema='dc'][@qualifier='author'])"/>
                                        <xsl:if test="$contAutor != $contAutorAtual">
                                            <br/>
                                            <hr/>
                                        </xsl:if>
                                    </xsl:for-each>
                                </xsl:if>
                            </td>
                        </tr>
                    </xsl:if>
                </xsl:when>
<!-- FIM 130328 andre.assada@usp.br agrupar dc.description.sponsorship sem usputils (temporario) FIM -->


            <!-- =========================================================================================================== -->
            <!-- / FIM 130328 agrupar metadados dentro de um TD, conforme determinacoes da profa. sueli (outubro 2010) FIM / -->
            <!-- =========================================================================================================== -->

            <xsl:otherwise>
                <tr>
                    <xsl:attribute name="class">
                        <xsl:text>ds-table-row </xsl:text>
                        <xsl:if test="(position() div 2 mod 2 = 0)">even </xsl:if>
                        <xsl:if test="(position() div 2 mod 2 = 1)">odd </xsl:if>
                    </xsl:attribute>
                    <td class="label-cell">
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                    <i18n:text>
                        <xsl:text>metadataTrad.</xsl:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->
                        <xsl:value-of select="./@mdschema"/>
                        <xsl:text>.</xsl:text>
                        <xsl:value-of select="./@element"/>
                        <xsl:if test="./@qualifier">
                            <xsl:text>.</xsl:text>
                            <xsl:value-of select="./@qualifier"/>
                        </xsl:if>
<!-- 130327 andre.assada@usp.br mascara nos nomes dos metadados -->
                    </i18n:text>
<!-- FIM 130327 andre.assada@usp.br mascara nos nomes dos metadados FIM -->                    
                    </td>
                <td>
                  <xsl:copy-of select="./node()"/>
                  <xsl:if test="./@authority and ./@confidence">
                    <xsl:call-template name="authorityConfidenceIcon">
                      <xsl:with-param name="confidence" select="./@confidence"/>
                    </xsl:call-template>
                  </xsl:if>
                </td>
                    <td><xsl:value-of select="./@language"/></td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- don't render the item-view-toggle automatically in the summary view, only when it gets called -->
    <xsl:template match="dri:p[contains(@rend , 'item-view-toggle') and
        (preceding-sibling::dri:referenceSet[@type = 'summaryView'] or following-sibling::dri:referenceSet[@type = 'summaryView'])]">
    </xsl:template>

    <!-- don't render the head on the item view page -->
    <xsl:template match="dri:div[@n='item-view']/dri:head" priority="5">
    </xsl:template>

        <xsl:template match="mets:fileGrp[@USE='CONTENT']">
        <xsl:param name="context"/>
        <xsl:param name="primaryBitstream" select="-1"/>

        <h2><i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-head</i18n:text></h2>
        <div class="file-list">
            <xsl:choose>
                <!-- If one exists and it's of text/html MIME type, only display the primary bitstream -->
                <xsl:when test="mets:file[@ID=$primaryBitstream]/@MIMETYPE='text/html'">
                    <xsl:apply-templates select="mets:file[@ID=$primaryBitstream]">
                        <xsl:with-param name="context" select="$context"/>
                    </xsl:apply-templates>
                </xsl:when>
                <!-- Otherwise, iterate over and display all of them -->
                <xsl:otherwise>
                    <xsl:apply-templates select="mets:file">
                     	<!--Do not sort any more bitstream order can be changed-->
                        <!--<xsl:sort data-type="number" select="boolean(./@ID=$primaryBitstream)" order="descending" />-->
                        <!--<xsl:sort select="mets:FLocat[@LOCTYPE='URL']/@xlink:title"/>-->
                        <xsl:with-param name="context" select="$context"/>
                    </xsl:apply-templates>
                </xsl:otherwise>
            </xsl:choose>
        </div>
    </xsl:template>

    <xsl:template match="mets:file">
        <xsl:param name="context" select="."/>
        <div class="file-wrapper clearfix">
            <div class="thumbnail-wrapper">
                <a class="image-link">
                    <xsl:attribute name="href">
                        <xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="$context/mets:fileSec/mets:fileGrp[@USE='THUMBNAIL']/
                        mets:file[@GROUPID=current()/@GROUPID]">
                            <img alt="Thumbnail">
                                <xsl:attribute name="src">
                                    <xsl:value-of select="$context/mets:fileSec/mets:fileGrp[@USE='THUMBNAIL']/
                                    mets:file[@GROUPID=current()/@GROUPID]/mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
                                </xsl:attribute>
                            </img>
                        </xsl:when>
                        <xsl:otherwise>
                            <img alt="Icon" src="{concat($theme-path, '/images/mime.png')}" style="height: {$thumbnail.maxheight}px;"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
            </div>
            <div class="file-metadata" style="height: {$thumbnail.maxheight}px;">
                <div>
                    <span class="bold">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-name</i18n:text>
                        <xsl:text>:</xsl:text>
                    </span>
                    <span>
                        <xsl:attribute name="title"><xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:title"/></xsl:attribute>
                        <xsl:value-of select="util:shortenString(mets:FLocat[@LOCTYPE='URL']/@xlink:title, 17, 5)"/>
                    </span>
                </div>
                <!-- File size always comes in bytes and thus needs conversion -->
                <div>
                    <span class="bold">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-size</i18n:text>
                        <xsl:text>:</xsl:text>
                    </span>
                    <span>
                        <xsl:choose>
                            <xsl:when test="@SIZE &lt; 1024">
                                <xsl:value-of select="@SIZE"/>
                                <i18n:text>xmlui.dri2xhtml.METS-1.0.size-bytes</i18n:text>
                            </xsl:when>
                            <xsl:when test="@SIZE &lt; 1024 * 1024">
                                <xsl:value-of select="substring(string(@SIZE div 1024),1,5)"/>
                                <i18n:text>xmlui.dri2xhtml.METS-1.0.size-kilobytes</i18n:text>
                            </xsl:when>
                            <xsl:when test="@SIZE &lt; 1024 * 1024 * 1024">
                                <xsl:value-of select="substring(string(@SIZE div (1024 * 1024)),1,5)"/>
                                <i18n:text>xmlui.dri2xhtml.METS-1.0.size-megabytes</i18n:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="substring(string(@SIZE div (1024 * 1024 * 1024)),1,5)"/>
                                <i18n:text>xmlui.dri2xhtml.METS-1.0.size-gigabytes</i18n:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </span>
                </div>
                <!-- Lookup File Type description in local messages.xml based on MIME Type.
         In the original DSpace, this would get resolved to an application via
         the Bitstream Registry, but we are constrained by the capabilities of METS
         and can't really pass that info through. -->
                <div>
                    <span class="bold">
                        <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-format</i18n:text>
                        <xsl:text>:</xsl:text>
                    </span>
                    <span>
                        <xsl:call-template name="getFileTypeDesc">
                            <xsl:with-param name="mimetype">
                                <xsl:value-of select="substring-before(@MIMETYPE,'/')"/>
                                <xsl:text>/</xsl:text>
                                <xsl:value-of select="substring-after(@MIMETYPE,'/')"/>
                            </xsl:with-param>
                        </xsl:call-template>
                    </span>
                </div>
                <!---->
                <!-- Display the contents of 'Description' only if bitstream contains a description -->
                <xsl:if test="mets:FLocat[@LOCTYPE='URL']/@xlink:label != ''">
                    <div>
                        <span class="bold">
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-description</i18n:text>
                            <xsl:text>:</xsl:text>
                        </span>
                        <span>
                            <xsl:attribute name="title"><xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:label"/></xsl:attribute>
                            <!--<xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:label"/>-->
                            <xsl:value-of select="util:shortenString(mets:FLocat[@LOCTYPE='URL']/@xlink:label, 17, 5)"/>
                        </span>
                    </div>
                </xsl:if>
            </div>
            <div class="file-link" style="height: {$thumbnail.maxheight}px;">
                <xsl:choose>
                    <xsl:when test="@ADMID">
                        <xsl:call-template name="display-rights"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="view-open"/>
                    </xsl:otherwise>
                </xsl:choose>
            </div>
        </div>
    </xsl:template>

    <xsl:template name="view-open">
        <a>
            <xsl:attribute name="href">
                <xsl:value-of select="mets:FLocat[@LOCTYPE='URL']/@xlink:href"/>
            </xsl:attribute>
            <i18n:text>xmlui.dri2xhtml.METS-1.0.item-files-viewOpen</i18n:text>
        </a>
    </xsl:template>

    <xsl:template name="display-rights">
        <xsl:variable name="file_id" select="jstring:replaceAll(jstring:replaceAll(string(@ADMID), '_METSRIGHTS', ''), 'rightsMD_', '')"/>
        <xsl:variable name="rights_declaration" select="../../../mets:amdSec/mets:rightsMD[@ID = concat('rightsMD_', $file_id, '_METSRIGHTS')]/mets:mdWrap/mets:xmlData/rights:RightsDeclarationMD"/>
        <xsl:variable name="rights_context" select="$rights_declaration/rights:Context"/>
        <xsl:variable name="users">
            <xsl:for-each select="$rights_declaration/*">
                <xsl:value-of select="rights:UserName"/>
                <xsl:choose>
                    <xsl:when test="rights:UserName/@USERTYPE = 'GROUP'">
                       <xsl:text> (group)</xsl:text>
                    </xsl:when>
                    <xsl:when test="rights:UserName/@USERTYPE = 'INDIVIDUAL'">
                       <xsl:text> (individual)</xsl:text>
                    </xsl:when>
                </xsl:choose>
                <xsl:if test="position() != last()">, </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="not ($rights_context/@CONTEXTCLASS = 'GENERAL PUBLIC') and ($rights_context/rights:Permissions/@DISPLAY = 'true')">
                <a href="{mets:FLocat[@LOCTYPE='URL']/@xlink:href}">
                    <img width="64" height="64" src="{concat($theme-path,'/images/Crystal_Clear_action_lock3_64px.png')}" title="Read access available for {$users}"/>
                    <!-- icon source: http://commons.wikimedia.org/wiki/File:Crystal_Clear_action_lock3.png -->
                </a>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="view-open"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
