package org.atricore.idbus.capabilities.sso.ui.authn;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.request.component.IRequestableComponent;
import org.atricore.idbus.capabilities.sso.ui.internal.SSOWebSession;
import org.atricore.idbus.capabilities.sso.ui.page.LoginPage;

/**
 * @author: sgonzalez@atriocore.com
 * @date: 2/28/13
 */
public class JossoAuthorizationStrategy implements IAuthorizationStrategy {

    public boolean isActionAuthorized(Component component, Action action) {

        // TODO : Implement some authorization framework hook ?!
        return true;
    }

    public <T extends IRequestableComponent> boolean isInstantiationAuthorized(
            Class<T> componentClass)
    {
        // Check if the new Page requires authentication (implements the marker interface)

        if (AuthenticatedWebPage.class.isAssignableFrom(componentClass)) {
            // Is user signed in?
            if (((SSOWebSession) Session.get()).isAuthenticated()) {
                // okay to proceed
                return true;
            }

            // Intercept the request, but remember the target for later.
            // Invoke Component.continueToOriginalDestination() after successful logon to
            // continue with the target remembered.

            // Trigger authentication
            //getRequestCycle().scheduleRequestHandlerAfterCurrent(new RedirectRequestHandler(claimsConsumerUrl));
            // TODO : This will not work, we need to redirect to IdP
            throw new RestartResponseAtInterceptPageException(JossoLoginPage.class);
        }

        // okay to proceed
        return true;
    }
}
