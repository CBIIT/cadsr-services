<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="2.0">
    <!-- Version 4 Added 
        Trigger Action CDE ID version -->
    <!-- Version 7 added 
        adminstrative information to all objects that require it in caDSR -->
    <!-- Version 8 added 
        generate a question/checkAllThatApply (deprecated) element based on text in question/instruction/preferred-definition-->
    <!-- Version 9 added  
        generate a question/usageCategory element based on the text in the modules/long-name field
        generate a dataElement/shortName EMPTY tag
        generate a form/disease EMPTY tag 
        generate a valueDomain/shortName EMPTY tag
        changed mapping of module.longName
        fixes spelling of current cart questionRepititions -> questionRepetitions in transformed cart -->
    <!-- Version 10 add remaining fields that are planned for 4.1  
        generate a question/dataElement/dataElementDerivation (see dataElement template)
        generate a dataElement/designation 
        add module/preferredDefinition-->
    <!-- Version 11 change tag from question/checkAllThatApply to question/multiValue 
        add "choose all" to the list of text that could inidcate "yes" for this attribute 
        add default value for question/usageCategory/usageType = "None" -->
    <!-- Version 12 add extraction of isEditable for Question repetitions -->
    <!-- Version 13 
         add creation of form/module/question/dataElement/valueDomain/type, values are Enumerated or NonEnumerated -->
    <!-- Version 14
        add form/module/question/dataElement/cdeBrowserLink, generate from hueristic -->
    <!-- Version 15 
         fix to form/module/question/dataElement/valueDomain/formatName, changed from value-domain/format-name to value-domain/display-format 
         add form/module/question/dataElement/ valueDomain/valueDomainConcept for the Primary Concept of the Value domain
         add form/module/question/dataElement/ valueDomain/nciTermBrowserLink, for the primary conncept of the Value Domain, gnerated by hueristic (still working on best format)
         add form/module/question/validValue/valueMeaning/designation 
         add designation/classification -->
    <!-- Version 16
         make usageCategory a module attribute instead of a question attribute.  changed form/module/question/usgaeCategory to form/module/usageCategory based on walk through 
         added form/module/dataElement/dataElementDerivation/componentDataElement/displayOrder 
         added form/module/publicID and form/module/version-->
    <!-- Version 17
         transform cart dates into xs:dateTime datatype format -->
    <!-- FROZEN EXCEPT FOR BUG FIXES -->
    <!-- Version 18 
         BUG FIX change test for instructions when form/module/question/multiValue from Uppercase "Report all" to "report all",and "Include all" to "include all" 
         BUG FIX usageCategory/usageType xPath expression
         FIX add transformation to set usageCategory/rule based on Module instruction -->
    <!-- Version 19
         BUG FIX change order of protocol elements, moving leadOrganization to the top, to match xsdV16 -->
    <!-- Version 20 
         add empty nodes (including sub-element in complexType elements)  
            form/dateCreated, form/modifiedBy, form/changeNote, form/definition (alternate definitons), form/contactCommunication
            form/module/question/triggerAction
            form/protocol generate all sub-elements whether present or not
         add explicit apply-templates for form/modules, form/protocols, form/ref-docs, referenceDocuments/URL 
         add permissibleValue to valueDomain -->
    <!-- Version 21
         add change valueDomainConcept to complex type, include:
             add primaryConceptCode and primaryConceptName
             make nciTermBrowserLink sub-elements
         add normalize-space to doc-text, form-v2-transfer-object/preferred-definition, valid-value/description cs-csi/class-scheme-definition, 
             csi/description text fields that contain sentences -->
    <!-- Version 22 Dec-05-2012
        generate the following elements (already present in the XSD): 
                 dataElementDerivation/rule 
                 dataElementDerivation/concatenationCharacter 
                 dataElementDerivation/componentDataElement/usageCategory/usageType 
                 dataElementDerivation/componentDataElement/usageCategory/rule 
                 valueDomain/valueMeaning/definition 
                 valueDomain/valueMeaning/preferredDefinition 
       add form/module/question/publicID (added to XSD v19) 
       add form/module/question/version (added to XSD v19) -->
    <!-- Version 23 Dec-11-2012
        Fix dateModified when empty, generate valid empty date -->
    <!-- Version 24 Feb-05-2013
        Fix dataElement/valueDomain/permissibleValue/valueMeaning/publicID -> should be 000000 because the permissible value information is not in the
        current download -->
    <!-- Version 25 Feb-21-2013 
        Fix add valueMeaning/longName -> was overlooked in original design -->        
    <!-- Version 26 Mar-04-2013 
        Fix module/usageCatetory/rule xpath 
        Generate empty valueDomain/referenceDocument -->
    <xsl:output indent="yes" exclude-result-prefixes="xsi"/>
    <xsl:variable name="FILLDATE">0001-01-01T00:00:01</xsl:variable>
    <xsl:variable name="FILLPUBLICID">0000000</xsl:variable>
    <xsl:variable name="FILLVERSION">1.0</xsl:variable>

    <xsl:template match="/">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="form-v2-transfer-object">
        <xsl:element name="form">
            <xsl:element name="context">
                <xsl:value-of select="./context-name"/>
            </xsl:element>
            <xsl:element name="createdBy">
                <xsl:value-of select="created-by"/>
            </xsl:element>
            <xsl:element name="dateCreated">
                <!-- new in formCartV2 -->
                <!--  e.g. 2012-08-17 10:59:57.0 transform to xs:dateTime format 2001-10-26T21:32:52.12679-->
                <xsl:choose>
                    <xsl:when test="date-created != ''">
                		<xsl:value-of
                    		select="concat(substring(date-created, 1, 10), 'T', substring(date-created, 12, 10))"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:element>
            <xsl:element name="dateModified">
                <!--  e.g. 2012-08-17 10:59:57.0 transform to xs:dateTime format 2001-10-26T21:32:52.12679-->
                <xsl:choose>
                    <xsl:when test="date-modified != ''">
                		<xsl:value-of
                    		select="concat(substring(date-modified, 1, 10), 'T', substring(date-modified, 12, 10))"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:element>
            <xsl:element name="modifiedBy">
            <!-- new in formCartV2 -->
                <xsl:value-of select="./modified-by"/>
            </xsl:element>
            <xsl:element name="longName">
                <xsl:value-of select="./long-name"/>
            </xsl:element>
            <xsl:element name="changeNote">
            <!-- new in formCartV2 -->
                <xsl:value-of select="./change-note"/>
            </xsl:element>
            <xsl:element name="preferredDefinition">
                <xsl:value-of select="normalize-space(./preferred-definition)"/>
            </xsl:element>
            <xsl:element name="cadsrRAI">
                <xsl:value-of select="./registry-id"/>
            </xsl:element>
            <xsl:element name="publicID">
                <xsl:value-of select="./@public-id"/>
            </xsl:element>
            <xsl:element name="version">
                <xsl:value-of select="./version"/>
            </xsl:element>
            <xsl:apply-templates select="registration-status"/>
            <!-- new in formCartV2  -->
            <xsl:element name="workflowStatusName">
                <xsl:value-of select="./asl-name"/>
            </xsl:element>
            <xsl:element name="categoryName">
                <xsl:value-of select="./form-category"/>
            </xsl:element>
            <xsl:element name="disease"/>
            <!-- new in formCartV2 (new database field) -->
            <xsl:element name="type">
                <xsl:value-of select="./form-type"/>
            </xsl:element>
            <xsl:apply-templates select="designations"/>
            
            <!-- Commented out so we don't generate fake data any more -->
            <!--  xsl:call-template name="Designation"/> -->
            
            <!-- new in formCartV2 (complexType element) -->
            <xsl:apply-templates select="definitions"/>
            
            <!-- Commented out so we don't generate fake data any more -->
            <!--  xsl:call-template name="Definition"/> -->
            
            <!-- new in formCartV2 (complexType element) -->
            <xsl:element name="headerInstruction">
                <xsl:element name="text">
                    <xsl:value-of select="normalize-space(instruction/preferred-definition)"/>
                </xsl:element>
            </xsl:element>
            <xsl:element name="footerInstruction">
                <xsl:element name="text">
                    <xsl:value-of
                        select="normalize-space(./footer-instructions/preferred-definition)"/>
                </xsl:element>
            </xsl:element>
            <xsl:apply-templates select="modules"/>
            <xsl:apply-templates select="protocols"/>
            <xsl:apply-templates select="referece-docs"/>
            <xsl:apply-templates select="classifications"/>
            <xsl:apply-templates select="contact-communication-v2"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="instruction[parent::form-v2-transfer-object]"/>
    <xsl:template match="context[parent::form-v2-transfer-object]"/>

    <xsl:template match="registration-status">
        <xsl:element name="registrationStatus">
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@xsi:type"/>

    <xsl:template match="modules">
        <xsl:element name="module">
            <xsl:element name="displayOrder">
                <xsl:value-of select="./@display-order"/>
            </xsl:element>
            <xsl:element name="maximumModuleRepeat">
                <xsl:value-of select="./@number-of-repeats"/>
            </xsl:element>
            <xsl:element name="createdBy"/>
            <!-- New in formCartV2 -->
            <xsl:element name="dateCreated">
            	<xsl:choose>
                    <xsl:when test="date-created">
                        <xsl:value-of select="concat(substring(date-created, 1, 10), 'T', substring(date-created, 12, 10))"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:element>
            <xsl:element name="dateModified">
            	<xsl:choose>
                    <xsl:when test="date-modified">
                        <xsl:value-of select="concat(substring(date-modified, 1, 10), 'T', substring(date-modified, 12, 10))"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:element>
            <xsl:element name="modifiedBy"/>
            <!-- New in formCartV2 -->
            <xsl:element name="longName">
                <xsl:value-of select="long-name"/>
            </xsl:element>
            <xsl:element name="instruction">
                <xsl:element name="text">
                    <xsl:value-of select="normalize-space(instruction/preferred-definition)"/>
                </xsl:element>
            </xsl:element>
            <xsl:element name="preferredDefinition">
                <xsl:value-of select="preferred-definition"/>
            </xsl:element>
            <xsl:element name="publicID">
                <xsl:value-of select="./@public-id"/>
            </xsl:element>
            <xsl:element name="version">
                <xsl:value-of select="version"/>
            </xsl:element>
            <!-- move usageCategory to MODULE level based on walk-through 10/01/2012 -->
            <!-- set usageCategory.rule to module instruction -->
            <xsl:element name="usageCategory">
                <!-- New in formCartV2 - generated by Form Builder 4.0.4 - new database field in Form Builder 4.1 -->
                <xsl:element name="usageType">
                    <xsl:choose>
                        <xsl:when test="contains(lower-case(long-name), 'mandatory')">
                            <xsl:text>Mandatory</xsl:text>
                        </xsl:when>
                        <xsl:when test="contains(lower-case(long-name), 'optional')">
                            <xsl:text>Optional</xsl:text>
                        </xsl:when>
                        <xsl:when test="contains(lower-case(long-name), 'conditional')">
                            <xsl:text>Conditional</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>None</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:element>
                <xsl:element name="rule">
                    <xsl:value-of select="instruction/preferred-definition"/>
                </xsl:element>
            </xsl:element>
            <xsl:apply-templates select="questions"/>
            <xsl:apply-templates select="trigger-actions"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="questions">
        <xsl:element name="question">
            <!-- added V22 -->
            <xsl:element name="publicID">
                <xsl:value-of select="@public-id"/>
            </xsl:element>
            <!-- added V22 -->
            <xsl:element name="version">
                <xsl:value-of select="./version"/>
            </xsl:element>
            <xsl:element name="isDerived">
                <xsl:value-of select="@de-derived"/>
            </xsl:element>
            <xsl:element name="displayOrder">
                <xsl:value-of select="@display-order"/>
            </xsl:element>
            <!-- Added in formCartV2 -->
            <xsl:element name="createdBy"/>
            <!-- Added in formCartV2 -->
            <xsl:element name="dateCreated">
                <xsl:choose>
                    <xsl:when test="date-created">
                        <xsl:value-of select="concat(substring(date-created, 1, 10), 'T', substring(date-created, 12, 10))"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:element>
            <!-- Added in formCartV2 -->
            <xsl:element name="dateModified">
                <xsl:choose>
                    <xsl:when test="date-modified">
                        <xsl:value-of select="concat(substring(date-modified, 1, 10), 'T', substring(date-modified, 12, 10))"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:element>
            <!-- Added in formCartV2 -->
            <xsl:element name="modifiedBy"/>
            <xsl:element name="questionText">
                <xsl:value-of select="normalize-space(long-name)"/>
            </xsl:element>
            <xsl:element name="instruction">
                <xsl:element name="text">
                    <xsl:value-of select="normalize-space(instruction/preferred-definition)"/>
                </xsl:element>
            </xsl:element>
            <xsl:apply-templates select="default-valid-value"/>
            <xsl:element name="isEditable">
                <xsl:choose>
                    <xsl:when test="@editable = 'true'">
                        <xsl:text>Yes</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>No</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            <xsl:element name="isMandatory">
                <xsl:choose>
                    <xsl:when test="@mandatory = 'true'">
                        <xsl:text>Yes</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>No</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            <xsl:element name="multiValue">
                <!-- New in formCartV2 - generated field in Form Builder 4.0.4 - not a database field -->
                <xsl:choose>
                    <xsl:when
                        test="contains(lower-case(instruction/preferred-definition), 'check all')">
                        <xsl:text>Yes</xsl:text>
                    </xsl:when>
                    <xsl:when
                        test="contains(lower-case(instruction/preferred-definition), 'mark all')">
                        <xsl:text>Yes</xsl:text>
                    </xsl:when>
                    <xsl:when
                        test="contains(lower-case(instruction/preferred-definition), 'select all')">
                        <xsl:text>Yes</xsl:text>
                    </xsl:when>
                    <xsl:when
                        test="contains(lower-case(instruction/preferred-definition), 'choose all')">
                        <xsl:text>Yes</xsl:text>
                    </xsl:when>
                    <xsl:when
                        test="contains(lower-case(instruction/preferred-definition), 'all that')">
                        <xsl:text>Yes</xsl:text>
                    </xsl:when>
                    <xsl:when
                        test="contains(lower-case(instruction/preferred-definition), 'enter all')">
                        <xsl:text>Yes</xsl:text>
                    </xsl:when>
                    <xsl:when
                        test="contains(lower-case(instruction/preferred-definition), 'report all')">
                        <xsl:text>Yes</xsl:text>
                    </xsl:when>
                    <xsl:when
                        test="contains(lower-case(instruction/preferred-definition), 'include all')">
                        <xsl:text>Yes</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>No</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            <xsl:apply-templates select="data-element"/>
            <xsl:apply-templates select="valid-values"/>
            <xsl:element name="triggerAction"/>
            <!-- This is a QUESTION level skip. Added in formCartV2 -->
            <xsl:apply-templates select="question-repititions"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="data-element">
        <xsl:element name="dataElement">
            <xsl:element name="longName">
                <xsl:value-of select="long-name"/>
            </xsl:element>
            <xsl:element name="shortName">
                <xsl:value-of select="preferred-name"/>
            </xsl:element>
            <!-- Added in formCartV2 filled in by Form Builder 4.0.4 -->
            <xsl:element name="publicID">
                <xsl:value-of select="CDEId"/>
            </xsl:element>
            <xsl:element name="version">
                <xsl:value-of select="version"/>
            </xsl:element>
            <xsl:element name="workflowStatusName">
                <xsl:value-of select="asl-name"/>
            </xsl:element>
            <xsl:element name="preferredDefinition">
                <xsl:value-of select="normalize-space(./preferred-definition)"/>
            </xsl:element>
            <!--  xsl:call-template name="Designation"/>-->
            <xsl:apply-templates select="designations"/>
            <!-- Added in formCartV2  -->
            <xsl:apply-templates select="value-domain"/>
            <xsl:choose>
                <xsl:when test="parent::questions/@de-derived = 'true'">
                    <xsl:element name="dataElementDerivation">
                        <!-- Added in formCartV2 -->
                        <xsl:element name="type"/>
                        <xsl:element name="methods"/>
                        <xsl:element name="concatenationCharacter"/>
                        <xsl:element name="rule"/>
                        <xsl:element name="componentDataElement">
                            <xsl:element name="usageCategory">
                                <!-- complext type, not a database field yet -->
                                <xsl:element name="usageType">Mandatory</xsl:element>
                                <!-- not a database field yet -->
                                <xsl:element name="rule"/>
                            </xsl:element>
                            <xsl:element name="displayOrder">0</xsl:element>
                            <xsl:element name="dataElement">
                                <xsl:element name="publicID">0</xsl:element>
                                <xsl:element name="version">0</xsl:element>
                                <xsl:call-template name="ValueDomain"/>
                                <!-- added in V20 complexType-->
                            </xsl:element>
                        </xsl:element>
                    </xsl:element>
                </xsl:when>
                <xsl:otherwise/>
            </xsl:choose>
            <xsl:apply-templates select="referece-docs"/>
            <!-- generate url to link to CDE Browser for the data element -->
            <xsl:element name="cdeBrowserLink">
                <!-- Added in formCartV2 - generated in Form Builder 4.0.4 - not a database field -->
                <xsl:variable name="baseURL"
                    >https://cdebrowser.nci.nih.gov/CDEBrowser/search?elementDetails=9&amp;FirstTimer=0&amp;PageId=ElementDetailsGroup&amp;publicId=</xsl:variable>
                <xsl:variable name="publicIdValue" select="CDEId"/>
                <xsl:variable name="attributeName">&amp;version=</xsl:variable>
                <xsl:variable name="value" select="version"/>

                <xsl:value-of select="concat($baseURL, $publicIdValue, $attributeName, $value)"/>

            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="context">
        <xsl:element name="context">
            <xsl:value-of select="./name"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="value-domain">
        <xsl:variable name="VDid" select="./@public-id"/>
        <xsl:element name="valueDomain">
            <xsl:element name="longName">
                <xsl:value-of select="long-name"/>
            </xsl:element>
            <xsl:element name="shortName">
             	<xsl:value-of select="preferred-name"/>
            </xsl:element>
            <!-- New in formCartV2  -->
            <xsl:element name="publicID">
                <xsl:value-of select="@public-id"/>
            </xsl:element>
            <xsl:element name="version">
                <xsl:value-of select="version"/>
            </xsl:element>
            
            <xsl:element name="type">
                <!-- New in formCartV2  - not a database field - derived from the presence of permissible values -->
                <xsl:choose>
                    <xsl:when test="permissible-value-v2">Enumerated</xsl:when>
                    <xsl:otherwise>NonEnumerated</xsl:otherwise>
                </xsl:choose>  
            </xsl:element>
           
            <xsl:element name="context">TEST</xsl:element>
            <!-- New in formCartV2 -->
            <xsl:element name="workflowStatusName">
                <xsl:value-of select="asl-name"/>
            </xsl:element>
            <xsl:element name="datatypeName">
                <xsl:value-of select="datatype"/>
            </xsl:element>
            <xsl:element name="decimalPlace">
                <xsl:value-of select="decimal-place"/>
            </xsl:element>
            <xsl:element name="formatName">
                <xsl:value-of select="display-format"/>
            </xsl:element>
            <xsl:element name="highValueNumber">
                <xsl:value-of select="high-value"/>
            </xsl:element>
            <xsl:element name="lowValueNumber">
                <xsl:value-of select="low-value"/>
            </xsl:element>
            <xsl:element name="maximumLengthNumber">
                <xsl:value-of select="max-length"/>
            </xsl:element>
            <xsl:element name="minimumLengthNumber">
                <xsl:value-of select="min-length"/>
            </xsl:element>
            <xsl:element name="UOMName">
                <xsl:value-of select="unit-of-measure"/>
            </xsl:element>
            <xsl:apply-templates select="concept-derivation-rule/component-concepts"/>
            <!-- new in formCartV2 - complex element -->
            <xsl:apply-templates select="permissible-value-v2"/>                      
                         
            <!-- new in formCartV2 - complex element -->
            <xsl:element name="referenceDocument"/> <!-- New - empty node for 4.0.4 - in Version 26 of xsl --> 
        </xsl:element>
    </xsl:template>

    <xsl:template match="default-valid-value">
        <xsl:element name="defaultValue">
            <xsl:value-of select="long-name"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="valid-values">
        <xsl:element name="validValue">
            <xsl:element name="displayOrder">
                <xsl:value-of select="./@display-order"/>
            </xsl:element>
            <xsl:element name="value">
                <xsl:value-of select="./long-name"/>
            </xsl:element>
            <xsl:element name="meaningText">
                <xsl:value-of select="normalize-space(./form-value-meaning-text)"/>
            </xsl:element>
            <xsl:element name="description">
                <xsl:value-of select="normalize-space(./form-value-meaning-desc)"/>
            </xsl:element>
            <xsl:element name="instruction">
                <xsl:element name="text">
                    <xsl:value-of select="normalize-space(./instruction/preferred-definition)"/>
                </xsl:element>
            </xsl:element>
            <!-- copied value-meaning from value-meaning-v2 definition  SULA -->
            <xsl:apply-templates select="value-meaning"/>
            <xsl:apply-templates select="trigger-actions"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="concept-derivation-rule/component-concepts">
        <!-- generate link to open the concept in NCI Browser -->
        <xsl:variable name="baseURL"
            >http://ncit.nci.nih.gov/ncitbrowser/ConceptReport.jsp?dictionary=</xsl:variable>
        <xsl:variable name="dictionary" select="replace(concept/origin, ' ', '%20')"/>
        <xsl:variable name="identifier">&amp;code=</xsl:variable>
        <xsl:variable name="code" select=".[./@is-primary = 'true']/concept/code"/>

        <xsl:element name="valueDomainConcept">
            <xsl:element name="primaryConceptName">
                <xsl:value-of select=".[./@is-primary = 'true']/concept/long-name"/>
            </xsl:element>
            <xsl:element name="primaryConceptCode">
                <xsl:value-of select=".[./@is-primary = 'true']/concept/code"/>
            </xsl:element>
            <xsl:element name="nciTermBrowserLink">
                <!-- not a database field, generated by this template -->
                <xsl:value-of select="concat($baseURL,$dictionary, $identifier, $code)"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

<!-- This is for the valueMeaning under DataElement/valueDomain/permissibleValue/valueMeaning -->
    <xsl:template match="value-meaning-v2">
        <xsl:element name="valueMeaning">
            <xsl:element name="publicID">
            	<xsl:value-of select="./@public-id"/>
            </xsl:element>
            <xsl:element name="version">
            	<xsl:value-of select="./version"/>
            </xsl:element>
            <xsl:element name="longName">
                <xsl:value-of select="./long-name"/>
            </xsl:element>
            <xsl:choose>
                <xsl:when test="designations">
                    <xsl:apply-templates select="designations"/>
                </xsl:when>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="definitions">
                    <xsl:apply-templates select="definitions"/>
                    <!-- Added in V22 -->
                </xsl:when>
            </xsl:choose>
            <xsl:element name="preferredDefinition">
                <!-- Added in V22 -->
                <xsl:value-of select="preferred-definition"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="value-meaning">
        <xsl:element name="valueMeaning">
            <xsl:element name="publicID">
            	<xsl:value-of select="./@public-id"/>
            </xsl:element>
            <xsl:element name="version">
            	<xsl:value-of select="./version"/>
            </xsl:element>
            
        </xsl:element>
    </xsl:template>

    <xsl:template match="designations" name="Designations">
        <xsl:element name="designation">
            <xsl:element name="createdBy">
            	<xsl:value-of select="name"/>
            </xsl:element>
            <!-- Added in formCartV2  -->
            <xsl:element name="dateCreated">
                <!-- Added in formCartV2  -->
                <xsl:value-of select="concat(substring(date-created, 1, 10), 'T', substring(date-created, 12, 10))"/>
            </xsl:element>
	                <!-- Added in formCartV2  -->
	        <xsl:element name="dateModified">
            	 <xsl:choose>
                    <xsl:when test="date-modified">
                        <xsl:value-of select="concat(substring(date-modified, 1, 10), 'T', substring(date-modified, 12, 10))"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:element>
	        <xsl:if test="modifiedBy">
            <xsl:element name="modifiedBy"/>
            </xsl:if>
            <!-- Added in formCartV2 -->
            <xsl:element name="languageName">
                <xsl:value-of select="language"/>
            </xsl:element>
            <xsl:if test="name">
	            <xsl:element name="name">
	                <xsl:value-of select="name"/>
	            </xsl:element>
            </xsl:if>
            <xsl:element name="type">
                <xsl:value-of select="type"/>
            </xsl:element>
            <xsl:apply-templates select="context"/>
            <xsl:apply-templates select="cs-csis"/>
        </xsl:element>
    </xsl:template>
    
    <!-- This is for form level classification -->
    <xsl:template match="classifications">
        <xsl:element name="classification">
            <xsl:element name="name">
                <xsl:value-of select="class-scheme-long-name"/>
            </xsl:element>
            <xsl:element name="publicID">
                <xsl:value-of select="cs-iD"/>
            </xsl:element>
            <xsl:element name="version">
                <xsl:value-of select="cs-version"/>
            </xsl:element>
            <xsl:element name="preferredDefinition">
                <xsl:value-of select="normalize-space(class-scheme-definition)"/>
            </xsl:element>
            <xsl:element name="classificationSchemeItem">
                <xsl:element name="name">
                    <xsl:value-of select="class-scheme-item-name"/>
                </xsl:element>
                <xsl:element name="publicID">
                    <xsl:value-of select="csi-id"/>
                </xsl:element>
                <xsl:element name="version">
                    <xsl:value-of select="csi-version"/>
                </xsl:element>
                <xsl:element name="type">
                    <xsl:value-of select="class-scheme-item-type"/>
                </xsl:element>
                <xsl:element name="preferredDefinition">
                    <xsl:value-of select="normalize-space(csi-description)"/>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="cs-csis">
        <xsl:element name="classification">
            <xsl:element name="name">
                <xsl:value-of select="class-scheme-long-name"/>
            </xsl:element>
            <xsl:element name="publicID">
                <xsl:value-of select="cs-iD"/>
            </xsl:element>
            <xsl:element name="version">
                <xsl:value-of select="cs-version"/>
            </xsl:element>
            <xsl:element name="preferredDefinition">
                <xsl:value-of select="normalize-space(class-scheme-definition)"/>
            </xsl:element>
            <xsl:element name="classificationSchemeItem">
                <xsl:element name="name">
                    <xsl:value-of select="class-scheme-item-name"/>
                </xsl:element>
                <xsl:element name="publicID">
                    <xsl:value-of select="csi-id"/>
                </xsl:element>
                <xsl:element name="version">
                    <xsl:value-of select="csi-version"/>
                </xsl:element>
                <xsl:element name="type">
                    <xsl:value-of select="class-scheme-item-type"/>
                </xsl:element>
                <xsl:element name="preferredDefinition">
                    <xsl:value-of select="normalize-space(csi-description)"/>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="trigger-actions">
        <xsl:element name="triggerAction">
            <xsl:apply-templates select="action-target/module"/>
            <xsl:apply-templates select="instruction"/>

            <xsl:element name="targetQuestionDisplayOrder">
                <xsl:value-of select="action-target/@display-order"/>
            </xsl:element>
            <xsl:element name="targetDataElementPublicID">
                <xsl:value-of select="action-target/data-element/CDEId"/>
            </xsl:element>
            <xsl:element name="targetDataElementVersion">
                <xsl:value-of select="action-target/data-element/version"/>
            </xsl:element>
            <xsl:apply-templates select="protocols"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="question-repititions">
        <xsl:element name="questionRepetitions">
            <xsl:element name="repeatSequenceNumber">
                <xsl:value-of select="./@repeat-sequence"/>
            </xsl:element>
            <xsl:element name="defaultValidValue">
                <xsl:value-of
                    select="normalize-space(./default-valid-value/form-value-meaning-text)"/>
            </xsl:element>
            <xsl:element name="isEditable">
                <xsl:choose>
                    <xsl:when test="@editable = 'true'">
                        <xsl:text>Yes</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>No</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="instruction">
        <xsl:element name="instruction">
            <xsl:element name="text">
                <xsl:value-of select="normalize-space(.)"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="action-target/module">
        <xsl:element name="targetModuleDisplayOrder">
            <xsl:value-of select="./@display-order"/>
        </xsl:element>
        <xsl:element name="targetModuleName">
            <xsl:value-of select="normalize-space(./long-name)"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="protocols">
        <xsl:element name="protocol">
            <xsl:element name="leadOrganization">
                <xsl:value-of select="lead-org"/>
            </xsl:element>
            <xsl:element name="phase">
                <xsl:value-of select="phase"/>
            </xsl:element>
            <xsl:element name="type">
                <xsl:value-of select="type"/>
            </xsl:element>
            <xsl:element name="protocolID">
                <xsl:value-of select="protocol-id"/>
            </xsl:element>
            <xsl:element name="longName">
                <xsl:value-of select="normalize-space(long-name)"/>
            </xsl:element>
            <xsl:apply-templates select="context"/>
            <xsl:element name="shortName">
                <xsl:value-of select="normalize-space(preferred-name)"/>
            </xsl:element>
            <xsl:element name="preferredDefinition">
                <xsl:value-of select="normalize-space(preferred-definition)"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="referece-docs" name="reference-document">
        <xsl:element name="referenceDocument">
            <xsl:element name="name">
                <xsl:value-of select="doc-name"/>
            </xsl:element>
            <xsl:element name="type">
                <xsl:value-of select="doc-type"/>
            </xsl:element>
            <xsl:apply-templates select="context"/>
            <xsl:element name="doctext">
                <xsl:value-of select="normalize-space(doc-text)"/>
            </xsl:element>
            <xsl:element name="URL">
                <xsl:value-of select="url"/>
            </xsl:element>
            <xsl:apply-templates select="attachments"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attachments">
        <xsl:element name="attachments">
            <xsl:element name="name">
                <xsl:value-of select="./name"/>
            </xsl:element>
            <xsl:element name="mimeType">
                <xsl:value-of select="./mime-type"/>
            </xsl:element>
            <xsl:element name="size">
                <xsl:value-of select="./@doc-size"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="*"/>
    <xsl:template match="@*"/>

    <xsl:template name="Designation">
        <!-- added v20 to support generating dataElementDerivation/componentDataElement details -->
        <xsl:element name="designation">
            <xsl:element name="createdBy"/>
            <xsl:element name="dateCreated">
                <xsl:value-of select="$FILLDATE"/>
            </xsl:element>
            <xsl:element name="dateModified">
                <xsl:value-of select="$FILLDATE"/>
            </xsl:element>
            <xsl:element name="modifiedBy"/>
            <xsl:element name="languageName">ENGLISH</xsl:element>
            <xsl:element name="name"/>
            <xsl:element name="type">ABBREVIATION</xsl:element>
            <xsl:element name="context">caBIG</xsl:element>
            <xsl:call-template name="Classification"/>
            <!-- complexType -->
        </xsl:element>
    </xsl:template>

    <xsl:template name="ValueDomain">
        <!-- added v20 to support dataElementDerivation/componentDataElement details -->
        <xsl:element name="valueDomain">
            <xsl:element name="longName"/>
            <xsl:element name="shortName"/>
            <xsl:element name="publicID">
                <xsl:value-of select="$FILLPUBLICID"/>
            </xsl:element>
            <xsl:element name="version">
                <xsl:value-of select="$FILLVERSION"/>
            </xsl:element>
            <xsl:element name="type">Enumerated</xsl:element>
            <xsl:element name="context">TEST</xsl:element>
            <xsl:element name="workflowStatusName">DRAFT NEW</xsl:element>
            <xsl:element name="datatypeName">CHARACTER</xsl:element>
            <xsl:element name="decimalPlace"/>
            <xsl:element name="formatName"/>
            <xsl:element name="highValueNumber"/>
            <xsl:element name="lowValueNumber"/>
            <xsl:element name="maximumLengthNumber"/>
            <xsl:element name="minimumLengthNumber"/>
            <xsl:element name="UOMName"/>
            <xsl:element name="valueDomainConcept">
                <xsl:element name="primaryConceptName"/>
                <xsl:element name="primaryConceptCode"/>
                <xsl:element name="nciTermBrowserLink">http://blankNode</xsl:element>
            </xsl:element>
            <xsl:apply-templates select="permissible-value-v2"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="permissible-value-v2">
        <!-- added v20 to support dataElementDerivation/componentDataElement details -->
        <xsl:element name="permissibleValue">
           	<xsl:element name="value">
           		<xsl:value-of select="value"/>
           	</xsl:element>            
           	<xsl:apply-templates select="value-meaning-v2"/>
           	<xsl:element name="beginDate"/>
           	<xsl:element name="endDate"/>
        </xsl:element>
    </xsl:template>


    <xsl:template name="Classification">
        <!-- added v20 to support dataElementDerivation/componentDataElement details -->
        <xsl:element name="classification">
            <xsl:element name="name"/>
            <xsl:element name="publicID">
                <xsl:value-of select="$FILLPUBLICID"/>
            </xsl:element>
            <xsl:element name="version">
                <xsl:value-of select="$FILLVERSION"/>
            </xsl:element>
            <xsl:element name="preferredDefinition"/>
            <xsl:element name="classificationSchemeItem">
                <xsl:element name="name"/>
                <xsl:element name="publicID">
                    <xsl:value-of select="$FILLPUBLICID"/>
                </xsl:element>
                <xsl:element name="version">
                    <xsl:value-of select="$FILLVERSION"/>
                </xsl:element>
                <xsl:element name="type">TEST</xsl:element>
                <xsl:element name="preferredDefinition"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="definitions" name="Definition">
        <!-- Added v20 to support generating multiple definitions for forms and to support valueMeaning definitions -->
        <xsl:element name="definition">
            <xsl:element name="createdBy"/>
            <xsl:element name="dateCreated">
                <!-- xsl:value-of select="date-created"/>  -->
                <xsl:value-of select="concat(substring(date-created, 1, 10), 'T', substring(date-created, 12, 10))"/>
            </xsl:element>
            <xsl:element name="dateModified">
            	 <xsl:choose>
                    <xsl:when test="date-modified">
                        <xsl:value-of select="concat(substring(date-modified, 1, 10), 'T', substring(date-modified, 12, 10))"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:element>
            <xsl:element name="modifiedBy"/>
            <xsl:element name="languageName">
                <!-- modified in V22 -->
                <xsl:choose>
                    <xsl:when test="langauge">
                        <xsl:value-of select="language"/>
                    </xsl:when>
                    <xsl:otherwise>ENGLISH</xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            <xsl:element name="text">
                <!-- modified in V22 -->
                <xsl:value-of select="definition"/>
            </xsl:element>
            <xsl:element name="type">
                <!-- modified in V22 -->
                <xsl:choose>
                    <xsl:when test="type">
                        <xsl:value-of select="type"/>
                    </xsl:when>
                    <xsl:otherwise>NCI</xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            <xsl:choose>
                <xsl:when test="cs-csis">
                    <!-- added in V 22 -->
                    <xsl:apply-templates select="cs-csis"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="Classification"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:element name="context">
                <!-- Added in V22 -->
                <xsl:choose>
                    <xsl:when test="context">
                        <xsl:value-of select="context/name"/>
                    </xsl:when>
                    <xsl:otherwise>TEST</xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="contact-communication-v2">
        <!-- Added v20 for completeness -->
        <xsl:element name="contactCommunication">
            <xsl:element name="rank">
            	<xsl:value-of select="./@rank-order"/>
            </xsl:element>
            <xsl:element name="type">
            	<xsl:value-of select="./type"/>
            </xsl:element>
            <xsl:element name="value">
            	<xsl:value-of select="./value"/>
            </xsl:element>
            <xsl:element name="organizationName">
            	<xsl:value-of select="./organization-name"/>
            </xsl:element>
            <xsl:element name="organizationRAI">
            	<xsl:value-of select="./organization-rai"/>
            </xsl:element>            
            <xsl:element name="person">
                <xsl:element name="firstName">
	            	<xsl:value-of select="./person/first-name"/>
    	        </xsl:element>     
                <xsl:element name="lastName">
					<xsl:value-of select="./person/last-name"/>
				</xsl:element>               
                <xsl:element name="position">
					<xsl:value-of select="./person/position"/>
				</xsl:element>
				<xsl:apply-templates select="./person/addresses"/>				
            </xsl:element>
            <xsl:element name="createdBy">
                <xsl:value-of select="created-by"/>
            </xsl:element>
            <xsl:element name="dateCreated">
                <!--  e.g. 2012-08-17 10:59:57.0 transform to xs:dateTime format 2001-10-26T21:32:52.12679-->
                <xsl:choose>
                    <xsl:when test="date-created != ''">
                <xsl:value-of
                    select="concat(substring(date-created, 1, 10), 'T', substring(date-created, 12, 10))"
                />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$FILLDATE"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            <xsl:element name="dateModified">
                <!--  e.g. 2012-08-17 10:59:57.0 transform to xs:dateTime format 2001-10-26T21:32:52.12679-->
                <xsl:choose>
                    <xsl:when test="date-modified != ''">
                <xsl:value-of
                    select="concat(substring(date-modified, 1, 10), 'T', substring(date-modified, 12, 10))"
                />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$FILLDATE"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
            <xsl:element name="modifiedBy">
                <xsl:value-of select="./modified-by"/>
            </xsl:element>            
        </xsl:element>
    </xsl:template>

	<xsl:template match="addresses">
		<xsl:element name="address">
			<xsl:element name="addressLine1">
				<xsl:value-of select="address-line1" />
			</xsl:element>
			<xsl:element name="addressLine2">
				<xsl:value-of select="address-line2" />
			</xsl:element>
			<xsl:element name="city">
				<xsl:value-of select="city" />
			</xsl:element>
			<xsl:element name="state">
				<xsl:value-of select="state" />
			</xsl:element>
			<xsl:element name="country">
				<xsl:value-of select="country" />
			</xsl:element>
			<xsl:element name="postalCode">
				<xsl:value-of select="postal-code" />
			</xsl:element>
			<xsl:element name="rank">
				<xsl:value-of select="rank" />
			</xsl:element>
			<xsl:element name="type">
				<xsl:value-of select="type" />
			</xsl:element>
		</xsl:element>
	</xsl:template>    
</xsl:stylesheet>