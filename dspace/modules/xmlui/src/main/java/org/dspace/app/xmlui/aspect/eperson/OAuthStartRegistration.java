/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.eperson;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.mail.Session;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Head;
import org.dspace.app.xmlui.wing.element.Hidden;
import org.dspace.app.xmlui.wing.element.Item;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Radio;
import org.dspace.app.xmlui.wing.element.Text;
import org.xml.sax.SAXException;

/**
 * Display the new user registration form, allowing the user to enter 
 * in an email address and have the system verify the email address
 * before allowing the user create an account
 * 
 * There are two parameters that may be given to the form:
 * 
 * email - The email of the new account account
 * 
 * retry - A boolean value indicating that the previously entered email was invalid.
 * 
 * accountExists - A boolean value indicating the email previously entered already
 *   belongs to a user.
 *   
 * @author Scott Phillips
 */

public class OAuthStartRegistration extends AbstractDSpaceTransformer implements CacheableProcessingComponent
{
    /** language strings */
    private static final Message T_title =
        message("xmlui.EPerson.OAuthStartRegistration.title");
    
    private static final Message T_dspace_home =
        message("xmlui.general.dspace_home");
    
    private static final Message T_trail_new_registration =
        message("xmlui.EPerson.trail_new_registration");
    
    private static final Message T_head1 =
        message("xmlui.EPerson.OAuthStartRegistration.head1");
    
    private static final Message T_para1 = 
        message("xmlui.EPerson.OAuthStartRegistration.para1");
    
    private static final Message T_reset_password_for =
        message("xmlui.EPerson.OAuthStartRegistration.reset_password_for");
    
    private static final Message T_submit_reset = 
        message("xmlui.EPerson.OAuthStartRegistration.submit_reset");
    
    private static final Message T_head2 = 
        message("xmlui.EPerson.OAuthStartRegistration.head2");
    
    private static final Message T_para2 = 
        message("xmlui.EPerson.OAuthStartRegistration.para2");
    
    private static final Message T_email_address =
        message("xmlui.EPerson.OAuthStartRegistration.email_address");

    private static final Message T_email_address_help =
        message("xmlui.EPerson.OAuthStartRegistration.email_address_help");
    
    private static final Message T_error_bad_email =
        message("xmlui.EPerson.OAuthStartRegistration.error_bad_email");
    
    private static final Message T_submit_register = 
        message("xmlui.EPerson.OAuthStartRegistration.submit_register");
    

    /** The email address previously entered */
    private String email;
    
    private String usp_bdpi_oauth_loginUsuario;
    private String usp_bdpi_oauth_nomeUsuario;
    private String usp_bdpi_oauth_tipoUsuario;
    private String usp_bdpi_oauth_emailPrincipalUsuario;
    private String usp_bdpi_oauth_emailAlternativoUsuario;
    private String usp_bdpi_oauth_emailUspUsuario;
    private String usp_bdpi_oauth_numeroTelefoneFormatado;
    
    /** Determine if the user failed on their last attempt to enter an email address */
    private java.util.List<String> errors;
    
    /** 
     * Determine if the last failed attempt was because an account already 
     * existed for the given email address 
     */
    private boolean accountExists;
    
    public void setup(SourceResolver resolver, Map objectModel, String src,
            Parameters parameters) throws ProcessingException, SAXException,
            IOException
    { 
        super.setup(resolver,objectModel,src,parameters);
                
        this.email = parameters.getParameter("email","");
        
        this.usp_bdpi_oauth_loginUsuario = parameters.getParameter("usp_bdpi_oauth_loginUsuario","");
        this.usp_bdpi_oauth_nomeUsuario = parameters.getParameter("usp_bdpi_oauth_nomeUsuario","");
        this.usp_bdpi_oauth_tipoUsuario = parameters.getParameter("usp_bdpi_oauth_tipoUsuario","");
        this.usp_bdpi_oauth_emailPrincipalUsuario = parameters.getParameter("usp_bdpi_oauth_emailPrincipalUsuario","");
        this.usp_bdpi_oauth_emailAlternativoUsuario = parameters.getParameter("usp_bdpi_oauth_emailAlternativoUsuario","");
        this.usp_bdpi_oauth_emailUspUsuario = parameters.getParameter("usp_bdpi_oauth_emailUspUsuario","");
        this.usp_bdpi_oauth_numeroTelefoneFormatado = parameters.getParameter("usp_bdpi_oauth_numeroTelefoneFormatado","");
        
        // this.usp_bdpi_oauth_di = parameters.getParameter("usp_bdpi_oauth_di","");
        
        this.accountExists = parameters.getParameterAsBoolean("accountExists",false);
        String errors = parameters.getParameter("errors","");
        if (errors.length() > 0)
        {
            this.errors = Arrays.asList(errors.split(","));
        }
        else
        {
            this.errors = new ArrayList<String>();
        }
    }
     
    
    /**
     * Generate the unique caching key.
     * This key must be unique inside the space of this component.
     */
    public Serializable getKey()
    {
        // Only cache on the first attempt.
        if (email == null && !accountExists && errors != null && errors.size() == 0)
        {
            // cacheable
            return "1";
        }
        else
        {
            // Uncachable
            return "0";
        }
    }

    /**
     * Generate the cache validity object.
     */
    public SourceValidity getValidity()
    {
        if (email == null && !accountExists && errors != null && errors.size() == 0)
        {
            // Always valid
            return NOPValidity.SHARED_INSTANCE;
        }
        else
        {
            // invalid
            return null;
        }
    }
    
    
    public void addPageMeta(PageMeta pageMeta) throws WingException 
    {
        // Set the page title
        pageMeta.addMetadata("title").addContent(T_title);

        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
        pageMeta.addTrail().addContent(T_trail_new_registration);
    }
    
   public void addBody(Body body) throws WingException {
        
       if (accountExists) {
           Division exists = body.addInteractiveDivision("register-account-exists",contextPath+"/oauthregister",Division.METHOD_POST,"primary");

           exists.setHead(T_head1);
           
           exists.addPara(T_para1);

           List form = exists.addList("form");
           
           form.addLabel(T_reset_password_for);
           form.addItem(this.email);
           
           form.addLabel();
           Item submit = form.addItem();
           submit.addButton("submit_forgot").setValue(T_submit_reset);
           
           exists.addHidden("email").setValue(this.email);
           
           exists.addHidden("eperson-continue").setValue(knot.getId()); 
       }
       
       
       Division register = body.addInteractiveDivision("register",
               contextPath+"/oauthregister",Division.METHOD_POST,"primary");
       
       
       Head tmphead = register.setHead();
       tmphead.addContent(T_head2);
       tmphead.addContent(", " + this.usp_bdpi_oauth_nomeUsuario + "!");
       
       EPersonUtils.registrationProgressList(register,1);
       
       register.addPara(T_para2);
       
       List form = register.addList("form",List.TYPE_FORM);
       
       Radio emails = form.addItem().addRadio("emails");
       if(this.usp_bdpi_oauth_emailUspUsuario.trim().length()>0)
    	   emails.addOption(false, this.usp_bdpi_oauth_emailUspUsuario , this.usp_bdpi_oauth_emailUspUsuario);
       if(this.usp_bdpi_oauth_emailPrincipalUsuario.trim().length()>0)
    	   emails.addOption(false, this.usp_bdpi_oauth_emailPrincipalUsuario , this.usp_bdpi_oauth_emailPrincipalUsuario);
       if(this.usp_bdpi_oauth_emailAlternativoUsuario.trim().length()>0)
    	   emails.addOption(false, this.usp_bdpi_oauth_emailAlternativoUsuario , this.usp_bdpi_oauth_emailAlternativoUsuario);
       
       Text email = form.addItem().addText("email");
       email.setRequired();
       email.setAutofocus("autofocus");
       email.setLabel(T_email_address);
       email.setHelp(T_email_address_help);
       email.setValue(this.email);
       
       if (errors.contains("email"))
       {
           email.addError(T_error_bad_email);
       }
       
       Hidden numerusp = form.addItem().addHidden("numerusp");
       numerusp.setValue(this.usp_bdpi_oauth_loginUsuario);
       
       Item submit = form.addItem();
       submit.addButton("submit").setValue(T_submit_register);
       
       register.addHidden("eperson-continue").setValue(knot.getId()); 
   }
   
   /**
    * Recycle
    */
   public void recycle() 
   {
       this.email = null;
       this.errors = null;
       super.recycle();
   }
}
