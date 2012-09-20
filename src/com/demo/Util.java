/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.demo;

import com.sun.jersey.api.client.ClientResponse;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 *
 * @author warren
 */
public class Util {

    /**
     * Convert a wrapped JAXBElement to a string representation
     *
     * @param e
     * @return
     */
    public static String objectToString(JAXBElement e, OAMPolicyManager oamPolicyManager) {
        String s = "<marshall-failed/>";
        try {
            StringWriter sw = new StringWriter();
            Marshaller m = oamPolicyManager.jaxb.createMarshaller();
            m.marshal(e, sw);
            s = sw.toString();
        } catch (JAXBException ex) {
            Logger.getLogger(OAMPolicyManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }

    /**
     * Parse the returned URI - strip off the id=xxx. This is the id of the
     * newly created object.
     *
     * @param response
     * @return
     */
    public static String getId(ClientResponse response) {
        String x = response.getEntity(String.class);
        int i = x.lastIndexOf("id=") + 3;
        return x.substring(i);
    }
    
    
    
}
