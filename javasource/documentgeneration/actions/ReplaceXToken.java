// This file was generated by Mendix Modeler.
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

/**
 * Search the parameter text for the token fromt the parameter TokenObject, replace the value with a value from the parameter ValueObject.
 */
public class ReplaceXToken extends CustomJavaAction<java.lang.String>
{
	private IMendixObject __TokenObject;
	private documentgeneration.proxies.XToken TokenObject;
	private IMendixObject ValueObject;
	private java.lang.String Text;

	public ReplaceXToken(IContext context, IMendixObject TokenObject, IMendixObject ValueObject, java.lang.String Text)
	{
		super(context);
		this.__TokenObject = TokenObject;
		this.ValueObject = ValueObject;
		this.Text = Text;
	}

	@Override
	public java.lang.String executeAction() throws Exception
	{
		this.TokenObject = __TokenObject == null ? null : documentgeneration.proxies.XToken.initialize(getContext(), __TokenObject);

		// BEGIN USER CODE
		return XTokenReplacer.replaceToken(this.getContext(), this.Text, this.__TokenObject, this.ValueObject);
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@Override
	public java.lang.String toString()
	{
		return "ReplaceXToken";
	}

	// BEGIN EXTRA CODE
	// END EXTRA CODE
}