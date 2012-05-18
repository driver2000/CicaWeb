package it.iervolino.web;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.sql.DataSource;

@ManagedBean
@RequestScoped
public class Sensore implements Serializable {

  private String nome;
  private String posizione;
  private String ora;
  private int campioni;
  @Resource(name = "jdbc/sisma")
  DataSource dataSource;

  public Sensore() {
  }

  public Sensore(String nome, String posizione, String ora, int campioni) {
    this.nome = nome;
    this.posizione = posizione;
    this.ora = ora;
    this.campioni = campioni;
  }

  public int getCampioni() {
    return campioni;
  }

  public void setCampioni(int campioni) {
    this.campioni = campioni;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getOra() {
    return ora;
  }

  public void setOra(String ora) {
    this.ora = ora;
  }

  public String getPosizione() {
    return posizione;
  }

  public void setPosizione(String posizione) {
    this.posizione = posizione;
  }

  public String save() throws SQLException {
    if (dataSource == null) {
      throw new SQLException("Unable to obtain DataSource");

    }
    Connection connection = dataSource.getConnection();
    if (connection == null) {
      throw new SQLException("impossibile contteresi al database");

    }

    String fileName = getNome() + "_"
            + getPosizione().charAt(0)
            + "_" + getOra().substring(0, 2) + ".in";

    File doc = new File(System.getProperty("user.home") + System.getProperty("file.separator")
            + "Database/" + fileName);

    if (doc.exists()) {
      try {
        PreparedStatement addEntry = connection.prepareStatement(
                "INSERT INTO APP.SENSORI (NOME, POSIZIONE, ORA, CAMPIONI) "
                + "VALUES( ?, ?, ?, ? )");

        addEntry.setString(1, getNome());
        addEntry.setString(2, getPosizione());
        addEntry.setString(3, getOra());
        addEntry.setInt(4, getCampioni());
        addEntry.executeUpdate();
        return "index";

      } finally {
        connection.close();
      }
    } else {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
              "Warning", "Caricare il file"));
      return null;
    }
  }

  @Override
  public int hashCode() {
    int result;
    result = (nome != null ? nome.hashCode() : 0);
    result = 29 * result + (posizione != null ? posizione.hashCode() : 0);
    result = 29 * result + (ora != null ? ora.hashCode() : 0);
    result = 29 * result + (int) (campioni ^ (campioni >>> 16));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Sensore other = (Sensore) obj;
    if ((this.nome == null) ? (other.nome != null) : !this.nome.equals(other.nome)) {
      return false;
    }
    if ((this.posizione == null) ? (other.posizione != null) : !this.posizione.equals(other.posizione)) {
      return false;
    }
    if ((this.ora == null) ? (other.ora != null) : !this.ora.equals(other.ora)) {
      return false;
    }
    if (this.campioni != other.campioni) {
      return false;
    }
    return true;
  }
}
