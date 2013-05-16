package org.scribe.builder.api;

import org.scribe.model.*;
import java.util.*;

public class USPdigitalApi extends DefaultApi10a
{
  private static final String AUTHORIZE_URL = "https://labs.uspdigital.usp.br/wsusuario/oauth/authorize?oauth_token=%s";
  private static final String REQUEST_TOKEN_URL = "https://labs.uspdigital.usp.br/wsusuario/oauth/request_token";

  private final Set<String> scopes;

  public USPdigitalApi()
  {
    scopes = Collections.emptySet();
  }

  public USPdigitalApi(Set<String> scopes)
  {
    this.scopes = Collections.unmodifiableSet(scopes);
  }

  @Override
  public String getAccessTokenEndpoint()
  {
    return "https://labs.uspdigital.usp.br/wsusuario/oauth/access_token";
  }

  @Override
  public String getRequestTokenEndpoint()
  {
    return scopes.isEmpty() ? REQUEST_TOKEN_URL : REQUEST_TOKEN_URL + "?scope=" + scopesAsString();
  }

  private String scopesAsString()
  {
    StringBuilder builder = new StringBuilder();
    for(String scope : scopes)
    {
      builder.append("+").append(scope);
    }
    return builder.substring(1);
  }

  @Override
  public String getAuthorizationUrl(Token requestToken)
  {
    return String.format(AUTHORIZE_URL, requestToken.getToken());
  }

  public static USPdigitalApi withScopes(String... scopes)
  {
    Set<String> scopeSet = new HashSet<String>(Arrays.asList(scopes));
    return new USPdigitalApi(scopeSet);
  }
  
}