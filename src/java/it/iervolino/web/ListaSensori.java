package it.iervolino.web;

import com.sun.rowset.CachedRowSetImpl;
import it.iervolino.JCica;
import it.iervolino.Plot;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;

import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.primefaces.context.RequestContext;

@SessionScoped
@ManagedBean(name = "lista")
public class ListaSensori implements Serializable {

  private int points;
  private int window;
  private int overlap;
  private List<Sensore> sensori;
  private Sensore[] selezione;
  private File filePath;
  @Resource(name = "jdbc/sisma")
  DataSource dataSource;

  public ListaSensori() {
    this.points = 512;
    this.window = 512;
    this.overlap = 128;
    this.filePath = new File(System.getProperty("user.home") + "/NetBeansProjects/CICAWeb/web/resources"
            + System.getProperty("file.separator"));
    this.sensori = new ArrayList<Sensore>();

  }

  public int getOverlap() {
    return overlap;
  }

  public void setOverlap(int overlap) {
    this.overlap = overlap;
  }

  public int getPoints() {
    return points;
  }

  public void setPoints(int points) {
    this.points = points;
  }

  public int getWindow() {
    return window;
  }

  public void setWindow(int window) {
    this.window = window;
  }

  public Sensore[] getSelezione() {
    return selezione;
  }

  public void setSelezione(Sensore[] selezione) {
    this.selezione = selezione;
  }

  private void reset() {
    points = 512;
    window = 512;
    overlap = 128;
    sensori = new ArrayList<Sensore>();


  }

  public List<Sensore> getSensori() throws SQLException {

    sensori.clear();
    if (dataSource == null) {
      throw new SQLException("Unable to obtain DataSource");
    }

    Connection connection = dataSource.getConnection();

    if (connection == null) {
      throw new SQLException("Unable to obtain Connection");
    }

    try {
      PreparedStatement ps = connection.prepareStatement(
              "SELECT NOME, POSIZIONE, ORA, CAMPIONI "
              + "FROM APP.SENSORI ORDER BY POSIZIONE,ORA");
      CachedRowSet crs = new CachedRowSetImpl();
      crs.populate(ps.executeQuery());
      while (crs.next()) {
        Sensore s = new Sensore(crs.getString("nome"), crs.getString("posizione"), crs.getString("ora"), crs.getInt("campioni"));
        sensori.add(s);
      }

      return sensori;

    } finally {
      connection.close();
    }

  }

  public void validation(ActionEvent event) {
    RequestContext context = RequestContext.getCurrentInstance();
    boolean isValid = false;
    if (selezione.length < 2) {

      isValid = false;
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
              "Warning", "Selezionare almeno due segnali"));

    } else {
      isValid = true;
    }
   
    context.addCallbackParam("isValid", isValid);
  }

  public String remove() throws SQLException {
    if (selezione != null) {
      int rows = selezione.length;
      if (dataSource == null) {
        throw new SQLException("Unable to obtain DataSource");
      }
    
      Connection connection = dataSource.getConnection();

      if (connection == null) {
        throw new SQLException("Impossibile connettersi al database");
      }

      try {
        String fileNames[] = new String[rows];
        for (int i = 0; i < rows; i++) {
          fileNames[i] = selezione[i].getNome() + "_"
                  + selezione[i].getPosizione().charAt(0)
                  + "_" + selezione[i].getOra().substring(0, 2) + ".in";

          File doc = new File(System.getProperty("user.home") + System.getProperty("file.separator")
                  + "Database/" + fileNames[i]);
          boolean delete = doc.delete();

          PreparedStatement removeEntry = connection.prepareStatement(
                  "DELETE FROM APP.SENSORI WHERE NOME=? AND POSIZIONE=? AND ORA=? ");
          removeEntry.setString(1, selezione[i].getNome());
          removeEntry.setString(2, selezione[i].getPosizione());
          removeEntry.setString(3, selezione[i].getOra());
          removeEntry.executeUpdate();

        }

        return "index";
      } finally {
        connection.close();
      }

    } else {
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
              "Warning", "Selezionare un evento"));
      return null;
    }
  }

  public String process() throws IOException {
    int rows = selezione.length;
    int columns = Integer.MAX_VALUE;
    String fileNames[] = new String[rows];

    for (int i = 0; i < rows; i++) {

      if (selezione[i].getCampioni() < columns) {
        columns = selezione[i].getCampioni();

      }
      fileNames[i] = selezione[i].getNome() + "_"
              + selezione[i].getPosizione().charAt(0)
              + "_" + selezione[i].getOra().substring(0, 2) + ".in";
    }

    RealMatrix mix = new Array2DRowRealMatrix(rows, columns);

    for (int i = 0; i < rows; i++) {

      File doc = new File(System.getProperty("user.home") + System.getProperty("file.separator")
              + "Database/" + fileNames[i]);
      BufferedReader reader = new BufferedReader(new FileReader(doc));
      String str = null;

      while ((str = reader.readLine()) != null) {

        StringTokenizer token = new StringTokenizer(str, ";");
        int j = 0;
        while (token.hasMoreTokens()) {
          mix.setEntry(i, j, Double.parseDouble(token.nextToken()));
          ++j;
        }

      }

    }

    JCica cica = new JCica(points, window, overlap, mix);
    RealMatrix demixSignals = cica.demix();
    RealMatrix spettro = cica.powerSpectrum();
    cica.saveToFile(filePath.getAbsolutePath() + "/demix.out");
    Plot plot = new Plot(demixSignals.getData(), "SegnaliSeparati");
    Plot spectrum = new Plot(spettro.getData(), "Spettro");
    plot.plotToFile(filePath.getAbsolutePath() + "/segnali.png");
    spectrum.plotToFile(filePath.getAbsolutePath() + "/spettro.png");
    reset();
    return "risultati";
  }
}
