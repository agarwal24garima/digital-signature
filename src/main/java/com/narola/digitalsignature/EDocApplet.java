package com.narola.digitalsignature;

import com.narola.amazon.s3.AmazonS3Server;


import javax.swing.*;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mhimakshi on 30-11-2017.
 */
public class EDocApplet extends Applet implements ActionListener{
    private char[] password;
    TextField pin1=new TextField();
    TextField pin2=new TextField();
    File file=null;

    public void init() {
        this.add(new Label("Enter PIN1"));
        this.add(pin1);

        this.add(new Label("Enter PIN2"));
        this.add(pin2);

        Button b=new Button("Generate");
        add(b);
        b.addActionListener(this);

        Button upload=new Button("Upload File");
        add(upload);
        upload.addActionListener(this);

    }


    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand()=="Upload File"){
            final JFileChooser fc = new JFileChooser();
            if(e.getActionCommand().equals("Upload File")){
                int returnVal = fc.showOpenDialog(EDocApplet.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();
                } else {
                    //..log.append("Open command cancelled by user." + newline);
                }

            }
        }else if(e.getActionCommand()=="Generate"){
            try {
                    String[] s={file.getPath(),file.getPath()+".edoc",pin1.getText(),pin2.getText()};
                    EDoc2Generator.generateSignedDocument(s);
                } catch (Exception e1) {
                    javax.swing.JOptionPane.showMessageDialog(this,"Please upload file or check the card reader is connected properly");
                }
        }

   }
}
