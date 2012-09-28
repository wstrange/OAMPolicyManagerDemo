/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.demo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 *
 * @author warren.strange@oracle.com
 */
public class Main {
    /**
     * Read a file that specifies the Application Description. 
     * Creates Policies for Apps
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //String URL = "http://osc2:17001/";
        String URL = "http://osc:7001/";
        String OAMDomain = "OAMApplication";
       
        String username = "weblogic";
        String password = "Oracle123";

        OAMPolicyManager pm = new OAMPolicyManager(URL, OAMDomain, username, password);

        String fileName = "app.txt"; //default input file name to look for
        if (args.length >= 1) {
            fileName = args[0];
        }
        
        
        // set this to true to remove the policies instead of create. 
        // TODO: Make this an arg flag
        boolean removeFlag = false;
        
        try {
            InputStream fis = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
            String line;
            while (
                    (line = br.readLine()) != null) {
                // input file is csv with pipe delimeter
                // Lines starting with # are ignored
                // see app.txt for sample input
                if (!line.startsWith("#")) {
                    String[] field = line.split("\\|");
                    
                    if( removeFlag )
                        pm.removeApplication(field[0], field[2], field[3]);
                    else
                        pm.addApplication(field[0], field[2], field[3]);
                    
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
