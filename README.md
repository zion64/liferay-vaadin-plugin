liferay-vaadin-plugin
=====================

Usage
-----
(Usage instructions TBD)

Building
--------

### Requirements

* JDK 1.6 or later
* Maven 2.0.9 or later

### Environment set up

If you want to use tasks of Maven Liferay Plugin (already configured in the
 project), you need to specify some local configuration properties such
 as location of the Liferay installation. The recommended way is to specify
 properties in a Maven Profile defined in ~/.m2/settings.xml.

An example of specifying liferay properties in <profiles> section of ~/.m2/settings.xml:

	<profile>
		<id>liferay61</id>
		<properties>
			<liferay.home>/Users/jdoe/opt/liferay/liferay-portal-6.1</liferay.home>
			<liferay.auto.deploy.dir>${liferay.home}/deploy</liferay.auto.deploy.dir>
		</properties>
	</profile>

The profile can be activated by various ways:
1. By using settings.xml/<activeProfiles>
2. By defining rule in <profile>/<activation>
3. From command line option -P

Here's an example of specifying the <activeProfiles> in settings.xml:

	<activeProfiles>
		<activeProfile>liferay61</activeProfile>
	</activeProfiles>

### Building the war

Simple invoke the appropriate Maven life cycle: 'compile' will build sources,
 'package' will build the war (includes widgetset compilation) and 'install'
 will install the package to the local m2 repository (cache). In other words,
 to build the project you should type the following command:

	$ mvn install

To deploy the war to a Liferay instance, type the following command: 

	$ mvn liferay:deploy

If something goes wrong, try cleaning the project first:

	$ mvn clean
	

