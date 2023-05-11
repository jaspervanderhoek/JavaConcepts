// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package java_library.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.webui.CustomJavaAction;
import system.proxies.FileDocument;

public class CreateZipFile extends CustomJavaAction<java.lang.Boolean>
{
	private java.util.List<IMendixObject> __ExportFileList;
	private java.util.List<system.proxies.FileDocument> ExportFileList;
	private IMendixObject __TargetZipDocument;
	private system.proxies.FileDocument TargetZipDocument;
	private java.lang.String TargetFileName;

	public CreateZipFile(IContext context, java.util.List<IMendixObject> ExportFileList, IMendixObject TargetZipDocument, java.lang.String TargetFileName)
	{
		super(context);
		this.__ExportFileList = ExportFileList;
		this.__TargetZipDocument = TargetZipDocument;
		this.TargetFileName = TargetFileName;
	}

	@java.lang.Override
	public java.lang.Boolean executeAction() throws Exception
	{
		this.ExportFileList = java.util.Optional.ofNullable(this.__ExportFileList)
			.orElse(java.util.Collections.emptyList())
			.stream()
			.map(__ExportFileListElement -> system.proxies.FileDocument.initialize(getContext(), __ExportFileListElement))
			.collect(java.util.stream.Collectors.toList());

		this.TargetZipDocument = this.__TargetZipDocument == null ? null : system.proxies.FileDocument.initialize(getContext(), __TargetZipDocument);

		// BEGIN USER CODE
		// Create a buffer for reading the files
		byte[] buf = new byte[1024];

		File fDir = new File(Core.getConfiguration().getTempPath().getAbsolutePath() + "/" + UUID.randomUUID().toString().substring(0, 5));
		while (fDir.exists())
			fDir = new File(Core.getConfiguration().getTempPath().getAbsolutePath() + "/" + UUID.randomUUID().toString().substring(0, 5));
		fDir.mkdirs();
		
		try {
			// Create the ZIP file
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(fDir.getAbsolutePath() + "/" + this.TargetFileName));
			ArrayList<String> nameList = new ArrayList<String>();

			// Compress the files
			for (FileDocument doc : this.ExportFileList) {
				// Add ZIP entry to output stream.
				String docname = checkName(nameList, doc.getName()).replaceAll("\\\\", "-");
				nameList.add(docname);

				Core.getLogger(this.toString()).trace("Add file: '" + docname + "' to zip");
				out.putNextEntry(new ZipEntry(docname));

				InputStream stream = Core.getFileDocumentContent(this.getContext(), doc.getMendixObject());
				int len;
				// Transfer bytes from the file to the ZIP file
				while ((len = stream.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				// Complete the entry
				out.closeEntry();
				stream.close();
			}
			// Complete the ZIP file
			out.close();

			File zipFile = new File(fDir.getAbsoluteFile() + "/" + this.TargetFileName);
			FileInputStream stream = new FileInputStream(zipFile);
			Core.storeFileDocumentContent(this.getContext(), this.TargetZipDocument.getMendixObject(),
					this.TargetFileName, stream);
			zipFile.delete();
		} catch (IOException e) {
			Core.getLogger(this.toString()).error(e);
			return false;
		}
		fDir.delete();

		return true;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 * @return a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "CreateZipFile";
	}

	// BEGIN EXTRA CODE
	private static String checkName(ArrayList<String> nameList, String Docname) {
		if (nameList.size() > 0) {
			int counter = 1;
			String checkname = Docname;
			while (containsName(nameList, checkname)) {
				int dotplace = Docname.lastIndexOf(".");
				if (dotplace >= 0) {
					checkname = Docname.substring(0, dotplace) + "_" + counter
							+ Docname.substring(dotplace, Docname.length());
				} else {
					checkname = Docname + "_" + counter;
				}
				counter++;
			}
			return checkname;
		}
		return Docname;
	}

	private static boolean containsName(ArrayList<String> NameList, String Docname) {
		for (String existingName : NameList) {
			if (existingName.equalsIgnoreCase(Docname)) {
				return true;
			}
		}
		return false;
	}
	// END EXTRA CODE
}
