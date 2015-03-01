package org.jhsheets.jdbi.freemarker;

import org.skife.jdbi.v2.sqlobject.SqlStatementCustomizer;
import org.skife.jdbi.v2.sqlobject.SqlStatementCustomizerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Take information about the class annotated with {@link org.jhsheets.jdbi.freemarker.FreemarkerTemplate}, and construct a
 * {@link org.jhsheets.jdbi.freemarker.FreemarkerSqlCustomizer} which will be used to find the Freemarker template to apply.
 */
public class FreemarkerLocatorFactory
implements SqlStatementCustomizerFactory
{
    /**
     * @param sqlObjectType
     * @return The path to the passed-in class, with '/' instead of '.'
     */
    private String getDefaultTemplatePath(final Class sqlObjectType)
    {
        return sqlObjectType.getName().replace(".", "/") + ".xml";
    }

    @Override
    public SqlStatementCustomizer createForMethod(final Annotation annotation, final Class sqlObjectType, final Method method)
    {
        final FreemarkerTemplate instance = (FreemarkerTemplate)annotation;

        // See if we were passed-in the templateLoc file location.
        // Default to a file with the same name as as the annotated class with an XML file extension
        final String templateLocation   = instance.templateLoc().isEmpty()
                                        ? getDefaultTemplatePath(sqlObjectType)
                                        : instance.templateLoc();

        // See if we were passed-in the name of the statement in the templateLoc file to use.
        final String statementName      = instance.statementID().isEmpty()
                                        ? null
                                        : instance.statementID();

        return new FreemarkerSqlCustomizer(templateLocation, statementName);
    }

    @Override
    public SqlStatementCustomizer createForType(final Annotation annotation, final Class sqlObjectType)
    {
        final FreemarkerTemplate instance = (FreemarkerTemplate)annotation;

        // See if we were passed-in the templateLoc file location.
        // Default to a file with the same name as as the annotated class with an XML file extension
        final String templateLocation   = instance.templateLoc().isEmpty()
                                        ? sqlObjectType.getName() + ".xml"
                                        : instance.templateLoc();

        // Ignore the statementID if we're annotated on a class; it's only valid on methods
        final String statementName      = null;

        return new FreemarkerSqlCustomizer(templateLocation, statementName);
    }

    @Override
    public SqlStatementCustomizer createForParameter(final Annotation annotation, final Class sqlObjectType, final Method method, final Object arg)
    {
        throw new UnsupportedOperationException("Not defined on parameter");
    }
}