js-overlay-maven-plugin
=======================

Maven plugin to generate GWT Javascript Objects from a jar file


#summary
How to configure and use the js-overlay plugin

##Introduction
If you have a large or small object model you want to create javascript overlay types in GWT this plugin will do that.

##Details

Add the following to your pom.xml

	<build>
	...
	 <plugin>
	  <groupId>com.moesol</groupId>
	  <artifactId>js-overlay-maven-plugin</artifactId>
	  <version>1.0</version>
	  <dependencies>
	    <dependency>
	        <groupId>[object-model-groupid-id]</groupId>
	        <artifactId>[object-model-artifact-id]</artifactId>
	        <version>[object-model-version]</version>
	        <scope>compile</scope>
	      </dependency>
	    </dependencies>
	    <configuration>
	       <fromPackage>current.model.package</fromPackage>
	       <toPackage>new.model.package</toPackage>
	       <sourcePackage>current.model.package</sourcePackage>
	    </configuration>
	    <executions>
	      <execution>
	         <goals>
	         <goal>overlay</goal>
	         </goals>
	      </execution>
	    </executions>
	  </plugin>
	...
	</build>

fromPackage and toPackage are optional.  They are used if you want to change the package.  For example, you have a com.mymodel package and want to end up with com.myapp.shared.mymodel package you would have the following configuration

	<configuration>
	  <fromPackage>com.mymodel</fromPackage>
	  <toPackage>com.myapp.shared.mymodel</toPackage>
	  <sourcePackage>com.mymodel</sourcePackage>
	</configuration>

All class files found in the package com.mymodel and below will have javascript overlay types created and all java bean properties will be generated for both get and set methods.
