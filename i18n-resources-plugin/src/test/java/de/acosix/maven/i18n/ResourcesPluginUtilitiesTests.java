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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Axel Faust
 */
public class ResourcesPluginUtilitiesTests
{

    @Test
    public void extractBaseResourceNameAndLocaleFromFileName()
    {
        String fileBaseName;
        final StringBuilder resourceNameBuilder = new StringBuilder();
        final StringBuilder localeBuilder = new StringBuilder();
        String resourceName;
        String locale;

        fileBaseName = "a";
        ResourcesPluginUtilities.extractBaseResourceNameAndLocaleFromFileName(fileBaseName, resourceNameBuilder, localeBuilder);
        resourceName = resourceNameBuilder.toString();
        locale = localeBuilder.toString();

        Assert.assertEquals("a", resourceName);
        Assert.assertEquals("", locale);

        resourceNameBuilder.delete(0, resourceNameBuilder.length());
        localeBuilder.delete(0, localeBuilder.length());

        fileBaseName = "a_en";
        ResourcesPluginUtilities.extractBaseResourceNameAndLocaleFromFileName(fileBaseName, resourceNameBuilder, localeBuilder);
        resourceName = resourceNameBuilder.toString();
        locale = localeBuilder.toString();

        Assert.assertEquals("a", resourceName);
        Assert.assertEquals("en", locale);

        resourceNameBuilder.delete(0, resourceNameBuilder.length());
        localeBuilder.delete(0, localeBuilder.length());

        fileBaseName = "a_en_gb";
        ResourcesPluginUtilities.extractBaseResourceNameAndLocaleFromFileName(fileBaseName, resourceNameBuilder, localeBuilder);
        resourceName = resourceNameBuilder.toString();
        locale = localeBuilder.toString();

        Assert.assertEquals("a", resourceName);
        Assert.assertEquals("en_gb", locale);

        resourceNameBuilder.delete(0, resourceNameBuilder.length());
        localeBuilder.delete(0, localeBuilder.length());

        fileBaseName = "a_en_gb_sct";
        ResourcesPluginUtilities.extractBaseResourceNameAndLocaleFromFileName(fileBaseName, resourceNameBuilder, localeBuilder);
        resourceName = resourceNameBuilder.toString();
        locale = localeBuilder.toString();

        Assert.assertEquals("a", resourceName);
        Assert.assertEquals("en_gb_sct", locale);

        resourceNameBuilder.delete(0, resourceNameBuilder.length());
        localeBuilder.delete(0, localeBuilder.length());

        fileBaseName = "a_bc_en_gb_sct";
        ResourcesPluginUtilities.extractBaseResourceNameAndLocaleFromFileName(fileBaseName, resourceNameBuilder, localeBuilder);
        resourceName = resourceNameBuilder.toString();
        locale = localeBuilder.toString();

        Assert.assertEquals("a_bc", resourceName);
        Assert.assertEquals("en_gb_sct", locale);

        resourceNameBuilder.delete(0, resourceNameBuilder.length());
        localeBuilder.delete(0, localeBuilder.length());

        fileBaseName = "a_bc_eN_Gb_sCt";
        ResourcesPluginUtilities.extractBaseResourceNameAndLocaleFromFileName(fileBaseName, resourceNameBuilder, localeBuilder);
        resourceName = resourceNameBuilder.toString();
        locale = localeBuilder.toString();

        Assert.assertEquals("a_bc", resourceName);
        Assert.assertEquals("eN_Gb_sCt", locale);

        resourceNameBuilder.delete(0, resourceNameBuilder.length());
        localeBuilder.delete(0, localeBuilder.length());

        fileBaseName = "ab";
        ResourcesPluginUtilities.extractBaseResourceNameAndLocaleFromFileName(fileBaseName, resourceNameBuilder, localeBuilder);
        resourceName = resourceNameBuilder.toString();
        locale = localeBuilder.toString();

        Assert.assertEquals("ab", resourceName);
        Assert.assertEquals("", locale);

        resourceNameBuilder.delete(0, resourceNameBuilder.length());
        localeBuilder.delete(0, localeBuilder.length());

        fileBaseName = "ab_en";
        ResourcesPluginUtilities.extractBaseResourceNameAndLocaleFromFileName(fileBaseName, resourceNameBuilder, localeBuilder);
        resourceName = resourceNameBuilder.toString();
        locale = localeBuilder.toString();

        Assert.assertEquals("ab", resourceName);
        Assert.assertEquals("en", locale);

        resourceNameBuilder.delete(0, resourceNameBuilder.length());
        localeBuilder.delete(0, localeBuilder.length());

        fileBaseName = "ab_en_gb";
        ResourcesPluginUtilities.extractBaseResourceNameAndLocaleFromFileName(fileBaseName, resourceNameBuilder, localeBuilder);
        resourceName = resourceNameBuilder.toString();
        locale = localeBuilder.toString();

        Assert.assertEquals("ab", resourceName);
        Assert.assertEquals("en_gb", locale);

        resourceNameBuilder.delete(0, resourceNameBuilder.length());
        localeBuilder.delete(0, localeBuilder.length());

        fileBaseName = "ab_en_gb_sct";
        ResourcesPluginUtilities.extractBaseResourceNameAndLocaleFromFileName(fileBaseName, resourceNameBuilder, localeBuilder);
        resourceName = resourceNameBuilder.toString();
        locale = localeBuilder.toString();

        Assert.assertEquals("ab", resourceName);
        Assert.assertEquals("en_gb_sct", locale);

        resourceNameBuilder.delete(0, resourceNameBuilder.length());
        localeBuilder.delete(0, localeBuilder.length());

        fileBaseName = "ab_cd_en_gb_sct";
        ResourcesPluginUtilities.extractBaseResourceNameAndLocaleFromFileName(fileBaseName, resourceNameBuilder, localeBuilder);
        resourceName = resourceNameBuilder.toString();
        locale = localeBuilder.toString();

        Assert.assertEquals("ab_cd", resourceName);
        Assert.assertEquals("en_gb_sct", locale);

        resourceNameBuilder.delete(0, resourceNameBuilder.length());
        localeBuilder.delete(0, localeBuilder.length());

        fileBaseName = "ab_cd_eN_Gb_sCt";
        ResourcesPluginUtilities.extractBaseResourceNameAndLocaleFromFileName(fileBaseName, resourceNameBuilder, localeBuilder);
        resourceName = resourceNameBuilder.toString();
        locale = localeBuilder.toString();

        Assert.assertEquals("ab_cd", resourceName);
        Assert.assertEquals("eN_Gb_sCt", locale);

        resourceNameBuilder.delete(0, resourceNameBuilder.length());
        localeBuilder.delete(0, localeBuilder.length());
    }
}
