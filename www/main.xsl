<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns="http://www.w3.org/1999/xhtml"
version="1.0" 
xmlns:ni="xalan://org.apache.xalan.lib.NodeInfo"
exclude-result-prefixes="ni">
 
<xsl:output
method="xml"
indent="yes"
encoding="utf-8"
doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

<xsl:param name="basedir"/>
<xsl:param name="branding"/>
<xsl:variable name="brand" select="document(concat($branding,'/branding.xml'))/brand"/>
<xsl:param name="outputext"/>
<xsl:param name="sourceref"/>
<xsl:variable name="source" select="substring-after(ni:systemId(),concat($sourceref,'/'))"/>

<xsl:include href="menu.xsl"/>
<xsl:include href="docbook.xsl"/>

<xsl:template match="/">
    <html>
    <head>
    <title><xsl:value-of select="$brand/title"/></title>
    <xsl:for-each select="$brand/stylesheet">
        <link rel="stylesheet" type="text/css" href="{concat($basedir, '/', @file)}" media="screen,screen"/>
    </xsl:for-each>
    <xsl:for-each select="$brand/meta">
        <meta>
        <xsl:copy-of select="@*"/>
        </meta>
    </xsl:for-each>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <!-- The ; below is for Netscape 4 (to avoid generating <script/>) -->
    <script type="text/javascript" src="{$basedir}/style/footer.js">;</script>
    </head>

    <body>
    <div id="header">
    <h1><a href="{$basedir}/"><xsl:apply-templates mode="copy-no-ns" select="$brand/title/node()"/></a></h1>
    <p><xsl:apply-templates mode="copy-no-ns" select="$brand/headline/node()"/></p>
    </div>
    
    
    <!-- <p>NI: <xsl:value-of select="ni:systemId()"/></p>
    <p>Sourceref: <xsl:value-of select="$sourceref"/></p>
    <p>Source file: <xsl:value-of select="$source"/></p>
    <p>Menu path: <xsl:call-template name="menupath"/></p> -->
    
    <div id="nav">
    <xsl:call-template name="sidebar"/>
    </div>

    <div id="content">
    <xsl:apply-templates/>
    </div>

       
    <div id="footer">
    <div id="address"><a href="mailto:info%5Bat%5Dflymine.org">info[at]flymine.org</a> - Tel: +44 (0)1223 333377 - University of Cambridge - UK</div>
    <div id="wellcome"><xsl:apply-templates mode="copy-no-ns" select="$brand/funding/node()"/></div>
    </div>
    </body>
    </html>
</xsl:template>

<xsl:template match="index">
<ul>
<xsl:apply-templates/>
</ul>
</xsl:template>

<xsl:template match="section">
<li><span><xsl:apply-templates select="heading"/></span><ul><xsl:apply-templates select="item"/></ul></li>
</xsl:template>

<xsl:template match="section/item">
<li><xsl:apply-templates/></li>
</xsl:template>

<xsl:template match="ulink">
    <a href="{@url}">
    <xsl:choose>
        <xsl:when test="substring(@url, string-length(@url)-3) = '.sxi'">
            <xsl:attribute name="class">openoffice</xsl:attribute>
        </xsl:when>
        <xsl:when test="substring(@url, string-length(@url)-3) = '.xml'">
            <xsl:attribute name="href">
                <xsl:value-of select="substring(@url,1,string-length(@url)-3)"/>
                <xsl:value-of select="$outputext"/>
            </xsl:attribute>
        </xsl:when>
    </xsl:choose>
    
    <xsl:choose>
        <xsl:when test="count(child::node())=0">
            <xsl:value-of select="@url"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:apply-templates/>
        </xsl:otherwise>
    </xsl:choose>

    </a>
</xsl:template>

<xsl:template name="menulink">
    <xsl:param name="url"/>
    <xsl:param name="title"/>
    <a>
    <xsl:attribute name="href">
        <xsl:choose>
            <xsl:when test="starts-with($url,'http')">
                <xsl:value-of select="$url"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$basedir"/>
                <xsl:value-of select="substring(@url,1,string-length(@url)-3)"/>
                <xsl:value-of select="$outputext"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:attribute>
    
    <xsl:value-of select="$title"/>
    </a>
</xsl:template>

<xsl:template match="address">
    <address><xsl:apply-templates/>
    </address>
</xsl:template>

<xsl:template mode="copy-no-ns" match="*">
    <xsl:element name="{name(.)}">
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates mode="copy-no-ns"/>
    </xsl:element>
</xsl:template>

</xsl:stylesheet>

