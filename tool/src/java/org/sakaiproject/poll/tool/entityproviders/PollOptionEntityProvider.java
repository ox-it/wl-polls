/**
 * $Id$
 * $URL$
 * VoteEntityProvider.java - polls - Aug 22, 2008 9:50:39 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.poll.tool.entityproviders;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;


/**
 * Entity provider which represents poll votes
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class PollOptionEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, RESTful {

    private PollListManager pollListManager;
    public void setPollListManager(PollListManager pollListManager) {
        this.pollListManager = pollListManager;
    }

    public static String PREFIX = "poll-option";
    public String getEntityPrefix() {
        return PREFIX;
    }

    public boolean entityExists(String id) {
        if (id == null) {
            return false;
        }
        if ("".equals(id)) {
            return true;
        }
        Option option = getOptionById(id);
        boolean exists = (option != null);
        return exists;
    }

    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        String userRef = developerHelperService.getCurrentUserReference();
        if (userRef == null) {
            throw new SecurityException("user must be logged in to create new options");
        }
        Option option = (Option) entity;
        if (option.getPollId() == null) {
            throw new IllegalArgumentException("Poll Id must be set to create an option");
        }
        Long pollId = option.getPollId();
        if (option.getOptionText() == null) {
            throw new IllegalArgumentException("Poll Option text must be set to create an option");
        }
        // validate poll
        Poll poll = pollListManager.getPollById(pollId, false);
        if (poll == null) {
            throw new IllegalArgumentException("Invalid poll id ("+pollId+"), could not find poll");
        }
        // check permissions
        String siteRef = "/site/" + poll.getSiteId();
        if (! developerHelperService.isUserAllowedInEntityReference(userRef, PollListManager.PERMISSION_ADD, siteRef)) {
            throw new SecurityException("User ("+userRef+") is not allowed to add options in this poll ("+pollId+")");
        }
        // set default values
        option.setUUId( UUID.randomUUID().toString() );
        boolean saved = pollListManager.saveOption(option);
        if (!saved) {
            throw new IllegalStateException("Unable to save option ("+option+") for user ("+userRef+"): " + ref);
        }
        return option.getId()+"";
    }

    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        String id = ref.getId();
        if (id == null) {
            throw new IllegalArgumentException("The reference must include an id for updates (id is currently null)");
        }
        String userReference = developerHelperService.getCurrentUserReference();
        if (userReference == null) {
            throw new SecurityException("anonymous user cannot update option: " + ref);
        }
        Option current = getOptionById(id);
        if (current == null) {
            throw new IllegalArgumentException("No option found to update for the given reference: " + ref);
        }
        Option option = (Option) entity;
        String location = developerHelperService.getCurrentLocationReference();
        // should this check a different permission?
        boolean allowed = developerHelperService.isUserAllowedInEntityReference(userReference, PollListManager.PERMISSION_ADD, location);
        if (!allowed) {
            throw new SecurityException("Current user ("+userReference+") cannot update poll options in location ("+location+")");
        }
        developerHelperService.copyBean(option, current, 0, new String[] {"id", "pollId", "UUId"}, true);
        pollListManager.saveOption(current);
    }

    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
        String id = ref.getId();
        String userReference = developerHelperService.getCurrentUserReference();
        if (userReference == null) {
            throw new SecurityException("anonymous user cannot delete option: " + ref);
        }
        Option option = getOptionById(id);
        if (option == null) {
            throw new IllegalArgumentException("No option found to delete for the given reference: " + ref);
        }
        String location = developerHelperService.getCurrentLocationReference();
        // should this check a different permission?
        boolean allowed = developerHelperService.isUserAllowedInEntityReference(userReference, PollListManager.PERMISSION_ADD, location);
        if (!allowed) {
            throw new SecurityException("Current user ("+userReference+") cannot update poll options in location ("+location+")");
        }
        pollListManager.deleteOption(option);
    }

    public Object getSampleEntity() {
        return new Option();
    }

    public Object getEntity(EntityReference ref) {
        String id = ref.getId();
        if (id == null) {
            return new Option();
        }
        String currentUser = developerHelperService.getCurrentUserReference();
        if (currentUser == null) {
            throw new SecurityException("Anonymous users cannot view specific votes: " + ref);
        }
        Option option = getOptionById(id);
        if (developerHelperService.isEntityRequestInternal(ref.toString())) {
            // ok to retrieve internally
        } else {
            // need to security check
            if (developerHelperService.isUserAdmin(currentUser)) {
                // ok to view this vote
            } else {
                // not allowed to view
                throw new SecurityException("User ("+currentUser+") cannot view option ("+ref+")");
            }
        }
        return option;
    }

    public List<?> getEntities(EntityReference ref, Search search) {
        String currentUser = developerHelperService.getCurrentUserReference();
        if (currentUser == null) {
            throw new SecurityException("Anonymous users cannot view poll options: " + ref);
        }
        // get the pollId
        Restriction pollRes = search.getRestrictionByProperty("pollId");
        if (pollRes == null || pollRes.getSingleValue() == null) {
            throw new IllegalArgumentException("Must include a non-null pollId in order to retreive a list of votes");
        }
        Long pollId = null;
        try {
            pollId = developerHelperService.convert(pollRes.getSingleValue(), Long.class);
        } catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException("Invalid: pollId must be a long number: " + e.getMessage(), e);
        }
        // get the poll
        Poll poll = pollListManager.getPollById(pollId);
        if (poll == null) {
            throw new IllegalArgumentException("pollId ("+pollId+") is invalid and does not match any known polls");
        }
        // get the options
        List<Option> options = pollListManager.getOptionsForPoll(pollId);
        return options;
    }

    public String[] getHandledOutputFormats() {
        return new String[] {Formats.XML, Formats.JSON};
    }

    public String[] getHandledInputFormats() {
        return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
    }


    /**
     * @param id
     * @return
     */
    private Option getOptionById(String id) {
        Long optionId;
        try {
            optionId = new Long(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert id ("+id+") to long: " + e.getMessage(), e);
        }
        Option option = pollListManager.getOptionById(optionId);
        return option;
    }

}