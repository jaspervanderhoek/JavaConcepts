// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package logging.actions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONObject;
import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.core.objectmanagement.member.MendixAutoNumber;
import com.mendix.core.objectmanagement.member.MendixObjectReference;
import com.mendix.core.objectmanagement.member.MendixObjectReferenceSet;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IMendixObjectMember;
import com.mendix.webui.CustomJavaAction;

public class MendixObjToString extends CustomJavaAction<java.lang.String>
{
	private IMendixObject ObjectToPrint;
	private logging.proxies.OutputType OutputTypeValue;
	private java.lang.String DateFormat;
	private java.lang.Boolean WithAssociations;

	public MendixObjToString(IContext context, IMendixObject ObjectToPrint, java.lang.String OutputTypeValue, java.lang.String DateFormat, java.lang.Boolean WithAssociations)
	{
		super(context);
		this.ObjectToPrint = ObjectToPrint;
		this.OutputTypeValue = OutputTypeValue == null ? null : logging.proxies.OutputType.valueOf(OutputTypeValue);
		this.DateFormat = DateFormat;
		this.WithAssociations = WithAssociations;
	}

	@java.lang.Override
	public java.lang.String executeAction() throws Exception
	{
		// BEGIN USER CODE

		return objectToString(getContext(), this.ObjectToPrint,
				this.WithAssociations, DateFormat, OutputTypeValue, true);
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 * @return a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "MendixObjToString";
	}

	// BEGIN EXTRA CODE

	private static String objectToString(IContext c, IMendixObject objectToPrint, Boolean withAssociations, String dateFormat, logging.proxies.OutputType outputType, boolean rootCall) {
		StringBuilder sb = new StringBuilder();
		StringBuilder endTags = new StringBuilder();
		Map<String, ? extends IMendixObjectMember<?>> members = objectToPrint.getMembers(c);

		Set<String> memberSet = members.keySet();
		List<String> sorted = asSortedList(memberSet);
		JSONObject jsonObj = new JSONObject();
		String tagName = null;
		// addFields(members.)
		int i = 0;
		for (String key : sorted) {
			IMendixObjectMember<?> m = members.get(key);
			if (m.isVirtual())
				continue;
			if (m instanceof MendixAutoNumber)
				continue;
			if (withAssociations
				|| ((!(m instanceof MendixObjectReference)
				&& !(m instanceof MendixObjectReferenceSet) && !(m instanceof MendixAutoNumber)))) {
				
				if (i == 0) {
					// Start Tag
					startTag(objectToPrint.getType(), sb, outputType, endTags, jsonObj);
					// Name Name is stored only for the Json
					tagName = objectToPrint.getType();
				}
			}
			if (m.getValue(c) == null) {
				addFields(key, "NULL", sb, outputType, jsonObj);
			} else if (withAssociations && m instanceof MendixObjectReference) {
				// Only a Root Caller can make a call to itself to prevent an
				// infinite loop
				if (rootCall) {
					MendixObjectReference mr = (MendixObjectReference) m;
					IMendixIdentifier referencedObjectID = objectToPrint.getValue(c, mr.getName());
					try {
						IMendixObject referencedObject = Core.retrieveId(c, referencedObjectID);
						sb.append(objectToString(c, referencedObject, withAssociations, dateFormat, outputType, false));
					} catch (CoreException e) {
						addFields(key, "NULL", sb, outputType, jsonObj);
					}
				}
			} else {
				Object value = m.getValue(c);
				if (value instanceof Date && dateFormat != null
						&& !dateFormat.equals("")) {
					SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
					addFields(key, sdf.format(m.getValue(c)), sb, outputType, jsonObj);
				} else
					addFields(key, m.getValue(c).toString(), sb, outputType, jsonObj);
			}
			i++;
		}
		if (outputType.getCaption().equals("Json")) {
			jsonObj.put(tagName, endTags);
		} else {
			sb.append(endTags.toString());
		}
		return sb.toString();
	}

	public static int randBetween(int start, int end) {
		return start + (int) Math.round(Math.random() * (end - start));
	}

	private static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}

	private static void addFields(String columnName, String value, StringBuilder sb, logging.proxies.OutputType outputType, JSONObject jsonObj) {
		if (outputType.getCaption().equals("String")) {
			sb.append(" ");
			sb.append(columnName);
			sb.append(" ");
			sb.append("[");
			sb.append(value);
			sb.append("]");
		} else if (outputType.getCaption().equals("XML")) {
			sb.append("<");
			sb.append(columnName);
			sb.append(">");
			sb.append(value);
			sb.append("</");
			sb.append(columnName);
			sb.append(">");
		} else if (outputType.getCaption().equals("Json")) {
			jsonObj.put(columnName, value);
		}
	}

	private static void startTag(String columnName, StringBuilder sb, logging.proxies.OutputType outputType, StringBuilder endTags, JSONObject jsonObj) {
		if (outputType.getCaption().equals("String")) {
			sb.append(" ");
			sb.append(columnName);
			sb.append(" ");
		} else if (outputType.getCaption().equals("XML")) {
			sb.append("<");
			sb.append(columnName);
			sb.append(">");
			endTags.append("</");
			endTags.append(columnName);
			endTags.append(">");
		} else if (outputType.getCaption().equals("Json")) {
			// jsonObj.put(columnName, value);
		}
	}
	// END EXTRA CODE
}
