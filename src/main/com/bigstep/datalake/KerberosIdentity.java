package com.bigstep.datalake;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.security.Credentials;
import org.apache.hadoop.security.authentication.util.KerberosUtil;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.security.token.TokenIdentifier;

import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.hadoop.util.PlatformName.IBM_JAVA;

/**
 * Created by alexandrubordei on 24/07/2016.
 * This is an UserGroupInformation alternative that can only handle Kerberos with keytab and  principal name.
 * However it is not static and allows various combinations between secure and insecure clusters.
 */
public class KerberosIdentity {

    public static final Log LOG = LogFactory.getLog(KerberosIdentity.class);

    private final long DEFAULT_RELOGIN_TIME = 1000 * 60 * 60 * 5;
    private long reloginTime;

    private Subject subject;

    private String kerberosPrincipal;
    private String kerberosKeytab;
    private String kerberosRealm;

    private Date lastLogin;

    public KerberosIdentity(long reloginTime) {
        this.reloginTime = reloginTime;
    }

    public KerberosIdentity() {
        this.reloginTime = DEFAULT_RELOGIN_TIME;
    }

    public Subject getSubject() {
        return subject;
    }

    public String getRealm() {
        return kerberosRealm;
    }


    public void login(String kerberosPrincipal, String kerberosKeytab, String kerberosRealm) throws IOException {

        File kerberosKeytabFile = new File(kerberosKeytab);

        if (!kerberosKeytabFile.exists())
            throw new IOException("Kerberos keytab file " + kerberosKeytab + " not found");

        if (!kerberosKeytabFile.canRead())
            throw new IOException("Kerberos keytab file " + kerberosKeytab + " cannot be accessed");

        try {

            KerberosConfiguration loginContextConfiguration = new KerberosConfiguration(
                    kerberosKeytab, kerberosPrincipal, kerberosRealm);

            LoginContext lc = new LoginContext("", null, null, loginContextConfiguration);
            LOG.debug("Attempting login as " + kerberosPrincipal + " using " + kerberosKeytab);

            lc.login();

            subject = lc.getSubject();

            lastLogin = new Date();

        } catch (LoginException le) {
            throw new IOException(le);
        }
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public boolean isLoginNecessary() {
        long lastLoginTime = getLastLogin().getTime();
        long currentTime = new Date().getTime();
        return (currentTime - lastLoginTime) > reloginTime;
    }

    public void relogin() throws IOException {
        login(kerberosPrincipal, kerberosKeytab, kerberosRealm);
    }

    public void reloginIfNecessary() throws IOException {
        if (isLoginNecessary())
            relogin();
    }


    public <T> T doAsPrivileged(PrivilegedAction<T> action) {
        logPrivilegedAction(subject, action);
        return Subject.doAsPrivileged(subject, action, null);
    }

    public <T> T doAsPriviledged(PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        logPrivilegedAction(subject, action);
        return Subject.doAsPrivileged(subject, action, null);
    }

    public <T> T doAs(PrivilegedAction<T> action) {
        logPrivilegedAction(subject, action);
        return Subject.doAs(subject, action);
    }

    public <T> T doAs(PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        logPrivilegedAction(subject, action);
        return Subject.doAs(subject, action);
    }

    private void logPrivilegedAction(Subject subject, Object action) {
        if (LOG.isDebugEnabled()) {
            // would be nice if action included a descriptive toString()
            String where = new Throwable().getStackTrace()[2].toString();
            LOG.debug("PrivilegedAction as:" + this + " from:" + where);
        }
    }

    public Principal getPrincipal() {
        Set<Principal> principals = subject.getPrincipals();
        if (principals == null)
            return null;

        Iterator iter = principals.iterator();

        if (iter.hasNext())
            return ((Principal) iter.next());

        return null;
    }

    public String getPrincipalShortName() {
        Principal principal = getPrincipal();
        String fullUserName = principal.getName();

        Pattern r = Pattern.compile("(.*)(\\/(.*))?\\@(.*)");
        Matcher m = r.matcher(fullUserName);
        if (m.find())
            return m.group(1);

        return null;
    }

    public String getPrincipalName() {
        Principal principal = getPrincipal();
        return principal.getName();
    }


    private synchronized Credentials getCredentialsInternal() {
        final Credentials credentials;
        final Set<Credentials> credentialsSet =
                subject.getPrivateCredentials(Credentials.class);
        if (!credentialsSet.isEmpty()) {
            credentials = credentialsSet.iterator().next();
        } else {
            credentials = new Credentials();
            subject.getPrivateCredentials().add(credentials);
        }
        return credentials;
    }

    public Collection<Token<? extends TokenIdentifier>> getTokens() {
        synchronized (subject) {
            return Collections.unmodifiableCollection(
                    new ArrayList<Token<?>>(getCredentialsInternal().getAllTokens()));
        }
    }

    @Override
    public String toString() {
        return "KerberosIdentity (" + getPrincipalName() + "[" + getPrincipalShortName() + "])";
    }

    private static class KerberosConfiguration extends javax.security.auth.login.Configuration {
        private String keytab;
        private String principal;
        private String realm;

        public KerberosConfiguration(String keytab, String principal, String realm) {
            this.keytab = keytab;
            this.principal = principal;
            this.realm = realm;
        }

        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            Map<String, String> options = new HashMap<String, String>();
            if (IBM_JAVA) {
                options.put("useKeytab",
                        keytab.startsWith("file://") ? keytab : "file://" + keytab);
                options.put("principal", principal);
                options.put("credsType", "acceptor");
            } else {
                options.put("keyTab", keytab);
                options.put("realm", realm);
                options.put("principal", principal);
                options.put("useKeyTab", "true");
                options.put("storeKey", "true");
                options.put("doNotPrompt", "true");
                options.put("useTicketCache", "false");
                options.put("renewTGT", "false");
                options.put("isInitiator", "true");
                if (LOG.isDebugEnabled())
                    options.put("debug", "true");

            }
            options.put("refreshKrb5Config", "true");

            return new AppConfigurationEntry[]{
                    new AppConfigurationEntry(KerberosUtil.getKrb5LoginModuleName(),
                            AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                            options),};
        }
    }
}
