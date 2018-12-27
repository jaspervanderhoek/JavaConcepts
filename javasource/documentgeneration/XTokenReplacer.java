package documentgeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import mxmodelreflection.proxies.Microflows;
import mxmodelreflection.proxies.MxObjectMember;
import mxmodelreflection.proxies.MxObjectReference;
import mxmodelreflection.proxies.MxObjectType;
import mxmodelreflection.proxies.Status;
import mxmodelreflection.proxies.Token;


import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.core.objectmanagement.member.MendixEnum;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IMendixObjectMember;
import com.mendix.systemwideinterfaces.core.meta.IMetaAssociation;
import com.mendix.systemwideinterfaces.core.meta.IMetaAssociation.AssociationOwner;
import com.mendix.systemwideinterfaces.core.meta.IMetaEnumValue;

import documentgeneration.proxies.XToken;
import documentgeneration.proxies.XTokenType;

public class XTokenReplacer {
	
	public static List<IMendixObject> validateTokens(IContext context, String text, List<IMendixObject> tokenList) throws CoreException
	{
		if (tokenList == null)
		{
			throw new CoreException("The given token list is empty.");
		}
		if (text == null)
		{
			throw new CoreException("The source text is empty.");
		}
		
		List<IMendixObject> missingTokens = new ArrayList<IMendixObject>();
		for (IMendixObject token : tokenList)
		{
			if( !isTokenPresent(context, text, token) )
				missingTokens.add(token);
		}

		return missingTokens;
	}
	
	public static String replaceTokens(IContext context, String text, List<IMendixObject> tokenList, IMendixObject replacementObject) throws CoreException
	{
		if (tokenList == null)
		{
			throw new CoreException("The given token list is empty.");
		}
		if (text == null)
		{
			throw new CoreException("The source text is empty.");
		}

		for (IMendixObject token : tokenList)
		{
			text = replaceToken(context, text, token, replacementObject);
		}

		return text;
	}

	public static String replaceToken(IContext context, String text, IMendixObject token, IMendixObject replacementObject) throws CoreException
	{
		if (token == null)
		{
			throw new CoreException("The given token is empty.");
		}
		if (text == null)
		{
			throw new CoreException("The source text is empty.");
		}

		
		String tokenValue = (String) token.getValue(context, Token.MemberNames.CombinedToken.toString());
		if (!Status.Valid.toString().equals(token.getValue(context, Token.MemberNames.Status.toString())))
		{
			throw new CoreException("The token: " + tokenValue + " is not valid, only valid tokens can be replaced.");
		}

		try
		{
			Object value = "";
			String format = (String) token.getValue(context,XToken.MemberNames.XParseFormat.toString());
			
			if (replacementObject != null)
			{
				XTokenType tokenType = XTokenType.valueOf((String) token.getValue(context, XToken.MemberNames.XTokenType.toString()));
				if (tokenType == XTokenType.Attribute)
				{

					IMendixObject member = Core.retrieveId(context, (IMendixIdentifier) token.getValue(context, Token.MemberNames.Token_MxObjectMember.toString()));
					IMendixObject selectedObjType = Core.retrieveId(context, (IMendixIdentifier) token.getValue(context, Token.MemberNames.Token_MxObjectType_Start.toString()));

					if (!Core.isSubClassOf((String) selectedObjType.getValue(context, MxObjectType.MemberNames.CompleteName.toString()), replacementObject.getType()))
					{
						throw new CoreException("wrong object type in token: " + tokenValue + " The object should be of type: " + selectedObjType.getValue(context, MxObjectType.MemberNames.CompleteName.toString()) + " but is of type: " + replacementObject.getType());
					}

					
					
					
					
					
					
					value = replacementObject.getMember(context, (String) member.getValue(context, MxObjectMember.MemberNames.AttributeName.toString()));
					try
					{
						IMendixObjectMember<?> attrMember = replacementObject.getMember(context, (String) member.getValue(context, MxObjectMember.MemberNames.AttributeName.toString()));
						if (attrMember instanceof MendixEnum)
						{
							MendixEnum enumeration = (MendixEnum) attrMember;
							IMetaEnumValue enumValue = enumeration.getEnumeration().getEnumValues().get(enumeration.getValue(context));
							value = Core.getInternationalizedString(context, enumValue.getI18NCaptionKey());
							
						}
					} catch (Exception e)
					{
						Core.getLogger("TokenReplacer").warn(e);
					}
					

				}
				else
				{
					IMendixObject reference = Core.retrieveId(context, (IMendixIdentifier) token.getValue(context, Token.MemberNames.Token_MxObjectReference.toString()));
					IMendixObject objectTypeStart = Core.retrieveId(context, (IMendixIdentifier) token.getValue(context, Token.MemberNames.Token_MxObjectType_Start.toString()));
					IMendixObject objectTypeReference = Core.retrieveId(context, (IMendixIdentifier) token.getValue(context, Token.MemberNames.Token_MxObjectType_Referenced.toString()));
					
					// ref ref associatie
					
					IMendixObject refref = null;
					IMendixObject refassociation = null;
					
					
					if (tokenType == XTokenType.RefRef)
					{
						refref = Core.retrieveId(context, (IMendixIdentifier) token.getValue(context, XToken.MemberNames.Token_MxObjectType_RefRef.toString()));
						refassociation = Core.retrieveId(context, (IMendixIdentifier) token.getValue(context, XToken.MemberNames.Token_Ref_Association.toString()));
					}
					// end ref ref
					
					IMendixObject member = Core.retrieveId(context, (IMendixIdentifier) token.getValue(context, Token.MemberNames.Token_MxObjectMember.toString()));
					

					if( Core.isSubClassOf( (String) objectTypeStart.getValue(context, MxObjectType.MemberNames.CompleteName.toString()), replacementObject.getType() )  )
					{
						IMetaAssociation association = Core.getMetaAssociation((String) reference.getValue(context, MxObjectReference.MemberNames.CompleteName.toString()));
						if (association.getOwner() == AssociationOwner.BOTH || association.getParent().getName().equals(objectTypeStart.getValue(context, MxObjectType.MemberNames.CompleteName.toString())))
						{
							IMendixIdentifier refObjectId = replacementObject.getValue(context, (String) reference.getValue(context, MxObjectReference.MemberNames.CompleteName.toString()));
							
							if (refObjectId != null)
							{
								IMendixObject refObj = Core.retrieveId(context, refObjectId);
								
								if (tokenType == XTokenType.Reference)
								{
								value = refObj.getValue(context, (String) member.getValue(context, MxObjectMember.MemberNames.AttributeName.toString()));
								try
								{
									IMendixObjectMember<?> attrMember = refObj.getMember(context, (String) member.getValue(context, MxObjectMember.MemberNames.AttributeName.toString()));
									if (attrMember instanceof MendixEnum)
									{
										MendixEnum enumeration = (MendixEnum) attrMember;
										IMetaEnumValue enumValue = enumeration.getEnumeration().getEnumValues().get(enumeration.getValue(context));
										value = Core.getInternationalizedString(context, enumValue.getI18NCaptionKey());
									}
								} catch (Exception e)
								{
									String clas = token.getClass().getName();
									Core.getLogger("TokenReplacer").warn("Exception while replacing reference: " + clas + "  ", e);
								}
								}
								else
								{
									
									
									
										IMetaAssociation referencedassociation = Core.getMetaAssociation((String) refassociation.getValue(context, MxObjectReference.MemberNames.CompleteName.toString()));
										if (referencedassociation.getOwner() == AssociationOwner.BOTH || referencedassociation.getParent().getName().equals(objectTypeReference.getValue(context, MxObjectType.MemberNames.CompleteName.toString())))
											
										{
											IMendixIdentifier refassoObjectId = refObj.getValue(context, (String) refassociation.getValue(context, MxObjectReference.MemberNames.CompleteName.toString()));
											
											if (refassoObjectId != null)
											{
												IMendixObject refassoObj = Core.retrieveId(context, refassoObjectId);
												
												value = refassoObj.getValue(context, (String) member.getValue(context, MxObjectMember.MemberNames.AttributeName.toString()));
												try
												{
													IMendixObjectMember<?> attrMember = refassoObj.getMember(context, (String) member.getValue(context, MxObjectMember.MemberNames.AttributeName.toString()));
													if (attrMember instanceof MendixEnum)
													{
														MendixEnum enumeration = (MendixEnum) attrMember;
														IMetaEnumValue enumValue = enumeration.getEnumeration().getEnumValues().get(enumeration.getValue(context));
														value = Core.getInternationalizedString(context, enumValue.getI18NCaptionKey());
													}
												} catch (Exception e)
												{
													Core.getLogger("TokenReplacer").warn("Exception while replacing reference: ", e);
												}
											}
										}
									
									
									
									
								}
							}
						}
						else
						{
							List<IMendixObject> result = Core.retrieveXPathQuery(context, "//" + objectTypeReference.getValue(context, MxObjectType.MemberNames.CompleteName.toString()) + "[" + reference.getValue(context, MxObjectReference.MemberNames.CompleteName.toString()) + "='" + replacementObject.getId().toLong() + "']");
							if (result.size() > 0)
							{
								IMendixObject rsObject = result.get(0);
								value = rsObject.getValue(context, (String) member.getValue(context, MxObjectMember.MemberNames.AttributeName.toString()));
								try
								{
									IMendixObjectMember<?> attrMember = rsObject.getMember(context, (String) member.getValue(context, MxObjectMember.MemberNames.AttributeName.toString()));
									if (attrMember instanceof MendixEnum)
									{
										MendixEnum enumeration = (MendixEnum) attrMember;
										IMetaEnumValue enumValue = enumeration.getEnumeration().getEnumValues().get(enumeration.getValue(context));
										value = Core.getInternationalizedString(context, enumValue.getI18NCaptionKey());
									}
								} catch (Exception e)
								{
									Core.getLogger("TokenReplacer").warn("Exception while replacing reference: ", e);
								}
							}
						}
					}
					else
					{
						throw new CoreException("wrong object type in token: " + tokenValue + ", Expecting type: " + objectTypeStart.getValue(context, MxObjectType.MemberNames.CompleteName.toString()) + " received type: " +  replacementObject.getType());
					}
				}
				
				
			}

			String replacementValue = "";
			
			replacementValue = XDataParser.getStringValue(value, context, format);
			
			Core.getLogger("TokenReplacer").debug("For tokenvalue: "+tokenValue+" is the value retrieved: " +replacementValue);
			if (replacementValue == null)
			{
				replacementValue = "";
			}
			String returnText = text.replace(tokenValue, replacementValue);

			return returnText;
		} 
		catch (Exception e)
		{
			Core.getLogger("TokenReplacer").error(e.getMessage(), e);
		}

		return text;
	}
	public static boolean isTokenPresent(IContext context, String text, IMendixObject token) throws CoreException
	{
		if (token == null)
		{
			throw new CoreException("The given token is empty.");
		}
		if (text == null)
		{
			throw new CoreException("The source text is empty.");
		}

		Boolean tokenOptional = (Boolean) token.getValue(context, Token.MemberNames.IsOptional.toString());
		String tokenValue = (String) token.getValue(context, Token.MemberNames.CombinedToken.toString());
		if (!Status.Valid.toString().equals(token.getValue(context, Token.MemberNames.Status.toString())))
		{
			throw new CoreException("The token: " + tokenValue + " is not valid, only valid tokens can be replaced.");
		}

		try
		{
			int tokenPosition = text.indexOf(tokenValue);
			Core.getLogger("TokenReplacer").debug("Token: " + tokenValue+" located at position: " + tokenPosition);

			return tokenOptional || tokenPosition >= 0;
		} 
		catch (Exception e)
		{
			Core.getLogger("TokenReplacer").error(e.getMessage(), e);
		}

		return tokenOptional;
	}

	public static Boolean checkAssociation(IContext context,
			IMendixObject token, IMendixObject replacementObject) throws CoreException
	{
		
		if (token == null)
		{
			return false;
		}
		
		
		if (!Status.Valid.toString().equals(token.getValue(context, Token.MemberNames.Status.toString())))
		{
			return false;
		}
		
		if (replacementObject == null)
		{
			return false;
		}
		
		XTokenType tokenType = XTokenType.valueOf((String) token.getValue(context, Token.MemberNames.TokenType.toString()));
		if (tokenType == XTokenType.Attribute)
		{
			return true;
		}
		
		
		IMendixObject reference = Core.retrieveId(context, (IMendixIdentifier) token.getValue(context, Token.MemberNames.Token_MxObjectReference.toString()));
		IMendixObject objectTypeStart = Core.retrieveId(context, (IMendixIdentifier) token.getValue(context, Token.MemberNames.Token_MxObjectType_Start.toString()));
		IMendixObject objectTypeReference = Core.retrieveId(context, (IMendixIdentifier) token.getValue(context, Token.MemberNames.Token_MxObjectType_Referenced.toString()));
		
		
		
		
		
		IMendixObject refassociation = null;
		
		if (tokenType == XTokenType.RefRef)
		{
			refassociation = Core.retrieveId(context, (IMendixIdentifier) token.getValue(context, XToken.MemberNames.Token_Ref_Association.toString()));
		}
		
		

		if( Core.isSubClassOf( (String) objectTypeStart.getValue(context, MxObjectType.MemberNames.CompleteName.toString()), replacementObject.getType() )  )
		{
			IMetaAssociation association = Core.getMetaAssociation((String) reference.getValue(context, MxObjectReference.MemberNames.CompleteName.toString()));
			if (association.getOwner() == AssociationOwner.BOTH || association.getParent().getName().equals(objectTypeStart.getValue(context, MxObjectType.MemberNames.CompleteName.toString())))
			{
				IMendixIdentifier refObjectId = replacementObject.getValue(context, (String) reference.getValue(context, MxObjectReference.MemberNames.CompleteName.toString()));
				// so far so good now some extra tricks
				if (refObjectId != null)
				{
					IMendixObject refObj = Core.retrieveId(context, refObjectId);
					if (refObj == null)
					{
						return false;
					}
							
					if (tokenType == XTokenType.Reference)
					{
						return true;
					}
					else
					{
						IMetaAssociation referencedassociation = Core.getMetaAssociation((String) refassociation.getValue(context, MxObjectReference.MemberNames.CompleteName.toString()));
						if (referencedassociation.getOwner() == AssociationOwner.BOTH || referencedassociation.getParent().getName().equals(objectTypeReference.getValue(context, MxObjectType.MemberNames.CompleteName.toString())))
							
						{
							IMendixIdentifier refassoObjectId = refObj.getValue(context, (String) refassociation.getValue(context, MxObjectReference.MemberNames.CompleteName.toString()));
							
							if (refassoObjectId != null)
							{
								IMendixObject refassoObj = Core.retrieveId(context, refassoObjectId);
								if (refassoObj == null)
								{
									return false;
								}
								else
								{
									return true;
								}
							}
							else
							{
								return false;
							}
						}
					}
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
		
		if (tokenType == XTokenType.Reference)
		{
			return true;
		}
		
		
		
		
		return true;
	}

}
