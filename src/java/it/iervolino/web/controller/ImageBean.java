package it.iervolino.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@ManagedBean
@RequestScoped
public class ImageBean implements Serializable {

  private StreamedContent image;
  private StreamedContent spectrum;

  public ImageBean() {

    File segnali = new File(System.getProperty("user.home")
            + "/NetBeansProjects/CICAWeb/web/resources/" + "segnali.png");

    File spettro = new File(System.getProperty("user.home")
            + "/NetBeansProjects/CICAWeb/web/resources/" + "spettro.png");
    try {
      this.image = new DefaultStreamedContent(new FileInputStream(segnali), "image/png");
      this.spectrum = new DefaultStreamedContent(new FileInputStream(spettro), "image/png");
    } catch (FileNotFoundException ex) {
      Logger.getLogger(ImageBean.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  public StreamedContent getSpectrum() {
    return spectrum;
  }

  public StreamedContent getImage() {
    return this.image;
  }
}