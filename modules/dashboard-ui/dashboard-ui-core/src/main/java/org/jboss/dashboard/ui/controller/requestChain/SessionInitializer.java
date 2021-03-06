/**
 * Copyright (C) 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.dashboard.ui.controller.requestChain;

import org.jboss.dashboard.LocaleManager;
import org.jboss.dashboard.ui.controller.ControllerException;
import org.jboss.dashboard.ui.controller.ControllerListener;
import org.jboss.dashboard.ui.controller.responses.RedirectToURLResponse;
import org.jboss.dashboard.ui.controller.responses.ShowScreenResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.util.StringTokenizer;

public class SessionInitializer extends RequestChainProcessor {

    private static transient Log log = LogFactory.getLog(SessionInitializer.class.getName());

    /**
     * Attributes to store in session
     */
    private final static String SESSION_ATTRIBUTE_INITIALIZED = "controller.initialized";

    /**
     * Attributes to store in session
     */
    private final static String SESSION_ATTRIBUTE_BIND_LISTENER = "controller.bind.listener";

    private ControllerListener[] listeners;
    private String expiredUrl = "/expired.jsp";
    private boolean performExpiredRecovery = true;
    private NavigationCookieProcessor navigationCookieProcessor;

    public NavigationCookieProcessor getNavigationCookieProcessor() {
        return navigationCookieProcessor;
    }

    public void setNavigationCookieProcessor(NavigationCookieProcessor navigationCookieProcessor) {
        this.navigationCookieProcessor = navigationCookieProcessor;
    }

    public boolean isPerformExpiredRecovery() {
        return performExpiredRecovery;
    }

    public void setPerformExpiredRecovery(boolean performExpiredRecovery) {
        this.performExpiredRecovery = performExpiredRecovery;
    }

    public String getExpiredUrl() {
        return expiredUrl;
    }

    public void setExpiredUrl(String expiredUrl) {
        this.expiredUrl = expiredUrl;
    }

    public ControllerListener[] getListeners() {
        return listeners;
    }

    public void setListeners(ControllerListener[] listeners) {
        this.listeners = listeners;
    }

    /**
     * Make required processing of request.
     *
     * @return true if processing must continue, false otherwise.
     */
    protected boolean processRequest() throws Exception {
        // Retrieve session
        HttpSession session = getRequest().getSession(true);
        boolean isNewSession = !"true".equals(session.getAttribute(SESSION_ATTRIBUTE_INITIALIZED));

        if (isNewSession) initSession();

        // Check session expiration
        if (getRequest().getRequestedSessionId() != null && !getRequest().getRequestedSessionId().equals(session.getId())) {
            return handleExpiration();
        }

        // Verify session integrity
        if (!verifySession(session)) {
            throw new ControllerException("Session verification failed.");
        }

        return true;
    }

    /**
     * Called when a new session is created. Its default behaviour is notifying
     * the event to all the listeners registered.
     */
    protected void initSession() {
        log.debug("New session created. Firing event");
        for (int i = 0; i < listeners.length; i++) {
            ControllerListener listener = listeners[i];
            listener.initSession(getRequest(), getResponse());
        }
        // Store a HttpBindingListener object to detect session expiration
        getRequest().getSession().setAttribute(SESSION_ATTRIBUTE_BIND_LISTENER, new HttpSessionBindingListener() {
            public void valueBound(HttpSessionBindingEvent httpSessionBindingEvent) {
            }

            public void valueUnbound(HttpSessionBindingEvent httpSessionBindingEvent) {
                for (int i = 0; i < listeners.length; i++) {
                    ControllerListener listener = listeners[i];
                    listener.expireSession(httpSessionBindingEvent.getSession());
                }
            }
        });
        getRequest().getSession().setAttribute(SESSION_ATTRIBUTE_INITIALIZED, "true");
    }

    /**
     * Check that current session has all the required parameters, and issue warnings if not.
     */
    protected boolean verifySession(HttpSession session) {
        boolean error = false;
        Object initialized = session.getAttribute(SESSION_ATTRIBUTE_INITIALIZED);
        if (!"true".equals(initialized)) {
            log.error("Current session seems to be not initialized.");
            error = true;
        }
        return !error;
    }

    /**
     * Handles expiration of session
     *
     * @return false to halt processing
     */
    protected boolean handleExpiration() {
        log.debug("Session expiration detected.");
        if (isPerformExpiredRecovery()) {
            //Forward to the same uri, ignoring the request parameters
            handleExpirationRecovery();
        } else {
            if (expiredUrl != null) {
                getControllerStatus().setResponse(new ShowScreenResponse(expiredUrl));
            } else {
                try {
                    getResponse().sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                } catch (java.io.IOException e) {
                    log.error("I can't handle so many errors in a nice way", e);
                }
            }
        }
        getControllerStatus().consumeURIPart(getControllerStatus().getURIToBeConsumed());
        return false;
    }

    protected void handleExpirationRecovery() {
        getControllerStatus().setResponse(new RedirectToURLResponse(getExpirationRecoveryURL()));
    }

    protected String getExpirationRecoveryURL() {
        //Parse cookie
        String[] cookieValues = getParsedNavigationCookieValues(getNavigationCookieValue());
        String defaultUrl = StringUtils.defaultString(getRequest().getRequestURI());

        if (cookieValues == null) { // No cookie => Nothing to do!
            return defaultUrl;
        }

        String lang = cookieValues[0];
        String sectionURL = String.valueOf(Long.parseLong(cookieValues[1], getNavigationCookieProcessor().getIdsRadix()));
        String workspaceURL = cookieValues[2];

        // Set the lang
        LocaleManager.lookup().setCurrentLang(lang);

        String contextPath = StringUtils.defaultString(getRequest().getContextPath());
        while (contextPath.endsWith("/"))
            contextPath = contextPath.substring(0, contextPath.length() - 1);

        if (!defaultUrl.startsWith(contextPath + FriendlyUrlProcessor.FRIENDLY_MAPPING)) {
            // URL is not friendly, use cookie to construct it!
            defaultUrl = contextPath + FriendlyUrlProcessor.FRIENDLY_MAPPING + "/" + workspaceURL + "/" + sectionURL;
        }

        return defaultUrl + "?" + getRequest().getQueryString();
    }

    protected String getNavigationCookieValue() {
        Cookie[] cookies = getRequest().getCookies();
        if (cookies != null)
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                if (getNavigationCookieProcessor().getCookieName().equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        return null;
    }

    protected String[] getParsedNavigationCookieValues(String cookieValue) {
        if (cookieValue == null)
            return null;
        String[] s = new String[3];
        int index = 0;
        StringTokenizer strtk = new StringTokenizer(cookieValue, getNavigationCookieProcessor().getCookieSeparator());
        while (strtk.hasMoreTokens() && index < s.length) {
            s[index++] = strtk.nextToken();
        }
        if (index != 3) //Incorrect cookie value
            return null;
        return s;
    }
}
