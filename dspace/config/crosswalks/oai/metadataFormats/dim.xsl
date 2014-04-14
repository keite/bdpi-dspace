<?xml version="1.0" encoding="UTF-8" ?>
<!-- 

	The contents of this file are subject to the license and copyright detailed 
	in the LICENSE and NOTICE files at the root of the source tree and available 
	online at http://www.dspace.org/license/ 
	
	Developed by DSpace @ Lyncode <dspace@lyncode.com> 
	
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:doc="http://www.lyncode.com/xoai" version="1.0">
	<xsl:output omit-xml-declaration="yes" method="xml" indent="yes" />

	<!-- An identity transformation to show the internal XOAI generated XML -->
	<xsl:template match="/">
		<dim:dim xmlns:dim="http://www.dspace.org/xmlns/dspace/dim" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
			xsi:schemaLocation="http://www.dspace.org/xmlns/dspace/dim http://www.dspace.org/schema/dim.xsd">
			<xsl:for-each select="doc:metadata/doc:element/doc:element/doc:element">
				<xsl:choose>
					<xsl:when test="doc:element">					
						<xsl:variable name="schema" select="../../@name"/>
						<xsl:variable name="element" select="../@name"/>
						<xsl:variable name="qualifier" select="@name"/>
						<xsl:variable name="language">
							<xsl:choose>
								<xsl:when test="doc:element[@name='none']"></xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="doc:element/@name" />									
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>					
						<xsl:for-each select="doc:element/doc:field[@name='value']">
							<dim:field>
								<xsl:attribute name="mdschema">
									<xsl:value-of select="$schema" />
								</xsl:attribute>
								<xsl:attribute name="element">
									<xsl:value-of select="$element" />
								</xsl:attribute>
								<xsl:attribute name="qualifier">
									<xsl:value-of select="$qualifier" />
								</xsl:attribute>
								<xsl:attribute name="lang">
									<xsl:value-of select="$language" />
								</xsl:attribute>							
								<xsl:if test="following-sibling::doc:field[position()=1][@name='authority']">
									<xsl:attribute name="authority">
										<xsl:value-of select="following-sibling::doc:field[position()=1][@name='authority']/text()" />
									</xsl:attribute>
									</xsl:if>
									<xsl:if test="following-sibling::doc:field[position()=2][@name='confidence']">
										<xsl:attribute name="confidence">
											<xsl:value-of select="following-sibling::doc:field[position()=2][@name='confidence']" />
    										
										</xsl:attribute>
									</xsl:if>
																
								<xsl:value-of select="text()"></xsl:value-of>							
							</dim:field>				
						</xsl:for-each>
						
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="schema" select="../../@name"/>
						<xsl:variable name="element" select="../@name"/>
						<xsl:variable name="language">
							<xsl:choose>
								<xsl:when test="@name='none'"></xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="@name" />									
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>					
						<xsl:for-each select="doc:field[@name='value']">					
							<dim:field>
								<xsl:attribute name="mdschema">
									<xsl:value-of select="$schema" />
								</xsl:attribute>
								<xsl:attribute name="element">
									<xsl:value-of select="$element" />
								</xsl:attribute>
								<xsl:attribute name="lang">
									<xsl:value-of select="$language" />
								</xsl:attribute>
								<xsl:if test="following-sibling::doc:field[position()=1][@name='authority']">
									<xsl:attribute name="authority">
										<xsl:value-of select="following-sibling::doc:field[position()=1][@name='authority']/text()" />
									</xsl:attribute>
								</xsl:if>
									<xsl:if test="following-sibling::doc:field[position()=2][@name='confidence']">
										<xsl:attribute name="confidence">
											
      										<xsl:value-of select="following-sibling::doc:field[position()=2][@name='confidence']" />
    										
										</xsl:attribute>
									</xsl:if>
															
								<xsl:value-of select="text()"></xsl:value-of>
							</dim:field>
						</xsl:for-each>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</dim:dim>
	</xsl:template>
</xsl:stylesheet>
