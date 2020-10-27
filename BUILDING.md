# Building

This project can be built with Maven. However, it requires an update site based on the Notes client with Designer (not Domino). Such an update site can be created using the [`generate-domino-update-site`](https://github.com/OpenNTF/generate-domino-update-site) project when pointed at a client installation.

Once the update site is created, you should reference it as a property named `designer-platform` in your Maven settings.xml file in file URL format, such as:

```xml
	<profiles>
		<profile>
			<id>main</id>
			<properties>
				<designer-platform>file:///Users/jesse/Java/IBM/Notes10</designer-platform>
			</properties>
		</profile>
	</profiles>
	<activeProfiles>
		<activeProfile>main</activeProfile>
	</activeProfiles>
````