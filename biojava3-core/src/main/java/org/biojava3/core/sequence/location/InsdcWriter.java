/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biojava3.core.sequence.location;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.EnumSet;
import java.util.List;
import org.biojava3.core.sequence.DataSource;
import org.biojava3.core.sequence.Strand;
import org.biojava3.core.sequence.location.template.Location;

/**
 * INSDC location which emits locations in Genbank form (where each part
 * of a join is individually complemented) or ENA (where a global complement
 * is applied at the highest available level).
 * 
 * The code cannot create indecisive locations (where complement switches 
 * inside the location).
 *
 * @author ayates
 */
public class InsdcWriter {

    private static final EnumSet<DataSource> INSDC_SOURCES =
            EnumSet.of(DataSource.ENA, DataSource.GENBANK, DataSource.DDBJ);

    private final DataSource dataSource;

    public InsdcWriter() {
        this(DataSource.ENA);
    }

    public InsdcWriter(DataSource dataSource) {
        if(! INSDC_SOURCES.contains(dataSource)) {
            throw new RuntimeException(dataSource+" is not INSDC");
        }
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public String writeString(Location location) {
        StringWriter writer = new StringWriter();
        try {
            write(location, writer);
        }
        catch (IOException e) {
            throw new RuntimeException("Cannot write Location", e);
        }
        return writer.toString();
    }

    public void write(Location location, Writer writer) throws IOException {
        write(location, writer, false);
    }

    protected void write(Location location, Writer writer, boolean alreadyComplement) throws IOException {
        int openBracket = 0;
        if(location.getStrand() == Strand.NEGATIVE) {
            if( (!location.isComplex() && !alreadyComplement) ||
                (DataSource.ENA == getDataSource() && location.isComplex()) ) {
                writer.append("complement(");
                alreadyComplement = true;
                openBracket++;
            }
        }
        if(location.isComplex()) {
            writer.append(join(location));
            writer.append('(');
            openBracket++;
            List<Location> locs = location.getSubLocations();
            int size = locs.size();
            for(int i =0; i < size; i++) {
                Location l = locs.get(i);
                write(l, writer, alreadyComplement);
                if(i != (size-1))
                    writer.append(',');
            }
            writer.append(')');
            openBracket--;
            alreadyComplement = false;
        }
        else {
            Integer start = location.getStart().getPosition();
            Integer end = location.getEnd().getPosition();
            if(location.getStart().isUnknown())
                writer.append('<');
            writer.append(start.toString());
            if(!start.equals(end)) {
                if(location.isBetweenCompounds()) {
                    writer.append('^');
                }
                else {
                    writer.append("..");
                }
                if(location.getEnd().isUnknown())
                    writer.append('>');
                writer.append(end.toString());
            }
        }
        for(int i =0; i<openBracket; i++) {
            writer.append(')');
        }
    }

    protected String join(Location l) {
        Class<? extends Location> clazz = l.getClass();
        if(InsdcLocations.OrderLocation.class.isAssignableFrom(clazz))
            return "order";
        else if (InsdcLocations.OneOfLocation.class.isAssignableFrom(clazz))
            return "one-of";
        else if (InsdcLocations.GroupLocation.class.isAssignableFrom(clazz))
            return "group";
        else
            return "join";
    }
}
