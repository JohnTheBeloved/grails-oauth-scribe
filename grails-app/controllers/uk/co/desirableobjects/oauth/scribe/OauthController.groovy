package uk.co.desirableobjects.oauth.scribe

import org.scribe.model.Token
import org.scribe.model.Verifier
import uk.co.desirableobjects.oauth.scribe.holder.RedirectHolder
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import uk.co.desirableobjects.oauth.scribe.exception.MissingRequestTokenException

class OauthController {

    private static final Token EMPTY_TOKEN = new Token('', '')

    OauthService oauthService

    def callback() {

        //println params
         

        String providerName = params.provider
        OauthProvider provider = oauthService.findProviderConfiguration(providerName)

        Verifier verifier = extractVerifier(provider, params)

        if (!verifier) {
            redirect(uri: provider.failureUri)
            return
        }

        Token requestToken = provider.oauthVersion == SupportedOauthVersion.TWO ?
            new Token(params?.code, "") :
            (Token) session[oauthService.findSessionKeyForRequestToken(providerName)]
  

        if (!requestToken) {
            throw new MissingRequestTokenException(providerName)
        }
        
        Token accessToken
  
        try {
            accessToken = oauthService.getAccessToken(providerName, requestToken, verifier)
        } catch(OAuthException){
            log.error("Cannot authenticate with oauth")
            log.error OAuthException
            return redirect(uri: provider.failureUri)
        }

        log.error accessToken.properties
        
        session[oauthService.findSessionKeyForAccessToken(providerName)] = accessToken
        //session.removeAttribute(oauthService.findSessionKeyForRequestToken(providerName))
       
        def redirectUrl = params.redirect_uri ?: '/';
        if(session[oauthService.findSessionKeyForAccessToken(providerName)]){
             return redirect(uri: provider.successUri, params: ["redirectUrl": redirectUrl])
        }else{
            return redirect(uri: provider.failureUri, params: ["redirectUrl": redirectUrl])
        }

    }

    private Verifier extractVerifier(OauthProvider provider, GrailsParameterMap params) {

        String verifierKey = determineVerifierKey(provider)

        if (!params[verifierKey]) {
             log.error("Cannot authenticate with oauth: Could not find oauth verifier in ${params}.")
             return null
        }

        String verification = params[verifierKey]
        return new Verifier(verification)

    }

    private String determineVerifierKey(OauthProvider provider) {

        return SupportedOauthVersion.TWO == provider.oauthVersion ? 'code' : 'oauth_verifier'

    }

    def authenticate() {

        String providerName = params.provider
        OauthProvider provider = oauthService.findProviderConfiguration(providerName)

        Token requestToken = EMPTY_TOKEN
        if (provider.oauthVersion == SupportedOauthVersion.ONE) {
            requestToken = provider.service.requestToken
        }

        session[oauthService.findSessionKeyForRequestToken(providerName)] = requestToken
        String url = oauthService.getAuthorizationUrl(providerName, requestToken)

        RedirectHolder.setUri(params.redirectUrl)
        redirect(url: url,params:[redirect_uri:params.redirectUrl])
        return

    }

}
