<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml"
    xmlns:sbhl="http://net.sf.xslthl/ConnectorSaxonB"
    xmlns:saxon6="http://icl.com/saxon" xmlns:saxonb="http://saxon.sf.net/" xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:xslthl="http://xslthl.sf.net" extension-element-prefixes="sbhl xslthl">
    
    <!-- produce xhtml -->
    <xsl:output indent="no" method="html" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
        doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" />
        
    <xsl:param name="xslthl.config" />
    
    <saxonb:script implements-prefix="sbhl" language="java" src="java:net.sf.xslthl.ConnectorSaxonB" />  
    
    <xsl:template match="/">
        <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
            <head>
                <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
                <title>XML</title>
                <style><![CDATA[
BODY {
    color: black;
    background: #ffe;
}               

PRE {
}

H1 {
    border-left: 5px solid #aaf;
    padding-left: 0.25em;
    margin-left: -0.5em;
    background: #eef;
}
                ]]></style>
            </head>
            <body>
                <xsl:apply-templates />
            </body>
        </html>
    </xsl:template>
    <xsl:template match="para">
        <p>
            <xsl:apply-templates />
        </p>
    </xsl:template>
    <xsl:template match="header">
        <h1>
            <xsl:apply-templates />
        </h1>
    </xsl:template>
    <xsl:template match="bold">
        <b>
            <xsl:apply-templates />
        </b>
    </xsl:template>
    <xsl:template match="underline">
        <u>
            <xsl:apply-templates />
        </u>
    </xsl:template>
    <xsl:template match="code">
        <xsl:variable name="result">
            <xsl:call-template name="syntax-highlight">
                <xsl:with-param name="language">
                    <xsl:value-of select="@language" />
                </xsl:with-param>
                <xsl:with-param name="source" select="." />
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="count(ancestor::code) &gt; 0">
                <!-- prevent starting a new "pre" part when it's already highlighted -->
                <xsl:copy-of select="$result" />
            </xsl:when>
            <xsl:otherwise>
                <pre>
                    <xsl:copy-of select="$result" />
                </pre>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- highlighting of the xslthl tags -->
    <xsl:template match="xslthl:*">
        <xsl:value-of select="." /><!-- fallback -->
    </xsl:template>
    <xsl:template match="xslthl:keyword">
        <span style="font-weight: bold;">
            <xsl:value-of select="." />
        </span>
    </xsl:template>
    <xsl:template match="xslthl:string">
        <span style="color: blue;">
            <xsl:value-of select="." />
        </span>
    </xsl:template>
    <xsl:template match="xslthl:number">
        <span style="color: blue;">
            <xsl:value-of select="." />
        </span>
    </xsl:template>
    <xsl:template match="xslthl:comment">
        <span style="color: green; font-style: italic;">
            <xsl:value-of select="." />
        </span>
    </xsl:template>
    <xsl:template match="xslthl:doccomment|xslthl:doctype">
        <span style="color: teal; font-style: italic;">
            <xsl:value-of select="." />
        </span>
    </xsl:template>
    <xsl:template match="xslthl:directive">
        <span style="color: maroon;">
            <xsl:value-of select="." />
        </span>
    </xsl:template>
    <xsl:template match="xslthl:annotation">
        <span style="color: gray; font-style: italic;">
            <xsl:value-of select="." />
        </span>
    </xsl:template>

    <!-- default XML styles -->
    <xsl:template match="xslthl:tag">
        <span style="color: teal;">
            <xsl:value-of select="." />
        </span>
    </xsl:template>
    <xsl:template match="xslthl:attribute">
        <span style="color: purple;">
            <xsl:value-of select="." />
        </span>
    </xsl:template>
    <xsl:template match="xslthl:value">
        <span style="color: blue;">
            <xsl:value-of select="." />
        </span>
    </xsl:template>
    
    <xsl:template match="xslthl:xslt">
        <span style="color: red;">
            <xsl:value-of select="." />
        </span>
    </xsl:template>
    
    <!-- This template will perform the actual highlighting -->
    <xsl:template name="syntax-highlight">
        <xsl:param name="language" />
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="function-available('sbhl:highlight')">
                <xsl:variable name="highlighted" select="sbhl:highlight($language, $source, $xslthl.config)" />
                <xsl:apply-templates select="$highlighted" />
            </xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

