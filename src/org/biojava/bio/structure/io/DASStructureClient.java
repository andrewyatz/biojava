
/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on 05.05.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.bio.structure.io;

// the biojava-structure stuff
import org.biojava.bio.structure.*;
import org.biojava.bio.structure.io.*;
       
// das communication part
import org.biojava.bio.program.das.dasstructure.* ;

import java.io.*;

/**
 * A DAS client that connects to a DAS structure service and
 * returns a Biojava structure class.
 */
public class DASStructureClient implements StructureIO { 

    String pdb_code                 ;

    StructureImpl structure      ;
    
    public DASStructureClient() {
	pdb_code = null ;
    }

    /* the interfaced procedures: */
    
    /** set the PDB code of a structure
     */
    public void setId(String id) {pdb_code = id ;   }

    /** get the PDB code of a structure
     */
    public String getId() {return pdb_code ;  }
    

    /** set the pdb_code (@see setId)
     * connect to a DAS-structure service and retreive data
     * return a Structure class       
     */
    public Structure getStructure(String pdb_code) 
	throws IOException
    {
	setId(pdb_code);
	return getStructure();
    }


    /** 
     * if pdb code is set (@see setId) 
     * connect to a DAS-structure service and retreive data
     * return a Structure class
     */
 
    public Structure getStructure()
	throws IOException 
    {
	
	if (pdb_code == null) {
	    throw new IOException ("no pdb code found - call setId() first!");
	}

	/* now connect to DAS server */

	DASStructureCall dasstructure = new DASStructureCall();

	Structure structure = dasstructure.getStructure(pdb_code);

	return structure;
    }


   
}
