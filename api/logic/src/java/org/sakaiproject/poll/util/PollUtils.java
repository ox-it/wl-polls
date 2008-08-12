package org.sakaiproject.poll.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PollUtils {

	private static Log log = LogFactory.getLog(PollUtils.class);
	
	

	   public static String ENDING_P_SPACE_TAGS = "<p>&nbsp;</p>";
	   public static String STARTING_P_TAG = "<p>";
	   public static String ENDING_P_TAG = "</p>";

	   /**
	    * Attempts to remove all unnecessary P tags from html strings
	    * 
	    * @param cleanup an html string to cleanup
	    * @return the cleaned up string
	    */
	   public static String cleanupHtmlPtags(String cleanup) {
		      if (cleanup == null) {
		         // nulls are ok
		         return null;
		      } else if (cleanup.trim().length() == 0) {
		         // nothing to do
		         return cleanup;
		      }
		      cleanup = cleanup.trim();

		      if (cleanup.length() > ENDING_P_SPACE_TAGS.length()) {
		         // (remove trailing blank lines)
		         // - While (cleanup ends with "<p>&nbsp;</p>") remove trailing "<p>&nbsp;</p>".
		         while (cleanup.toLowerCase().endsWith(ENDING_P_SPACE_TAGS)) {
		            // chop off the end
		            cleanup = cleanup.substring(0, cleanup.length() - ENDING_P_SPACE_TAGS.length()).trim();
		         }
		      }

		      if (cleanup.length() > (STARTING_P_TAG.length() + ENDING_P_TAG.length())) {
		         // (remove a single set of <p> tags)
		         // if cleanup starts with "<p>" and cleanup ends with "</p>" and, remove leading "<p>" and trailing "</p>" from cleanup
		         String lcCheck = cleanup.toLowerCase();
		         if (lcCheck.startsWith(STARTING_P_TAG) 
		               && lcCheck.endsWith(ENDING_P_TAG)) {
		            if (lcCheck.indexOf(STARTING_P_TAG, STARTING_P_TAG.length()) == -1 
		                  && lcCheck.lastIndexOf(ENDING_P_TAG, lcCheck.length() - ENDING_P_TAG.length() - 1) == -1) {
		               // chop off the front and end P tags
		               cleanup = cleanup.substring(STARTING_P_TAG.length(), cleanup.length() - ENDING_P_TAG.length()).trim();
		            }
		         }
		      }

		      return cleanup;
		   }
	   
	   
		public static boolean isValidEmail(String email) {
			// TODO: Use a generic Sakai utility class (when a suitable one exists)
			
			if (email == null || email.equals(""))
				return false;
			
			email = email.trim();
			//must contain @
			if (email.indexOf("@") == -1)
				return false;
			
			//an email can't contain spaces
			if (email.indexOf(" ") > 0)
				return false;
			
			//"^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*$" 
			if (email.matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*$")) 
				return true;
		
			log.warn(email + " is not a valid eamil address");
			return false;
		}
	   
}
