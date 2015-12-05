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
	/** 
	 * Implicitly looks for a statement with an ID that matches the function name 
	 * in an implicity defined XML template location with the same package/name as our interface
	 */
	@SqlQuery 
    	@FreemarkerTemplate()
	public int getRecords(@Bind("idList") List<Integer> idList);

	/** 
	 * Implicitly looks for a statement with an ID that matches the function name
	 * in the explicitly defined XML template location 'com/test/MyDao.xml' 
	 */
	@SqlUpdate 
    	@FreemarkerTemplate(templateLoc="com/test/MyDao.xml")
	public void insertRecord(@Bind("id") int id, @Bind("name") String name);

	/**
	 * Explicity looks for a statement with ID 'updateRecord',
	 * in the explicitly defined XML template location 'com/test/MyDao.xml'
	 */
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
Queries are stored in XML template files, and not in a single-template format that Freemarker natively uses. The XML format is very minimal, as outlined above. There is a root <queries> element, with child elements of <select>, <insert> and <update>. Each of the child elements must define an 'id' attributes with a name unique to that XML file.

Templates are cached using Freemarker's SoftCacheStorage.  While this means templates may be repeatedly parsed, it will ensure you will not have memory issues with cached queries.

Also of note is that templates are loaded using Thread.currentThread().getContextClassLoader().getResource().

##Requirements
* Java 1.7
* [Freemarker](http://freemarker.org/)
* [JDBI](http://jdbi.org/)

##Download
To download via maven, add the following to your settings.xml or pom.xml
```xml
<repositories>
    <repository>
        <id>jdbi-freemarker-mvn-repo</id>
        <name>jdbi-freemarker maven repo</name>
        <url>https://raw.github.com/jhsheets/jdbi-freemarker/mvn-repo/</url>
        <layout>default</layout>
    </repository>
</repositories>
```
And add the dependency to your pom.xml file
```xml
<dependency>
    <groupId>org.jhsheets</groupId>
    <artifactId>jdbi-freemarker</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

You can also download the JAR directly from github:
https://github.com/jhsheets/jdbi-freemarker/tree/mvn-repo/org/jhsheets/jdbi-freemarker
