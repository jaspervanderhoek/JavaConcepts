package java_library;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

/**
 * A program demonstrates how to upload files from local computer to a remote
 * FTP server using Apache Commons Net API.
 * 
 * @author www.codejava.net
 */
public class FTPConnector {

	private String pass;
	private String user;
	private int port;
	private String server;
	FTPClient ftpClient;

	private String proxyHost = null;
	private String proxyPassword = null;
	private String proxyUser = null;
	private int proxyPort;
	private boolean implicit = false;
	private boolean useFTPS = false;
	private String protocol = null;


	private static final ILogNode _logNode = Core.getLogger("FTPConnector");

	public FTPConnector( String server, int port, String user, String pass ) {
		this.server = server;
		this.port = port;
		this.user = user;
		this.pass = pass;
	}

	public void setProxyHost( String proxyHost, int proxyPort, String proxyUser, String proxyPass ) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.proxyUser = proxyUser;
		this.proxyPassword = proxyPass;
	}

	public void setFTPS( boolean useFTPS, boolean implicit, String protocol ) {
		this.useFTPS = useFTPS;
		this.implicit = implicit;
		this.protocol = protocol;
	}

	public void connect() throws FTPException {
		if ( this.ftpClient == null || !this.ftpClient.isConnected() ) {

			if ( !this.useFTPS ) {
				if ( this.proxyHost != null ) {
					_logNode.trace("Using HTTP proxy server: " + this.proxyHost);
					this.ftpClient = new FTPHTTPClient(this.proxyHost, this.proxyPort, this.proxyUser, this.proxyPassword);
				}
				else {
					this.ftpClient = new FTPClient();
				}
			}
			else {
				FTPSClient ftps;
				if( this.protocol == null )
					ftps = new FTPSClient(this.implicit);
				else 
					ftps = new FTPSClient(this.protocol, this.implicit);
				
//				ftps.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
				
				this.ftpClient = ftps;
			}

			try {
				_logNode.trace("Attempting to connect to: " + this.server + ":" + this.port + " [" + this.user + "]");
				this.ftpClient.connect(this.server, this.port);
				this.ftpClient.login(this.user, this.pass);
				this.ftpClient.enterLocalPassiveMode();
				this.ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			}
			catch( IOException e ) {
				throw new FTPException("Unable to connect to " + this.server + ":" + this.port, e);
			}
			_logNode.trace("Connect to: " + this.server + ":" + this.port);
		}
	}

	public void closeConnection() {
		try {
			if ( this.ftpClient != null && this.ftpClient.isConnected() ) {
				this.ftpClient.logout();
				this.ftpClient.disconnect();
			}
		}
		catch( IOException e ) {
			_logNode.error("Unable to close the connection to: " + this.server, e);
		}
	}


	public boolean downloadFile( IContext context, IMendixObject fileDocument, String fileLocation ) throws FTPException {
		try {
			connect();

			try {
				InputStream inputStream = this.ftpClient.retrieveFileStream(fileLocation);
				Core.storeFileDocumentContent(context, fileDocument, inputStream);
			}
			catch( IOException e ) {
				throw new FTPException("Unable to download file" + fileLocation, e);
			}
		}
		finally {
			closeConnection();
		}

		return false;
	}

	public boolean uploadFile( IContext context, IMendixObject fileDocument, String fileLocation ) throws FTPException {
		try {
			connect();

			InputStream inputStream = Core.getFileDocumentContent(context, fileDocument);
			try {
				this.ftpClient.storeFile(fileLocation, inputStream);
				inputStream.close();
			}
			catch( IOException e ) {
				throw new FTPException("Unable to store file: " + fileLocation, e);
			}
		}
		finally {
			closeConnection();
		}

		return false;
	}


	public class FTPException extends Exception {

		private static final long serialVersionUID = 164679292464251755L;

		public FTPException( String message, Exception e ) {
			super(message, e);
		}

	}

	public static void main( String[] args ) {
		FTPConnector connector = new FTPConnector("www.myserver.com", 21, "user", "pass");

		try {
			connector.connect();

			connector.downloadFile(Core.createSystemContext(), null, "/test/video.mp4");
			connector.closeConnection();
		}
		catch( FTPException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}