package loginexample;

import com.mendix.core.action.user.LoginAction;
import com.mendix.systemwideinterfaces.core.UserActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: pbosse
 * Date: 08/11/13
 * Time: 10:10 AM
 * Copyright Ciena
 */
public class CustomLoginActionListener extends UserActionListener<LoginAction> {

    public CustomLoginActionListener() {
        super(LoginAction.class);
    }

    /**
     * This action validates if the custom login action should be executed, when the result is true
     * the custom login action will be executed
     */
    @Override
    public boolean check(LoginAction action) {
        if (action == null)
            throw new IllegalArgumentException("Action should not be null");

        return true;
    }

}
