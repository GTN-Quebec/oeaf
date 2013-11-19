<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:character-map name="html-tags">
        <xsl:output-character character="&#xE001;" string="&lt;"/>
        <xsl:output-character character="&#xE002;" string="&gt;"/> 
        <xsl:output-character character="&#xE003;" string="&amp;"/>
    </xsl:character-map>

    <xsl:character-map name="c1-control-range">
        <xsl:output-character character="&#128;" string="€"/>

        <xsl:output-character character="&#130;" string="‚"/>
        <xsl:output-character character="&#131;" string="ƒ"/>
        <xsl:output-character character="&#132;" string="„"/>
        <xsl:output-character character="&#133;" string="…"/>
        <xsl:output-character character="&#134;" string="†"/>
        <xsl:output-character character="&#135;" string="‡"/>

        <xsl:output-character character="&#137;" string="‰"/>
        <xsl:output-character character="&#138;" string="Š"/>
        <xsl:output-character character="&#139;" string="‹"/>
        <xsl:output-character character="&#140;" string="Œ"/>

        <xsl:output-character character="&#142;" string="Ž"/>


        <xsl:output-character character="&#145;" string="‘"/>
        <xsl:output-character character="&#146;" string="’"/>
        <xsl:output-character character="&#147;" string="“"/>
        <xsl:output-character character="&#148;" string="”"/>
        <xsl:output-character character="&#149;" string="•"/>
        <xsl:output-character character="&#150;" string="–"/>
        <xsl:output-character character="&#151;" string="—"/>
        <xsl:output-character character="&#152;" string="˜"/>
        <xsl:output-character character="&#153;" string="™"/>
        <xsl:output-character character="&#154;" string="š"/>
        <xsl:output-character character="&#155;" string="›"/>
        <xsl:output-character character="&#156;" string="œ"/>

        <xsl:output-character character="&#158;" string="ž"/>
        <xsl:output-character character="&#159;" string="Ÿ"/>
    </xsl:character-map>

</xsl:stylesheet>
