package org.biojava.bio.program.unigene;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import org.biojava.utils.*;
import org.biojava.utils.io.*;
import org.biojava.bio.*;
import org.biojava.bio.program.indexdb.*;
import org.biojava.bio.program.tagvalue.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

class FlatFileUnigeneFactory
implements UnigeneFactory {
  private static final String DATA_INDEX = "data.index";
  private static final String LIB_INFO_INDEX = "libInfo.index";
  private static final String UNIQUE_INDEX = "unique.index";
  private static final String ALL_INDEX = "all.index";
  
  public boolean canAccept(URL unigeneLoc) {
    return unigeneLoc.getProtocol().equals("file");
  }

  public UnigeneDB loadUnigene(URL unigeneLoc)
  throws BioException {
    if(!unigeneLoc.getProtocol().equals("file")) {
      throw new BioException(
        "Can't create unigene from non-file URL: " +
        unigeneLoc
      );
    }
    
    File unigeneDir = new File(unigeneLoc.getPath());
    if(!unigeneDir.exists()) {
      throw new BioException("Could not locate directory: " + unigeneDir);
    }
    if(!unigeneDir.isDirectory()) {
      throw new BioException("Expecting a directory at: " + unigeneDir);
    }
    
    
    // load a pre-made unigene file set
    try {
      return new FlatFileUnigeneDB(
        new BioStore(new File(unigeneDir, DATA_INDEX), true),
        new BioStore(new File(unigeneDir, LIB_INFO_INDEX), true),
        new BioStore(new File(unigeneDir, UNIQUE_INDEX), true),
        new BioStore(new File(unigeneDir, ALL_INDEX), true)
      );
    } catch (IOException ioe) {
      throw new BioException(ioe, "Could not instantiate flat file unigene db");
    }
  }
  
  public UnigeneDB createUnigene(URL unigeneLoc)
  throws BioException {
    if(!unigeneLoc.getProtocol().equals("file")) {
      throw new BioException(
        "Can't create unigene from non-file URL: " +
        unigeneLoc
      );
    }
    
    File unigeneDir = new File(unigeneLoc.getPath());
    if(!unigeneDir.exists()) {
      throw new BioException("Could not locate directory: " + unigeneDir);
    }
    if(!unigeneDir.isDirectory()) {
      throw new BioException("Expecting a directory at: " + unigeneDir);
    }

    try {
      indexAll(unigeneDir);
      indexUnique(unigeneDir);
      indexData(unigeneDir);
      indexLibInfo(unigeneDir);
    } catch (IOException ioe) {
      throw new BioException(ioe, "Failed to index data");
    }
    
    return loadUnigene(unigeneLoc);
  }

  private void indexData(File unigeneDir)
  throws BioException, IOException {
    // create index file for all *.data files
    File dataIndexFile = new File(unigeneDir, DATA_INDEX);
    BioStoreFactory dataBSF = new BioStoreFactory();
    dataBSF.setPrimaryKey("ID");
    dataBSF.addKey("ID", 10);
    dataBSF.setStoreLocation(dataIndexFile);
    BioStore dataStore = dataBSF.createBioStore();
    File[] dataFiles = unigeneDir.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getName().endsWith(".data");
      }
    });
    for(int i = 0; i < dataFiles.length; i++) {
      File f = dataFiles[i];
      try {
        Indexer indexer = new Indexer(f, dataStore);
        indexer.setPrimaryKeyName("ID");
        Parser parser = new Parser();
        ParserListener pl = UnigeneTools.buildDataParser(indexer);
        while(parser.read(
          indexer.getReader(),
          pl.getParser(),
          pl.getListener()
        )) { ; }
      } catch (ParserException pe) {
        throw new BioException(pe, "Failed to parse " + f);
      }
    }
    try {
      dataStore.commit();
    } catch (NestedException ne) {
      throw new BioException(ne);
    }
  }

  private void indexLibInfo(File unigeneDir)
  throws BioException, IOException {
    // create index for all *.lib.info files
    File liIndexFile = new File(unigeneDir, LIB_INFO_INDEX);
    BioStoreFactory liBSF = new BioStoreFactory();
    liBSF.setPrimaryKey("ID");
    liBSF.addKey("ID", 7);
    liBSF.setStoreLocation(liIndexFile);
    BioStore liStore = liBSF.createBioStore();
    File[] liFiles = unigeneDir.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getName().endsWith(".lib.info");
      }
    });
    for(int i = 0; i < liFiles.length; i++) {
      File f = liFiles[i];
      try {
        Indexer indexer = new Indexer(f, liStore);
        indexer.setPrimaryKeyName("ID");
        Parser parser = new Parser();
        ParserListener pl = UnigeneTools.buildLibInfoParser(indexer);
        while(parser.read(
            indexer.getReader(),
            pl.getParser(),
            pl.getListener()
        )) { ; }
      } catch (ParserException pe) {
        throw new BioException(pe, "Failed to parse " + f);
      }
    }
    try {
      liStore.commit();
    } catch (NestedException ne) {
      throw new BioException(ne);
    }
  }
  
  private void indexUnique(File unigeneDir)
  throws BioException, IOException {
    File uniqueIndex = new File(unigeneDir, UNIQUE_INDEX);
    BioStoreFactory uniqueBSF = new BioStoreFactory();
    uniqueBSF.setStoreLocation(uniqueIndex);
    uniqueBSF.setPrimaryKey("ID");
    uniqueBSF.addKey("ID", 10);
    BioStore uniqueStore = uniqueBSF.createBioStore();
    File[] uniqueFiles = unigeneDir.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getName().endsWith(".seq.uniq");
      }
    });
    for(int i = 0; i < uniqueFiles.length; i++) {
      File f = uniqueFiles[i];
      RAF raf = new RAF(f, "r");
      FastaIndexer indexer = new FastaIndexer(
        raf,
        uniqueStore,
        Pattern.compile("#(\\S+)"),
        1
      );
      FastaFormat format = new FastaFormat();
      SymbolTokenization tok = DNATools.getDNA().getTokenization("token");
      StreamReader sreader = new StreamReader(
        indexer.getReader(),
        format,
        tok,
        indexer
      );
      while(sreader.hasNext()) {
        sreader.nextSequence();
      }
    }
    try {
      uniqueStore.commit();
    } catch (NestedException ne) {
      throw new BioException(ne);
    }
  }
  
  private void indexAll(File unigeneDir)
  throws BioException, IOException {
    File allIndex = new File(unigeneDir, ALL_INDEX);
    BioStoreFactory allBSF = new BioStoreFactory();
    allBSF.setStoreLocation(allIndex);
    allBSF.setPrimaryKey("ID");
    allBSF.addKey("ID", 10);
    BioStore allStore = allBSF.createBioStore();
    File[] allFiles = unigeneDir.listFiles(new FileFilter() {
      public boolean accept(File pathName) {
        return pathName.getName().endsWith(".seq.all");
      }
    });
    Pattern pattern = Pattern.compile("/gb=(\\S+)");
    for(int i = 0; i < allFiles.length; i++) {
      File f = allFiles[i];
      RAF raf = new RAF(f, "r");
      CountedBufferedReader reader = new CountedBufferedReader(new FileReader(f));
      
      long offset = -1;
      String id = null;
      for(String line = reader.readLine(); line != null; line = reader.readLine()) {
        if(line.startsWith("#")) {
          long nof = reader.getFilePointer();
          if(id != null) {
            allStore.writeRecord(raf, offset, (int) (nof - offset), id, Collections.EMPTY_MAP);
          }
          Matcher matcher = pattern.matcher(line);
          matcher.find();
          id = matcher.group(1);
          offset = nof;
        }
      }
    }
    try {
      allStore.commit();
    } catch (NestedException ne) {
      throw new BioException(ne);
    }
  }

  private static class FastaIndexer implements SequenceBuilderFactory {
    private final Map map = new HashMap();
    private final RAF raf;
    private final IndexStore store;
    private final CountedBufferedReader reader;
    private final Pattern idPattern;
    private final int idGroup;

    public FastaIndexer(RAF raf, IndexStore store, Pattern idPattern, int idGroup)
    throws IOException {
      this.raf = raf;
      this.store = store;
      this.idPattern = idPattern;
      this.idGroup = idGroup;
      reader = new CountedBufferedReader(
        new FileReader(
          raf.getFile()
        )
      );
    }

    public CountedBufferedReader getReader() {
      return reader;
    }

    public SequenceBuilder makeSequenceBuilder() {
      return new SeqIOIndexer();
    }

    class SeqIOIndexer extends SeqIOAdapter implements SequenceBuilder {
      long offset = 0L;
      String id;

      public void startSequence() {
        id = null;
        offset = reader.getFilePointer();
      }

      public void addSequenceProperty(Object key, Object value) {
        if(key.equals(FastaFormat.PROPERTY_DESCRIPTIONLINE)) {
          String line = (String) value;
          Matcher m = idPattern.matcher(line);
          m.find();
          id = m.group(idGroup);
        }
      }

      public void endSequence() {
        long nof = reader.getFilePointer();
        store.writeRecord(raf, offset, (int) (nof - offset), id, map);
        offset = nof;
      }

      public Sequence makeSequence() {
        return null;
      }
    }
  }
}