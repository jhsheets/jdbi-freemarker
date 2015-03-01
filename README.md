# jdbi-freemarker

##Overview
This library provides the ability to use Freemarker templates defined in an external file with JDBI.

Unlike JDBI's built-in @UseStringTemplate3StatementLocator annotation - which only allows for basic string replacemnt - Freemarker allows you to dynamically generate your SQL queries using Freemarker markup.

##Use
Simply put the @FreemarkerTemplate attribute on your JDBI interface or methods, and make sure your methods also have the @SqlUpdate or @SqlQuery attribute defined.

The examples below should outline the usage fairly well.

See the Javadoc comments for @FreemarkerTemplate for more details.

##Example
DAO Interface: com.test.MyDao.java
```java
package com.test
public interface MyDao {
	/** Basic use */
	@SqlQuery 
    @FreemarkerTemplate()
	public int getRecords(@Bind("idList") List<Integer> idList);

	/** Explicitly define the path to template file */
	@SqlUpdate 
    @FreemarkerTemplate(templateLoc="com/test/MyDao.xml")
	public void insertRecord(@Bind("id") int id, @Bind("name") String name);

	/** My method and parameter names don't match the xml file, but my statementID an @Bind parameters do */
	@SqlUpdate 
    @FreemarkerTemplate(templateLoc="com/db/Misc.xml", statementID="updateRecord")
	public void misnamedFunc(@Bind("id") int someRecordID, @Bind("name") String someRecordName);
}
```

Template file: com.test.MyDao.xml
```xml
<queries>
	
    <!-- Use freemarker to dynamically generate a query based on the number of items in the list -->
    <select id="getRecords">
	<![CDATA[
		<#assign doUnion = false>
        
		<#list idList as id>
		<#if doUnion == true>
		UNION ALL
		</#if>
		SELECT name FROM record WHERE id = ${id}
		<#assign doUnion = true>
		</#list>
	]]>
	</select>

	<!-- Normal jdbi query with parameters -->
	<insert id="insertRecord">
        INSERT INTO record (id, name) VALUES :id, :name
	</insert>

</queries>
```

Template file: com.db.Misc.xml
```xml
<queries>
	<!-- mix jdbi parameters with freemarker text replacement -->
	<update id="updateRecord">
        UPDATE record SET name = '${name}' WHERE id = :id
	</update>
</queries>
```	

##Notes
Templates are cached using Freemarker's SoftCacheStorage.  While this means templates may be repeatedly parsed, it will ensure you will not have memory issues with cached queries.

Templates are also stored in a custom XML format, and not in a traditional, single-template format that Freemarker traditionally uses.

Also of note is that templates are loaded using Thread.currentThread().getContextClassLoader().getResource().

##Requirements
* Java 1.7
* [Freemarker](http://freemarker.org/)
* [JDBI](http://jdbi.org/)
