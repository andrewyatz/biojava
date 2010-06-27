package org.biojava3.core.sequence.location;

import org.biojava3.core.sequence.Strand;
import org.biojava3.core.sequence.location.template.Location;
import org.junit.Assert;
import org.junit.Test;

public class LocationParserTest {

    public static final InsdcParser PARSER = new InsdcParser();

    @Test
    public void basicLocations() {
        assertInsdcLoc("1", new SimpleLocation(1, 1, Strand.POSITIVE));
        assertInsdcLoc("1..10", new SimpleLocation(1, 10, Strand.POSITIVE));
    }

    @Test
    public void inbetweenBaseLocation() {
        assertInsdcLoc("1^2", new SimpleLocation(
                new SimplePoint(1),
                new SimplePoint(2),
                Strand.POSITIVE, false, true));
    }

    @Test
    public void complementLocation() {
        assertInsdcLoc("complement(1..10)", new SimpleLocation(1, 10, Strand.NEGATIVE));
    }

    @Test
    public void complexLocation() {

        assertInsdcLoc("join(1..2,7..8)", new SimpleLocation(
                new SimplePoint(1), new SimplePoint(8), Strand.POSITIVE,
                new SimpleLocation(1, 2, Strand.POSITIVE),
                new SimpleLocation(7, 8, Strand.POSITIVE)));

        assertInsdcLoc("complement(join(1..2,7..8))", new SimpleLocation(
                new SimplePoint(1), new SimplePoint(8), Strand.NEGATIVE,
                new SimpleLocation(1, 2, Strand.NEGATIVE),
                new SimpleLocation(7, 8, Strand.NEGATIVE)));

        //Reverse relationship
        assertInsdcLoc("join(complement(1..2),complement(7..8))", new SimpleLocation(
                new SimplePoint(1), new SimplePoint(8), Strand.NEGATIVE,
                new SimpleLocation(1, 2, Strand.NEGATIVE),
                new SimpleLocation(7, 8, Strand.NEGATIVE)));

        //Complex sub relations
        assertInsdcLoc("join(1..2,join(4..5,complement(6..8)))", new SimpleLocation(
                new SimplePoint(1), new SimplePoint(8), Strand.UNDEFINED,
                new SimpleLocation(1, 2, Strand.POSITIVE),
                new SimpleLocation(4, 8, Strand.UNDEFINED,
                    new SimpleLocation(4, 5, Strand.POSITIVE),
                    new SimpleLocation(6, 8, Strand.NEGATIVE)
                )));

        assertInsdcLoc("join(5..10,1..3)", new SimpleLocation(
                new SimplePoint(5), new SimplePoint(13), Strand.POSITIVE,
                true, //Circular genome
                new SimpleLocation(5, 10, Strand.POSITIVE),
                new SimpleLocation(1, 3, Strand.POSITIVE)));
    }

    @Test
    public void switchingStrandLocation() {
        assertInsdcLoc("complement(join(123..456,complement(789..1000)))",
                new SimpleLocation(
                    new SimplePoint(123), new SimplePoint(1000), Strand.UNDEFINED,
                    new SimpleLocation(123, 456, Strand.NEGATIVE),
                    new SimpleLocation(789, 1000, Strand.POSITIVE)));
    }

    @Test
    public void orderLocation() {
        assertInsdcLoc("order(1..2,7..8)", new InsdcLocations.OrderLocation(
                new SimplePoint(1), new SimplePoint(8), Strand.POSITIVE,
                new SimpleLocation(1, 2, Strand.POSITIVE),
                new SimpleLocation(7, 8, Strand.POSITIVE)));
    }

    @Test
    public void testRealTransplicedExample() {
		String locStr = "join(11024..11409,complement(239890..240081),complement(241499..241580),complement(251354..251412),complement(315036..315294))";
        assertInsdcLoc(locStr, new SimpleLocation(new SimplePoint(11024), new SimplePoint(315294), Strand.UNDEFINED,
                new SimpleLocation(11024, 11409),
                new SimpleLocation(239890, 240081, Strand.NEGATIVE),
                new SimpleLocation(241499, 241580, Strand.NEGATIVE),
                new SimpleLocation(251354, 251412, Strand.NEGATIVE),
                new SimpleLocation(315036, 315294, Strand.NEGATIVE)));
    }

    @Test
    public void testRealComplementExample() {
        String locStr = "complement(join(3371316..3371723))";
        assertInsdcLoc(locStr, new SimpleLocation(new SimplePoint(3371316), new SimplePoint(3371723),
                Strand.NEGATIVE));
    }

    @Test
    public void testListParseSimpleNoJoinFuzzy() {
        String locStr = "<123..>456";
        assertInsdcLoc(locStr, new SimpleLocation(
                new SimplePoint(123, true, false),
                new SimplePoint(456, true, false),
                Strand.POSITIVE));
    }

    @Test
    public void testListParseSimpleComplementNoJoinFuzzy() {
		String locStr = "complement(<123..>456)";
        assertInsdcLoc(locStr, new SimpleLocation(
                new SimplePoint(123, true, false),
                new SimplePoint(456, true, false),
                Strand.NEGATIVE));
    }

    @Test
    public void testCircularLocation() {
		String locStr = "join(1737907..1738505,1..61)";
        assertInsdcLoc(locStr, new SimpleLocation(
                new SimplePoint(1737907, false, false),
                new SimplePoint(1738566, false, false),
                Strand.POSITIVE,
                true,
                new SimpleLocation(1737907, 1738505, Strand.POSITIVE),
                new SimpleLocation(1, 61, Strand.POSITIVE)
                ));
    }

    public void assertInsdcLoc(String stringLoc, Location expected) {
        Location actual = PARSER.parse(stringLoc);
        Assert.assertEquals("Asserting locations are the same", expected, actual);
    }
}
