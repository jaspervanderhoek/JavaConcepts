// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package versiondisplay.actions;

import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.webui.CustomJavaAction;

public class MendixVersion extends CustomJavaAction<java.lang.String>
{
	public MendixVersion(IContext context)
	{
		super(context);
	}

	@java.lang.Override
	public java.lang.String executeAction() throws Exception
	{
		// BEGIN USER CODE
//        JSONParser parser = new JSONParser();
//        URL resource = this.getClass().getClassLoader().getResource("translations.properties");
//        String path = resource.getPath().replace("lib/i18n/translations.properties", "metadata.json").replaceAll("%20", " ");
//
//        org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject)parser.parse(new FileReader(path));
//
//        JSONObject jobj = new JSONObject(jsonObject);
//
//        Core.getConfiguration().RUNTIME_VERSION gmetadata = new Metadata(jobj);

//        return metadata.getRuntimeVersion();

		return "";
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 * @return a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "MendixVersion";
	}

	// BEGIN EXTRA CODE
	// END EXTRA CODE
}
