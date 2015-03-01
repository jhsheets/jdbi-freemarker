package org.jhsheets.jdbi.freemarker;

import freemarker.cache.TemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * A custom template loader which can be used to parse our XML files that contain multiple Freemarker templates
 * representing SQL queries.
 * <br/><br/>
 * Resources are loaded using {@code Thread.currentThread().getContextClassLoader().getResource()}
 */
public class SqlTemplateLoader
implements TemplateLoader
{
    private static Logger logger = LoggerFactory.getLogger(SqlTemplateLoader.class);


    /**
     * @return Generate a name that can be used to uniquely identify a statement within a templatefile
     */
    public static String buildTemplateName(final String templateLocation, final String statementID)
    {
        return templateLocation + "#" + statementID;
    }

    private String getTemplatePath(final Object templateSource)
    {
        final String name = templateSource.toString();
        return name != null && name.isEmpty() == false ? name.split("#")[0] : null;
    }

    private URL getTemplateURL(final String templatePath)
    {
        return Thread.currentThread().getContextClassLoader().getResource( templatePath );
    }

    private String getStatementID(final Object templateSource)
    {
        final String name = templateSource.toString();
        return name != null && name.isEmpty() == false ? name.split("#")[1] : null;
    }

    @Override
    public Object findTemplateSource(final String name)
    throws IOException
    {
        return name;
    }

    @Override
    public long getLastModified(final Object templateSource)
    {
        // Get the last modified time of the xml file
        final String tp = getTemplatePath(templateSource);
        final URL url = getTemplateURL( tp );
        try
        {
            final URLConnection c = url.openConnection();
            return c.getLastModified();
        }
        catch (Exception e)
        {
           logger.error("Error reading timestamp for file: " + tp, e);
        }
        return System.currentTimeMillis();
    }

    @Override
    public Reader getReader(final Object templateSource, final String encoding)
            throws IOException {
        logger.debug("Looking for Freemarker template: {}", templateSource.toString());

        final String statementID = getStatementID(templateSource);
        final String templateFile = getTemplatePath(templateSource);
        final URL templateUrl = getTemplateURL( templateFile );

        try
        {
            // Configure XML template parser
            final XmlHandler xmlHandler = new XmlHandler();
            final SAXParserFactory spf = SAXParserFactory.newInstance();
            final SAXParser parser = spf.newSAXParser();
            final XMLReader xmlReader = parser.getXMLReader();
            xmlReader.setErrorHandler(new XmlHandler.ErrHandler(logger));
            xmlReader.setContentHandler(xmlHandler);

            // Open the XML file and parse it
            try (final InputStream is = templateUrl.openStream())
            {
                xmlReader.parse(new InputSource(is));

                // Get all of the statements defined in the file by statement ID
                final Map<String, SqlStatementInfo> queries = xmlHandler.getStatements();

                // Iterate over each of the statements found....
                for (final String key : queries.keySet())
                {
                    logger.debug("Found statement: {}", key);
                    // Found a match; return it
                    if (statementID.equalsIgnoreCase(key))
                    {
                        final SqlStatementInfo stmt = queries.get(key);
                        logger.trace("Statement found: {}", stmt.statement);
                        return new StringReader(stmt.statement);
                    }
                }
            }
        }
        catch (javax.xml.parsers.ParserConfigurationException | org.xml.sax.SAXException e)
        {
            throw new IOException("Error parsing statement: " + statementID + " in XML template file: " + templateFile, e);
        }

        logger.warn("Unable to find statement: {} in file: {}", statementID, templateFile);
        return null;
    }

    @Override
    public void closeTemplateSource(Object templateSource)
    throws IOException
    {
        // Do nothing
    }

}
