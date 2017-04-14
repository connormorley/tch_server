package tcrunch.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import tccrunch.loggers.LogHandler;
import tccrunch.loggers.LogObject;
import tccrunch.loggers.LtA;


/*	Created by:		Connor Morley
 *  Date:			27/01/2017
 * 	Title:			TCrunch Control Server
 *  Version:		2.3
 *  Notes:			Main class - Controls settings file read and configuration and preliminary checks before enabling the server to establish outside communication.
 *  
 *  References:		> "File > New > Import Spring Getting Started Content > Rest Server" = Spring boot configuration obtained through this function.  
 *  				  This provided the default gradle files, pom.xml and three class files which were this class with only the main method and one line;
 *  				  a basic version if what is now named the "InterfaceController" with only one entry and a third object class used to create an example output
 *  				  from an http request. Apart from the gradle files and pom.xml, none of the original code has remained the same except for the line:
 *  				  "SpringApplication.run(StartController.class);" which is used to initiated the server.
 *  
 *   				> https://spring.io/guides/gs/sts/ = Guide to using the "Import Spring Getting Started Content" function.
 */



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
	
	public static Map<String, String> setting = new HashMap<String,String>();
	private final static Logger logger = Logger.getLogger(StartController.class.getName());
	private static LtA logA = new LogObject();
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		System.out.print("");
		System.out.print("\nd888888P  a88888b.  888888ba  dP     dP 888888ba   a88888b. dP     dP  \n"
				+ "   88    d8'   `88  88    `8b 88     88 88    `8b d8'   `88 88     88  \n"
				+ "   88    88        a88aaaa8P' 88     88 88     88 88        88aaaaa88a \n"
				+ "   88    88         88   `8b. 88     88 88     88 88        88     88  \n"
				+ "   88    Y8.   .88  88     88 Y8.   .8P 88     88 Y8.   .88 88     88  \n"
				+ "   dP     Y88888P'  dP     dP `Y88888P' dP     dP  Y88888P' dP     dP \n" + "\n"
				+ "<< Version 2.7>>      << Created by C.Morley 2016/2017 >>\n" + "\n");
		start();
	}

	public static void start() throws InterruptedException, ExecutionException {
		readSettingsFile();
		configureSetting(setting);
		logA.doLog("Start", "[Start]Server Boot variables passed verification", "Info");
		SpringApplication.run(StartController.class);
	}

	public static void readSettingsFile() {
		File file = null;
		if (System.getProperty("os.name").contains("Windows"))
			file = new File(".\\settings.properties");
		if (System.getProperty("os.name").contains("Linux"))
			file = new File("./settings.properties");
		Properties prop = new Properties();

		try {
			InputStream input = new FileInputStream(file);
			prop.load(input);
			setting.put("mysqlAddress", prop.getProperty("MySQL_Host"));
			setting.put("mysqlDbName", prop.getProperty("MySQL_Database_Name"));
			setting.put("mysqlUser", prop.getProperty("MySQL_Username"));
			setting.put("mysqlPassword", prop.getProperty("MySQL_Password"));
			if (prop.getProperty("Use_email_notify").equals("yes")) {
				setting.put("smtpHost", prop.getProperty("SMTP_Host"));
				setting.put("smtpUser", prop.getProperty("SMTP_Username"));
				setting.put("smtpPass", prop.getProperty("SMTP_Password"));
				setting.put("targetEmail", prop.getProperty("Target_Email"));
			}
			setting.put("maxLog", prop.getProperty("Maximum_Log_Size"));
			setting.put("logLevel", prop.getProperty("Log_Level"));
			setting.put("spass", prop.getProperty("Server_Password"));
			if (!prop.getProperty("Use_email_notify").equals("yes")) {
				if (setting.size() != 7) {
					logA.doLog("Start",
							"[Start]Settings are incorrect, please check the settings properties file. - TERMINATING",
							"Critical");
					System.exit(0);
				}
			} else if (setting.size() != 11) {
				logA.doLog("Start",
						"[Start]Settings are incorrect, please check the settings properties file. - TERMINATING",
						"Critical");
				System.exit(0);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		File fileA = null;
		if (System.getProperty("os.name").contains("Windows"))
			fileA = new File(".\\application.properties");
		if (System.getProperty("os.name").contains("Linux"))
			fileA = new File("./application.properties");
		Scanner scnrA = null;
		try {
			scnrA = new Scanner(fileA);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logA.doLog("Start", "[Start]System Settings file not found - Server Terminating", "Critical");
			System.exit(0);
		}
		while (scnrA.hasNextLine()) {
			String lineA = scnrA.nextLine();
			if (lineA.contains("server.address")) {
				setting.put("ip", lineA.substring(lineA.indexOf("=") + 1));
			} else if (lineA.contains("server.port")) {
				setting.put("port", lineA.substring(lineA.indexOf("=") + 1));
			}
		}
		if (!prop.getProperty("Use_email_notify").equals("yes") && setting.size() != 9) {
			logA.doLog("Start",
					"[Start]Error in the application properties file. Please check the applicaiton settings. - TERMINATING",
					"Critical");
			System.exit(0);
		} else if (setting.size() != 13) {
			logA.doLog("Start",
					"[Start]Error in the application properties file. Please check the applicaiton settings. - TERMINATING",
					"Critical");
			System.exit(0);
		}
	}
    
	public static void configureSetting(Map<String, String> setting) throws InterruptedException, ExecutionException {
		String sqlServer = "jdbc:mysql://" + setting.get("mysqlAddress") + "/" + setting.get("mysqlDbName") + "?user="
				+ setting.get("mysqlUser") + "&password=" + setting.get("mysqlPassword");
		String ip = setting.get("ip");
		String port = setting.get("port");
		InterfaceController.setKeyData(sqlServer);
		if (setting.size() == 13) {
			String smtpH = setting.get("smtpHost");
			String smtpU = setting.get("smtpUser");
			String smtpP = setting.get("smtpPass");
			EmailController.setDetails(smtpH, smtpU, smtpP, ip, port);
			InterfaceController.emailNotify = true;
		}
		LogHandler.limit = Integer.parseInt(setting.get("maxLog"));
		LogHandler.setLevel(setting.get("logLevel"));
		InterfaceController.serverPassword = setting.get("spass");
		try {
			checkValidity();
		} catch (Exception e) {
			logA.doLog("Start",
					"[Start]Boot configuration error was encountered, please confirm settings and re-try boot. - TERMINATING",
					"Critical");
			System.exit(0);
		}
		checkForRecoveredAttack();
	}
    
	public static void checkForRecoveredAttack() {
		boolean recovered = DatabaseController.checkRecoveredAttack();
		if (recovered) {
			logA.doLog("Start" , "[Start]Termination during attack detected, initiating attack recovery process.", "Info");
			InterfaceController.recovering = true;
			try {
				DatabaseController.checkForRecoveredFailedSequences();
				BufferedReader br = new BufferedReader(new FileReader("testingTCFile"));
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();

				while (line != null) {
					sb.append(line);
					line = br.readLine();
				}
				br.close();
				AttackController.target = sb.toString();
			} catch (IOException e) {
				logA.doLog("Start" , "[Start]Recovery of attack data failed, please examine relative data and restart. (alternative to correct db to no running attack). - TERMINATING", "Critical");
				System.exit(0);
			}
			logA.doLog("Start" , "[Start]Attack recovery process successful. Attack ID " + AttackController.attackID.get() + " has been reinitialized.", "Info");
		}
		else{
			DatabaseController.getLastAID(); // Set Attack ID to that of the last running attack, as such the next issued attack will have the next incremented ID
		}
	}
    
	public static void checkValidity() {
		try {
			Connection conn = DatabaseController.SQLConnect();
			DatabaseController.close(conn);
		} catch (Exception e) {
			logA.doLog("Start",
					"[Start]There was an error with the SQL configuration or address, please confirm details.",
					"Critical");
			throw new RuntimeException(e);
		}
	}
}
