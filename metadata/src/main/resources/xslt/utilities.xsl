<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"> 

    <xsl:template name="generateTripleElements">
        <xsl:param name="triplesAsString"/>
        <xsl:variable name="tokenizedTriples" select="tokenize( $triplesAsString, '@@@' )"/>
        <xsl:for-each select="$tokenizedTriples">
            <xsl:variable name="tokenizedTriple" select="tokenize( ., '###' )"/> 
            <xsl:variable name="subject" select="$tokenizedTriple[1]"/>
            <xsl:variable name="predicate" select="$tokenizedTriple[2]"/>
            <xsl:variable name="object" select="$tokenizedTriple[3]"/>
            <xsl:variable name="isLiteral" select="$tokenizedTriple[4]"/>
            <xsl:variable name="lang" select="if( count( $tokenizedTriple ) &gt; 4 ) then $tokenizedTriple[5] else ''"/>
            <xsl:if test="$subject != '' and $predicate != '' and normalize-space($object) != '' and $isLiteral != ''">
                <xsl:element name="triple">
                    <xsl:element name="subject"><xsl:value-of select="$subject"/></xsl:element>
                    <xsl:element name="predicate"><xsl:value-of select="$predicate"/></xsl:element>
                    <xsl:element name="object"><xsl:value-of select="$object"/></xsl:element>
                    <xsl:element name="literal"><xsl:value-of select="$isLiteral"/></xsl:element>
                    <xsl:if test="$lang != ''">
                        <xsl:element name="language"><xsl:value-of select="$lang"/></xsl:element>
                    </xsl:if>
                </xsl:element>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>

