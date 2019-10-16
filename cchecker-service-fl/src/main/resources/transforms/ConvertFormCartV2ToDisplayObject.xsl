<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="2.0">

<!-- This is a transformation from the new object cart format to a FormCartDisplayObject. Only the data needed for the Form Cart display (displayFormsCart.jsp) is included in the transformation.  -->

    <xsl:output indent="yes" exclude-result-prefixes="xsi"/>

    <xsl:template match="/">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="form">
        <xsl:element name="form-cart-display-object">
            <xsl:attribute name="public-id">
                <xsl:value-of select="./publicID"/>
            </xsl:attribute>
            <xsl:element name="long-name">
                <xsl:value-of select="./longName"/>
            </xsl:element>
            <xsl:element name="context-name">
     	          <xsl:value-of select="./context"/>
            </xsl:element>
            <xsl:element name="form-type">
                <xsl:value-of select="./type"/>
            </xsl:element>
            <xsl:element name="asl-name">
                <xsl:value-of select="./workflowStatusName"/>
            </xsl:element>
            <xsl:element name="version">
                <xsl:value-of select="./version"/>
            </xsl:element>

<!--            <xsl:apply-templates select="protocol"/>  -->

        </xsl:element>
    </xsl:template>

    <xsl:template match="protocol">
            <xsl:element name="protocols">
			<xsl:attribute name="xsi:type">java:gov.nih.nci.ncicb.cadsr.common.dto.ProtocolTransferObject</xsl:attribute>
	            <xsl:attribute name="public-id">0</xsl:attribute>
	            <xsl:attribute name="is-published">false</xsl:attribute>
	            <xsl:element name="long-name">
      	          <xsl:value-of select="longName"/>
	            </xsl:element>
            </xsl:element>
    </xsl:template>

</xsl:stylesheet>




