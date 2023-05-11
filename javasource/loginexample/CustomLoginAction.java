package loginexample;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.core.AuthenticationRuntimeException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.ISession;
import com.mendix.systemwideinterfaces.core.IUser;
import com.mendix.systemwideinterfaces.core.UserAction;


public class CustomLoginAction extends UserAction<ISession> {
    private final String username;
    private final String password;

    //These parameters should all be present in the params hashmap.
    public final static String SESSION_MANAGER_PARAM = "sessionManager";
    public final static String USER_NAME_PARAM = "userName";
    public final static String PASSWORD_PARAM = "password";
    public final static String LOCALE_PARAM = "locale";
    public final static String CURRENT_SESSION_ID_PARAM = "currentSessionId";

    private ReentrantLock lock = new ReentrantLock();

    public CustomLoginAction(IContext context, Map<String, ? extends Object> params) {
    	super(context);
        this.username = (String) params.get(USER_NAME_PARAM);
        this.password = (String) params.get(PASSWORD_PARAM);
    }

    @Override
    public ISession executeAction() throws Exception {
        try {
            if (this.username == null || this.username.isEmpty() || this.password == null || this.password.isEmpty())
                throw new AuthenticationRuntimeException("CustomLogin FAILED: empty usernames or passwords are not allowed: " + this.username);

            IContext context = Core.createSystemContext();
            Boolean loginAllowed = false;
				/*
				 * TODO Implement the custom login action, with a microflow call, webservice,etc..
				 */

            loginAllowed = Core.microflowCall("VWAN_NSM.WebServices.Login.Login")
            	.withParam("username", this.username)
            	.withParam("password", this.password)
            	.execute(context);


            if( loginAllowed != null && loginAllowed == true ) {
                ISession session = null;
                IUser user = Core.getUser(context, this.username);
                Set<String> roles = user.getUserRoleNames();

                if ( user.isActive() &&
                    !user.isBlocked() &&
                    ( roles != null && roles.size() > 0) ) {
                this.lock.lock();
                try {
                    session = Core.initializeSession(user, null);
                }
                finally {
                    this.lock.unlock();
                }
                
                //Add this code to your action
//                IContext sessionContext = session.createContext();
//                IMendixObject sessionObj = Core.instantiate(sessionContext, yourModule.proxies.Session.getType());
//                sessionObj.setValue(sessionContext, yourModule.proxies.Session.MemberNames.SessionInfo.toString(), yourSessionInfo);
//                sessionObj.setValue(sessionContext, yourModule.proxies.Session.MemberNames.Session_User.toString(), user.getMendixObject().getId());
//                
//                session.retain(sessionObj);
                    
                }
                else
                    throw new AuthenticationRuntimeException("CustomLogin FAILED: user " + this.username + " is inactive or blocked or has no userroles");

                if (session == null)
                    throw new AuthenticationRuntimeException("CustomLogin FAILED: unable to initialize the session for user " + this.username);

                return session;
            }

            throw new AuthenticationRuntimeException("CustomLogin FAILED for user " + this.username);
        }
        catch (AuthenticationRuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            // If the number of sessions has exceeded we just want to rethrow the original exception
            if (e.getMessage() != null && e.getMessage().contains("sessions exceeded"))
                throw e;
            throw new AuthenticationRuntimeException("CustomLogin FAILED: error while checking login: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "[CustomLoginAction:: username: " + this.username + " password: " + "]";
    }

}