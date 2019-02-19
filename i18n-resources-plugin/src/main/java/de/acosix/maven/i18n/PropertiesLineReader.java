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
import java.io.IOException;
import java.util.Properties;

/**
 * This class is a utility for reading properties files line by line on a logical basis, similar to how {@link Properties} does internally.
 *
 * @author Axel Faust
 */
public class PropertiesLineReader
{

    private final BufferedReader reader;

    private final boolean endReached = false;

    public PropertiesLineReader(final BufferedReader reader)
    {
        this.reader = reader;
    }

    public String readLine() throws IOException
    {
        String result;

        if (!this.endReached)
        {
            final StringBuilder logicalLineBuilder = new StringBuilder();

            String lineFromFile;

            boolean isNewLine = true;
            boolean skipWhitespace = true;
            boolean skipEmptyLine = true;

            while ((lineFromFile = this.reader.readLine()) != null)
            {
                if (skipWhitespace && !lineFromFile.isEmpty())
                {
                    int lineStartIdx = 0;
                    char c = lineFromFile.charAt(lineStartIdx);
                    while ((c == ' ' || c == '\t' || c == '\f') && lineStartIdx + 1 < lineFromFile.length())
                    {
                        c = lineFromFile.charAt(++lineStartIdx);
                    }

                    if (c == ' ' || c == '\t' || c == '\f')
                    {
                        lineFromFile = "";
                    }
                    else if (lineStartIdx > 0)
                    {
                        lineFromFile = lineFromFile.substring(lineStartIdx);
                    }
                }

                if (lineFromFile.isEmpty())
                {
                    if (skipEmptyLine)
                    {
                        continue;
                    }
                    break;
                }

                if (isNewLine)
                {
                    isNewLine = false;
                    final char firstChar = lineFromFile.charAt(0);
                    if (firstChar == '#' || firstChar == '!')
                    {
                        // skip
                        isNewLine = true;
                        continue;
                    }
                }

                final char lastChar = lineFromFile.charAt(lineFromFile.length() - 1);
                if (lastChar == '\\')
                {
                    logicalLineBuilder.append(lineFromFile.substring(0, lineFromFile.length() - 1));
                    skipWhitespace = true;
                }
                else
                {
                    logicalLineBuilder.append(lineFromFile);
                    break;
                }
                skipEmptyLine = false;
            }

            if (logicalLineBuilder.length() > 0)
            {
                result = logicalLineBuilder.toString();
            }
            else
            {
                result = null;
            }
        }
        else
        {
            result = null;
        }

        return result;
    }
}
