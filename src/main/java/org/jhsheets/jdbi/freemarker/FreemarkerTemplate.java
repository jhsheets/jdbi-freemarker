package org.jhsheets.jdbi.freemarker;

import org.skife.jdbi.v2.sqlobject.SqlStatementCustomizingAnnotation;

import java.lang.annotation.*;

/**
 * <h2>Description:</h2>
 * This annotation signifies that a Freemarker templateLoc stores the SQL to be used, somewhat similar to JDBI's built-in
 * {@link org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator} annotation.
 * <br/><br/>
 * By default this class will attempt to locate an XML file in the same package as invoking class with the same class
 * name. It expects to find a <b>&lt;select&gt;</b>|<b>&lt;update&gt;</b>|<b>&lt;insert&gt;</b> element with an
 * <b>id</b> attribute with the same name as the methods. You mush also annotate your methods with a corresponding
 * {@link org.skife.jdbi.v2.sqlobject.SqlQuery}|{@link org.skife.jdbi.v2.sqlobject.SqlUpdate} annotation.
 * You may apply this annotation to an entire class or to individual methods.  To pass parameters into your Freemarker
 * template, use the familiar {@link org.skife.jdbi.v2.sqlobject.Bind} annotation on your parameters.
 * <br/><br/>
 * If you want to name your file something different - or you want your file to exist in a different package - you may
 * pass in the location and name using the <b>templateLoc</b> parameter.  You must pass in the full path, using forward-
 * slashes between path folders.  Do not include a leading slash.
 * <br/><br/>
 * If you want to use a different statement ID than your method name, you can specify it using the <b>statementID</b>
 * parameter.  Note: this only works when you annotate methods, and will be ignored if you use it on a class annotation.
 * <br/><br/>
 * <h2>Example Class:</h2>
 * <pre>
 * {@code
 *
 * package com.test
 * public interface MyDao {
 *     // Basic use
 *     @_@SqlQuery @FreemarkerTemplate()
 *     public int getRecords(@Bind("idList") List<Integer> idList);
 *
 *     // Explicitly define the path to template file
 *     @_@SqlUpdate @FreemarkerTemplate(templateLoc="com/test/MyDao.xml")
 *     public void insertRecord(@Bind("id") int id, @Bind("name") String name);
 *
 *     // My method and parameter names don't match the xml file, but my statementID an @Bind parameters do
 *     @_@SqlUpdate @FreemarkerTemplate(templateLoc="com/test/MyDao.xml", statementID="updateRecord")
 *     public void misnamedFunc(@Bind("id") int someRecordID, @Bind("name") String someRecordName);
 * } }
 * </pre>
 * <h2>Example Template:</h2>
 * com.test.MyDao.xml
 * <pre>
 * {@code
 * <queries>
 *
 *     <!-- Use freemarker to dynamically generate a query based on the number of items in the list -->
 *     <select id="getRecords">
 *     <![CDATA[
 *        <#assign doUnion = false>
 *        <#list idList as id>
 *
 *        <#if doUnion == true>
 *        UNION ALL
 *        </#if>
 *        SELECT name
 *        FROM record
 *        WHERE id = ${id}
 *        <#assign doUnion = true>
 *        </#list>
 *     ]]>
 *     </select>
 *
 *     <!-- Normal jdbi query with parameters -->
 *     <insert id="insertRecord>
 *         INSERT INTO record (id, name) VALUES :id, :name
 *     </insert>
 *
 *     <!-- mix jdbi parameters with freemarker text replacement -->
 *     <update id="updateRecord>
 *         UPDATE record SET name = '${name}' WHERE id = :id
 *     </update>
 *
 * </queries>
 * }
 * </pre>
 * <h2>Limitations:</h2>
 * <ul>
 * <li>this class doesn't use a normal Freemarker templateLoc.  It uses an XML format which contains Freemarker templates.
 * This was done to demarcate multiple templates within the same file.</li>
 * <li>It also isn't possible to reference/include other Freemarker templates.</li>
 * </ul>
 */
@SqlStatementCustomizingAnnotation(FreemarkerLocatorFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface FreemarkerTemplate
{
    String templateLoc() default "";
    String statementID() default "";
}
