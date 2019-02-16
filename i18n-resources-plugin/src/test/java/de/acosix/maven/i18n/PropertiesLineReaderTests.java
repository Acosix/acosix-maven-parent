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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * @author Axel Faust
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class PropertiesLineReaderTests
{

    @Test
    public void verifyUsingTestFile() throws IOException
    {
        final InputStream is = this.getClass().getClassLoader().getResourceAsStream("test-file.properties");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)))
        {
            final PropertiesLineReader lineReader = new PropertiesLineReader(br);

            final String firstLogicalLine = lineReader.readLine();
            Assert.assertEquals("firstKey=firstValue", firstLogicalLine);

            final String secondLogicalLine = lineReader.readLine();
            Assert.assertEquals("secondKey=Value to be continued", secondLogicalLine);

            final String thirdLogicalLine = lineReader.readLine();
            Assert.assertEquals("thirdKey=Value with #comment line", thirdLogicalLine);

            final String fourthLogicalLine = lineReader.readLine();
            Assert.assertEquals("fourthKey=Multiline value over three lines", fourthLogicalLine);

            final String fifthLogicalLine = lineReader.readLine();
            Assert.assertEquals("fifthKey=Multiline value with empty lines in between", fifthLogicalLine);

            final String endOfLines = lineReader.readLine();
            Assert.assertNull(endOfLines);
        }
    }
}
