package tcrunch.loggers;

/*	Created by:		Connor Morley
 * 	Title:			Default Logger Object
 *  Version update:	1.5
 *  Notes:			Class is used as a logger instance and is referred to in order to execute logging instances. This class is identical
 *  				across all system components.
 *  
 *  References:		N/A
 */

public interface LtA {
    public void doLog(String sentName, String logInfo, String level);
}