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

@RequestScoped
@ManagedBean(name = "fileDownload")
public class FileDownloadBean implements Serializable {

    private StreamedContent file;
    
    public FileDownloadBean() {
        File fileDownload = new File("/home/daniele/NetBeansProjects/CICAWeb/web/resources/demix.out");
        try {
            file = new DefaultStreamedContent(new FileInputStream(fileDownload), "text/plain", "demix.out");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileDownloadBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public StreamedContent getFile() {
        return this.file;
    }
}
