package org.jhsheets.jdbi.freemarker;

import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.SqlStatementCustomizer;

import java.sql.SQLException;

/**
 * Associate a {@link FreemarkerTemplateLocator} with the statement
 */
public class FreemarkerSqlCustomizer
implements SqlStatementCustomizer
{
    private final String templateLocation;
    private final String statementName;

    public FreemarkerSqlCustomizer(final String templateLocation, final String statementName)
    {
        this.templateLocation = templateLocation;
        this.statementName = statementName;
    }

    @Override
    public void apply(final SQLStatement q) throws SQLException
    {
        q.setStatementLocator( new FreemarkerTemplateLocator(templateLocation, statementName) );
    }
}