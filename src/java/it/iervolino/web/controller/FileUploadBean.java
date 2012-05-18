package it.iervolino.web.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import org.primefaces.event.FileUploadEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.RequestScoped;
import org.primefaces.model.UploadedFile;

@RequestScoped
@ManagedBean(name = "fileUpload")
public class FileUploadBean implements Serializable {

  public void upload(FileUploadEvent event) {

    if (event != null) {

      UploadedFile fileUp = event.getFile();
      File fileToWrite = new File("/home/daniele/Database/" + fileUp.getFileName());

      try {
        byte[] buffer = new byte[1024];
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileToWrite));
        BufferedInputStream in = new BufferedInputStream(fileUp.getInputstream());
        int byteRead = 0;

        while ((byteRead = in.read(buffer)) != -1) {
          out.write(buffer, 0, byteRead);
          out.flush();
        }

        out.close();
        in.close();
        FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, msg);

      } catch (FileNotFoundException ex) {
        Logger.getLogger(FileUploadBean.class.getName()).log(Level.SEVERE, null, ex);
        FacesMessage error = new FacesMessage("The files were not uploaded!");
        FacesContext.getCurrentInstance().addMessage(null, error);

      } catch (IOException ex) {
        Logger.getLogger(FileUploadBean.class.getName()).log(Level.SEVERE, null, ex);
        FacesMessage error = new FacesMessage("The files were not uploaded!");
        FacesContext.getCurrentInstance().addMessage(null, error);
      }
    }
  }
}
