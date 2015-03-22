<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/TR/WD-xsl">
    <xsl:template match="/">
            <HTML>
              <HEAD>
               <META http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <TITLE><xsl:apply-templates select="/lazy/title"/></TITLE>
              </HEAD>
	           <script>
	              function NavigateToArticle(f){ 
                 window.navigate(f);
	              } 
	           </script>
                <xsl:apply-templates/>
            </HTML>
    </xsl:template>	
    
	
    <xsl:template match="lazy">
             <xsl:apply-templates/>
    </xsl:template>
	
    <xsl:template match="title">
    </xsl:template>
	
	<xsl:template match="lazycss">
	  <link><xsl:attribute name="rel">stylesheet</xsl:attribute>
               <xsl:attribute name="type">text/css</xsl:attribute>
               <xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>
         </link>
    </xsl:template>
    
	<xsl:template match="lazybody">
	  <body><xsl:attribute name="onload">NavigateToArticle('#expanded')</xsl:attribute>
               <xsl:attribute name="background"><xsl:value-of select="."/></xsl:attribute>
         </body>
    </xsl:template>
	
	<xsl:template match="TEST">
	 	<b>
	 	 	<xsl:value-of select="."/>
     	</b>
	</xsl:template>
 
     <xsl:template match="*|@*|comment()|text()">
      <xsl:copy>
          <xsl:apply-templates select="*|@*|comment()|text()"/>
      </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
