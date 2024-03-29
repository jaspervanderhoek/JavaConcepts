// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package sqlquery.actions;

import static com.mendix.core.Core.dataStorage;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.webui.CustomJavaAction;

public class RunQuery extends CustomJavaAction<java.lang.Boolean>
{
	private IMendixObject __Query;
	private sqlquery.proxies.Query Query;

	public RunQuery(IContext context, IMendixObject Query)
	{
		super(context);
		this.__Query = Query;
	}

	@java.lang.Override
	public java.lang.Boolean executeAction() throws Exception
	{
		this.Query = this.__Query == null ? null : sqlquery.proxies.Query.initialize(getContext(), __Query);

		// BEGIN USER CODE
		try {
			dataStorage().executeWithConnection(c -> this.executeQuery(c, this.Query));
		}
		catch(Exception e ) {
			this.Query.setResult( ExceptionUtils.getStackTrace(e) );
		}
		
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
		return "RunQuery";
	}

	// BEGIN EXTRA CODE
	
    private boolean executeQuery(final Connection connection, final sqlquery.proxies.Query QueryParameter1) {
        try(Statement stmt = connection.createStatement()) {
        	String sql = QueryParameter1.getQuery();
        	
        	ResultSet rs = stmt.executeQuery(sql);
            
			StringBuilder sb = new StringBuilder();
			ResultSetMetaData md = rs.getMetaData();
			int colCount = md.getColumnCount();
			for( int i = 1; i <= colCount; i++ ) {
				sb.append("\"").append( md.getColumnLabel(i) ).append("\"");
				if( i < colCount ) 
					sb.append(",");
			}
			sb.append("\r\n");
			
			int count = 0;
			while( rs.next() ) {
				count++;
				for( int i = 1; i <= colCount; i++ ) {
					int colType = md.getColumnType(i);
					
					if( colType == Types.INTEGER || colType == Types.TINYINT || colType == Types.BIGINT || colType == Types.DECIMAL || colType == Types.NUMERIC)
						sb.append( rs.getObject(i) );
					else 
						sb.append("\"").append( rs.getObject(i) ).append("\"");
					if( i < colCount ) 
						sb.append(",");
				}
				sb.append("\r\n");
			}
			
			this.Query.setRows(count);
			this.Query.setResult(sb.toString());
            
        } catch (SQLException sqe) {
            throw new RuntimeException(sqe);
        }
        
        return true;
    }
	// END EXTRA CODE
}
