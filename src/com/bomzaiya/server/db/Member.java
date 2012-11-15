package com.bomzaiya.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Member {
  private Connection mConnection = null;

  public void connect() {
    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      mConnection = DriverManager.getConnection("jdbc:mysql://localhost/test", "root", "admin");

      if (!mConnection.isClosed())
        System.out.println("Successfully connected to " + "MySQL server using TCP/IP...");

    } catch (Exception e) {
      System.err.println("Exception: " + e.getMessage());
    }
  }

  public ResultSet getMember() {
    ResultSet rs = null;

    if (mConnection == null) {
      connect();
    }
    try {
      if (mConnection.isClosed()) {
        connect();
      }
      Statement statement = mConnection.createStatement();
      if (statement.execute("SELECT * from member")) {
        rs = statement.getResultSet();
        while (rs.next()) {
          System.out.println("row: " + rs.getString("username"));
        }
      }

    } catch (SQLException e) {
      System.err.println("Exception: " + e.getMessage());
    } finally {
      try {
        if (mConnection != null)
          mConnection.close();
      } catch (SQLException e) {
      }
    }

    return rs;
  }
}
