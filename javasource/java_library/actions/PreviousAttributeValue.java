// This file was generated by Mendix Modeler.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package java_library.actions;

import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IMendixObjectMember;
import com.mendix.webui.CustomJavaAction;

/**
 * This action will check the object for the attribute, and returns the previous value.
 * 
 * This action can work for any attribute type without only a small Java alteration
 */
public class PreviousAttributeValue extends CustomJavaAction<java.lang.String>
{
	private IMendixObject MyObject;
	private java.lang.String AttributeName;

	public PreviousAttributeValue(IContext context, IMendixObject MyObject, java.lang.String AttributeName)
	{
		super(context);
		this.MyObject = MyObject;
		this.AttributeName = AttributeName;
	}

	@Override
	public java.lang.String executeAction() throws Exception
	{
		// BEGIN USER CODE
		IMendixObjectMember<?> member = this.MyObject.getMember(getContext(), this.AttributeName);
		if( member == null )
			throw new CoreException("No attribute found with name: " + this.AttributeName + " in entity " + this.MyObject.getType() );
		
		return (String) member.getOriginalValue(getContext());
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@Override
	public java.lang.String toString()
	{
		return "PreviousAttributeValue";
	}

	// BEGIN EXTRA CODE
	// END EXTRA CODE
}
