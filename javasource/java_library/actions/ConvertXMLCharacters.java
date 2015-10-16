package java_library.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

public class ConvertXMLCharacters {

	
	public void convertLine( IContext context, IMendixObject myFileDocument ) throws CoreException {
		try {
			InputStream in = Core.getFileDocumentContent(context, myFileDocument);
			
			String tmpFile = Core.getConfiguration().getTempPath().getAbsolutePath() + "/" + System.currentTimeMillis() + ".xml";
			BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
			FileWriter outputStream = new FileWriter(tmpFile);
		
			String line;
			while( (line = reader.readLine()) != null ) {
		        char current; // Used to reference the current character.
		        if( in != null && !"".equals(in) ) {
			        for (int i = 0; i < line.length(); i++) {
			            current = line.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
			            if ((current == 0x9) ||
			                (current == 0xA) ||
			                (current == 0xD) ||
			                ((current >= 0x20) && (current <= 0xD7FF)) ||
			                ((current >= 0xE000) && (current <= 0xFFFD)) ||
			                ((current >= 0x10000) && (current <= 0x10FFFF)))
			            	outputStream.write(current);
			            else 
			            	Core.getLogger("ConvertXMLCharacters").info("Skipping character: " + current);
			        }
		        }
			}
			reader.close();
            outputStream.close();
            
            File f =  new File( tmpFile );
            FileInputStream newIn = new FileInputStream( f );
            Core.storeFileDocumentContent(context, myFileDocument, newIn );
            f.delete();
		}
		catch (IOException e) {
			throw new CoreException(e);
		}
	}
}
