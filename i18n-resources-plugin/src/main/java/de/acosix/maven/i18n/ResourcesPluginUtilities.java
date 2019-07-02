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

/**
 * This class bundles common utility operations relevant across MOJOs.
 *
 * @author Axel Faust
 */
public class ResourcesPluginUtilities
{

    private static final String VARIANT_CODE_PATTERN = "^[a-zA-Z]+$";

    private static final String TWO_LETTER_CODE_PATTERN = "^[a-zA-Z]{2}$";

    /**
     * Constructs a new instance of this class. {@code private} to make the class close to uninstantiable.
     */
    private ResourcesPluginUtilities()
    {
        // NO-OP
    }

    /**
     * Extracts the base resource name and locale suffix from a given file name, appending the result to the provided string builders. With
     * regards to locale suffixes in file names, this operation will only process at most 3 distinct 2-letter code suffix fragments.
     *
     * @param fileBaseName
     *            the provided base file name (without {@code .properties} extension)
     * @param resourceNameBuilder
     *            the builder to which to append the extracted base resource name
     * @param localeBuilder
     *            the builder to which to append the extracted locale suffix
     */
    protected static void extractBaseResourceNameAndLocaleFromFileName(final String fileBaseName, final StringBuilder resourceNameBuilder,
            final StringBuilder localeBuilder)
    {
        final String[] fragments = fileBaseName.split("_");
        final int count = fragments.length;

        int localeStartIdx = -1;
        if (count > 3 && fragments[count - 1].matches(VARIANT_CODE_PATTERN) && fragments[count - 2].matches(TWO_LETTER_CODE_PATTERN)
                && fragments[count - 3].matches(TWO_LETTER_CODE_PATTERN))
        {
            localeStartIdx = count - 3;
        }
        else if (count > 2 && fragments[count - 1].matches(TWO_LETTER_CODE_PATTERN)
                && fragments[count - 2].matches(TWO_LETTER_CODE_PATTERN))
        {
            localeStartIdx = count - 2;
        }
        else if (count > 1 && fragments[count - 1].matches(TWO_LETTER_CODE_PATTERN))
        {
            localeStartIdx = count - 1;
        }

        if (localeStartIdx != -1)
        {
            for (int idx = 0; idx < count; idx++)
            {
                if (idx == 0)
                {
                    resourceNameBuilder.append(fragments[idx]);
                }
                else if (idx < localeStartIdx)
                {
                    resourceNameBuilder.append('_').append(fragments[idx]);
                }
                else if (idx == localeStartIdx)
                {
                    localeBuilder.append(fragments[idx]);
                }
                else
                {
                    localeBuilder.append('_').append(fragments[idx]);
                }
            }
        }
        else
        {
            resourceNameBuilder.append(fileBaseName);
        }
    }
}
