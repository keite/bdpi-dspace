/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.eperson;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.sitemap.PatternException;
import org.dspace.app.xmlui.utils.AuthenticationUtil;
import org.dspace.authenticate.AuthenticationManager;
import org.dspace.authenticate.OAuthAuthentication;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

/**
 * Attempt to authenticate the user based upon their presented OAuth credentials. 

 * If the authentication attempt is successful then an HTTP redirect will be
 * sent to the browser redirecting them to their original location in the 
 * system before authenticated or if none is supplied back to the DSpace 
 * homepage. The action will also return true, thus contents of the action will
 * be executed.
 * 
 * If the authentication attempt fails, the action returns false.
 * 
 * Example use:
 * 
 * <map:act name="OAuthAction">
 *   <map:serialize type="xml"/>
 * </map:act>
 * <map:transform type="try-to-login-again-transformer">
 *
 * @author <a href="mailto:janleduc@usp.br">Leduc de Lara, Jan</a>
 */

public class OAuthAction extends AbstractAction
{

    /**
     * Attempt to authenticate the user. 
     */
	public Map act(Redirector redirector, SourceResolver resolver, Map objectModel,
            String source, Parameters parameters) throws Exception
    {
		
        Request request = ObjectModelHelper.getRequest(objectModel);

        String oauth_token = request.getParameter("oauth_token");
        String oauth_verifier = request.getParameter("oauth_verifier");

        // Protect against NPE errors inside the authentication
        // class.
        if ((oauth_token == null) || (oauth_verifier == null))
        {
                return null;
        }
                
        try
        {
            Context context = AuthenticationUtil.authenticate(objectModel, oauth_token, oauth_verifier, null); // authenticate ja loga o usuario
            EPerson eperson = context.getCurrentUser();
            if(eperson == null){
                context.setIgnoreAuthorization(true);            
                try {
                    eperson = EPerson.create(context);
                } catch (AuthorizeException ex) {
                    java.util.logging.Logger.getLogger(OAuthAuthentication.class.getName()).log(Level.SEVERE, null, ex);
                }
                eperson.setCanLogIn(true);
                eperson.setLanguage("pt_BR");
                eperson.setSelfRegistered(true);
                context.setIgnoreAuthorization(false);
            }
            AuthenticationManager.initEPerson(context, request, eperson); // cria ou atualiza usuario
            // eperson = EPerson.findByNetid(context, (String) request.getSession().getAttribute("usp_bdpi_oauth_loginUsuario"));
            AuthenticationUtil.logIn(objectModel, eperson);
            /*
            System.out.println("eperson ID: ".concat(Integer.toString(eperson.getID())));
            System.out.println("AUTHENTICATED_USER_ID: ".concat((String) request.getSession().getAttribute("AUTHENTICATED_USER_ID")) );
            System.out.println("EFFECTIVE_USER_ID".concat((String) request.getSession().getAttribute("EFFECTIVE_USER_ID")));
            */
            /*
            session.setAttribute(EFFECTIVE_USER_ID, eperson.getID());
            session.setAttribute(AUTHENTICATED_USER_ID,eperson.getID());
             */
            
            // The user has successfully logged in
            String redirectURL = request.getContextPath();
            if (AuthenticationUtil.isInterupptedRequest(objectModel))
            {
                    // Resume the request and set the redirect target URL to
                    // that of the originally interrupted request.
                    redirectURL += AuthenticationUtil.resumeInterruptedRequest(objectModel);
            }
            else
            {
                    // Otherwise direct the user to the specified 'loginredirect' page (or homepage by default)
                    String loginRedirect = ConfigurationManager.getProperty("xmlui.user.loginredirect");
                    redirectURL += (loginRedirect != null) ? loginRedirect.trim() : "/";	
            }
            // Authentication successful send a redirect.
            final HttpServletResponse httpResponse = (HttpServletResponse) objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
            httpResponse.sendRedirect(redirectURL);

            // log the user out for the rest of this current request, however they will be reauthenticated
            // fully when they come back from the redirect. This prevents caching problems where part of the
            // request is performed before the user was authenticated and the other half after it succeeded. This
            // way the user is fully authenticated from the start of the request.
            context.setCurrentUser(null);
            return new HashMap();
        }
        catch (SQLException sqle)
        {
            throw new PatternException("Unable to perform authentication",
                    sqle);
        }

    }

}
