<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:saxon="java:ca.licef.proeaf.core.util.XSLTUtil"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:funct="http://proeaf.licef.ca/functions"
    exclude-result-prefixes="funct xs saxon">

    <xsl:template name="capitalize-string">
        <xsl:param name="str"/>
        <xsl:value-of select="concat( upper-case( substring( $str, 1, 1 ) ), substring( $str, 2, string-length( $str ) - 1 ) )"/>
    </xsl:template>

    <xsl:template name="replace-substring">
        <xsl:param name="original"/>
        <xsl:param name="substring"/>
        <xsl:param name="replacement" select="''"/>
        <xsl:variable name="first">
            <xsl:choose>
                <xsl:when test="contains( $original, $substring )">
                    <xsl:value-of select="substring-before( $original, $substring )"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$original"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="middle">
            <xsl:choose>
                <xsl:when test="contains( $original, $substring )">
                    <xsl:value-of select="$replacement"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text></xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="last">
            <xsl:choose>
                <xsl:when test="contains( $original, $substring )">
                    <xsl:choose>
                        <xsl:when test="contains( substring-after( $original, $substring ), $substring )">
                            <xsl:call-template name="replace-substring">
                                <xsl:with-param name="original">
                                    <xsl:value-of select="substring-after( $original, $substring )"/>
                                </xsl:with-param>
                                <xsl:with-param name="substring">
                                    <xsl:value-of select="$substring"/>
                                </xsl:with-param>
                                <xsl:with-param name="replacement">
                                    <xsl:value-of select="$replacement"/>
                                </xsl:with-param>
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="substring-after( $original, $substring )"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text></xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="concat( $first, $middle, $last )"/>
    </xsl:template>

    <xsl:template name="render-photo">
        <xsl:param name="photo"/>
        <xsl:param name="photoCssClass"/>
        <xsl:param name="maxWidth" select="140" as="xs:integer"/>
        <xsl:param name="maxHeight" select="64" as="xs:integer"/>
        <xsl:variable name="dim" select="tokenize( saxon:getImageDimension( $photo ), ',' )"/>
        <xsl:variable name="width" select="$dim[1]"/>
        <xsl:variable name="height" select="$dim[2]"/>
        <xsl:variable name="w">
            <xsl:choose>
                <xsl:when test="number($width) = -1">
                    <xsl:value-of select="''"/>
                </xsl:when>
                <xsl:when test="$width &gt; $height">
                    <xsl:variable name="tempHeight" select="round($maxWidth * number($height) div number($width))"/>
                    <xsl:value-of select="if( $tempHeight &gt; $maxHeight ) then round($maxHeight * number($width) div number($height)) else $maxWidth"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="tempWidth" select="round($maxHeight * number($width) div number($height))"/>
                    <xsl:value-of select="if( $tempWidth &gt; $maxWidth ) then $maxWidth else $tempWidth"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="h">
            <xsl:choose>
                <xsl:when test="number($height) = -1">
                    <xsl:value-of select="$maxHeight"/>
                </xsl:when>
                <xsl:when test="$height &gt; $width">
                    <xsl:variable name="tempWidth" select="round($maxHeight * number($width) div number($height))"/>
                    <xsl:value-of select="if( $tempWidth &gt; $maxWidth ) then round($maxWidth * number($height) div number($width)) else $maxHeight"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="tempHeight" select="round($maxWidth * number($height) div number($width))"/>
                    <xsl:value-of select="if( $tempHeight &gt; $maxHeight ) then $maxHeight else $tempHeight"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <img>
            <xsl:attribute name="class"><xsl:value-of select="$photoCssClass"/></xsl:attribute>
            <xsl:attribute name="src"><xsl:value-of select="$photo"/></xsl:attribute>
            <xsl:attribute name="width"><xsl:value-of select="$w"/></xsl:attribute>
            <xsl:attribute name="height"><xsl:value-of select="$h"/></xsl:attribute>
        </img>
    </xsl:template>

    <xsl:function name="funct:getLanguageString" as="xs:string">
        <xsl:param name="langCode" as="xs:string"/>
        <xsl:param name="renderingLang" as="xs:string"/>
        <xsl:choose>
            <xsl:when test="starts-with( $renderingLang, 'fr' )">
                <xsl:choose>
                    <xsl:when test="starts-with( $langCode, 'fr' )">
                        <xsl:value-of select="'Français'"/>
                    </xsl:when>
                    <xsl:when test="starts-with( $langCode, 'en' )">
                        <xsl:value-of select="'Anglais'"/>
                    </xsl:when>
                    <xsl:when test="starts-with( $langCode, 'es' )">
                        <xsl:value-of select="'Espagnol'"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$langCode"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="starts-with( $renderingLang, 'en' )">
                <xsl:choose>
                    <xsl:when test="starts-with( $langCode, 'fr' )">
                        <xsl:value-of select="'French'"/>
                    </xsl:when>
                    <xsl:when test="starts-with( $langCode, 'en' )">
                        <xsl:value-of select="'English'"/>
                    </xsl:when>
                    <xsl:when test="starts-with( $langCode, 'es' )">
                        <xsl:value-of select="'Spanish'"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$langCode"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$langCode"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

</xsl:stylesheet>
