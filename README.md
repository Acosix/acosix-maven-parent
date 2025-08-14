# About
This project aggregates any universally-useful Maven plugins, definitions or other utilities developed / used by Acosix GmbH for building any of our projects.

# Use in projects

Details concerning the usage of the Maven plugin usage can be found in their respective README files:

* [i18n-resources-plugin](./i18n-resources-plugin/README.md)
* [jshint-plugin](./jshint-plugin/README.md)

## Using SNAPSHOT builds

In order to use a pre-built SNAPSHOT artifact published to Maven Central, the central artifact repository needs to be added to the POM, global settings.xml or an artifact repository proxy server with snapshots enabled (default `central` repository only handles release versions). The following is the XML snippet for inclusion in a POM file.

```xml
<repositories>
    <repository>
        <id>central-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```