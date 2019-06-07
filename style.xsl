<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" encoding="UTF-8"/>

<xsl:template match="/osm">
    <!-- header -->
    <xsl:text>id,lat,lon&#10;</xsl:text>
    <!-- data -->
    <xsl:for-each select="node">
        <!-- node data -->
        <xsl:value-of select="@id" />
        <xsl:text>,</xsl:text>
        <xsl:value-of select="@lat" />
        <xsl:text>,</xsl:text>
        <xsl:value-of select="@lon" />
        <xsl:text>&#10;</xsl:text>
    </xsl:for-each>
</xsl:template>

</xsl:stylesheet>