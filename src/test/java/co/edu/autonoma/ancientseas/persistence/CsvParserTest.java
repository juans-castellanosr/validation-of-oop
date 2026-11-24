package co.edu.autonoma.ancientseas.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class CsvParserTest {

    @Test
    void lineNumbersFollowThePhysicalLinesEvenAcrossQuotedNewlinesAndBlankLines() {
        List<CsvRecord> records = CsvParser.parse("a,b\n\n\"x\ny\",z\r\nlast,row", ',');

        assertEquals(3, records.size());
        assertEquals(1, records.get(0).line());
        assertEquals(3, records.get(1).line());
        assertEquals(List.of("x\ny", "z"), records.get(1).fields());
        assertEquals(5, records.get(2).line());
    }

    @Test
    void loneCarriageReturnsEndRowsAndEmptyFieldsSurvive() {
        List<CsvRecord> records = CsvParser.parse("a,,c\rd,e,\"\"", ',');

        assertEquals(List.of("a", "", "c"), records.get(0).fields());
        assertEquals(List.of("d", "e", ""), records.get(1).fields());
    }

    @Test
    void unterminatedQuoteIsRejectedWithItsLine() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> CsvParser.parse("a\nb,\"open\nmore", ','));

        assertEquals(true, e.getMessage().contains("línea 2"));
    }
}
