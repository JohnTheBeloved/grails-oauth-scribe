package uk.co.desirableobjects.oauth.scribe

import java.util.concurrent.TimeUnit

import org.scribe.model.OAuthRequest
import org.scribe.model.Response
import org.scribe.model.Token
import org.scribe.model.Verb
import org.scribe.oauth.OAuthService
import org.scribe.model.OAuthConstants

import uk.co.desirableobjects.oauth.scribe.resource.ResourceAccessor

class OauthResourceService {

    static transactional = false

    Response accessResource(OAuthService service, Token accessToken, ResourceAccessor ra) {

        OAuthRequest req = buildOauthRequest(ra.verb, ra.url, ra.connectTimeout, ra.receiveTimeout)
         
        if (ra.payload) {
          req.addPayload(ra.payload)
        } 
        ra.headers.each { String name, String value ->
            req.addHeader(name, value)
        }
        ra.bodyParameters?.each {String k, String v->
            req.addBodyParameter(k, v)
        }
	    ra.querystringParams?.each {String name, String value ->
		    req.addQuerystringParameter(name, value)
	    }
        return signAndSend(service, accessToken, req)
        //here

    }

    private OAuthRequest buildOauthRequest(Verb verb, String url, int connectTimeout, int receiveTimeout) {

        OAuthRequest req = new OAuthRequest(verb, url)
        req.setConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
        req.setReadTimeout(receiveTimeout, TimeUnit.MILLISECONDS)
        request.addHeader(OAuthConstants.HEADER, "Basic c21hcnRpbnN1cmU6c2VjcmV0");
         
        return req

    }

    private Response signAndSend(OAuthService service, Token accessToken, OAuthRequest req) {

        request.addHeader(OAuthConstants.HEADER, "Basic c21hcnRpbnN1cmU6c2VjcmV0");
         service.signRequest(accessToken, req)
        return req.send()

    }
}
