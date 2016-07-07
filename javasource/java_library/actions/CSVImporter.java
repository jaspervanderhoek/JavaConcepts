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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;

import replication.MetaInfo;
import replication.ReplicationSettings;
import replication.ReplicationSettings.KeyType;
import replication.ReplicationSettings.MendixReplicationException;
import replication.ReplicationSettings.ObjectSearchAction;
import replication.ValueParser.ParseException;
import replication.ValueParser;
import replication.implementation.CustomReplicationSettings;
import replication.implementation.ErrorHandler;
import replication.interfaces.IValueParser;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import au.com.bytecode.opencsv.CSVParser;
import bestpractices.proxies.Customer;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.meta.IMetaPrimitive.PrimitiveType;

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
			reader.close();
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

		/**
		 * Given an Excel date with either 1900 or 1904 date windowing, converts it to a java.util.Date.
		 * 
		 * NOTE: If the default <code>TimeZone</code> in Java uses Daylight Saving Time then the conversion back to an Excel
		 * date may not give the same value, that is the comparison
		 * <CODE>excelDate == getExcelDate(getJavaDate(excelDate,false))</CODE> is not always true. For example if default
		 * timezone is <code>Europe/Copenhagen</code>, on 2004-03-28 the minute after 01:59 CET is 03:00 CEST, if the excel
		 * date represents a time between 02:00 and 03:00 then it is converted to past 03:00 summer time
		 * 
		 * @param date The Excel date.
		 * @param use1904windowing true if date uses 1904 windowing, or false if using 1900 date windowing.
		 * @return Java representation of the date, or null if date is not a valid Excel date
		 * @see java.util.TimeZone
		 */
		@SuppressWarnings("static-access")
		public Date getJavaDate( double date, boolean use1904windowing ) {
			if ( !HSSFDateUtil.isValidExcelDate(date) ) {
				return null;
			}
			int wholeDays = (int) Math.floor(date);
			int millisecondsInDay = (int) ((date - wholeDays) * ((24 * 60 * 60) * 1000L) + 0.5);
			Calendar calendar = new GregorianCalendar(); // using default time-zone
			setCalendar(calendar, wholeDays, millisecondsInDay, use1904windowing);
			return calendar.getTime();
		}

		public void setCalendar( Calendar calendar, int wholeDays, int millisecondsInDay, boolean use1904windowing ) {
			int startYear = 1900;
			int dayAdjust = -1; // Excel thinks 2/29/1900 is a valid date, which it isn't
			if ( use1904windowing ) {
				startYear = 1904;
				dayAdjust = 1; // 1904 date windowing uses 1/2/1904 as the first day
			}
			else if ( wholeDays < 61 ) {
				// Date is prior to 3/1/1900, so adjust because Excel thinks 2/29/1900 exists
				// If Excel date == 2/29/1900, will become 3/1/1900 in Java representation
				dayAdjust = 0;
			}
			calendar.set(startYear, 0, wholeDays + dayAdjust, 0, 0, 0);
			calendar.set(Calendar.MILLISECOND, millisecondsInDay);
		}
	}
}
