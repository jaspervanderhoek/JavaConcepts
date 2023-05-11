// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package documentgeneration.actions;

import mxmodelreflection.TokenReplacer;
import com.mendix.systemwideinterfaces.core.UserAction;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import documentgeneration.XTokenReplacer;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.webui.CustomJavaAction;

public class CheckIfReferncedObjectExists_XToken extends CustomJavaAction<java.lang.Boolean>
{
	private IMendixObject __TokenObject;
	private documentgeneration.proxies.XToken TokenObject;
	private IMendixObject ValueObject;

	public CheckIfReferncedObjectExists_XToken(IContext context, IMendixObject TokenObject, IMendixObject ValueObject)
	{
		super(context);
		this.__TokenObject = TokenObject;
		this.ValueObject = ValueObject;
	}

	@java.lang.Override
	public java.lang.Boolean executeAction() throws Exception
	{
		this.TokenObject = this.__TokenObject == null ? null : documentgeneration.proxies.XToken.initialize(getContext(), __TokenObject);

		// BEGIN USER CODE
		try
		{
		return XTokenReplacer.checkAssociation(this.getContext(),  this.__TokenObject, this.ValueObject);
		}
		catch (Exception e)
		{
			return false;
		}
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 * @return a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "CheckIfReferncedObjectExists_XToken";
	}

	// BEGIN EXTRA CODE
	// END EXTRA CODE
}
