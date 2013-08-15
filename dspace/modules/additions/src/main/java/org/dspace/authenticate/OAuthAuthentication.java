package org.dspace.authenticate;

import java.sql.SQLException;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import static org.dspace.authenticate.AuthenticationMethod.NO_SUCH_USER;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
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
    
    private int httpRequestHashCode = 0;
    private String strLoginPageURL = "";

    private void unSetRequestToken(HttpServletRequest httprequest) {
        httprequest.getSession().removeAttribute("requesttoken");
    }
    
    private void setRequestToken(HttpServletRequest httprequest) {
        // System.out.println("chamou setRequestToken");
        httprequest.getSession().setAttribute("requesttoken", getOauthservice(httprequest).getRequestToken());
        // System.out.println("olha o token agora: ".concat(((Token) httprequest.getSession().getAttribute("requesttoken")).getToken()));
    }

    private Token getRequestToken(HttpServletRequest httprequest) {
        // System.out.println("chamou getRequestToken");
        if (httprequest.getSession().getAttribute("requesttoken") == null) {
            setRequestToken(httprequest);
        }
        return (Token) httprequest.getSession().getAttribute("requesttoken");
    }
    
    private void destroyOauthService(HttpServletRequest httprequest){
        if (httprequest.getSession().getAttribute("oauthservice") != null) {
            httprequest.getSession().removeAttribute("oauthservice");
        }
    }

    private OAuthService getOauthservice(HttpServletRequest httprequest) {

        if (httprequest.getSession().getAttribute("oauthservice") == null) {
            setOauthService(httprequest);
        }
        return (OAuthService) httprequest.getSession().getAttribute("oauthservice");

    }

    private void setOauthService(HttpServletRequest httprequest) {
        // System.out.println("iniciou setoauthservice");
        httprequest.getSession().setAttribute("oauthservice", new ServiceBuilder()
                .apiKey(API_KEY)
                .apiSecret(API_SECRET)
                .provider(USPdigitalApi.class)
                .build());
        // System.out.println("terminou setoauthservice");
    }

    public boolean canSelfRegister(Context context,
            HttpServletRequest request,
            String username)
            throws SQLException {
        return true;
    }

    public void initEPerson(Context context, HttpServletRequest request,
            EPerson eperson)
            throws SQLException {
        
        // System.out.println("faz init eperson");
    
        /*
            request.getSession().setAttribute("usp_bdpi_oauth_di", jso);
            request.getSession().setAttribute("usp_bdpi_oauth_loginUsuario", jso.getString("loginUsuario"));
            request.getSession().setAttribute("usp_bdpi_oauth_nomeUsuario", jso.getString("nomeUsuario"));
            request.getSession().setAttribute("usp_bdpi_oauth_tipoUsuario", jso.getString("tipoUsuario"));
            request.getSession().setAttribute("usp_bdpi_oauth_emailPrincipalUsuario", jso.getString("emailPrincipalUsuario"));
            request.getSession().setAttribute("usp_bdpi_oauth_emailAlternativoUsuario", jso.getString("emailAlternativoUsuario"));
            request.getSession().setAttribute("usp_bdpi_oauth_emailUspUsuario", jso.getString("emailUspUsuario"));
            request.getSession().setAttribute("usp_bdpi_oauth_numeroTelefoneFormatado", jso.getString("numeroTelefoneFormatado"));
        */
        
        // Need to create new eperson
        // FIXME: TEMPORARILY need to turn off authentication, as usually
        // only site admins can create e-people
        context.setIgnoreAuthorization(true);
        
        eperson.setEmail((String) request.getSession().getAttribute("usp_bdpi_oauth_emailPrincipalUsuario"));
        eperson.setNetid((String) request.getSession().getAttribute("usp_bdpi_oauth_loginUsuario"));
        String[] name = ((String) request.getSession().getAttribute("usp_bdpi_oauth_nomeUsuario")).trim().split("\\s+");
        StringBuilder firstname = new StringBuilder();
        StringBuilder lastname = new StringBuilder();
        for(int stri=0; stri<name.length ; stri++){
            if(stri==0) firstname.append(name[stri]);
            else {
                if(lastname.length()==0) lastname.append(name[stri]);
                else lastname.append(" ").append(name[stri]);
            }
        }
        eperson.setFirstName(firstname.toString());
        eperson.setLastName(lastname.toString());
        eperson.setMetadata("phone", (String) request.getSession().getAttribute("usp_bdpi_oauth_numeroTelefoneFormatado"));
        eperson.setMetadata("uspdigital_email_alternativo", (String) request.getSession().getAttribute("usp_bdpi_oauth_emailAlternativoUsuario"));
        eperson.setMetadata("uspdigital_email_usp", (String) request.getSession().getAttribute("usp_bdpi_oauth_emailUspUsuario"));
        eperson.setMetadata("uspdigital_usuario_tipo", (String) request.getSession().getAttribute("usp_bdpi_oauth_tipoUsuario"));
        
        try {
            eperson.update();
        } catch (AuthorizeException ex) {
            java.util.logging.Logger.getLogger(OAuthAuthentication.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        context.setIgnoreAuthorization(false);
        
    }

    public boolean allowSetPassword(Context context,
            HttpServletRequest request,
            String username)
            throws SQLException {
        return false;
    }

    public boolean isImplicit() {
        return false;
    }

    public int authenticate(Context context,
            String oauth_token,
            String oauth_verifier,
            String realm,
            HttpServletRequest request)
            throws SQLException {
        
        // System.out.println("chamou authenticate");
        
        EPerson eperson = null;

        Token accessToken = getOauthservice(request).getAccessToken(this.getRequestToken(request), new Verifier(oauth_verifier));

        OAuthRequest orequest = new OAuthRequest(Verb.POST, PROTECTED_RESOURCE_URL);

        getOauthservice(request).signRequest(accessToken, orequest);
        Response oresponse = orequest.send();

        try {

            JSONObject jso = new JSONObject(oresponse.getBody());

            /*
            System.out.println("#########retornou######");
            System.out.println(oresponse.getBody());
            System.out.println("#########retornou######");
            */

            if (jso.getString("loginUsuario").length() > 0) {

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
                    eperson = EPerson.findByEmail(context, jso.getString("emailPrincipalUsuario"));
                }
                if(eperson == null){
                    eperson = EPerson.findByEmail(context, jso.getString("emailAlternativoUsuario"));
                }
                if(eperson == null){
                    eperson = EPerson.findByEmail(context, jso.getString("emailUspUsuario"));
                }
                if (eperson == null) {
                    return NO_SUCH_USER;
                } else {
                    context.setCurrentUser(eperson);
                    return SUCCESS; //desta forma o metodo AuthenticationUtil.Authenticate faz o login
                }
            } else {
                return NO_SUCH_USER;
            }

        } catch (JSONException e) {

            log.trace("Failed to authorize looking up EPerson", e);

            return NO_SUCH_USER;

        } catch(SQLException sqle){
            log.trace("Failed to authorize looking up EPerson", sqle);

            return NO_SUCH_USER;
            
        } catch (AuthorizeException ex) {
            java.util.logging.Logger.getLogger(OAuthAuthentication.class.getName()).log(Level.SEVERE, null, ex);
            return NO_SUCH_USER;
        }

    }

    public String loginPageURL(Context context,
            HttpServletRequest request,
            HttpServletResponse response) {

        // System.out.println("pega loginPageURL");

        if (httpRequestHashCode != request.hashCode()) {
            
            // devido a um bug do dspace, o metodo loginPageURL
            // e chamado 3 vezes a cada vez que essa pagina
            // e carregada. O uso do httpRequestHashCode evita
            // que o token seja gerado 3 vezes a cada chamada.
            
            httpRequestHashCode = request.hashCode();
            // this.destroyOauthService(request);
            // this.unSetRequestToken(request);
            this.setRequestToken(request);
            strLoginPageURL = response.encodeRedirectURL(getOauthservice(request).getAuthorizationUrl(this.getRequestToken(request)));
            // request.getSession().invalidate();
        }
        // request.getSession().invalidate();
        // this.unSetRequestToken(request);
        return strLoginPageURL;
    }

    public String loginPageTitle(Context context) {
        
        // System.out.println("pega loginPageTitle");
        
        return "org.dspace.eperson.OAuthAuthentication.title";
    }

    public int[] getSpecialGroups(Context context, HttpServletRequest request) {
        try {
            if (!context.getCurrentUser().getNetid().equals("")) {
                String groupName = ConfigurationManager.getProperty("authentication-oauth", "login.specialgroup");
                if ((groupName != null) && (!groupName.trim().equals(""))) {
                    Group oauthGroup = Group.findByName(context, groupName);
                    if (oauthGroup == null) {
                        // Oops - the group isn't there.
                        /*
                         log.warn(LogManager.getHeader(context,
                         "oauth_specialgroup",
                         "Group defined in login.specialgroup does not exist"));
                         */
                        return new int[0];
                    } else {
                        return new int[]{oauthGroup.getID()};
                    }
                }
            }
        } catch (Exception npe) {
        }
        return new int[0];

    }
}
