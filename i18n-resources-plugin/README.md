# I18n Resources Maven Plugin


## Goals
* `i18n-resources:duplicateI18nResources`: duplicates internationalisation resource bundles, e.g. creating "en" locale-specific resource bundles based on the default, locale-free bundles 
* `i18n-resources:validateI18nResources`: validates the internationalisation resource bundles across one or more directories, potentially including generated resource bundles

## Configuration Options

### duplicateI18nResources

| Option          | Default Value                 | Explanation  |
| --------------- | :---------------------------: | ------------ |
| sourceDirectory | `${project.baseDir}/src/main` | The directory in which resource bundles will be processed |
| outputDirectory | `${project.build.directory}/i18n-resources` | The directory in which duplicated resource bundles will be saved using relative paths |
| includes | | The inclusion pattern to match resource bundles that should be processed (all `*.properties` are included by default)  |
| excludes | | The exclusion pattern to match resource bundles that should not be processed |
| sourceLocale | | The locale of the resource bundle to be duplicated into other locales (empty default value targets the default, locale-free resource bundles) |
| copyForLocales | | List of locales for which resource bundles should be duplicated from the bundle of the source locale if no specific bundle already exists |
| skip | `false` (or value of `-Di18n.skip.duplicate`) | The flag specifying the execution of this plugin should be skipped |

### validateI18nResources

| Option          | Default Value                 | Explanation  |
| --------------- | :---------------------------: | ------------ |
| defaultValidationDirectory | `${project.baseDir}/src/main` | The default directory in which resource bundles will be validated unless an explicit list of `validationDirectories` has been configured |
| validationDirectories | | The lit of directories in which resource bundles will be validated |
| includes | | The inclusion pattern to match resource bundles that should be processed (all `*.properties` are included by default)  |
| excludes | | The exclusion pattern to match resource bundles that should not be processed |
| requiredLocales | | The list of locales for which any resource bundle must always be provided as a locale-specific bundle |
| reportMissingLocaleBundles | `true` | Specifies whether the validation should report if a locale - for which at least one (other) resource bundle has been defined - is missing a locale-specific resource bundle |
| failOnMissingLocaleBundles | `false` | Specifies if missing locales should be considered an error and result in a build failure |
| reportInconsistentMessageKeys | `true` | Specifies whether the validation should report if message keys are inconsistent between the various locale-specific and locale-free variants of a resource bundle |
| failOnInconsistentMessageKeys | `false` | Specifies if inconsistent message keys should be considered an error and result in a build failure |
| reportEncodingIssues | `true` | Specifies whether the validation should report if message keys use unencoded non-ascii characters which may cause problems at runtime unless resource bundles are loaded with the same encoding as used in editing |
| failOnEncodingIssues | `true` | Specifies if the use of unencoded non-ascii characters should be considered an error and result in a build failure |
| skip | `false` (or value of `-Di18n.skip.validate`) | The flag specifying the execution of this plugin should be skipped |

## Example Configurations

```xml
<plugin>
     <groupId>de.acosix.maven</groupId>
     <artifactId>i18n-resources-plugin</artifactId>
     <version>1.0.4</version>
     <configuration>
         <sourceDirectory>${basedir}/src/main</sourceDirectory>
         <outputDirectory>${project.build.directory}/i18n-resources</outputDirectory>
         <includes>
             <include>messages/*.properties</include>
             <include>messages/**/*.properties</include>
             <include>webscripts/*.properties</include>
             <include>webscripts/**/*.properties</include>
             <include>site-webscripts/*.properties</include>
             <include>site-webscripts/**/*.properties</include>
             <include>templates/*.properties</include>
             <include>templates/**/*.properties</include>
             <include>webapp/*.properties</include>
             <include>webapp/**/*.properties</include>
         </includes>
         <copyForLocales>
             <copyForLocale>en</copyForLocale>
         </copyForLocales>
     </configuration>
     <executions>
         <execution>
             <goals>
                 <goal>duplicateI18nResources</goal>
             </goals>
         </execution>
     </executions>
</plugin>
```

```xml
<plugin>
     <groupId>de.acosix.maven</groupId>
     <artifactId>i18n-resources-plugin</artifactId>
     <version>1.0.4</version>
     <configuration>
         <validationDirectories>
             <validationDirectory>${basedir}/src/main<validationDirectory>
             <validationDirectory>${project.build.directory}/i18n-resources<validationDirectory>
         </validationDirectories>
         <requiredLocales>
             <requiredLocale>de</requiredLocale>
             <requiredLocale>en</requiredLocale>
         </requiredLocales>
         <failOnMissingLocaleBundles>true</failOnMissingLocaleBundles>
         <failOnInconsistentMessageKeys>true</failOnInconsistentMessageKeys>
     </configuration>
     <executions>
         <execution>
             <goals>
                 <goal>validateI18nResources</goal>
             </goals>
         </execution>
     </executions>
</plugin>
```