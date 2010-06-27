package org.biojava3.core.sequence.location;

import junit.framework.Assert;
import org.biojava3.core.sequence.DataSource;
import org.biojava3.core.sequence.location.template.Location;
import org.junit.Test;

public class LocationWriterTest {

    private InsdcWriter genbankWriter = new InsdcWriter(DataSource.GENBANK);
    private InsdcWriter writer = new InsdcWriter();
    private InsdcParser parser = new InsdcParser();

    @Test
    public void basic() {
        assertRoundtrip("1");
        assertRoundtrip("1..2");
        assertRoundtrip("1^2");
    }

    @Test
    public void complex() {
        assertRoundtrip("join(1..2,7..8)");
        assertRoundtrip("join(1..2,join(4..5,complement(6..8)))");
        assertGenbankRoundtrip("join(complement(1..2),complement(7..8))");
        assertRoundtrip("complement(join(1..2,7..8))");
        assertRoundtrip("join(11024..11409,complement(239890..240081),complement(241499..241580),complement(251354..251412),complement(315036..315294))");

        //Will never round trip
//        assertRoundtrip("complement(join(123..456,complement(789..1000)))");
    }

    @Test
    public void fuzzy() {
        assertRoundtrip("complement(<123..>456)");
    }

    @Test
    public void circular() {
        assertRoundtrip("join(1737907..1738505,1..61)");
    }

    @Test
    public void insdc() {
        assertRoundtrip("order(1..2,7..8)");
        assertRoundtrip("one-of(1..2,7..8)");
        assertRoundtrip("group(1..2,7..8)");
    }

    public void assertGenbankRoundtrip(String expected) {
        Location l = parser.parse(expected);
        String actual = genbankWriter.writeString(l);
        Assert.assertEquals("Checking genbank location "+expected+" roundtrips", expected, actual);
    }

    public void assertRoundtrip(String expected) {
        Location l = parser.parse(expected);
        String actual = writer.writeString(l);
        Assert.assertEquals("Checking ENA location "+expected+" roundtrips", expected, actual);
    }
}
