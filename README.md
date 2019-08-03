[![Build Status](https://travis-ci.org/Acosix/acosix-maven-parent.svg?branch=master)](https://travis-ci.org/Acosix/acosix-maven-parent)

# About
This project aggregates any universally-useful Maven plugins, definitions or other utilities developed / used by Acosix GmbH for building any of our projects.

# Use in projects

Details concerning the usage of the Maven plugin usage can be found in their respective README files:

* [i18n-resources-plugin](./i18n-resources-plugin/README.md)
* [jshint-plugin](./jshint-plugin/README.md)

## Using SNAPSHOT builds

In order to use a pre-built SNAPSHOT artifact published to the Open Source Sonatype Repository Hosting site, the artifact repository may need to be added to the POM, global settings.xml or an artifact repository proxy server. The following is the XML snippet for inclusion in a POM file.

```xml
<repositories>
    <repository>
        <id>ossrh</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```