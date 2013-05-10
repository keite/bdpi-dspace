
package org.dspace.authenticate;

import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.USPdigitalApi;
import org.scribe.oauth.OAuthService;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.model.Response;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * 
 * @author Jan Leduc de Lara
 * @version $Revision$
 */
public class OAuthAuthentication
    implements AuthenticationMethod {	
	
	
	private static Logger log = Logger.getLogger(OAuthAuthentication.class);

	private static String PROTECTED_RESOURCE_URL = ConfigurationManager.getProperty("authentication-oauth", "PROTECTED_RESOURCE_URL");     	
	private static String API_KEY = ConfigurationManager.getProperty("authentication-oauth", "API_KEY");
	private static String API_SECRET = ConfigurationManager.getProperty("authentication-oauth", "API_SECRET");
	
	private OAuthService getOauthservice(HttpServletRequest httprequest) {
    	
		if(httprequest.getSession().getAttribute("oauthservice")==null){			
			setOauthService(httprequest);
		}
		
		return (OAuthService) httprequest.getSession().getAttribute("oauthservice");
		
	}
	
	private void setOauthService(HttpServletRequest request){
		request.getSession().setAttribute("oauthservice", new ServiceBuilder()
        .apiKey(API_KEY)
        .apiSecret(API_SECRET)
        .provider(USPdigitalApi.class)
        .build());
	}
	

    public boolean canSelfRegister(Context context,
                                   HttpServletRequest request,
                                   String username)
        throws SQLException
    {
        return true;
    }

    public void initEPerson(Context context, HttpServletRequest request,
            EPerson eperson)
        throws SQLException
    {
    	
    }

    public boolean allowSetPassword(Context context,
                                    HttpServletRequest request,
                                    String username)
        throws SQLException
    {
        return false;
    }

    public boolean isImplicit()
    {
        return false;
    }

    

    public int authenticate(Context context,
                            String oauth_token,
                            String oauth_verifier,
                            String realm,
                            HttpServletRequest request)
        throws SQLException
    {
    	System.out.println("passaqui!!!!");
    	
    	EPerson eperson = null;
    	
    	if(oauth_verifier == null){
    		
    		if(request.getSession().getAttribute("usp_bdpi_oauth_loginUsuario") == null){
    			return NO_SUCH_USER;
    		}
    		else {
    			eperson = EPerson.findByNetid(context, (String) request.getSession().getAttribute("usp_bdpi_oauth_loginUsuario"));
    			
    			if(eperson == null){
    				return NO_SUCH_USER;
    			}
    			else {
    				return SUCCESS;
    			}
    		}
    	}
    	else {

        	System.out.println("passaqui A!!!!");
    		
	        Token accessToken = getOauthservice(request).getAccessToken(getOauthservice(request).getRequestToken(), new Verifier(oauth_verifier));
	        
	        OAuthRequest orequest = new OAuthRequest(Verb.POST, PROTECTED_RESOURCE_URL);
	        
	        getOauthservice(request).signRequest(accessToken, orequest);
	        Response oresponse = orequest.send();

        	System.out.println("passaqui B!!!!");
	        
	        try {
	        
	        	JSONObject jso = new JSONObject(oresponse.getBody());
	        	
	        	System.out.println("#########retornou######");
	        	System.out.println(oresponse.getBody());
	        	System.out.println("#########retornou######");

	        	System.out.println("passaqui C!!!!");
	        	
	        	if(jso.getString("loginUsuario").length()>0){
	        		
	        		request.getSession().setAttribute("usp_bdpi_oauth_di", jso);
	        		
	        		request.getSession().setAttribute("usp_bdpi_oauth_loginUsuario", jso.getString("loginUsuario"));	        		
	        		request.getSession().setAttribute("usp_bdpi_oauth_nomeUsuario", jso.getString("nomeUsuario"));
	        		request.getSession().setAttribute("usp_bdpi_oauth_tipoUsuario", jso.getString("tipoUsuario"));
	        		request.getSession().setAttribute("usp_bdpi_oauth_emailPrincipalUsuario", jso.getString("emailPrincipalUsuario"));
	        		request.getSession().setAttribute("usp_bdpi_oauth_emailAlternativoUsuario", jso.getString("emailAlternativoUsuario"));
	        		request.getSession().setAttribute("usp_bdpi_oauth_emailUspUsuario", jso.getString("emailUspUsuario"));
	        		request.getSession().setAttribute("usp_bdpi_oauth_numeroTelefoneFormatado", jso.getString("numeroTelefoneFormatado"));	        		
	        			        		
	        		eperson = EPerson.findByNetid(context, jso.getString("loginUsuario"));
	        		if(eperson == null){
		        		return BAD_CREDENTIALS;
	        		}
	        		else {	        			
	        			context.setCurrentUser(eperson);
	        			return SUCCESS;
	        		}
	        		
	        	}
	        	else {
	        		return NO_SUCH_USER;
	        	}
	        	
	        }
	        catch(JSONException e){
	        	
	            log.trace("Failed to authorize looking up EPerson", e);
	            
	            return NO_SUCH_USER;
	            
	        }
	        
    	}
        
    }

    public String loginPageURL(Context context,
                            HttpServletRequest request,
                            HttpServletResponse response)
    {
    	System.out.println("mostra loginPageURL!! " + getOauthservice(request).getRequestToken().getRawResponse());
    	
    	return response.encodeRedirectURL(getOauthservice(request).getAuthorizationUrl(getOauthservice(request).getRequestToken()));
    }

    public String loginPageTitle(Context context)
    {
    	System.out.println("pega loginpage title!!");
        return "org.dspace.eperson.OAuthAuthentication.title";
    }
    
    public int[] getSpecialGroups(Context context, HttpServletRequest request)
    {
        try
        {
            if (!context.getCurrentUser().getNetid().equals(""))
            {
                String groupName = ConfigurationManager.getProperty("authentication-oauth", "login.specialgroup");
                if ((groupName != null) && (!groupName.trim().equals("")))
                {
                    Group oauthGroup = Group.findByName(context, groupName);
                    if (oauthGroup == null)
                    {
                        // Oops - the group isn't there.
                        /*
                    	log.warn(LogManager.getHeader(context,
                                "oauth_specialgroup",
                                "Group defined in login.specialgroup does not exist"));
                        */
                        return new int[0];
                    } else
                    {
                        return new int[] { oauthGroup.getID() };
                    }
                }
            }
        }
        catch (Exception npe) {
        }
        return new int[0];
    
    }    
}
