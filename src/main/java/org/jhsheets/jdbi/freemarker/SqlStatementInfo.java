package org.jhsheets.jdbi.freemarker;

/**
 * A statement defined in an XML template
 */
public class SqlStatementInfo
{
    /** Possible statement types that can be defined in our XML Template */
    public enum StatementType { select, insert, update };

    public final String id;
    public final StatementType statementType;
    public final String statement;

    public SqlStatementInfo(final String id, final StatementType statementType, final String statement)
    {
        this.id = id;
        this.statementType = statementType;
        this.statement = statement;
    }
}
