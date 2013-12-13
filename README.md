Liferay Control Panel Plugin for Vaadin
=======================================

Usage
-----
By using Vaadin Control Panel you can easily handle Vaadin and Vaadin add-ons in a Liferay portal. The included portlet takes care of downloading new versions of Vaadin, building the widgetset for you and placing it in the correct location. All you have to do is download the add-ons and start using them. 

### Installation 
* Drop the included WAR in the Liferay deploy directory
* Changing the used version of Vaadin: 
	* Open the portlet and click Change version
	* Select the desired Vaadin release 
	* The portlet will download the selected version of Vaadin and the required version of GWT 
* After changing the Vaadin version, you should also recompile the widgetset and redeploy all portlets using Vaadin to get them to use the new version. 

### Adding add-ons
* Download the add-on(s) from Vaadin Directory 
* Place the add-on jar file(s) in the WEB-INF/lib for Liferay's root 
* If an add-on depends on additional jars, ensure they are found in WEB-INF/lib aswell 
 
### Rebuilding the widgetset
* Open the portlet and select the add-ons you want to include. 
* Select any additional depencies in the portlet 
* Click compile and wait for the process to finish 

After a successful compilation, a backup of the old widgetset is created and the new widgetset is immediately available for use. You just need to refresh your Liferay page in the browser.

Building
--------

### Requirements

* JDK 1.6 or later
* Maven 2.0.9 or later

### Environment set up

This project uses some open source libraries that are not available in the
 default Maven central (repo1.maven.org). All linked artifacts are available in
 repository http://oss.arcusys.com/mvn/content/groups/public. That repository
 is specified in <repositories> element of this project, but because our parent
 pom is also located in that repository, we need to fetch it first by
 invoking a special 'bootstrap' pom:
 
	mvn -f etc/bootstrap/pom.xml dependency:resolve
	
If you want to use tasks of Maven Liferay Plugin (already configured in the
 project), you need to specify some local configuration properties such
 as location of the Liferay installation. The recommended way is to specify
 properties in a Maven Profile defined in ~/.m2/settings.xml.

An example of specifying liferay properties in ~/.m2/settings.xml:

	<settings>
		<profiles>
			<profile>
				<id>liferay61</id>
				<properties>
					<liferay.home>${user.home}/opt/liferay/liferay-portal-6.1</liferay.home>
					<liferay.auto.deploy.dir>${liferay.home}/deploy</liferay.auto.deploy.dir>
				</properties>
			</profile>
		</profiles>
		
		<!-- ... -->
		<activeProfiles>
			<activeProfile>liferay61</activeProfile>
		</activeProfiles>
	</settings>
	
The profile can be activated by various ways:
1. By using settings.xml/<activeProfiles> (see the example above)
2. By defining rule in <profile>/<activation>
3. By using the command line option -P

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
	

