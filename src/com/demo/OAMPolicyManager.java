/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.demo;

import com.demo.jaxb.AuthenticationPolicies;
import com.demo.jaxb.AuthenticationPolicy;
import com.demo.jaxb.AuthorizationPolicies;
import com.demo.jaxb.AuthorizationPolicy;
import com.demo.jaxb.AuthorizationPolicy.Conditions;
import com.demo.jaxb.CombinerType;
import com.demo.jaxb.Identity;
import com.demo.jaxb.IdentityCondition;
import com.demo.jaxb.IdentityCondition.Identities;
import com.demo.jaxb.IdentityType;
import com.demo.jaxb.ObjectFactory;
import com.demo.jaxb.Resource;
import com.demo.jaxb.ResourceProtectionLevel;
import com.demo.jaxb.Rule;
import com.demo.jaxb.RuleCombiner;
import com.demo.jaxb.RuleConditionCombiner;
import com.demo.jaxb.RuleEffect;
import com.demo.jaxb.SimpleCombiner;
import com.demo.jaxb.SuccessResponses;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Created wrapper that jaxb did not generate
 *
 * @author warren
 */
public class OAMPolicyManager {

    Client client;
    WebResource base;
    String appDomain;
    ObjectFactory objFactory = new ObjectFactory(); // JAXB Obj factory
    JAXBContext jaxb;

    public OAMPolicyManager(String rootURL, String appDomain, String username, String password) {
        this.appDomain = appDomain;

        ClientConfig config = new DefaultClientConfig();
        client = Client.create(config);

        HTTPBasicAuthFilter authFilter = new HTTPBasicAuthFilter(username, password);
        client.addFilter(authFilter);
        client.addFilter(new LoggingFilter());

        base = client.resource(rootURL).
                path("/oam/services/rest/11.1.2.0.0/ssa/policyadmin").
                queryParam("appdomain", appDomain);
        try {
            ClassLoader cl = ObjectFactory.class.getClassLoader();
            jaxb = JAXBContext.newInstance("com.demo.jaxb", cl);
        } catch (JAXBException ex) {
            Logger.getLogger(OAMPolicyManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getAppDomainXML() {
        return base.path("/appdomain").get(String.class);
    }

    public String getResourcesXML() {
        return base.path("/resource").get(String.class);
    }

    public String getAuthNPolicyXML() {
        return base.path("/authnpolicy").get(String.class);
    }

    public AuthenticationPolicy getAuthNPolicy(String name) {
        ClientResponse r = base.path("/authnpolicy").
                queryParam("name", name).
                accept(MediaType.APPLICATION_XML).
                get(ClientResponse.class);
        AuthenticationPolicies p = r.getEntity(AuthenticationPolicies.class);
        
        return ( r.getStatus() == 200 ? p.getAuthenticationPolicy().get(0): null );
        
    }
    
    public AuthorizationPolicy getAuthZPolicy(String name) {
        ClientResponse r = base.path("/authzpolicy").
                queryParam("name", name).
                accept(MediaType.APPLICATION_XML).
                get(ClientResponse.class);
        AuthorizationPolicies p = r.getEntity(AuthorizationPolicies.class);
        
        return ( r.getStatus() == 200 ? p.getAuthorizationPolicy().get(0): null );
        
    }
         

   

    /**
     * Delete object with y name
     *
     * @param name - of object
     */
    public void deleteName(String name, String type) {
        base.path(type).queryParam("name", name).delete();
    }

    public Resource makeResourceObj(String resourceURL, String hostIdentifier) {
        Resource r = new Resource();

        String name = "HTTP::" + hostIdentifier + "::" + resourceURL + "::::";

        r.setName(name);
        r.setApplicationDomainName(appDomain);
        r.setHostIdentifierName(hostIdentifier);
        r.setProtectionLevel(ResourceProtectionLevel.PROTECTED);
        r.setResourceURL(resourceURL);
        return r;
    }
    
    public AuthorizationPolicy makeAuthorizationPolicyObj(String name,String conditionName) {
        AuthorizationPolicy p = new AuthorizationPolicy();
        
        p.setApplicationDomainName(appDomain);
        p.setName(name);
        Conditions c = new Conditions();
        p.setConditions( c );
        List<IdentityCondition> idcList = c.getIdentityCondition();
        IdentityCondition idc = new IdentityCondition();
        idc.setName(conditionName);
        Identity id = new Identity();
        id.setIdentityDomain("OUD");
        //id.setType(IdentityType.LDAP_SEARCH_FILTER);
        //id.setSearchFilter("(objectclass == foo)");
        id.setType(IdentityType.GROUP);
        //id.setName("FOO-Group");
        id.setSearchFilter("FOO-Group");
        
        
        Identities ids = new Identities();
        ids.getIdentity().add(id);
        idc.setIdentities(ids);
        idcList.add(idc);
        
        Rule r = new Rule();
        RuleCombiner rc = new RuleCombiner();
        
        SimpleCombiner simple = new SimpleCombiner();
        
        simple.setCombinerMode(RuleConditionCombiner.ALL);
        SimpleCombiner.Conditions sc = new SimpleCombiner.Conditions();
        simple.setConditions(sc);
        sc.getCondition().add(conditionName);
                
        r.setCombinerType(CombinerType.SIMPLE);
        RuleCombiner combine = new RuleCombiner();
        r.setCombiner(rc);
        r.setEffect(RuleEffect.ALLOW);
        rc.setSimpleCombiner(simple);
        AuthorizationPolicy.Rules rules = new AuthorizationPolicy.Rules();
        rules.getRule().add(r);
        
        p.setRules(rules);
        
        // add resources
        
        p.setResources( new AuthorizationPolicy.Resources() );
        
        p.setSuccessResponses( new SuccessResponses());
        
        
        return p;
    }
    
    public String createResource(Resource r) {
        JAXBElement e = objFactory.createResource(r);


        ClientResponse response = base.path("/resource").
                type(MediaType.APPLICATION_XML).
                post(ClientResponse.class, e);

        return getId(response);
    }
    
    public String createAuthorizationPolicy(AuthorizationPolicy authPolicy) {
        JAXBElement e = objFactory.createAuthorizationPolicy(authPolicy);

        ClientResponse response = base.path("/authzpolicy").
                type(MediaType.APPLICATION_XML).
                post(ClientResponse.class, e);

        return response.toString();
    }
     

    public String updateAuthNPolicy(AuthenticationPolicy ap) {
        JAXBElement e = objFactory.createAuthenticationPolicy(ap);
        ClientResponse r = base.path("/authnpolicy").
                type(MediaType.APPLICATION_XML).
                put(ClientResponse.class, e);
        return r.toString();
    }

    /**
     * Parse the returned URI - strip off the id=xxx. This is the id of the
     * newly created object.
     *
     * @param response
     * @return
     */
    protected String getId(ClientResponse response) {
        String x = response.getEntity(String.class);
        int i = x.lastIndexOf("id=") + 3;
        return x.substring(i);
    }

    /**
     * Convert a wrapped JAXBElement to a string representation
     *
     * @param e
     * @return
     */
    protected String objectToString(JAXBElement e) {
        String s = "<marshall-failed/>";
        try {
            StringWriter sw = new StringWriter();

            Marshaller m = jaxb.createMarshaller();
            m.marshal(e, sw);
            s = sw.toString();
        } catch (JAXBException ex) {
            Logger.getLogger(OAMPolicyManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }

    /**
     * Example of Usage
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String URL = "http://unit1122.oracleads.com:7001/";
        String OAMDomain = "OAMApplication";
        String username = "weblogic";
        String password = "Oracle123";

        OAMPolicyManager pm = new OAMPolicyManager(URL, OAMDomain, username, password);

        /*
         Resource r = pm.makeResourceObj("/foo/**", "ohs1");
         pm.deleteName(r.getName(), "/resource");
         String id = pm.createResource(r);  
         p("ID = " + id);
         * */

        //String id = "ac75e64ec9be6471298c78193e670300c";
        //p(pm.getAuthNPolicyXML());
        AuthenticationPolicy authn = pm.getAuthNPolicy("Protected Resource Policy");
        List<String> res = authn.getResources().getResource();
        //res.add(id);

        //pm.updateAuthNPolicy(ap);

        AuthorizationPolicy authz = pm.getAuthZPolicy("LDAPAuthorization");
        res = authz.getResources().getResource();
        p("Res=" + res);
        //p(pm.getResourcesXML());
        
        AuthorizationPolicy authPolicy = pm.makeAuthorizationPolicyObj("TestPolicy", "FOO Only");
        
        pm.createAuthorizationPolicy(authPolicy);
    }

    // print String s to stdout
    static void p(String s) {
        System.out.println(s);
    }

   
}
