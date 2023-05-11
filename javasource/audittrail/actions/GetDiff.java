// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package audittrail.actions;

import java.util.LinkedList;
import audittrail.log.diff_match_patch;
import audittrail.log.diff_match_patch.Diff;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.webui.CustomJavaAction;

public class GetDiff extends CustomJavaAction<java.lang.String>
{
	private java.lang.String OldText;
	private java.lang.String NewText;

	public GetDiff(IContext context, java.lang.String OldText, java.lang.String NewText)
	{
		super(context);
		this.OldText = OldText;
		this.NewText = NewText;
	}

	@java.lang.Override
	public java.lang.String executeAction() throws Exception
	{
		// BEGIN USER CODE
		String text = "";
		diff_match_patch dmp = new diff_match_patch();
		LinkedList<Diff> lst =  dmp.diff_main(this.OldText, this.NewText);
		dmp.diff_cleanupSemantic( lst );
		text = dmp.diff_prettyHtml( lst );
		
		return text;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 * @return a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "GetDiff";
	}

	// BEGIN EXTRA CODE
	// END EXTRA CODE
}
