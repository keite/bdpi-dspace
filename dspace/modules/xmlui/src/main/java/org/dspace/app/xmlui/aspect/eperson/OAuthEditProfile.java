/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.eperson;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import static org.dspace.app.xmlui.wing.AbstractWingTransformer.message;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Button;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Field;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Select;
import org.dspace.app.xmlui.wing.element.Text;
import org.dspace.authenticate.AuthenticationManager;
import org.dspace.authenticate.AuthenticationMethod;
import org.dspace.content.Collection;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.I18nUtil;
import org.dspace.core.LogManager;
import org.dspace.eperson.Group;
import org.dspace.eperson.Subscribe;
import org.xml.sax.SAXException;

/**
 * Display a form that allows the user to edit their profile.
 * There are two cases in which this can be used: 1) when an
 * existing user is attempting to edit their own profile, and
 * 2) when a new user is registering for the first time.
 *
 * There are several parameters this transformer accepts:
 *
 * email - The email address of the user registering for the first time.
 *
 * registering - A boolean value to indicate whether the user is registering for the first time.
 *
 * retryInformation - A boolean value to indicate whether there was an error with the user's profile.
 *
 * retryPassword - A boolean value to indicate whether there was an error with the user's password.
 *
 * allowSetPassword - A boolean value to indicate whether the user is allowed to set their own password.
 *
 * @author Scott Phillips
 */
public class OAuthEditProfile extends EditProfile
{
    
    private static Logger log = Logger.getLogger(OAuthEditProfile.class);

    /** Language string used: */
    
    private static final Message T_head_update =
        message("xmlui.EPerson.EditProfile.head_update");
    
    private static final Message T_email_address =
        message("xmlui.EPerson.EditProfile.email_address");
    
    private static final Message T_name =
        message("xmlui.administrative.eperson.ManageEPeopleMain.search_column3");
    
    private static final Message T_first_name =
        message("xmlui.EPerson.EditProfile.first_name");
    
    private static final Message T_last_name =
        message("xmlui.EPerson.EditProfile.last_name");
    
    private static final Message T_telephone =
        message("xmlui.EPerson.EditProfile.telephone");
    
    private static final Message T_submit_create =
        message("xmlui.EPerson.EditProfile.submit_create");
    
    private static final Message T_subscriptions =
        message("xmlui.EPerson.EditProfile.subscriptions");

    private static final Message T_subscriptions_help =
        message("xmlui.EPerson.EditProfile.subscriptions_help");

    private static final Message T_email_subscriptions =
        message("xmlui.EPerson.EditProfile.email_subscriptions");

    private static final Message T_select_collection =
        message("xmlui.EPerson.EditProfile.select_collection");
 
    private static final Message T_head_auth =
        message("xmlui.EPerson.EditProfile.head_auth");
    
    private static final Message T_head_identify =
        message("xmlui.EPerson.EditProfile.head_identify");
        
    /** The email address of the user registering for the first time.*/
    private String email;
    private String uspdigital_email_alternativo;
    private String uspdigital_email_usp;
    
    private boolean currentUserIsInOAuthGroup = false;
    
    @Override
    public void setup(SourceResolver resolver, Map objectModel, String src,
            Parameters parameters) throws ProcessingException, SAXException,
            IOException
    {
        
        super.setup(resolver,objectModel,src,parameters);
        
        try {
            
            Group[] memberships = Group.allMemberGroups(context, context.getCurrentUser());
            AuthenticationMethod am;
            Iterator<AuthenticationMethod> iam = AuthenticationManager.authenticationMethodIterator();
            while(iam.hasNext()){
                am = iam.next();
                if(am.getClass().getName().endsWith("OAuthAuthentication")){
                    for(int groupid : am.getSpecialGroups(context, ObjectModelHelper.getRequest(objectModel))){
                        for(Group membership : memberships){
                            if(membership.getID() == groupid){
                                currentUserIsInOAuthGroup = true;
                                break;
                            }
                        }
                        if(currentUserIsInOAuthGroup) break;
                    }
                    break;
                }
            }
            
        }
        catch (SQLException ex) {
            java.util.logging.Logger.getLogger(OAuthEditProfile.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(currentUserIsInOAuthGroup){
            this.email = eperson.getEmail();
            this.uspdigital_email_alternativo = eperson.getMetadata("uspdigital_email_alternativo");
            this.uspdigital_email_usp = eperson.getMetadata("uspdigital_email_usp");

            this.email=(this.email==null?"":this.email);
            this.uspdigital_email_alternativo=(this.uspdigital_email_alternativo==null?"":this.uspdigital_email_alternativo);
            this.uspdigital_email_usp=(this.uspdigital_email_usp==null?"":this.uspdigital_email_usp);
        }

    }
    
   @Override
   public void addBody(Body body) throws WingException, SQLException
   {
       
       if(currentUserIsInOAuthGroup){
            // Log that we are viewing a profile
            log.info(LogManager.getHeader(context, "view_oauthprofile", ""));

            String defaultFirstName="", defaultLastName="", defaultPhone="";
            
            defaultFirstName = eperson.getFirstName();
            defaultLastName = eperson.getLastName();
            defaultPhone = eperson.getMetadata("phone");
            
            String action = contextPath;
            action += "/profile";

            Division profile = body.addInteractiveDivision("information",
                    action,Division.METHOD_POST,"primary");

            profile.setHead(T_head_update);

            List form = profile.addList("form",List.TYPE_FORM);

            List identity = form.addList("identity",List.TYPE_FORM);
            identity.setHead(T_head_identify);
            
            identity.addItem().addXref((String) ConfigurationManager.getProperty("authentication-oauth", "provider.profile.url"), message("xmlui.EPerson.OAuthEditProfile.textlinktoprovider"));

            // Email
            if(email.length() > 0){
            identity.addLabel(T_email_address);
            identity.addItem(email);
            }
            
            if(defaultFirstName.length() > 0){
                identity.addLabel(T_name);
                identity.addItem(defaultFirstName.concat(" ").concat(defaultLastName));
            }
            // First name
            /*
            Text firstName = identity.addItem().addText("first_name");
            firstName.setAutofocus("autofocus");
            firstName.setRequired();
            firstName.setLabel(T_first_name);
            firstName.setValue(defaultFirstName);
            firstName.setDisabled();
            */
            
            // Last name
            
            /*
            if(defaultLastName.length() > 0){
                identity.addLabel(T_last_name);
                identity.addItem(defaultLastName);
            }
            */

            /*
            Text lastName = identity.addItem().addText("last_name");
            lastName.setRequired();
            lastName.setLabel(T_last_name);
            lastName.setValue(defaultLastName);
            lastName.setDisabled();
            */

            // Phone

            if(defaultPhone.length() > 0){
                identity.addLabel(T_telephone);
                identity.addItem(defaultPhone);
            }
            
            /*
            Text phone = identity.addItem().addText("phone");
            phone.setLabel(T_telephone);
            phone.setValue(defaultPhone);
            phone.setDisabled();
            */

            identity.addItem();

            // Subscriptions
            List subscribe = form.addList("subscriptions",List.TYPE_FORM);
            subscribe.setHead(T_subscriptions);

            subscribe.addItem(T_subscriptions_help);

            Collection[] currentList = Subscribe.getSubscriptions(context, context.getCurrentUser());
            Collection[] possibleList = Collection.findAll(context);

            Select subscriptions = subscribe.addItem().addSelect("subscriptions");
            subscriptions.setLabel(T_email_subscriptions);
            subscriptions.setHelp("");
            subscriptions.enableAddOperation();
            subscriptions.enableDeleteOperation();

            subscriptions.addOption(-1,T_select_collection);
            for (Collection possible : possibleList)
            {
                String name = possible.getMetadata("name");
                if (name.length() > 70)
                {
                    name = name.substring(0, 67) + "...";
                }
                subscriptions.addOption(possible.getID(), name);
            }

            for (Collection collection: currentList)
            {
                subscriptions.addInstance().setOptionSelected(collection.getID());
            }
            
            subscribe.addItem();

            Button submit = form.addItem().addButton("submit");
            submit.setValue(T_submit_create);
            
            form.addItem();

            profile.addHidden("eperson-continue").setValue(knot.getId());

            // Add a list of groups that this user is apart of.
            Group[] memberships = Group.allMemberGroups(context, context.getCurrentUser());
            
            // Not a member of any groups then don't do anything.
            if (!(memberships.length > 0))
            {
                return;
            }

            List list = profile.addList("memberships");
            list.setHead(T_head_auth);
            for (Group group: memberships)
            {
                    list.addItem(group.getName());
            }
            
       }
       else {
           super.addBody(body);
       }
    }
   
   /**
    * Recycle
    */
    @Override
    public void recycle()
    {
        this.currentUserIsInOAuthGroup = false;
        this.uspdigital_email_alternativo = null;
        this.uspdigital_email_usp = null;
        super.recycle();
    }
    
}
