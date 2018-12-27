package documentgeneration;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



import com.mendix.core.Core;
import com.mendix.core.objectmanagement.member.MendixAutoNumber;
import com.mendix.core.objectmanagement.member.MendixBoolean;
import com.mendix.core.objectmanagement.member.MendixCurrency;
import com.mendix.core.objectmanagement.member.MendixDateTime;
import com.mendix.core.objectmanagement.member.MendixEnum;
import com.mendix.core.objectmanagement.member.MendixFloat;
import com.mendix.core.objectmanagement.member.MendixHashString;
import com.mendix.core.objectmanagement.member.MendixInteger;
import com.mendix.core.objectmanagement.member.MendixLong;
import com.mendix.core.objectmanagement.member.MendixString;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObjectMember;
import com.mendix.systemwideinterfaces.core.meta.IMetaEnumValue;

public class XDataParser
	{
	 	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	    private static final SimpleDateFormat dateFormatEnglish = new SimpleDateFormat("dd MMMM yyyy");
	    

	    public static String getStringValue(Object value, IContext context,String format)
	    {
	        if (value == null)
	        {
	            return "";
	        }
	        else if (value instanceof Integer)
	        {
	            return Integer.toString( (Integer) value);
	        }
	        else if (value instanceof Boolean)
	        {
	            return Boolean.toString( (Boolean) value);
	        }
	        else if (value instanceof Double)
	        {
	        	if (format != null && format.equals(documentgeneration.proxies.X_ParseFormat.NumberGrouping.toString()))
	        	{
	        		return getFormattedNumber(context, (Double) value, 2, 5,true);
	        	}
	        	else
	        	{
	            return getFormattedNumber(context, (Double) value, 2, 5,false);
	        	}
	        }
	        else if (value instanceof Float)
	        {
	        	if (format != null && format.equals(documentgeneration.proxies.X_ParseFormat.NumberGrouping.toString()))
	        	{
	        		return getFormattedNumber(context, (Double) value, 2, 5,true);
	        	}
	        	else
	        	{
	            return getFormattedNumber(context, Double.valueOf((Float) value), 2, 5,false);
	        	}
	        }
	        else if (value instanceof Date)
	        {
	        	if (format != null && format.equals(documentgeneration.proxies.X_ParseFormat.DateEnglishStyle.toString()))
	        	{
	        		return dateFormatEnglish.format((Date) value);
	        	}
	        	else
	        	{
	            return dateFormat.format((Date) value);
	        	}
	        }
	        else if (value instanceof Long)
	        {
	            return Long.toString( (Long) value);
	        }
	        else if (value instanceof IMendixObjectMember)
	        {
	            IMendixObjectMember<?> member = (IMendixObjectMember<?>) value;
	            if (member.getValue(context) == null)
	            {
	                return "";
	            }
	            
	            if (value instanceof MendixBoolean)
	            {
	                return Boolean.toString(((MendixBoolean) value).getValue(context));
	            }
	            else if (value instanceof MendixCurrency)
	            {
	            	String val =  Double.toString(((MendixCurrency) value).getValue(context));
	            	Double parseback = Double.parseDouble(val);
	            	if (format != null && format.equals(documentgeneration.proxies.X_ParseFormat.NumberGrouping.toString()))
	            	{
	            		// return getFormattedNumber(context, (MendixCurrency) value, 2, 20,true);
	            		return getFormattedNumber(context, parseback, 2, 5,true);
	            	}
	            	else
	            	{
	                return getFormattedNumber(context, parseback, 2, 5,false);
	            	}
	            }
	            else if (value instanceof MendixDateTime)
	            {
	 //           	return dateFormat.format(((MendixDateTime) value).getValue(context));
	            	if (format != null && format.equals(documentgeneration.proxies.X_ParseFormat.DateEnglishStyle.toString()))
	            	{
	            		return dateFormatEnglish.format(((MendixDateTime) value).getValue(context));
	            	}
	            	else
	            	{
	                return dateFormat.format(((MendixDateTime) value).getValue(context));
	            	}
	            }
	            else if (value instanceof MendixEnum)
	            {
	                MendixEnum enumeration = (MendixEnum) value;
	                try
	                {
	                    IMetaEnumValue enumValue = enumeration.getEnumeration().getEnumValues().get(enumeration.getValue(context));
	                    return Core.getInternationalizedString(context, enumValue.getI18NCaptionKey());
	                } catch (Exception e)
	                {
	                    Core.getLogger("TokenReplacer").warn(e);
	                    return enumeration.getValue(context);
	                }
	            }
	            else if (value instanceof MendixFloat)
	            {
	            	String val =  Double.toString(((MendixFloat) value).getValue(context));
	            	Double parseback = Double.parseDouble(val);
	            	if (format != null && format.equals(documentgeneration.proxies.X_ParseFormat.NumberGrouping.toString()))
	            	{
	            		return getFormattedNumber(context, parseback, 2, 5,true);
	            	}
	            	else
	            	{
	                return getFormattedNumber(context, parseback, 2, 5,false);
	            	}
	            	// return Double.toString(((MendixFloat) value).getValue(context));
	            }
	            else if (value instanceof MendixHashString)
	            {
	                return ((MendixHashString) value).getValue(context);
	            }
	            else if (value instanceof MendixInteger)
	            {
	                return Integer.toString(((MendixInteger) value).getValue(context));
	            }
	            else if (value instanceof MendixString)
	            {
	                return ((MendixString) value).getValue(context);
	            }
	            else if (value instanceof MendixLong)
	            {
	                return Long.toString( ((MendixLong) value).getValue(context) );
	            }
	            else if (value instanceof MendixAutoNumber)
	            {
	                return Long.toString( ((MendixAutoNumber) value).getValue(context) );
	            }
	        }
	        if (value instanceof String)
	        {
	            return ((String)value).trim();
	        }

	        return "";
	    }

	    private static String getFormattedNumber(IContext context, Double curValue, int minPrecision, int maxPrecision,boolean group)
	    {
	        NumberFormat numberFormat = NumberFormat.getInstance(Core.getLocale(context));
	        numberFormat.setMaximumFractionDigits(maxPrecision);
	        numberFormat.setGroupingUsed(group);
	        numberFormat.setMinimumFractionDigits(minPrecision);

	        if (!Double.isNaN(curValue))
	        {
	            return numberFormat.format(curValue);
	        }

	        return null;
	    }
	}
