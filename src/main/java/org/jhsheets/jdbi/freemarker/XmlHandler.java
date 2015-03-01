package org.jhsheets.jdbi.freemarker;
import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses an XML template file into {@link SqlStatementInfo}'s which store a string that can be loaded into a
 * Freemarker {@link freemarker.template.Template}
 */
public class XmlHandler
extends DefaultHandler
{
    private final Map<String, SqlStatementInfo> statements = new HashMap<>();

    private boolean isQuery = false;
    private String statementType = "";
    private String statementID = "";
    private StringBuilder statementText = new StringBuilder();


    /**
     * @return the statements contained within this XML template.
     */
    public Map<String, SqlStatementInfo> getStatements()
    {
        return statements;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
    throws SAXException
    {
        if (qName.equalsIgnoreCase("select") || qName.equalsIgnoreCase("insert") || qName.equalsIgnoreCase("update"))
        {
            isQuery = true;
            statementType = qName;
            statementID = attributes.getValue("id");
            statementText = new StringBuilder(); // reset to blank
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
    throws SAXException
    {
        if (isQuery && !statementID.isEmpty() && statementText.length() > 0)
        {
            final SqlStatementInfo.StatementType type = SqlStatementInfo.StatementType.valueOf(statementType);
            final SqlStatementInfo stmt = new SqlStatementInfo(statementID, type, statementText.toString());
            statements.put(statementID, stmt);

            isQuery = false;
            statementType = "";
            statementID = "";
            statementText = new StringBuilder();
        }
    }

    @Override
    public void characters(char ch[], int start, int length)
    throws SAXException
    {
        if (isQuery)
        {
            final String line = new String(ch, start, length);
            this.statementText.append(line);
        }
    }


    /**
     * Logger for any sax errors
     */
    public static class ErrHandler
    implements ErrorHandler
    {
        public final Logger logger;

        public ErrHandler(final Logger logger)
        {
            this.logger = logger;
        }

        private final String getParseExceptionInfo(final SAXParseException spe)
        {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }

            return "URI=" + systemId + " Line=" + spe.getLineNumber() + ": " + spe.getMessage();
        }

        @Override
        public void warning(final SAXParseException spe)
        throws SAXException
        {
            logger.warn(getParseExceptionInfo(spe));
        }

        @Override
        public void error(final SAXParseException spe)
        throws SAXException
        {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        @Override
        public void fatalError(final SAXParseException spe)
        throws SAXException
        {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
}