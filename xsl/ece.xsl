<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/TR/WD-xsl">

    <xsl:template match="/">
            <HTML>
              <HEAD>
               <META http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <TITLE><xsl:apply-templates select="/lazy/title"/></TITLE>
              </HEAD>
                <xsl:apply-templates/>
            </HTML>
    </xsl:template>

	
    <xsl:template match="artist">
      <P>
      <xsl:value-of select="."/>
      </P>
    </xsl:template>
    
	<xsl:template match="headline">
	   <H1>
	   <xsl:value-of select="."/>
	   </H1>
    </xsl:template>
	
	
    <xsl:template match="title">
	   <h1>
	   <xsl:value-of select="."/>
	   </h1>
    </xsl:template>



    <xsl:template match="lazy">
		<P>bla</P>
             <xsl:apply-templates/>
    </xsl:template>
	
	
	
	
     <xsl:template match="lazycss">
	  <link><xsl:attribute name="rel">stylesheet</xsl:attribute>
               <xsl:attribute name="type">text/css</xsl:attribute>
               <xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>
         </link>
     </xsl:template>
	 
	 
     <xsl:template match="lazybody">
	  <P>
               <xsl:attribute name="background"><xsl:value-of select="."/></xsl:attribute>
      </P>
     </xsl:template>


</xsl:stylesheet>