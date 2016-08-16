package com.bigstep.hadoop.datalake;

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. See accompanying LICENSE file.
 */

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.security.authentication.client.*;
import org.ietf.jgss.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * The {@link org.apache.hadoop.security.authentication.client.KerberosAuthenticator} implements the Kerberos SPNEGO authentication sequence.
 * <p>
 * It uses the default principal for the Kerberos cache (normally set via kinit).
 * <p>
 * It falls back to the {@link PseudoAuthenticator} if the HTTP endpoint does not trigger an SPNEGO authentication
 * sequence.
 */
public class KerberosIdentityAuthenticator implements Authenticator {

    /**
     * HTTP header used by the SPNEGO server endpoint during an authentication sequence.
     */
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    /**
     * HTTP header used by the SPNEGO client endpoint during an authentication sequence.
     */
    public static final String AUTHORIZATION = "Authorization";
    /**
     * HTTP header prefix used by the SPNEGO client/server endpoints during an authentication sequence.
     */
    public static final String NEGOTIATE = "Negotiate";
    private static final String AUTH_HTTP_METHOD = "OPTIONS";
    private static Logger LOG = LoggerFactory.getLogger(
            KerberosIdentityAuthenticator.class);
    private URL url;
    private HttpURLConnection conn;
    private Base64 base64;
    private ConnectionConfigurator connConfigurator;
    private KerberosIdentity kerberosIdentity;

    public KerberosIdentityAuthenticator(KerberosIdentity identity) {
        this.kerberosIdentity = identity;
    }

    /**
     * Sets a {@link ConnectionConfigurator} instance to use for
     * configuring connections.
     *
     * @param configurator the {@link ConnectionConfigurator} instance.
     */
    @Override
    public void setConnectionConfigurator(ConnectionConfigurator configurator) {
        connConfigurator = configurator;
    }

    /**
     * Performs SPNEGO authentication against the specified URL.
     * <p>
     * If a token is given it does a NOP and returns the given token.
     * <p>
     * If no token is given, it will perform the SPNEGO authentication sequence using an
     * HTTP <code>OPTIONS</code> request.
     *
     * @param url   the URl to authenticate against.
     * @param token the authentication token being used for the user.
     * @throws IOException             if an IO error occurred.
     * @throws AuthenticationException if an authentication error occurred.
     */
    @Override
    public void authenticate(URL url, AuthenticatedURL.Token token)
            throws IOException, AuthenticationException {
        if (!token.isSet()) {
            this.url = url;
            base64 = new Base64(0);
            conn = (HttpURLConnection) url.openConnection();
            if (connConfigurator != null) {
                conn = connConfigurator.configure(conn);
            }
            conn.setRequestMethod(AUTH_HTTP_METHOD);
            conn.connect();

            boolean needFallback = false;
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                LOG.debug("JDK performed authentication on our behalf.");
                // If the JDK already did the SPNEGO back-and-forth for
                // us, just pull out the token.
                AuthenticatedURL.extractToken(conn, token);
                if (isTokenKerberos(token)) {
                    return;
                }
                needFallback = true;
            }
            if (!needFallback && isNegotiate()) {
                LOG.debug("Performing our own SPNEGO sequence.");
                doSpnegoSequence(token);
            } else {
                LOG.debug("Using fallback authenticator sequence.");
                Authenticator auth = getFallBackAuthenticator();
                // Make sure that the fall back authenticator have the same
                // ConnectionConfigurator, since the method might be overridden.
                // Otherwise the fall back authenticator might not have the information
                // to make the connection (e.g., SSL certificates)
                auth.setConnectionConfigurator(connConfigurator);
                auth.authenticate(url, token);
            }
        }
    }

    /**
     * If the specified URL does not support SPNEGO authentication, a fallback {@link Authenticator} will be used.
     * <p>
     * This implementation returns a {@link PseudoAuthenticator}.
     *
     * @return the fallback {@link Authenticator}.
     */
    protected Authenticator getFallBackAuthenticator() {
        Authenticator auth = new PseudoAuthenticator();
        if (connConfigurator != null) {
            auth.setConnectionConfigurator(connConfigurator);
        }
        return auth;
    }

    /*
     * Check if the passed token is of type "kerberos" or "kerberos-dt"
     */
    private boolean isTokenKerberos(AuthenticatedURL.Token token)
            throws AuthenticationException {
        if (token.isSet()) {
            AuthToken aToken = AuthToken.parse(token.toString());
            if (aToken.getType().equals("kerberos") ||
                    aToken.getType().equals("kerberos-dt")) {
                return true;
            }
        }
        return false;
    }

    /*
    * Indicates if the response is starting a SPNEGO negotiation.
    */
    private boolean isNegotiate() throws IOException {
        boolean negotiate = false;
        if (conn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            String authHeader = conn.getHeaderField(WWW_AUTHENTICATE);
            negotiate = authHeader != null && authHeader.trim().startsWith(NEGOTIATE);
        }
        return negotiate;
    }

    /**
     * Implements the SPNEGO authentication sequence interaction using the current default principal
     * in the Kerberos cache (normally set via kinit).
     *
     * @param atoken the authentication token being used for the user.
     * @throws IOException             if an IO error occurred.
     * @throws AuthenticationException if an authentication error occurred.
     */
    private void doSpnegoSequence(AuthenticatedURL.Token atoken) throws IOException, AuthenticationException {
        try {

            kerberosIdentity.doAsPriviledged(new PrivilegedExceptionAction<Void>() {

                @Override
                public Void run() throws Exception {
                    final Oid KERB_V5_OID = new Oid("1.2.840.113554.1.2.2");


                    GSSContext gssContext = null;
                    try {
                        GSSManager gssManager = GSSManager.getInstance();


                        final GSSName clientName = gssManager.createName(kerberosIdentity.getPrincipalName(), GSSName.NT_USER_NAME);
                        LOG.info("doSpnegoSequence() using principal:" + kerberosIdentity.getPrincipalName());
                        final GSSCredential clientCred = gssManager.createCredential(clientName,
                                8 * 3600,
                                KERB_V5_OID,
                                GSSCredential.INITIATE_ONLY);


                        final String applicationPrincipal = "HTTP@" + kerberosIdentity.getRealm();

                        final GSSName serverName = gssManager.createName(applicationPrincipal, GSSName.NT_HOSTBASED_SERVICE);

                        gssContext = gssManager.createContext(serverName,
                                KERB_V5_OID,
                                clientCred,
                                GSSContext.DEFAULT_LIFETIME);

                        gssContext.requestCredDeleg(true);
                        gssContext.requestMutualAuth(true);
                        gssContext.requestConf(false);
                        gssContext.requestInteg(true);

                        byte[] inToken = new byte[0];
                        byte[] outToken;
                        boolean established = false;

                        // Loop while the context is still not established
                        while (!established) {
                            LOG.info("doSpnegoSequence() using token:" + new BASE64Encoder().encode(inToken));
                            outToken = gssContext.initSecContext(inToken, 0, 0);
                            LOG.info("initSecContext() out token:" + new BASE64Encoder().encode(outToken));
                            if (outToken != null) {
                                sendToken(outToken);
                            }

                            if (!gssContext.isEstablished()) {
                                inToken = readToken();
                            } else {
                                established = true;
                            }
                        }
                    } finally {
                        if (gssContext != null) {
                            gssContext.dispose();
                            gssContext = null;
                        }
                    }
                    return null;
                }
            });
        } catch (PrivilegedActionException ex) {
            throw new AuthenticationException(ex.getException());
        }
        AuthenticatedURL.extractToken(conn, atoken);
    }

    /*
    * Sends the Kerberos token to the server.
    */
    private void sendToken(byte[] outToken) throws IOException {
        String token = base64.encodeToString(outToken);
        conn = (HttpURLConnection) url.openConnection();
        if (connConfigurator != null) {
            conn = connConfigurator.configure(conn);
        }
        conn.setRequestMethod(AUTH_HTTP_METHOD);
        conn.setRequestProperty(AUTHORIZATION, NEGOTIATE + " " + token);
        conn.connect();
    }

    /*
    * Retrieves the Kerberos token returned by the server.
    */
    private byte[] readToken() throws IOException, AuthenticationException {
        int status = conn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK || status == HttpURLConnection.HTTP_UNAUTHORIZED) {
            String authHeader = conn.getHeaderField(WWW_AUTHENTICATE);
            if (authHeader == null || !authHeader.trim().startsWith(NEGOTIATE)) {
                throw new AuthenticationException("Invalid SPNEGO sequence, '" + WWW_AUTHENTICATE +
                        "' header incorrect: " + authHeader);
            }
            String negotiation = authHeader.trim().substring((NEGOTIATE + " ").length()).trim();
            return base64.decode(negotiation);
        }
        throw new AuthenticationException("Invalid SPNEGO sequence, status code: " + status);
    }

}
