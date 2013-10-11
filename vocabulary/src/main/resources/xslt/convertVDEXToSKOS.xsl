<xsl:stylesheet version="2.0"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"     
    xmlns:skos="http://www.w3.org/2004/02/skos/core#"
    xmlns:vdex="http://www.imsglobal.org/xsd/imsvdex_v1p0"
    xmlns:xml="http://www.w3.org/XML/1998/namespace"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    exclude-result-prefixes="vdex">
    <xsl:output method="xml" indent="yes"/>

    <xsl:param name="vocabularyUri"/>
    <xsl:variable name="profileType" select="vdex:vdex/@profileType"/>

    <xsl:template name="processLangstring">
        <xsl:param name="element"/>
        <xsl:param name="langstrings"/>
        <xsl:for-each select="$langstrings">
            <xsl:element name="{$element}">
                <xsl:if test="@language">
                    <xsl:attribute name="xml:lang"><xsl:value-of select="@language"/></xsl:attribute>
                </xsl:if>
                <xsl:value-of select="."/> 
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="vdex:vdex">
        <rdf:RDF>
            <skos:ConceptScheme rdf:about="{$vocabularyUri}">
                <xsl:call-template name="processLangstring">
                    <xsl:with-param name="element" select="'skos:prefLabel'"/>
                    <xsl:with-param name="langstrings" select="vdex:vocabName/vdex:langstring"/>
                </xsl:call-template>
            </skos:ConceptScheme>
            <xsl:apply-templates select="//vdex:term"/>
        </rdf:RDF>
    </xsl:template>

    <xsl:template match="vdex:term">
        <xsl:variable name="termIdentifier" select="iri-to-uri(vdex:termIdentifier)"/>
        <xsl:variable name="termUri" select="concat( $vocabularyUri, '/', $termIdentifier )"/> 
        <skos:Concept rdf:about="{$termUri}">
            <skos:inScheme rdf:resource="{$vocabularyUri}"/>
            <xsl:call-template name="processLangstring">
                    <xsl:with-param name="element" select="'skos:prefLabel'"/>
                <xsl:with-param name="langstrings" select="vdex:caption/vdex:langstring"/>
            </xsl:call-template>
            <xsl:call-template name="processLangstring">
                    <xsl:with-param name="element" select="'skos:description'"/>
                <xsl:with-param name="langstrings" select="vdex:description/vdex:langstring"/>
            </xsl:call-template>
            <xsl:choose>
                <xsl:when test="../vdex:termIdentifier">
                    <xsl:variable name="parentTermIdentifier" select="iri-to-uri( ../vdex:termIdentifier )"/>
                    <xsl:variable name="parentTermUri" select="concat( $vocabularyUri, '/', $parentTermIdentifier )"/>
                    <skos:broader rdf:resource="{$parentTermUri}"/>
                </xsl:when>
                <xsl:otherwise>
                    <skos:topConceptOf rdf:resource="{$vocabularyUri}"/>
                </xsl:otherwise>
            </xsl:choose>
        </skos:Concept>
    </xsl:template>

</xsl:stylesheet>


