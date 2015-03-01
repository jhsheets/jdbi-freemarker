package org.jhsheets.jdbi.freemarker;

import freemarker.cache.SoftCacheStorage;
import freemarker.template.*;
import org.skife.jdbi.v2.Binding;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.StatementLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;

/**
 * Attempt to find the XML Template file specified, and the given statement ID within the XML file.
 * <br/><br/>
 * Once found, we'll extract the contents for the statement, and transform it into a Freemarker
 * {@link freemarker.template.Template}.
 * <br/><br/>
 * Templates are cached using a {@link freemarker.cache.SoftCacheStorage}.
 */
public class FreemarkerTemplateLocator
implements StatementLocator
{
    private static Logger logger = LoggerFactory.getLogger(FreemarkerTemplateLocator.class);

    private static Configuration cfg;
    static
    {
        // Load a static freemarker configuration
        cfg = new Configuration();
        cfg.setTemplateLoader(new SqlTemplateLoader());
        cfg.setCacheStorage(new SoftCacheStorage());
        cfg.setLocalizedLookup(false); // must keep this off, or freemarker will modify my source file names...
    }

    private final String templateFile;
    private final String explicitStatementID;


    /**
     * @param templateFile The path to the XML template file.  Must be a valid path that can be resolved using the
     *                     class loader of the annotated class so it can be loaded using {@link java.lang.ClassLoader#getResource(String)}
     * @param statementID The ID of the statement in the XML Template file to apply as a Freemarker template. If null,
     *                    we'll use the method-name for the annotated class as the statement ID
     */
    public FreemarkerTemplateLocator(final String templateFile, final String statementID)
    {
        if (templateFile == null || templateFile.isEmpty()) throw new IllegalArgumentException("You cannot have a blank XML template file location");

        this.templateFile = templateFile;
        this.explicitStatementID = statementID;
    }

    @Override
    public String locate(final String methodName, final StatementContext ctx)
    throws Exception
    {
        // If there's no explicitly defined statement ID, then use the method name
        final String statementID = explicitStatementID == null ? methodName : explicitStatementID;

        // Lookup the template.  It'll either be in the Freemarker cache, or it'll have to look it up from file
        final String fullStatementID = SqlTemplateLoader.buildTemplateName(templateFile, statementID);
        final Template template = cfg.getTemplate( fullStatementID );

        // Apply the template, and get the result as a string
        final FreemarkerTemplateHashModel bindingWrapper = new FreemarkerTemplateHashModel( ctx.getBinding() );
        final StringWriter stringWriter = new StringWriter();
        template.process(bindingWrapper, stringWriter);

        // Return the parsed template
        final String processedTemplate = stringWriter.toString();
        return processedTemplate;
    }

    /**
     * Wrapper around JDBI {@link org.skife.jdbi.v2.Binding} so Freemarker can access the passed-in variables.
     * Freemarker requires all objects used in a template to be a subclass of {@link freemarker.template.TemplateModel}
     * so it knows how to handle it.
     */
    private static final class FreemarkerTemplateHashModel
    implements TemplateHashModel
    {
        private final Binding bindings;

        public FreemarkerTemplateHashModel(final Binding bindings)
        {
            this.bindings = bindings;
        }

        @Override
        public TemplateModel get(final String key)
        throws TemplateModelException
        {
            // TODO: request a JDBI change that exposes a geValue() method on Argument so I don't need to reflect....

            Object o = null;
            final Argument a = bindings == null ? null : bindings.forName(key);
            // No clue why JDBI decided to make bound variables so inaccessible....
            final Class c = a.getClass();

            // This should be an ArgumentObject (which is package-private), which has a field called value (private)
            try {
                final Field valueField = c.getDeclaredField("value");
                valueField.setAccessible(true);
                o = valueField.get(a);
            } catch (NoSuchFieldException e) {
                logger.error("Unable to find 'value' field", e); // could happen if JDBI changes the class structure
            } catch (IllegalAccessException e) {
                logger.error("Unable to access 'value' field.", e);
            }

            // Use Freemarkers object wrapper to figure out the correct TemplateModel to wrap one of the items in our bindings
            return new DefaultObjectWrapper().wrap( o );
        }

        @Override
        public boolean isEmpty()
        throws TemplateModelException
        {
            return bindings == null;
        }
    };

}
