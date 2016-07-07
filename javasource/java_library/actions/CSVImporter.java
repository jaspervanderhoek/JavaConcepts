package java_library.actions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import replication.MetaInfo;
import replication.ReplicationSettings;
import replication.ReplicationSettings.KeyType;
import replication.ReplicationSettings.MendixReplicationException;
import replication.ReplicationSettings.ObjectSearchAction;
import replication.ValueParser;
import replication.implementation.CustomReplicationSettings;
import replication.implementation.ErrorHandler;
import replication.interfaces.IValueParser;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import bestpractices.proxies.Customer;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.meta.IMetaPrimitive.PrimitiveType;
import com.opencsv.CSVParser;


/**
 * 
 * @author Jasper van der Hoke
 * 
 * @dependency  icu4j-49_1.jar
 * @dependency  opencsv-3.8.jar
 * @dependency  org.apache.commons-io-2.3.0.jar
 * @dependency  replication.jar
 *  
 */
public class CSVImporter {
	private static final ILogNode _logger = Core.getLogger("CSVImporter");

	// Place this code in the Java action

	public static void importCSVFile( IContext context, LinkedHashMap<String, String> columnList, InputStream fileInputStream ) throws CoreException {
		CustomReplicationSettings settings = new CustomReplicationSettings(context, Customer.getType(), new ErrorHandler());
		settings.getObjectConfiguration(Customer.getType()).setObjectSearchAction(ObjectSearchAction.CreateEverything);
		
		/*
		 * For find create do:
		 * 
		 *	settings.getObjectConfiguration(Customer.getType())
		 *		.setObjectSearchAction(ObjectSearchAction.CreateEverything)
		 *		.addMember("1", "Name", true, true); 
		 *
		 * It is optional to specify all attributes that are present but not a key
		 */
		

		for( Entry<String, String> entry : columnList.entrySet() ) {
			settings.addColumnMapping(entry.getKey(), entry.getValue(), KeyType.NoKey, false, null);
		}

		importCSVFile(columnList, settings, fileInputStream);
	}

	public static void importCSVFile( LinkedHashMap<String, String> columnList, ReplicationSettings settings, InputStream fileInputStream ) throws MendixReplicationException {
		ValueParser vparser = new ExcelValueParser(settings.getValueParsers(), settings);
		MetaInfo info = new MetaInfo(settings, vparser, "CSVImporter");
		try {
			int lineNumber = 0, colNr = 0;
			;

			File tmpFile = createUTF8FileFromStream(fileInputStream);
			BufferedReader reader = new BufferedReader(new FileReader(tmpFile));
			tmpFile.deleteOnExit();
			CSVParser parser = new CSVParser();
			
			try {
				String nextLine;
				String[] content;
				while( (nextLine = reader.readLine()) != null ) {
					colNr = 0;
					if ( lineNumber != 0 ) {
						content = parser.parseLine(nextLine);
						if ( content.length == 1 )
							content = parser.parseLine(content[0]);
	
						for( Entry<String, String> entry : columnList.entrySet() ) {
							try {
								if ( _logger.isDebugEnabled() )
									_logger.debug(entry.getKey() + " - NrOfValues" + content.length);
								if ( colNr < content.length ) {
									info.addValue(String.valueOf(lineNumber), entry.getKey(), content[colNr]);
									colNr++;
								}
								else {
									break;
								}
							}
							catch( Exception e ) {
								throw new Exception("Error occured while processing line: " + (1 + lineNumber) + ", the error was: " + e.getMessage(), e);
							}
						}
					}
					lineNumber++;
				}
			}
			finally { 
				reader.close();
			}
		}
		catch( Exception e ) {
			throw new MendixReplicationException("Unable to import CSV file", e);
		}

		try {
			info.finished();
		}
		catch( Exception e ) {
			if ( e instanceof NumberFormatException )
				throw new MendixReplicationException("Error occured while processing the file, the error was: Invalid number " + e.getMessage(), e);
			else
				throw new MendixReplicationException("Error occured while processing the file, the error was: " + e.toString(), e);
		}
	}

	private static File createTempFile( InputStream is, String encoding ) throws FileNotFoundException, IOException {
		File file = File.createTempFile("CSVMx", "CSVMx");
		FileOutputStream ous = new FileOutputStream(file);
		byte buf[] = new byte[1024];
		int len;
		while( (len = is.read(buf)) > 0 ) {
			if ( encoding != null ) {
				byte b[] = IOUtils.toString(buf, encoding).getBytes("UTF-8");
				ous.write(b, 0, len);
			}
			else
				ous.write(buf, 0, len);
		}
		ous.close();
		is.close();
		return file;
	}

	private static File createUTF8FileFromStream( InputStream is ) throws FileNotFoundException, IOException {
		File file = createTempFile(is, null);

		BufferedInputStream fis1 = new BufferedInputStream(new FileInputStream(file));
		byte[] byteData = new byte[fis1.available()];
		fis1.read(byteData);
		fis1.close();

		CharsetDetector detector = new CharsetDetector();
		detector.setText(byteData);
		CharsetMatch match = detector.detect();

		BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
		File f2 = createTempFile(fis, match.getName());
		file.deleteOnExit();

		return f2;
	}
	public static class ExcelValueParser extends ValueParser {

		public ExcelValueParser( Map<String, IValueParser> customValueParsers, ReplicationSettings settings ) {
			super(settings, customValueParsers);
		}

		@Override
		public Object getValueFromDataSet( String column, PrimitiveType type, Object dataSet ) throws ParseException {
			throw new NotImplementedException();
		}
	}
}
