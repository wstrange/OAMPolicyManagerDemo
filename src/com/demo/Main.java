/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.demo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author warren.strange@oracle.com
 */
public class Main {

    /**
     * Read a file that specifies the Application Description. Creates Policies
     * for Apps
     *
     * Command args: [-r] [inputfile] -r - remove the app policies instead of
     * creating them
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // set this to true to remove the policies instead of create. 
        boolean removeFlag = false;
        String fileName = "app.txt"; //default input file name to look for

        // ""parse" args. TODO: More robust parsing...
        if (args.length == 1) {
            if (args[0].startsWith("-r")) {
                removeFlag = true;
            } else {
                fileName = args[0];
            }
        } else if (args.length == 2) {
            fileName = args[1];
            removeFlag = true;
        }

        // load props file
         Properties pro = new Properties();
         FileInputStream in;

        try {
           
            in = new FileInputStream("pm.properties");
            pro.load(in);
          
            
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "pm.properties not found. Will use defaults");
        }
        
        String URL = pro.getProperty("url","http://localhost:7001/" );
        String OAMDomain = pro.getProperty("domain", "OAMApplication");
        String username = pro.getProperty("user", "weblogic");
        String password = pro.getProperty("password", "Oracle123");


        OAMPolicyManager pm = new OAMPolicyManager(URL, OAMDomain, username, password);


        try {
            InputStream fis = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
            String line;
            while ((line = br.readLine()) != null) {
                // input file is csv with pipe delimeter
                // Lines starting with # are ignored
                // see app.txt for sample input
                if (!line.startsWith("#")) {
                    String[] field = line.split("\\|");

                    if (removeFlag) {
                        pm.removeApplication(field[0], field[2], field[3]);
                    } else {
                        pm.addApplication(field[0], field[2], field[3]);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
