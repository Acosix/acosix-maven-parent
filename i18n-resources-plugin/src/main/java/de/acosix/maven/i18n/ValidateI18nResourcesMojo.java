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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * This mojo validates i18n/l10n resource bundles (Java Properties files) in the current project against the following checks:
 * <ul>
 * <li>presence of resource bundles for a configured list of mandatory locales</li>
 * <li>existence of a localised variant of all resource bundles for a specific locale if at least one resource bundle for that locale
 * exists</li>
 * <li>consistency of message keys between all localised variants of individual resource bundles</li>
 * <li>correct use of unicode escape sequences for non-ASCII characters</li>
 * </ul>
 *
 * This mojo performs its checks primarily by reading the bundles of the project into runtime memory, just like a real Java application
 * would.
 *
 * @author Axel Faust
 */
@Mojo(name = "validateI18nResources", defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true)
public class ValidateI18nResourcesMojo extends AbstractMojo
{

    private static final String PROPERTIES_EXTENSION = ".properties";

    protected static final List<String> DEFAULT_INCLUDES = Collections.unmodifiableList(Arrays.asList("**/*.properties"));

    @Parameter(defaultValue = "${basedir}/src/main", readonly = true)
    protected File defaultValidationDirectory;

    /**
     * Specifies that execution of this mojo should be skipped.
     */
    @Parameter(defaultValue = "${i18n.skip.validate}")
    protected boolean skip;

    /**
     * Specifies the directories used for looking up the effective set of resources bundles for validation. Since individual resource
     * bundles may have been generated as part of other build steps, this option allows to configure multiple directories in order to
     * support a single aggregated validation run. If not configured, {@code ${basedir}/src/main} will be used as the base directory.
     *
     * @parameter
     */
    @Parameter
    protected List<File> validationDirectories;

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
     * Specifies all locales for which resource bundles must exist. If specified and at least one resource bundles does not contain
     * localised properties files for all the required locales, this Mojo will consider this an error and not allow the build to proceed
     * after the validation has completed. The generic default locale is always considered tobe required.
     *
     * @parameter
     */
    @Parameter
    protected List<String> requiredLocales;

    /**
     * Specifies the validation should report if a locale - for which at least one (other) resource bundle has been defined - is missing a
     * specific message bundle defined for other locales.
     *
     * @parameter
     */
    @Parameter(defaultValue = "true")
    protected boolean reportMissingLocaleBundles;

    /**
     * Specifies if a missing locale resource bundle should be considered an error and not allow the build to proceed after the validation
     * has completed.
     */
    @Parameter(defaultValue = "false")
    protected boolean failOnMissingLocaleBundles;

    /**
     * Specifies if inconsistent message keys should be reported. Message keys are considered inconsistent if message bundles of different
     * locales do not all share the same set of keys.
     *
     * @parameter
     */
    @Parameter(defaultValue = "true")
    protected boolean reportInconsistentMessageKeys;

    /**
     * Specifies if the presence of any resource bundles with inconsistent message keys should be considered an error and not allow the
     * build to proceed after the validation has completed.
     */
    @Parameter(defaultValue = "false")
    protected boolean failOnInconsistentMessageKeys;

    /**
     * Specifies if the presence of encoding issues (non-ASCII characters / incorrect unicode escape sequences) in resource bundles should
     * be reported.
     *
     * @parameter
     */
    @Parameter(defaultValue = "true")
    protected boolean reportEncodingIssues;

    /**
     * Specifies if the presence of encoding issues (non-ASCII characters / incorrect unicode escape sequences) in resource bundles should
     * be considered an error and not allow the build to
     * proceed after the validation has completed.
     */
    @Parameter(defaultValue = "true")
    protected boolean failOnEncodingIssues;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if (!this.skip)
        {
            final Map<Locale, Map<String, File>> filesByBasePathByLocale = this.scanForPropertiesFiles();
            final Collection<Locale> allLocales = new HashSet<>(filesByBasePathByLocale.keySet());
            final Collection<String> allResourceBundleBasePaths = new TreeSet<>();

            for (final Entry<Locale, Map<String, File>> filesByBasePath : filesByBasePathByLocale.entrySet())
            {
                allResourceBundleBasePaths.addAll(filesByBasePath.getValue().keySet());
            }

            if (!allResourceBundleBasePaths.isEmpty())
            {
                this.validateResourceBundles(filesByBasePathByLocale, allLocales, allResourceBundleBasePaths);
            }
            else
            {
                this.getLog().info("Found no resource bundles to validate");
            }
        }
        else
        {
            this.getLog().info("Skipping resource bundle validations");
        }
    }

    protected Map<Locale, Map<String, File>> scanForPropertiesFiles()
    {
        final Map<Locale, Map<String, File>> filesByBasePathByLocale = new HashMap<>(128);
        final DirectoryScanner scanner = new DirectoryScanner();

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

        if (this.validationDirectories == null)
        {
            this.validationDirectories = new ArrayList<>();
        }
        if (this.validationDirectories.isEmpty())
        {
            this.validationDirectories.add(this.defaultValidationDirectory);
        }

        for (final File directory : this.validationDirectories)
        {
            if (directory.exists())
            {
                scanner.setBasedir(directory);
                scanner.scan();

                for (final String includedFile : scanner.getIncludedFiles())
                {
                    if (includedFile.endsWith(PROPERTIES_EXTENSION))
                    {
                        final String fileName = includedFile.indexOf('/') != -1 ? includedFile.substring(includedFile.lastIndexOf('/') + 1)
                                : includedFile;
                        final String relativeBasePath = includedFile.indexOf('/') != -1
                                ? includedFile.substring(0, includedFile.lastIndexOf('/') + 1)
                                : "";
                        final String fileBaseName = fileName.substring(0, fileName.length() - PROPERTIES_EXTENSION.length());

                        final StringBuilder localeBuilder = new StringBuilder(16);
                        String resourceName = fileBaseName;
                        while (resourceName.lastIndexOf('_') == resourceName.length() - 3 && localeBuilder.length() < 8)
                        {
                            final String fragment = resourceName.substring(resourceName.lastIndexOf('_') + 1);
                            resourceName = resourceName.substring(0, resourceName.lastIndexOf('_'));
                            if (localeBuilder.length() != 0)
                            {
                                localeBuilder.insert(0, '-');
                            }
                            localeBuilder.insert(0, fragment);
                        }
                        final String basePath = relativeBasePath + resourceName;

                        Locale locale;
                        if (localeBuilder.length() > 0)
                        {
                            locale = Locale.forLanguageTag(localeBuilder.toString());
                        }
                        else
                        {
                            locale = Locale.ROOT;
                        }

                        Map<String, File> filesByBasePath = filesByBasePathByLocale.get(locale);
                        if (filesByBasePath == null)
                        {
                            filesByBasePath = new HashMap<>();
                            filesByBasePathByLocale.put(locale, filesByBasePath);
                        }
                        filesByBasePath.put(basePath, new File(directory, includedFile));
                    }
                }
            }
        }

        return filesByBasePathByLocale;
    }

    protected void validateResourceBundles(final Map<Locale, Map<String, File>> filesByBasePathByLocale,
            final Collection<Locale> allLocales, final Collection<String> allResourceBundleBasePaths)
            throws MojoFailureException, MojoExecutionException
    {
        final List<Locale> requiredLocales = new ArrayList<>();
        requiredLocales.add(Locale.ROOT);
        if (this.requiredLocales != null)
        {
            for (final String locale : this.requiredLocales)
            {
                requiredLocales.add(Locale.forLanguageTag(locale));
            }
        }
        this.validateRequiredLocales(allResourceBundleBasePaths, requiredLocales, filesByBasePathByLocale);

        final AtomicInteger missingLocaleBundles = new AtomicInteger(0);
        final AtomicInteger inconsistentMessageKeys = new AtomicInteger(0);
        this.validateResourceBundles(allLocales, allResourceBundleBasePaths, filesByBasePathByLocale, missingLocaleBundles,
                inconsistentMessageKeys);

        if (this.failOnMissingLocaleBundles && missingLocaleBundles.get() > 0)
        {
            throw new MojoFailureException("Some resource bundles are only defined for a subset of locales");
        }
        else if (this.failOnInconsistentMessageKeys && inconsistentMessageKeys.get() > 0)
        {
            throw new MojoFailureException("Some message bundles contain an inconsistent set of message keys");
        }
    }

    protected void validateRequiredLocales(final Collection<String> allBasePaths, final List<Locale> localesToValidate,
            final Map<Locale, Map<String, File>> filesByBasePathAndLocale) throws MojoFailureException
    {
        boolean fail = false;

        for (final Locale locale : localesToValidate)
        {
            final String localeLabel = (locale.equals(Locale.ROOT) ? "the default locale" : locale.toString());

            final Map<String, File> localeFilesByBasePath = filesByBasePathAndLocale.get(locale);
            if (localeFilesByBasePath == null || localeFilesByBasePath.isEmpty())
            {
                fail = true;

                for (final String missingBasePath : allBasePaths)
                {
                    this.getLog().warn(missingBasePath + " is missing a localised bundle for " + localeLabel);
                }

                this.getLog().error(allBasePaths.size() + " resource bundle base paths have been found, but no resource bundles exist for "
                        + localeLabel);
            }
            else
            {
                final Collection<String> missingBasePaths = new TreeSet<>(allBasePaths);
                missingBasePaths.removeAll(localeFilesByBasePath.keySet());

                if (!missingBasePaths.isEmpty())
                {
                    fail = true;

                    for (final String missingBasePath : missingBasePaths)
                    {
                        this.getLog().warn(missingBasePath + " is missing a localised bundle for " + localeLabel);
                    }

                    this.getLog().error(missingBasePaths.size() + " resource bundle(s) missing for " + localeLabel);
                }
            }
        }

        if (fail)
        {
            throw new MojoFailureException("Some of the required locales are missing specific resource bundles");
        }
    }

    protected void validateResourceBundles(final Collection<Locale> allLocales, final Collection<String> allBasePaths,
            final Map<Locale, Map<String, File>> filesByBasePathAndLocale, final AtomicInteger missingLocaleBundles,
            final AtomicInteger inconsistentMessageKeys) throws MojoExecutionException, MojoFailureException
    {
        final Map<Locale, String> localeLabels = new HashMap<>();
        for (final Locale locale : allLocales)
        {
            final String localeLabel = (locale.equals(Locale.ROOT) ? "the default locale" : locale.toString());
            localeLabels.put(locale, localeLabel);
        }

        if (this.reportEncodingIssues || this.failOnEncodingIssues)
        {
            final AtomicInteger encodingIssues = new AtomicInteger(0);

            this.validateResourceBundleEncoding(allLocales, filesByBasePathAndLocale, encodingIssues, localeLabels);
            if (this.failOnEncodingIssues && encodingIssues.get() > 0)
            {
                throw new MojoFailureException("Some resource bundles contain non-ASCII characters or incorrect unicode escape sequences");
            }
        }

        for (final String basePath : allBasePaths)
        {

            final Map<Locale, ResourceBundle> bundles = this.loadBundlesForBasePath(basePath, allLocales, filesByBasePathAndLocale,
                    missingLocaleBundles, localeLabels);

            if (this.reportInconsistentMessageKeys || this.failOnInconsistentMessageKeys)
            {
                this.validateResourceBundleConsistency(basePath, bundles, inconsistentMessageKeys, localeLabels);
            }
        }
    }

    protected void validateResourceBundleEncoding(final Collection<Locale> allLocales,
            final Map<Locale, Map<String, File>> filesByBasePathAndLocale, final AtomicInteger encodingIssues,
            final Map<Locale, String> localeLabels) throws MojoExecutionException
    {
        final Pattern nonAsciiCharacterPattern = Pattern.compile("[^\\x00-\\x7F]");
        final Pattern invalidUnicodeEscapeSequencePattern = Pattern.compile("\\\\u(?![0-9a-fA-F]{4}).{4}");

        for (final Locale locale : allLocales)
        {
            final Map<String, File> filesByBasePath = filesByBasePathAndLocale.get(locale);
            for (final Entry<String, File> filesByBasePathEntry : filesByBasePath.entrySet())
            {
                final String basePath = filesByBasePathEntry.getKey();
                final File file = filesByBasePathEntry.getValue();

                // need to custom load the file contents as regular Properties / ResourceBundle already decode escape sequences
                try (BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1)))
                {
                    final PropertiesLineReader lineReader = new PropertiesLineReader(bf);

                    int logicalLineNo = 0;
                    String logicalLine;
                    while ((logicalLine = lineReader.readLine()) != null)
                    {
                        logicalLineNo++;

                        final Matcher nonAsciiMatcher = nonAsciiCharacterPattern.matcher(logicalLine);
                        while (nonAsciiMatcher.find())
                        {
                            encodingIssues.incrementAndGet();

                            final String character = nonAsciiMatcher.group();
                            final String logMessage = basePath + " for " + localeLabels.get(locale)
                                    + " contains non-ASCII character " + character + " in (logical) line no " + logicalLineNo;
                            if (this.failOnEncodingIssues)
                            {
                                this.getLog().error(logMessage);
                            }
                            else if (this.reportEncodingIssues)
                            {
                                this.getLog().warn(logMessage);
                            }
                        }

                        final Matcher invalidUnicodeEscapeSequenceMatcher = invalidUnicodeEscapeSequencePattern.matcher(logicalLine);
                        while (invalidUnicodeEscapeSequenceMatcher.find())
                        {
                            encodingIssues.incrementAndGet();

                            final String logMessage = basePath + " for " + localeLabels.get(locale)
                                    + " contains ivalid unicode escape sequence " + invalidUnicodeEscapeSequenceMatcher.group()
                                    + " in (logical) line no " + logicalLineNo;
                            if (this.failOnEncodingIssues)
                            {
                                this.getLog().error(logMessage);
                            }
                            else if (this.reportEncodingIssues)
                            {
                                this.getLog().warn(logMessage);
                            }
                        }
                    }
                }
                catch (final IOException ioex)
                {
                    throw new MojoExecutionException("Failed to load file " + file, ioex);
                }
            }
        }
    }

    protected void validateResourceBundleConsistency(final String basePath, final Map<Locale, ResourceBundle> bundles,
            final AtomicInteger inconsistentMessageKeys, final Map<Locale, String> localeLabels)
    {
        final Collection<String> referenceKeys = new TreeSet<>(bundles.get(Locale.ROOT).keySet());
        final Collection<String> allKeys = new TreeSet<>();
        for (final ResourceBundle bundle : bundles.values())
        {
            allKeys.addAll(bundle.keySet());
        }

        this.getLog().debug(basePath + " defines " + allKeys.size() + " message key(s) across all its variants");

        final Map<String, List<Locale>> localesByMissingKeys = new HashMap<>();
        final Map<String, List<Locale>> localesBySuperflousKeys = new HashMap<>();

        for (final Entry<Locale, ResourceBundle> bundleEntry : bundles.entrySet())
        {
            final Locale locale = bundleEntry.getKey();
            if (!locale.equals(Locale.ROOT))
            {
                final ResourceBundle resourceBundle = bundleEntry.getValue();

                final Collection<String> missingKeys = new TreeSet<>(referenceKeys);
                missingKeys.removeAll(resourceBundle.keySet());

                for (final String missingKey : missingKeys)
                {
                    List<Locale> locales = localesByMissingKeys.get(missingKey);
                    if (locales == null)
                    {
                        locales = new ArrayList<>();
                        localesByMissingKeys.put(missingKey, locales);
                    }
                    locales.add(locale);
                }

                final Collection<String> superflousKeys = new TreeSet<>(resourceBundle.keySet());
                superflousKeys.removeAll(referenceKeys);

                for (final String superflousKey : superflousKeys)
                {
                    List<Locale> locales = localesBySuperflousKeys.get(superflousKey);
                    if (locales == null)
                    {
                        locales = new ArrayList<>();
                        localesBySuperflousKeys.put(superflousKey, locales);
                    }
                    locales.add(locale);
                }
            }
        }

        for (final Entry<String, List<Locale>> localesByMissingKey : localesByMissingKeys.entrySet())
        {
            this.logResourceBundleInconsistency(basePath, localesByMissingKey.getKey(), true, localesByMissingKey.getValue());
            inconsistentMessageKeys.incrementAndGet();
        }

        for (final Entry<String, List<Locale>> localesBySuperflousKey : localesBySuperflousKeys.entrySet())
        {
            this.logResourceBundleInconsistency(basePath, localesBySuperflousKey.getKey(), false, localesBySuperflousKey.getValue());
            inconsistentMessageKeys.incrementAndGet();
        }
    }

    protected Map<Locale, ResourceBundle> loadBundlesForBasePath(final String basePath, final Collection<Locale> allLocales,
            final Map<Locale, Map<String, File>> filesByBasePathAndLocale, final AtomicInteger missingLocaleBundles,
            final Map<Locale, String> localeLabels) throws MojoExecutionException
    {
        final Map<Locale, ResourceBundle> bundles = new HashMap<>();

        for (final Locale locale : allLocales)
        {
            final Map<String, File> localeFilesByBasePath = filesByBasePathAndLocale.get(locale);
            if (localeFilesByBasePath != null && localeFilesByBasePath.containsKey(basePath))
            {
                final File file = localeFilesByBasePath.get(basePath);
                try (InputStream is = new FileInputStream(file))
                {
                    final ResourceBundle bundle = new PropertyResourceBundle(is);
                    bundles.put(locale, bundle);
                }
                catch (final IOException ioex)
                {
                    throw new MojoExecutionException(
                            "Failed to load resource bundle " + basePath + " for " + localeLabels.get(locale) + " from " + file, ioex);
                }
                catch (final IllegalArgumentException iaex)
                {
                    if (iaex.getMessage().contains("Malformed \\uxxxx encoding"))
                    {
                        // we have separate report/fail options for encoding issues
                        this.getLog().debug("Resource bundle file " + file
                                + " contains a malformed unicode escape sequence and cannot be loaded for validation");
                    }
                    else
                    {
                        throw iaex;
                    }
                }
            }
            else
            {
                missingLocaleBundles.incrementAndGet();

                final String message = basePath + " resource bundle is missing for " + localeLabels.get(locale);
                if (this.failOnMissingLocaleBundles)
                {
                    this.getLog().error(message);
                }
                else if (this.reportMissingLocaleBundles)
                {
                    this.getLog().warn(message);
                }
            }
        }

        return bundles;
    }

    protected void logResourceBundleInconsistency(final String basePath, final String messageKey, final boolean missing,
            final List<Locale> locales)
    {
        final StringBuilder messageBuilder = new StringBuilder(256);
        messageBuilder.append(basePath).append(" key ").append(messageKey)
                .append(missing ? " defined in root bundle but not found in bundles for "
                        : " not defined in root bundle but found in bundles for ");
        for (final Locale locale : locales)
        {
            messageBuilder.append(locale).append(", ");
        }
        messageBuilder.delete(messageBuilder.length() - 2, messageBuilder.length());

        if (this.failOnInconsistentMessageKeys)
        {
            this.getLog().error(messageBuilder);
        }
        else if (this.reportInconsistentMessageKeys)
        {
            this.getLog().warn(messageBuilder);
        }
    }
}
