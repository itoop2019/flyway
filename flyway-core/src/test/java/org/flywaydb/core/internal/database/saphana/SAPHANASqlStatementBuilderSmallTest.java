/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.database.saphana;

import org.flywaydb.core.internal.database.Delimiter;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Test for SapHanaSqlStatementBuilder
 */
public class SAPHANASqlStatementBuilderSmallTest {
    /**
     * Class under test.
     */
    private SAPHANASqlStatementBuilder statementBuilder = new SAPHANASqlStatementBuilder(Delimiter.SEMICOLON);

    @Test
    public void unicodeString() {
        String sqlScriptSource = "SELECT 'Bria n' \"character string 1\", '100' \"character string 2\", N'abc' \"unicode string\" FROM DUMMY;\n";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }

    @Test
    public void binaryString() {
        String sqlScriptSource = "SELECT X'00abcd' \"binary string 1\", x'dcba00' \"binary string 2\" FROM DUMMY;";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }

    @Test
    public void binaryStringNot() {
        String sqlScriptSource = "SELECT '00abcd X' \"not a binary string 1\" FROM DUMMY;";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }

    @Test
    public void temporalString() {
        String sqlScriptSource = "SELECT date'2010-01-01' \"date\", time'11:00:00.001' \"time\", timestamp'2011-12-31 23:59:59' \"timestamp\" FROM DUMMY;";

        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        for (String line : lines) {
            statementBuilder.addLine(line);
        }

        assertTrue(statementBuilder.isTerminated());
    }

    @Test
    public void parseSimpleProcedure() {
        parseProcedureWithEmbeddedSemicola("org/flywaydb/core/internal/database/saphana/procedures/procedure.sql", 3);
    }

    @Test
    public void parseProcedureWithNestedBlocks() {
        parseProcedureWithEmbeddedSemicola("org/flywaydb/core/internal/database/saphana/procedures/procedureWithNestedBlocks.sql", 3);
    }
    @Test
    public void parseProcedureInComment() {
        parseProcedureWithEmbeddedSemicola("org/flywaydb/core/internal/database/saphana/procedures/procedureInComment.sql", 3);
    }
    @Test
    public void parseProcedureWithComments() {
        parseProcedureWithEmbeddedSemicola("org/flywaydb/core/internal/database/saphana/procedures/procedureWithComments.sql", 3);
    }
    @Test
    public void parseProcedureWithLinebreaks() {
        parseProcedureWithEmbeddedSemicola("org/flywaydb/core/internal/database/saphana/procedures/procedureWithLinebreaks.sql", 3);
    }

    private void parseProcedureWithEmbeddedSemicola(String resourceName, int numStatements) {
        final String sqlScriptSource =
                new ClassPathResource(resourceName, SAPHANASqlStatementBuilderSmallTest.class.getClassLoader())
                        .loadAsString("UTF-8");
        SAPHANASqlStatementBuilder statementBuilder = new SAPHANASqlStatementBuilder(Delimiter.SEMICOLON);
        String[] lines = StringUtils.tokenizeToStringArray(sqlScriptSource, "\n");
        final List<String> statements = new ArrayList<String>();
        for (String line : lines) {
            statementBuilder.addLine(line);
            if (statementBuilder.isTerminated()) {
                statements.add(statementBuilder.getSqlStatement().getSql());
                statementBuilder = new SAPHANASqlStatementBuilder(Delimiter.SEMICOLON);
            }
        }
        assertEquals("Number of recognized statements", numStatements, statements.size());
    }
}