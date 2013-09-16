package com.prey.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

 

public class PreyHttp {

	
	
	public static final String HTTP_OK = "200 OK";
	public static final String HTTP_PARTIALCONTENT = "206 Partial Content";
	public static final String HTTP_RANGE_NOT_SATISFIABLE = "416 Requested Range Not Satisfiable";
	public static final String HTTP_REDIRECT = "301 Moved Permanently";
	public static final String HTTP_NOTMODIFIED = "304 Not Modified";
	public static final String HTTP_FORBIDDEN = "403 Forbidden";
	public static final String HTTP_NOTFOUND = "404 Not Found";
	public static final String HTTP_BADREQUEST = "400 Bad Request";
	public static final String HTTP_INTERNALERROR = "500 Internal Server Error";
	public static final String HTTP_NOTIMPLEMENTED = "501 Not Implemented";
	
	
 
	public static final String MIME_PLAINTEXT = "text/plain";
			public static final String MIME_HTML = "text/html";
			public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
			public static final String MIME_XML = "text/xml";
	
	
	private int myTcpPort;
	private final ServerSocket myServerSocket;
	private Thread myThread=null;
 
	
	
	public PreyHttp( int port, File wwwroot ) throws IOException
	{
		myTcpPort = port;
	 
		myServerSocket = new ServerSocket( myTcpPort );
		myThread = new Thread( new Runnable(){
			public void run(){
				try{
					while( true )
						new PreyHttpSession( myServerSocket.accept());
				}
				catch ( IOException ioe ){

				}
			}
		});
		myThread.setDaemon( true );
		myThread.start();
	}
	
	public void stop()
	{
		try
		{
			myServerSocket.close();
			myThread.join();
		}
		catch ( IOException ioe ) {}
		catch ( InterruptedException e ) {}
	}
}
