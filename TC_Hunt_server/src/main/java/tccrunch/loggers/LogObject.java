package tccrunch.loggers;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*	Created by:		Connor Morley
 * 	Title:			Default Logger Level Control
 *  Version update:	1.5
 *  Notes:			Class is used to control which log level particular messages are logged as. These log levels correspond to that 
 *  				configured within the log handler. This class implements the log object class LtA. This class is identical across
 *  				all system components.
 *  
 *  References:		N/A
 */

public class LogObject implements LtA {
	@Override
    public void doLog(String sentName, String logInfo, String level) {
		if(level.equals("Info"))
		{
        try {
            LogHandler.getLogger(sentName).log(Level.INFO, logInfo);
        } catch (IOException ex) {
            Logger.getLogger(sentName).log(Level.SEVERE, null, ex);
        }
		}
		else if(level.equals("Warning"))
		{
	        try {
	            LogHandler.getLogger(sentName).log(Level.WARNING, logInfo);
	        } catch (IOException ex) {
	            Logger.getLogger(sentName).log(Level.SEVERE, null, ex);
	        }
		}
		else if(level.equals("Critical"))
		{
	        try {
	            LogHandler.getLogger(sentName).log(Level.SEVERE, logInfo);
	        } catch (IOException ex) {
	            Logger.getLogger(sentName).log(Level.SEVERE, null, ex);
	        }
		}
    }
}