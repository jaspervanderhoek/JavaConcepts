package audittrail.log;

import java.lang.IllegalArgumentException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;

import system.proxies.User;
import audittrail.proxies.AudittrailSuperClass;
import audittrail.proxies.Configuration;
import audittrail.proxies.Log;
import audittrail.proxies.LogLine;
import audittrail.proxies.MemberType;
import audittrail.proxies.TypeOfLog;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.core.objectmanagement.member.MendixObjectReference;
import com.mendix.core.objectmanagement.member.MendixObjectReferenceSet;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IMendixObject.ObjectState;
import com.mendix.systemwideinterfaces.core.IMendixObjectMember;
import com.mendix.systemwideinterfaces.core.meta.IMetaAssociation;
import com.mendix.systemwideinterfaces.core.meta.IMetaAssociation.AssociationOwner;
import com.mendix.systemwideinterfaces.core.meta.IMetaAssociation.AssociationType;
import com.mendix.systemwideinterfaces.core.meta.IMetaObject;

public class CreateLogObject {
	private static HashMap<String, String> associationMapping = new LinkedHashMap<String, String>();
	private static boolean isInitialized = false;
	
	private static Boolean CreateLogObjectWithoutMemberChanges = null;
	private static Boolean IncludeCalculatedAttributes = null;
	private static Boolean IncludeOnlyChangedAttributes = null;
	private static Boolean LogServerTimeZoneDateNotation = null;
	private static Boolean LogSessionTimeZoneDateNotation = null;
	private static String ServerTimeZone = null;
	private static String LogLineDateFormat = null;
	private static ILogNode _logNode = Core.getLogger("AuditTrail");
	
	private static synchronized void _initialize( ) {
		if( !isInitialized ) {
			IContext context = Core.createSystemContext();
			IMendixObject config = Core.instantiate(context, Configuration.getType());
			
			CreateLogObject.CreateLogObjectWithoutMemberChanges = config.getValue(context, Configuration.MemberNames.CreateLogObjectWithoutMemberChanges.toString());
			CreateLogObject.IncludeCalculatedAttributes = config.getValue(context, Configuration.MemberNames.IncludeCalculatedAttributes.toString());
			CreateLogObject.IncludeOnlyChangedAttributes = config.getValue(context, Configuration.MemberNames.IncludeOnlyChangedAttributes.toString());
			CreateLogObject.LogLineDateFormat = config.getValue(context, Configuration.MemberNames.LogLineDateFormat.toString());
			CreateLogObject.LogServerTimeZoneDateNotation = config.getValue(context, Configuration.MemberNames.LogServerTimeZoneDateNotation.toString());
			CreateLogObject.LogSessionTimeZoneDateNotation = config.getValue(context, Configuration.MemberNames.LogSessionTimeZoneDateNotation.toString());
			CreateLogObject.ServerTimeZone = config.getValue(context, Configuration.MemberNames.ServerTimeZone.toString());
			
			try {
				Core.rollback(context, config);
			} catch (CoreException e) { }
			
			isInitialized = true;
		}
	}
	
	private static synchronized String getAssociationName( String otherObjectType ) {
		return associationMapping.get(otherObjectType);
	}
	private static synchronized void setAssociationName( String otherObjectType, String associationName ) {
		associationMapping.put(otherObjectType, associationName);
	}
	
	public static IMendixObject CreateAuditLogItems( IMendixObject inputObject, IMendixObject auditTrailSuperClass, IContext context) throws CoreException{
		TypeOfLog log = TypeOfLog.Change;
		if( inputObject.isNew() || inputObject.getState() == ObjectState.INSTANTIATED || inputObject.getState() == ObjectState.AUTOCOMMITTED )
			log = TypeOfLog.Add;
		
		return CreateLogObject.CreateAuditLogItems(inputObject, auditTrailSuperClass, context, log);
	}
	
	public static IMendixObject CreateAuditLogItems( IMendixObject auditableObject, IMendixObject auditTrailSuperClass, IContext context, TypeOfLog logType ) throws CoreException{
		_initialize( );
		
		if( _logNode.isDebugEnabled() )
			_logNode.debug("Evaluating audit log for object: " + auditableObject.getType() + "(" + auditableObject.getId().toLong() + "), state: " + auditableObject.getState() + "/" + logType );
		
		
		IContext sudoContext = Core.createSystemContext();
		IMendixObject logObject = Core.instantiate( sudoContext, Log.getType() );;
		IMendixObject userObject = null;
		
		try {
			userObject = context.getSession().getUser().getMendixObject();
		} catch (Exception e) {
			try {
				List<IMendixObject> administrators = Core.retrieveXPathQuery(sudoContext, "//" + User.getType() + "[" + User.MemberNames.Name + "='" + Core.getConfiguration().getAdminUserName() + "']");
				userObject = administrators.get(0);
			} catch (CoreException e1) {
				_logNode.error("MxAdmin not found");
			}
		}
		
		logObject.setValue(sudoContext, Log.MemberNames.DateTime.toString(), new Date());
		logObject.setValue(sudoContext, Log.MemberNames.LogObject.toString(), auditableObject.getType());
		logObject.setValue(sudoContext, Log.MemberNames.Log_User.toString(), userObject.getId());
		logObject.setValue(sudoContext, Log.MemberNames.LogType.toString(), logType.toString());
		logObject.setValue(sudoContext, Log.MemberNames.ReferenceId.toString(), String.valueOf(auditableObject.getId().toLong()) );
		String association = null;
		
		if( auditTrailSuperClass != null && Core.isSubClassOf(AudittrailSuperClass.getType(), auditTrailSuperClass.getType()) ) {
			logObject.setValue(sudoContext, Log.MemberNames.Log_AudittrailSuperClass.toString(), auditTrailSuperClass.getId() );
		}
		else if( auditTrailSuperClass != null ) {
			association = getAssociationName(auditTrailSuperClass.getType());
			if( association == null ) {
				IMetaObject imObject = Core.getMetaObject(auditTrailSuperClass.getType());
				for( IMetaAssociation ass : imObject.getMetaAssociationsParent() ) {
					if( ass.getChild().getName().equals(Log.getType()) && ass.getType() == AssociationType.REFERENCESET ) {
						association = ass.getName();
						List<IMendixIdentifier> idList = auditTrailSuperClass.getValue(sudoContext, association);
						idList.add(logObject.getId());
						auditTrailSuperClass.setValue(sudoContext, association, idList );
						
						setAssociationName(auditTrailSuperClass.getType(), association);
						break;
					}
				}
				if( association == null ) {
					for( IMetaAssociation ass : imObject.getMetaAssociationsChild() ) {
						if( ass.getParent().getName().equals(Log.getType()) && (ass.getType() == AssociationType.REFERENCESET || ass.getOwner() != AssociationOwner.BOTH) ) {
							association = ass.getName();
							logObject.setValue(sudoContext, association, auditTrailSuperClass.getId() );
							
							setAssociationName(auditTrailSuperClass.getType(), association);
							break;
						}
					}
				}
			}
			else {
				boolean associationFound = false;
				try {
					if( auditTrailSuperClass.getMember(sudoContext, association) != null ) {
						associationFound = true;
						List<IMendixIdentifier> idList = auditTrailSuperClass.getValue(sudoContext, association);
						idList.add(logObject.getId());
						auditTrailSuperClass.setValue(sudoContext, association, idList );
					}
				}
				catch( IllegalArgumentException e ) {
					_logNode.error("Could not find association in audit trail super class: " + auditTrailSuperClass.getType(),e);
				}
				
				if( !associationFound )
					logObject.setValue(sudoContext, association, auditTrailSuperClass.getId() );
			}
			
			if( association == null )
				throw new CoreException("Unable to find a reference set between " + Log.getType() + " and " + auditTrailSuperClass.getType() );
		}
		
		if( createLogLines(auditableObject, logObject, sudoContext, logType, association) > 0 ) { 
			Core.commit(sudoContext, logObject);
			return logObject;
		}
		else if( CreateLogObjectWithoutMemberChanges ) {
			Core.commit(sudoContext, logObject);
			return logObject;
		}
		else {
			Core.delete(sudoContext, logObject);
			return null;
		}
	}
	
	private static int createLogLines(IMendixObject inputObject, IMendixObject logObject, IContext sudoContext, TypeOfLog logType, String skipAssociation ) throws CoreException{
		boolean isNew = false;
		if( logType != TypeOfLog.Delete ) {
			// The object is new
			if( inputObject.isNew() || inputObject.getState() == ObjectState.INSTANTIATED || inputObject.getState() == ObjectState.AUTOCOMMITTED ){
				logObject.setValue(sudoContext, Log.MemberNames.LogType.toString(), TypeOfLog.Add.toString());
				isNew = true;
			}
		}
		
		Collection<? extends IMendixObjectMember<?>> members = inputObject.getMembers(sudoContext).values();
		List<IMendixObject> logLineList = new ArrayList<IMendixObject>(members.size());
		IMendixObject line;
		for( IMendixObjectMember<?> member : members ) {
			if( member.getName().equals(skipAssociation) )
				continue;
			
			if( !IncludeCalculatedAttributes && member.isVirtual())
				continue;
			
			
			line = null;
			if (member instanceof MendixObjectReference){
				if( !member.getName().startsWith("System.") )
					line = createReferenceLogLine(logObject, member, isNew, sudoContext);
			}
			
			else if( member instanceof MendixObjectReferenceSet)					
				line = createReferenceSetLogLine(logObject, member, isNew, sudoContext);
			
			else{					
				String attributeName = member.getName();
				
				if(!attributeName.startsWith("System.") && !attributeName.equals("changedDate") && !attributeName.equals("createdDate")) {						
					line = createSingleLogLine( logObject, member, MemberType.Attribute.toString(), isNew, sudoContext );
				}
			}
			
			if( line != null ) 
				logLineList.add( line );
		}
		
		if( logLineList.size() > 0 ) { 
			Core.commit(sudoContext, logLineList);
			
			return logLineList.size();
		}
		
		return 0;
	}
	
	private static IMendixObject createSingleLogLine( IMendixObject logObject, IMendixObjectMember<?> member, String memberType, boolean isNew, IContext context ) throws CoreException {
		String oldValue = getValue( member, false, context ), newValue = getValue( member, true, context );
		if( IncludeOnlyChangedAttributes == false || !oldValue.equals(newValue) || isNew ) {
			IMendixObject logLine = Core.instantiate(context, LogLine.getType());
			
			logLine.setValue(context, LogLine.MemberNames.Member.toString(), member.getName() );
			logLine.setValue(context, LogLine.MemberNames.MemberType.toString(), memberType );
			logLine.setValue(context, LogLine.MemberNames.LogLine_Log.toString(), logObject.getId() );
			logLine.setValue(context, LogLine.MemberNames.NewValue.toString(), newValue );
			
			if (isNew)
				logLine.setValue(context, LogLine.MemberNames.OldValue.toString(), "" );		
			else
				logLine.setValue(context, LogLine.MemberNames.OldValue.toString(), oldValue );
	
			if( !oldValue.equals(newValue) || isNew  ) {
				_logNode.trace("Member: " + member.getName() + " has changed.");
				logObject.setValue(context, Log.MemberNames.NumberOfChangedMembers.toString(), (Integer) logObject.getValue(context, Log.MemberNames.NumberOfChangedMembers.toString()) + 1 );
			}
			
			return logLine;
		}
		
		_logNode.trace("Skipping member: " + member.getName() + " because it has not changed.");
		return null;
	}
	
	
	private static IMendixObject createReferenceLogLine(IMendixObject logObject, IMendixObjectMember<?> member, boolean isNew, IContext context) throws CoreException {
		//get current and previous id
		IMendixIdentifier cID = (IMendixIdentifier) member.getValue(context);
		IMendixIdentifier pID = (IMendixIdentifier) member.getOriginalValue(context);							
		
		// Get the values of reference objects
		String pValue = getValueFromReference(pID, context);
		String newValue = getValueFromReference(cID, context);
		
		if( IncludeOnlyChangedAttributes == false || !pValue.equals(newValue) || isNew ) {
			IMendixObject logLine = Core.instantiate(context, LogLine.getType());
			
			logLine.setValue(context, LogLine.MemberNames.Member.toString(), member.getName() );
			logLine.setValue(context, LogLine.MemberNames.MemberType.toString(), MemberType.Reference.toString() );
			logLine.setValue(context, LogLine.MemberNames.LogLine_Log.toString(), logObject.getId() );
			logLine.setValue(context, LogLine.MemberNames.NewValue.toString(), newValue );

			if (isNew)
				logLine.setValue(context, LogLine.MemberNames.OldValue.toString(), "" );		
			else
				logLine.setValue(context, LogLine.MemberNames.OldValue.toString(), pValue );

			if( !logLine.getValue(context, LogLine.MemberNames.OldValue.toString()).equals(logLine.getValue(context, LogLine.MemberNames.NewValue.toString())) || isNew ) {
				_logNode.trace("Member: " + member.getName() + " has changed.");
				logObject.setValue(context, Log.MemberNames.NumberOfChangedMembers.toString(), (Integer) logObject.getValue(context, Log.MemberNames.NumberOfChangedMembers.toString()) + 1 );
			}
		
			return logLine;
		}
		_logNode.trace("Skipping member: " + member.getName() + " because it has not changed.");
		return null;
	}
		
	
	private static String getValueFromReference(IMendixIdentifier ID, IContext context) throws CoreException {
		String value = "";
		
		if(ID != null) {
			//Get id of previous object
			IMendixObject refObj = Core.retrieveId( context, ID);
			
			if (refObj != null){
				//Get list of members with descriptive values
				Collection<? extends IMendixObjectMember<?>> list = refObj.getMembers(context).values();
				
				if (list.size() > 0){
					//loop the list
					for(IMendixObjectMember<?> member : list) {
						value += member.getName() + ": " + getValue(member, true, context) + "\n";				
					}
				}
			}
		}
		
		return value;
	}
	
	
	@SuppressWarnings("unchecked")
	private static IMendixObject createReferenceSetLogLine(IMendixObject logObject, IMendixObjectMember<?> member, boolean isNew, IContext context) throws CoreException {
		String currentValue = "", previousValue = "";

		List<IMendixIdentifier> currentIDList = (List<IMendixIdentifier>) member.getValue(context);
		List<IMendixIdentifier> previousIDList = (List<IMendixIdentifier>) member.getOriginalValue(context);
		
		if ( currentIDList != null && currentIDList.size() > 0 ) {
			for (IMendixIdentifier id : currentIDList) {
				currentValue += getValueFromReference(id, context);
			}
		}
		
		if ( previousIDList != null && previousIDList.size() > 0 ) {
			for (IMendixIdentifier id : previousIDList) {
				previousValue += getValueFromReference(id, context);
			}
		}
		
		if( IncludeOnlyChangedAttributes == false || !previousValue.equals(currentValue) || isNew ) {
			IMendixObject logLine = Core.instantiate(context, LogLine.getType());
			logLine.setValue(context, LogLine.MemberNames.Member.toString(), member.getName() );
			logLine.setValue(context, LogLine.MemberNames.MemberType.toString(), MemberType.ReferenceSet.toString() );
			logLine.setValue(context, LogLine.MemberNames.LogLine_Log.toString(), logObject.getId() );
			logLine.setValue(context, LogLine.MemberNames.NewValue.toString(), currentValue );
			
			if (isNew)
				logLine.setValue(context, LogLine.MemberNames.OldValue.toString(), "" );		
			else
				logLine.setValue(context, LogLine.MemberNames.OldValue.toString(), previousValue );

			if( !logLine.getValue(context, LogLine.MemberNames.OldValue.toString()).equals(logLine.getValue(context, LogLine.MemberNames.NewValue.toString())) || isNew ) {
				_logNode.trace("Member: " + member.getName() + " has changed.");
				logObject.setValue(context, Log.MemberNames.NumberOfChangedMembers.toString(), (Integer) logObject.getValue(context, Log.MemberNames.NumberOfChangedMembers.toString()) + 1 );
			}
			
			return logLine;
		}
		
		_logNode.trace("Skipping member: " + member.getName() + " because it has not changed.");
		return null;
	}
	
	private static String getValue( IMendixObjectMember<?> member, boolean fromCache, IContext context ) {
		Object value = null;
		// Values from cache
		if (fromCache == true)
			value = member.getValue(context);
			
		// Values form DB
		else
			value = member.getOriginalValue(context);
		
		if( value != null ) {
			
			if( value instanceof Date )
				return parseDate( (Date)value, context );
				
			else if( value instanceof String)
				return parseString( (String)value );
							
			return String.valueOf( value ).trim();
		}
		else
			return "";
	}
	
	private static String parseDate(Date date, IContext context) {
		String dateOutput = "";
		if( date != null ) {
			DateFormat dateFormat = new SimpleDateFormat(CreateLogObject.LogLineDateFormat);
			if( CreateLogObject.LogServerTimeZoneDateNotation ) {
				TimeZone zone = TimeZone.getTimeZone(CreateLogObject.ServerTimeZone);
				dateFormat.setTimeZone( zone );
				dateOutput = dateFormat.format(date) + " (UTC) ";
			}
			
			if( CreateLogObject.LogSessionTimeZoneDateNotation && context.getSession() != null && context.getSession().getTimeZone() != null) {
				if( !"".equals(dateOutput) ) 
					dateOutput += " / ";
				
				TimeZone zone = context.getSession().getTimeZone();
				dateFormat.setTimeZone( zone );
				dateOutput += dateFormat.format(date) + " (" + zone.getDisplayName() + ") ";
			}
		}
		
		return dateOutput; 
	}
	
	private static String parseString(String value) {
		if (value == null)
			return "";
		else
			return value;		 
	}
}