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
 */

package org.biojava.bio.seq.io;

import java.util.*;
import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.utils.*;

/**
 * class that makes ChunkedSymbolLists with the chunks
 * implemented as SymbolLists themselves.
 * <p>
 * The advantage is that those SymbolLists can be packed
 * implementations.
 *
 * <p>
 * You can build a SequenceBuilderFactory to create a packed chunked sequence from
 * an input file without making an intermediate symbol list with:-
 * <pre>
 * public class PackedChunkedListFactory implements SequenceBuilderFactory
 * {
 *   public SequenceBuilder makeSequenceBuilder()
 *   {
 *     return new SequenceBuilderBase() {
 *       private ChunkedSymbolListFactory chunker = new ChunkedSymbolListFactory(new PackedSymbolListFactory(true));
 *
 *       // deal with symbols
 *       public void addSymbols(Alphabet alpha, Symbol[] syms, int pos, int len)
 *         throws IllegalAlphabetException
 *       {
 *         chunker.addSymbols(alpha, syms, pos, len);
 *       }
 *
 *       // make the sequence
 *       public Sequence makeSequence()
 *       {
 *         try {
 *           // make the SymbolList
 *           SymbolList symbols = chunker.makeSymbolList();
 *           seq = new SimpleSequence(symbols, uri, name, annotation);
 *
 *           // call superclass method
 *           return super.makeSequence();
 *         }
 *         catch (IllegalAlphabetException iae) {
 *           throw new BioError("couldn't create symbol list");
 *         }
 *       }
 *     };
 *   }
 * }
 * </pre>
 *
 * <p>
 * Then reading in FASTA files can be done with something like:-
 * <p>
 * <pre>
 * SequenceIterator seqI = new StreamReader(br, new FastaFormat(), 
 *     DNATools.getDNA().getTokenization("token"), 
 *     new PackedChunkedListFactory() );
 * </pre>
 * <p>
 * Blend to suit taste.
 * <p>
 * Alternatively, you can input Symbols to the factory with addSymbols
 * make the sequence eventually with makeSymbolList.
 */
public class ChunkedSymbolListFactory
{
    private final static int CHUNK_SIZE = 1<<14;
    private SymbolListFactory symListFactory;
    private Alphabet alfa;

    // management variables for chunks
    private Symbol [] headChunk;
    private int headChunkPos = 0;
    private List chunkL = new ArrayList();

    // cached info for speedups
    private int currentMin = Integer.MAX_VALUE;
    private int currentMax = Integer.MIN_VALUE;
    private SymbolList currentChunk = null;


    // interlocks
    // you can only use symbolAt() or make(), not both.
    private boolean canDoMake = true;

    private class ChunkedSymbolList extends AbstractSymbolList
    {
        private SymbolList [] chunks;
        private final int chunkSize;

        private Alphabet alpha;
        private int length;

        protected void finalize() throws Throwable {
            super.finalize();
            alpha.removeChangeListener(ChangeListener.ALWAYS_VETO, Alphabet.SYMBOLS);
        }

        public ChunkedSymbolList(SymbolList [] chunks,
                                 int chunkSize,
                                 int length,
                                 Alphabet alpha)
        {
            this.chunks = chunks;
            this.chunkSize = chunkSize;
            this.length = length;
            this.alpha = alpha;
           alpha.addChangeListener(ChangeListener.ALWAYS_VETO, Alphabet.SYMBOLS);
        }

        public Alphabet getAlphabet() {
            return alpha;
        }

        public int length() {
            return length;
        }

        public Symbol symbolAt(int pos) {
            int offset;

            --pos;
            if ((pos < currentMin) || (pos > currentMax)) {
                int chnk = pos / chunkSize;
                offset =  pos % chunkSize;

                currentMin = pos - offset;
                currentMax = currentMin + chunkSize - 1;
                currentChunk = chunks[chnk];
            }
            else {
                offset = pos - currentMin;
            }

            return currentChunk.symbolAt(offset + 1);
        }

        public SymbolList subList(int start, int end) {
            if (start < 1 || end > length()) {
                throw new IndexOutOfBoundsException(
                    "Sublist index out of bounds " + length() + ":" + start + "," + end
                    );
            }

            if (end < start) {
                throw new IllegalArgumentException(
                    "end must not be lower than start: start=" + start + ", end=" + end
                    );
            }

            //
            // Mildly optimized for case where from and to are within
            // the same chunk.
            //

            int afrom = start - 1;
            int ato = end - 1;
            int cfrom = afrom / chunkSize;
            if (ato / chunkSize == cfrom) {
                return chunks[cfrom].subList((afrom % chunkSize) + 1, (ato % chunkSize) + 1);
            } else {
                return super.subList(start, end);
            }
        }
    }

    /**
     * @param symListFactory class which produces the SymbolLists that are used
     *        to store the chunked symbols.
     */
    public ChunkedSymbolListFactory(SymbolListFactory symListFactory)
    {
        this.symListFactory = symListFactory;
    }

    /**
     * tool to construct the SymbolList by adding Symbols.
     * Note that this class is not thread-safe.  Also, it
     * can only assemble one SymbolList at a time.  And the
     * composite formed by adding Symbols must not have interstitial
     * missing Symbols.
     */
    public void addSymbols(Alphabet alfa, Symbol[] syms, int pos, int len)
        throws IllegalArgumentException, IllegalAlphabetException
    {
        // lock out make()
        canDoMake = false;

        // check alphabet first
        if (this.alfa == null) {
            this.alfa = alfa;
        }
        else {
            if (this.alfa != alfa) {
                throw new IllegalAlphabetException("Alphabet changed!");
            }
        }

        // see if I need to create the first chunk
        if (headChunk == null) {
            headChunk = new Symbol[CHUNK_SIZE];
            headChunkPos = 0;
        }

        // transfer over new symbols
        int ipos = 0;

        while (ipos < len) {
            // is the current chunk full?
            if (headChunkPos == CHUNK_SIZE) {
                // yes.  Verify there are no surprises!
                for (int i= 0; i < CHUNK_SIZE; i++) {
                    if (headChunk[i] == null) {
                        // there's an interstitial null Symbol!
                        throw new IllegalArgumentException("symbols supplied are not tiling contiguously.");
                    }
                }

                // the chunk is alright, lets stash it.
                chunkL.add(symListFactory.makeSymbolList(headChunk, CHUNK_SIZE, alfa));
                headChunkPos = 0;
                headChunk = new Symbol[CHUNK_SIZE];
            }

            int read = Math.min(len - ipos, CHUNK_SIZE - headChunkPos);
            System.arraycopy(syms, pos + ipos, headChunk, headChunkPos, read);
            ipos += read;
            headChunkPos += read;
        }
    }

    private void clearState()
    {
        this.canDoMake = true;
        headChunk = null;
        headChunkPos = 0;

        chunkL = new ArrayList();
        alfa = null;        
    }

    public SymbolList makeSymbolList() 
        throws IllegalAlphabetException
    {
        try {
        // we have now acquired all symbols
        // the situation can be;-
        // i) there are no Symbols; then chunkL.size() == 0.
        // ii) one chunk worth, not full: chunkL.size() == 0. headChunkPos != 0;
        // iii) one chunk worth, full: chunkL.size() == 0. headChunkPos == CHUNK_SIZE;
        // iv) multiple chunks, all full: there'll be an unpacked chunk at headChunk;
        // v) multiple chunks, last part filled: chunkL.size() != 0. Unpacked chunk at headChunk;

        // so basically, if chunkL.size() != 0, there will be an unpacked chunk to pack
        // unless headChunkPos == 0;

        // do we have any chunks stashed?
        if (chunkL.size() == 0) {
            // no.  The only chunk if any is at headChunk.
            if (headChunkPos == 0) {
                // no symbols!
                return new SymbolList.EmptySymbolList();
            }
            else {
                // we do have ONE chunk to deal with.
                // reduce its size as necessary.
                if (headChunkPos < CHUNK_SIZE) {
                    Symbol[] oldChunk = headChunk;
                    headChunk = new Symbol[headChunkPos];
                    System.arraycopy(oldChunk, 0, headChunk, 0, headChunkPos);
                }

                // now return a SubArraySymbolList
                return new SubArraySymbolList(headChunk, headChunkPos, 0, alfa);
            }
        }
        else {
            // we have multiple chunks.

            // do we have an unstashed chunk?
            if (headChunkPos != 0) {
                // yes, let's stash it
                chunkL.add(symListFactory.makeSymbolList(headChunk, headChunkPos, alfa));
            }

            // everything we want to put away is now in chunkL
            // create an array to stash the SymbolLists into
            SymbolList [] symListArray = new SymbolList[chunkL.size()];

            // and fill it with the contents of our List
            for (int cnum=0; cnum < chunkL.size(); ++cnum) {
                symListArray[cnum] = (SymbolList) chunkL.get(cnum);
            }

            // now let's return a SymbolList to the user
            int length = (chunkL.size() - 1) * CHUNK_SIZE + headChunkPos;
            return new ChunkedSymbolList(symListArray, CHUNK_SIZE, length, alfa);
        }
        }
        finally {
            clearState();
        }
    }

    public SymbolList make(SymbolReader sr)
        throws IOException, IllegalSymbolException, IllegalAlphabetException, BioException
    {
        // check interlock
        if(!canDoMake) throw new BioException("you can't use make() and addSymbol() simultaneously.");

        chunkL = new ArrayList();
        headChunk = new Symbol[CHUNK_SIZE];
        headChunkPos = 0;
        alfa = sr.getAlphabet();

        while (sr.hasMoreSymbols()) {
            // is chunk full?
            if (headChunkPos == CHUNK_SIZE) {
                // yes.  Stash away this chunk in packed form.
                chunkL.add(symListFactory.makeSymbolList(headChunk, CHUNK_SIZE, sr.getAlphabet()));
                headChunkPos = 0;
            }

            // grab up all available Symbols up to end of chunk.
            int read = sr.readSymbols(headChunk, headChunkPos, CHUNK_SIZE - headChunkPos);
            headChunkPos += read;
        }

        clearState();
        return makeSymbolList();
    }
}