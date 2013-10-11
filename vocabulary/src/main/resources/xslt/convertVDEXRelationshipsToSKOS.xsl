<xsl:stylesheet version="2.0"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"     
    xmlns:skos="http://www.w3.org/2004/02/skos/core#"
    xmlns:vdex="http://www.imsglobal.org/xsd/imsvdex_v1p0"
    xmlns:xml="http://www.w3.org/XML/1998/namespace"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:saxon="java:ca.licef.proeaf.vocabulary.util.XSLTUtil"
    exclude-result-prefixes="vdex"
    extension-element-prefixes="saxon">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="vdex:vdex">
        <rdf:RDF>
            <xsl:apply-templates select="//vdex:relationship"/>
        </rdf:RDF>
    </xsl:template>

    <xsl:template match="vdex:relationship">
        <xsl:param name="sourceIdentifier" select="vdex:sourceTerm/@vocabularyIdentifier"/>
        <xsl:param name="sourceTerm" select="iri-to-uri(vdex:sourceTerm)"/>
        <xsl:param name="targetIdentifier" select="vdex:targetTerm/@vocabularyIdentifier"/>
        <xsl:param name="targetTerm" select="iri-to-uri(vdex:targetTerm)"/>
        <xsl:variable name="src"  select="saxon:getVocabularyConceptUri($sourceIdentifier, $sourceTerm)"/>
        <xsl:variable name="dest"  select="saxon:getVocabularyConceptUri($targetIdentifier, $targetTerm)"/>
        <xsl:choose>
            <xsl:when test="($src) and ($dest)">
                <skos:Concept rdf:about="{$src}">
                    <xsl:choose>
                        <xsl:when test="vdex:relationshipType='RT'">
                            <skos:closeMatch rdf:resource="{$dest}"/>
                        </xsl:when>
                        <xsl:when test="vdex:relationshipType='NT'">
                            <skos:narrowMatch rdf:resource="{$dest}"/>
                        </xsl:when>
                        <xsl:when test="vdex:relationshipType='BT'">
                            <skos:broadMatch rdf:resource="{$dest}"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <skos:relatedMatch rdf:resource="{$dest}"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </skos:Concept>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>


