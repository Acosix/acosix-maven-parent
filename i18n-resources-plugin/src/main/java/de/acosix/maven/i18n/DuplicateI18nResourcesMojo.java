/*
 * Copyright 2019 Acosix GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.acosix.maven.i18n;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

/**
 * This mojo duplicates existing resource bundles of a reference locale to specific target locales in order to fill in any missing bundle
 * coverage for those locales and to provide basic support for locales without having to fall back to the default locale bundles.
 *
 * @author Axel Faust
 */
@Mojo(name = "duplicateI18nResources", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class DuplicateI18nResourcesMojo extends AbstractMojo
{

    private static final String PROPERTIES_EXTENSION = ".properties";

    protected static final List<String> DEFAULT_INCLUDES = Collections.unmodifiableList(Arrays.asList("**/*.properties"));

    /**
     * Specifies that execution of this mojo should be skipped.
     */
    @Parameter(defaultValue = "${i18n.skip.duplicate}")
    protected boolean skip;

    /**
     * Specifies the source directory used for looking up resources bundles to duplicate as well as checking if a specific resource bundle
     * for a target locale actually already exists as an explicit source file and can be excluded from being created as a copy.
     *
     * @parameter
     */
    @Parameter(defaultValue = "${basedir}/src/main", required = true)
    protected File sourceDirectory;

    /**
     * Specifies the directory in which any duplicated i18n resource bundles are generated.
     */
    @Parameter(defaultValue = "${project.build.directory}/i18n-resources", required = true)
    protected File outputDirectory;

    /**
     * Specifies inclusion patterns to select which resource files should be processed.
     *
     * @parameter
     */
    @Parameter
    protected List<String> includes;

    /**
     * Specifies exclusion patterns to select which resource files should not be processed.
     *
     * @parameter
     */
    @Parameter
    protected List<String> excludes;

    /**
     * Specifies the locale to use when creating a copy in a specific target locale that does not already have an explicit resource file.
     *
     * @parameter
     */
    @Parameter
    protected String sourceLocale;

    /**
     * Specifies all the locales for which resource bundles should be created as copies (unless explicit files for individual bundles
     * already exist).
     *
     * @parameter
     */
    @Parameter(required = true)
    protected List<String> copyForLocales;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if (!this.skip)
        {
            final Collection<String> propertyFileNames = this.getPropertyFileNamesToProcess();
            if (!propertyFileNames.isEmpty())
            {
                for (final String propertyFileName : propertyFileNames)
                {
                    if (propertyFileName.endsWith(PROPERTIES_EXTENSION))
                    {
                        this.checkResourceBundleAndDuplicateIfNecessary(propertyFileName);
                    }
                }
            }
            else
            {
                this.getLog().info("Found no resource bundles to process");
            }
        }
        else
        {
            this.getLog().info("Skipping resource bundle duplication");
        }
    }

    protected void checkResourceBundleAndDuplicateIfNecessary(final String propertyFileName) throws MojoExecutionException
    {
        final String fileName = propertyFileName.indexOf('/') != -1 ? propertyFileName.substring(propertyFileName.lastIndexOf('/') + 1)
                : propertyFileName;
        final String relativePath = propertyFileName.indexOf('/') != -1
                ? propertyFileName.substring(0, propertyFileName.lastIndexOf('/') + 1)
                : "";
        final String fileBaseName = fileName.substring(0, fileName.length() - PROPERTIES_EXTENSION.length());

        final StringBuilder localeBuilder = new StringBuilder();
        String resourceName = fileBaseName;
        while (resourceName.lastIndexOf('_') == resourceName.length() - 3 && localeBuilder.length() < 8)
        {
            final String fragment = resourceName.substring(resourceName.lastIndexOf('_') + 1);
            resourceName = resourceName.substring(0, resourceName.lastIndexOf('_'));
            if (localeBuilder.length() != 0)
            {
                localeBuilder.insert(0, '_');
            }
            localeBuilder.insert(0, fragment);
        }

        if ((this.sourceLocale == null && localeBuilder.length() == 0)
                || (this.sourceLocale != null && this.sourceLocale.equals(localeBuilder.toString())))
        {
            for (final String targetLocale : this.copyForLocales)
            {
                final String targetResourceName = targetLocale.trim().length() > 0
                        ? MessageFormat.format("{0}_{1}", resourceName, targetLocale)
                        : resourceName;
                final String targetFileName = targetResourceName + PROPERTIES_EXTENSION;
                final String targetPropertyFileName = relativePath + targetFileName;

                // do not duplicate if there is actually an explicit source file
                final File sourceCandidate = new File(this.sourceDirectory, targetPropertyFileName);
                if (!sourceCandidate.exists())
                {
                    final File target = new File(this.outputDirectory, targetPropertyFileName);
                    target.getParentFile().mkdirs();
                    try
                    {
                        FileUtils.copyFile(new File(this.sourceDirectory, propertyFileName), target);
                    }
                    catch (final IOException ioEx)
                    {
                        throw new MojoExecutionException("Error copying resources", ioEx);
                    }
                }
            }
        }
    }

    protected Collection<String> getPropertyFileNamesToProcess()
    {
        final List<String> propertyFileNames = new ArrayList<>();
        if (this.sourceDirectory.exists())
        {
            final DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(this.sourceDirectory);

            if (this.includes != null)
            {
                scanner.setIncludes(this.includes.toArray(new String[0]));
            }
            else
            {
                scanner.setIncludes(DEFAULT_INCLUDES.toArray(new String[0]));
            }

            if (this.excludes != null)
            {
                scanner.setExcludes(this.excludes.toArray(new String[0]));
            }

            scanner.addDefaultExcludes();
            scanner.scan();

            for (final String includedFile : scanner.getIncludedFiles())
            {
                propertyFileNames.add(includedFile);
            }
        }

        return propertyFileNames;
    }
}
