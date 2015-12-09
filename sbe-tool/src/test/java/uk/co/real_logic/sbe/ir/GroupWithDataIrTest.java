/*
 * Copyright 2015 Real Logic Ltd.
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
package uk.co.real_logic.sbe.ir;

import org.junit.Test;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.ParserOptions;

import java.util.List;

import static java.lang.Integer.valueOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.co.real_logic.sbe.TestUtil.getLocalResource;
import static uk.co.real_logic.sbe.xml.XmlSchemaParser.parse;

public class GroupWithDataIrTest
{
    @Test
    public void shouldGenerateCorrectIrForSingleVarDataInRepeatingGroup()
        throws Exception
    {
        final MessageSchema schema = parse(getLocalResource("group-with-data-schema.xml"), ParserOptions.DEFAULT);
        final IrGenerator irg = new IrGenerator();
        final Ir ir = irg.generate(schema);
        final List<Token> tokens = ir.getMessage(1);

        /* 0=msg, 1=field, 2=enc, 3=fieldend, 4=group, 5=comp, 6=enc, 7=enc, 8=compend, ... */

        final Token groupToken = tokens.get(4);
        final Token dimensionsCompToken = tokens.get(5);
        final Token dimensionsBlEncToken = tokens.get(6);
        final Token varDataFieldToken = tokens.get(15);
        final Token lengthEncToken = tokens.get(17);
        final Token dataEncToken = tokens.get(18);

        /* assert on the group token */
        assertThat(groupToken.signal(), is(Signal.BEGIN_GROUP));
        assertThat(groupToken.name(), is("Entries"));
        assertThat(valueOf(groupToken.id()), is(valueOf(2)));

        /* assert on the comp token for dimensions */
        assertThat(dimensionsCompToken.signal(), is(Signal.BEGIN_COMPOSITE));
        assertThat(dimensionsCompToken.name(), is("groupSizeEncoding"));

        /* assert on the enc token for dimensions blockLength */
        assertThat(dimensionsBlEncToken.signal(), is(Signal.ENCODING));
        assertThat(dimensionsBlEncToken.name(), is("blockLength"));

        assertThat(varDataFieldToken.signal(), is(Signal.BEGIN_VAR_DATA));
        assertThat(varDataFieldToken.name(), is("varDataField"));
        assertThat(valueOf(varDataFieldToken.id()), is(valueOf(5)));

        assertThat(lengthEncToken.signal(), is(Signal.ENCODING));
        assertThat(lengthEncToken.encoding().primitiveType(), is(PrimitiveType.UINT8));

        assertThat(dataEncToken.signal(), is(Signal.ENCODING));
        assertThat(dataEncToken.encoding().primitiveType(), is(PrimitiveType.CHAR));
    }

    @Test
    public void shouldGenerateCorrectIrForMultipleVarDataInRepeatingGroup()
        throws Exception
    {
        final MessageSchema schema = parse(getLocalResource("group-with-data-schema.xml"), ParserOptions.DEFAULT);
        final IrGenerator irg = new IrGenerator();
        final Ir ir = irg.generate(schema);
        final List<Token> tokens = ir.getMessage(2);

        /* 0=msg, 1=field, 2=enc, 3=fieldend, 4=group, 5=comp, 6=enc, 7=enc, 8=compend, ... */

        final Token groupToken = tokens.get(4);
        final Token varDataField1Token = tokens.get(15);
        final Token varDataField2Token = tokens.get(21);

        /* assert on the group token */
        assertThat(groupToken.signal(), is(Signal.BEGIN_GROUP));
        assertThat(groupToken.name(), is("Entries"));
        assertThat(valueOf(groupToken.id()), is(valueOf(2)));

        assertThat(varDataField1Token.signal(), is(Signal.BEGIN_VAR_DATA));
        assertThat(varDataField1Token.name(), is("varDataField1"));
        assertThat(valueOf(varDataField1Token.id()), is(valueOf(5)));

        assertThat(varDataField2Token.signal(), is(Signal.BEGIN_VAR_DATA));
        assertThat(varDataField2Token.name(), is("varDataField2"));
        assertThat(valueOf(varDataField2Token.id()), is(valueOf(6)));
    }

    @Test
    public void shouldGenerateCorrectIrForVarDataInNestedRepeatingGroup()
        throws Exception
    {
        final MessageSchema schema = parse(getLocalResource("group-with-data-schema.xml"), ParserOptions.DEFAULT);
        final IrGenerator irg = new IrGenerator();
        final Ir ir = irg.generate(schema);
        final List<Token> tokens = ir.getMessage(3);

        /* 0=msg, 1=field, 2=enc, 3=fieldend, 4=group, 5=comp, 6=enc, 7=enc, 8=compend, ... */

        final Token groupToken = tokens.get(4);
        final Token nestedGroupToken = tokens.get(12);
        final Token varDataFieldNestedToken = tokens.get(20);
        final Token varDataFieldToken = tokens.get(27);

        assertThat(groupToken.signal(), is(Signal.BEGIN_GROUP));
        assertThat(groupToken.name(), is("Entries"));
        assertThat(valueOf(groupToken.id()), is(valueOf(2)));

        assertThat(nestedGroupToken.signal(), is(Signal.BEGIN_GROUP));
        assertThat(nestedGroupToken.name(), is("NestedEntries"));
        assertThat(valueOf(nestedGroupToken.id()), is(valueOf(4)));

        assertThat(varDataFieldNestedToken.signal(), is(Signal.BEGIN_VAR_DATA));
        assertThat(varDataFieldNestedToken.name(), is("varDataFieldNested"));
        assertThat(valueOf(varDataFieldNestedToken.id()), is(valueOf(6)));

        assertThat(varDataFieldToken.signal(), is(Signal.BEGIN_VAR_DATA));
        assertThat(varDataFieldToken.name(), is("varDataField"));
        assertThat(valueOf(varDataFieldToken.id()), is(valueOf(7)));
    }
}
