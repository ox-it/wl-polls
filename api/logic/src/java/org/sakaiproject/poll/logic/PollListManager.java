/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/uct/PollTool/trunk/api/logic/src/java/org/sakaiproject/poll/logic/PollListManager.java $
 * $Id: PollListManager.java 3783 2007-03-05 06:04:22Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.poll.logic;

import java.util.List;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;

/**
 * This is the interface for the Manager for our poll tool, 
 * it handles the data access functionality of the tool, we currently
 * have 2 implementations (memory and hibernate)
 * @author DH
 *
 */
public interface PollListManager extends EntityProducer {

//	the permissions
	  
	  public static final String PERMISSION_VOTE = "poll.vote";
	  public static final String PERMISSION_ADD = "poll.add";
	  public static final String PERMISSION_DELETE_OWN = "poll.deleteOwn";
	  public static final String PERMISSION_DELETE_ANY = "poll.deleteAny";
	  public static final String PERMISSION_EDIT_ANY = "poll.editAny";
	  public static final String PERMISSION_EDIT_OWN = "poll.editOwn";
	
	/**
	 *  Save a poll
	 * @param t - the poll object to save
	 * @return - true for success, false if failure
	 */
	public boolean savePoll(Poll t);

	/**
	 * Delete a poll
	 * @param t - the poll object to remove
	 * @return - true for success, false if failure
	 */
	public boolean deletePoll(Poll t) throws PermissionException;

	public boolean saveOption(Option t);

	/**
	 * Gets all the task objects for the site
	 * @param siteId - the siteId of the site
	 * @return - a collection of task objects (empty collection if none found)
	 */
	public List findAllPolls(String siteId);
	
	/**
	 * Retrieve a specific poll
	 * @param pollId
	 * @return a single poll object
	 */
	public Poll getPollById(Long pollId);
	
	/**
	 * Get a specific poll with all its votes
	 * @param pollId
	 * @return a poll object
	 */
	public Poll getPollWithVotes(Long pollId);

	/**
	 *  Get a specific option by its id
	 */
	public Option getOptionById(Long optionId);
	
	public void deleteOption(Option option);
}
