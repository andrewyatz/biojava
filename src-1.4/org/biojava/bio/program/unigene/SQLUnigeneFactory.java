package org.biojava.bio.program.unigene;

import java.io.*;
import java.net.*;
import java.sql.*;

import org.biojava.utils.*;
import org.biojava.bio.*;

/**
 * <p>An implementatoin of UnigeneFactory that manages it's data in an SQL
 * database.</p>
 *
 * <p><em>This class is for developers and power-users.</em> Usually you will
 * not use this class directly, but rather use UnigeneTools.loadDatabase() with
 * a jdbc URL.</p>
 *
 * <p>This class will store unigene data in a relational database with a schema
 * defined by the resource src/org/biojava/bio/program/unigene/createUnigene.sql
 * and currently only realy supports mysql. To import data to a newly created
 * database, repeatedly call addCluster() on the UnigeneDB you get back.</p>
 *
 * @author Matthew Pocock
 */
public class SQLUnigeneFactory
implements UnigeneFactory {
  private static String CREATE_DB_STATEMENT;

  private static String getCreateDBStatement() {
    if(CREATE_DB_STATEMENT == null) {
      StringBuffer stmt = new StringBuffer();
      BufferedReader stmtIn = new BufferedReader(
        new InputStreamReader(
          SQLUnigeneFactory.class.getClassLoader().getResourceAsStream(
            "/org/biojava/bio/program/unigene/createUnigene.sql"
          )
        )
      );
    }
    return CREATE_DB_STATEMENT;
  }

  /**
   * Accepts all URLs that are of the jdbc protocol.
   */
  public boolean canAccept(URL dbURL) {
    return dbURL.getProtocol().equals("jdbc");
  }

  public UnigeneDB loadUnigene(URL dbURL)
  throws BioException {
    if(!canAccept(dbURL)) {
      throw new BioException("Can't resolve url to an sql unigene db: " + dbURL);
    }

    JDBCConnectionPool conPool = new JDBCConnectionPool(dbURL.toString());

    return new SQLUnigeneDB(conPool);
  }

  public UnigeneDB createUnigene(URL dbURL)
  throws BioException {
    String dbString = dbURL.toString();
    int lastSlash = dbString.lastIndexOf("/");
    String rootURL = dbString.substring(0, lastSlash);
    String dbName = dbString.substring(lastSlash + 1);

    JDBCConnectionPool connPool = new JDBCConnectionPool(rootURL);

    Statement stmt = null;
    try {
      stmt = connPool.takeStatement();
      stmt.execute("create database " + dbName);
      stmt.execute("use " + dbName);
      stmt.execute(getCreateDBStatement());
    } catch (SQLException se) {
      throw new BioException(se, "Could not create database");
    } finally {
      try {
        connPool.putStatement(stmt);
      } catch (SQLException se) {
        // not much we can do about this
      }
    }

    return new SQLUnigeneDB(connPool);
  }
}