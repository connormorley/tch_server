package geneos_notification.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import geneos_notification.loggers.LogHandler;
import geneos_notification.loggers.LogObject;
import geneos_notification.loggers.LtA;


@SpringBootApplication
public class StartController {
	
/*	@Bean
	public Integer port() {
		return SocketUtils.findAvailableTcpPort();
	}
	
	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
		TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
		tomcat.addAdditionalTomcatConnectors(createStandardConnector());
		return tomcat;
	}
	
	private Connector createStandardConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setPort(8080);
		connector.setScheme("http");
		return connector;
	}*/
	
	//public static ArrayList<String> settings = new ArrayList<String>();
	public static Map<String, String> setting = new HashMap<String,String>();
	private final static Logger logger = Logger.getLogger(StartController.class.getName());
	private static FileHandler fh = null;
	static LtA logA = new LogObject();
	//private final static Logger LOGGER = Logger.getLogger(Application.class.getName());
	
	public static void main(String[] args) throws InterruptedException, ExecutionException
	{
		System.out.print("\n8888888888888888888888888b.  .d8888b. \n" +
				"  888      888    888   Y88bd88P  Y88b\n" +
				"  888      888    888    888Y88b.     \n" +
				"  888      888    888   d88P \"Y888b.  \n" +
				"  888      888    8888888P\"     \"Y88b.\n" +
				"  888      888    888 T88b        \"888\n" +
				"  888      888    888  T88b Y88b  d88P\n" +
				"8888888    888    888   T88b \"Y8888P\"  © \n" +
				"\n" +
				"<< Version 0.6>>      << Created by C.Morley 2016/2017 >>\n" +
				"\n");
		start();
	}
    
    public static void start() throws InterruptedException, ExecutionException {
    	readSettingsFile();
    	configureSetting(setting);
    	logA.doLog("Start" , "[Start]Server Boot variables passed verification", "Info");
        SpringApplication.run(StartController.class);
    }

public static void readSettingsFile() {
	//System.out.println(System.getProperty("os.name"));
	//System.getProperties().list(System.out);
	File file = null;
	if(System.getProperty("os.name").contains("Windows"))
	file = new File(".\\settings.txt");
	if(System.getProperty("os.name").contains("Linux"))
	file = new File("./settings.txt");
	Scanner scnr = null;
	try {
		scnr = new Scanner(file);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
		logA.doLog("Start" , "[Start]System Settings file not found - Server Terminating", "Critical");
		System.exit(0);
	}
	while(scnr.hasNextLine()) // Replace wih properties file system
	{
		String line = scnr.nextLine();
		if (line.contains("mySQL Host")) {
			setting.put("mySQLHost", line.substring(line.indexOf("=") + 1));
		} else if (line.contains("mySQL Database Name")) {
			setting.put("mySQLDB", line.substring(line.indexOf("=") + 1));
		} else if (line.contains("mySQL Username")) {
			setting.put("mySQLUser", line.substring(line.indexOf("=") + 1));
		} else if (line.contains("mySQL Password")) {
			setting.put("mySQLPass", line.substring(line.indexOf("=") + 1));
		} else if (line.contains("SMTP Host")) {
			setting.put("SMTPHost", line.substring(line.indexOf("=") + 1));
		} else if (line.contains("SMTP User")) {
			setting.put("SMTPUser", line.substring(line.indexOf("=") + 1));
		} else if (line.contains("SMTP Password")) {
			setting.put("SMTPPass", line.substring(line.indexOf("=") + 1));
		}  else if (line.contains("Maximum Log Size")) {
			setting.put("MaxLog", line.substring(line.indexOf("=") + 1));
		} else if (line.contains("Log Level")) {
			setting.put("LogLevel", line.substring(line.indexOf("=") + 1));
		} 
	}
	File fileA = null;
	if(System.getProperty("os.name").contains("Windows"))
	fileA = new File(".\\application.properties");
	if(System.getProperty("os.name").contains("Linux"))
	fileA = new File("./application.properties");
	Scanner scnrA = null;
	try {
		scnrA = new Scanner(fileA);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
		logA.doLog("Start" , "[Start]System Settings file not found - Server Terminating", "Critical");
		System.exit(0);
	}
	while(scnrA.hasNextLine())
	{
		String lineA = scnrA.nextLine();
		if (lineA.contains("server.address")) {
			setting.put("ip", lineA.substring(lineA.indexOf("=") + 1));
		} else if (lineA.contains("server.port")) {
			setting.put("port", lineA.substring(lineA.indexOf("=") + 1));
		} 
	}
	if(setting.size() != 11)
	{
		logA.doLog("Start" , "[Start]System Settings file is incorrect - Not enough details - Server Terminating", "Critical");
		System.exit(0);
	}
}
    
    public static void configureSetting(Map<String, String> setting) throws InterruptedException, ExecutionException
    {
    	String sqlServer = "jdbc:mysql://"+setting.get("mySQLHost")+"/"+setting.get("mySQLDB")+"?user="+setting.get("mySQLUser")+"&password="+setting.get("mySQLPass");
    	String smtpH = setting.get("SMTPHost");
    	String smtpU = setting.get("SMTPUser");
    	String smtpP = setting.get("SMTPPass");
    	String ip = setting.get("ip");
    	String port = setting.get("port");
    	InterfaceController.setKeyData(sqlServer);
    	EmailController.setDetails(smtpH, smtpU, smtpP, ip, port);
    	LogHandler.limit = Integer.parseInt(setting.get("MaxLog"));
    	LogHandler.setLevel(setting.get("LogLevel"));
    	try{
    	//checkValidity();
    	}
    	catch(Exception e)
    	{
    		logA.doLog("Start" , "[Start]Boot configuration error was encountered, please confirm settings and re-try boot. System shutting down.", "Critical");
    		System.exit(0);
    	}
    }
    
    public static void checkValidity()
    {
    	try{
    		DatabaseController.SQLConnect();
    		DatabaseController.close();
    	}
    	catch(Exception e)
    	{
    		logA.doLog("Start" , "[Start]There was an error with the SQL configuration or address, please confirm details.", "Critical");
    		throw new RuntimeException(e);
    	}
    }
}
