package tcrunch.controllers;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import tcrunch.loggers.LogObject;
import tcrunch.loggers.LtA;

/*	Created by:		Connor Morley
 * 	Title:			TCrunch Server Email Service Controller
 *  Version update:	2.0
 *  Notes:			Class is responsible for handling the email notifications issued when an attack operation is concluded. Emails are sent from a configured gmail 
 *  				account made specifically for the server. The destination address is specified in the settings properties file associated with the server. Email
 *  				notifications contain the id of the attack that has finished and the result of the attack operation.
 *  
 *  References:		N/A
 */

public class EmailController {

    private static String SMTP_HOST_NAME; 
    private static String SMTP_AUTH_USER; 
    private static String SMTP_AUTH_PWD;
    private static String IP;
    private static String PORT;
    static LtA logA = new LogObject();

    public static void sendMail(int id, String res) throws Exception{
       new EmailController().send(id, res);
    }

    public void send(int ID, String res){
    	try{
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST_NAME);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");

        Authenticator auth = new SMTPAuthenticator();
        Session mailSession = Session.getDefaultInstance(props, auth);
        Transport transport = mailSession.getTransport();

        MimeMessage message = new MimeMessage(mailSession);
        message.setSubject("Test result Id : " + ID);
        message.setContent("Hello, \nThe attack with ID : " + ID + " has concluded. \nThe result was : " + res, "text/plain");
        message.setFrom(new InternetAddress("tcrunchserver@gmail.com"));
        message.addRecipient(Message.RecipientType.TO,
             new InternetAddress("mc384@greenwich.ac.uk"));

        transport.connect();
        transport.sendMessage(message,
            message.getRecipients(Message.RecipientType.TO));
        transport.close();
    	}
    	catch(Exception e){
    		logA.doLog("Email" , "[Email]Error in creation/transmission of Email \nError is : " + e.toString(), "Critical");
    	}
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
           String username = SMTP_AUTH_USER;
           String password = SMTP_AUTH_PWD;
           return new PasswordAuthentication(username, password);
        }
    }
    
    public static void setDetails(String H, String U, String P, String address, String prt)
    {
    	SMTP_HOST_NAME = H;
    	SMTP_AUTH_USER = U;
    	SMTP_AUTH_PWD = P;
    	IP = address;
    	PORT = prt;
    }
}