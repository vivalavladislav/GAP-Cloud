package com.google.devrel.training.conference.spi;

import static com.google.devrel.training.conference.service.OfyService.ofy;

import java.text.Normalizer.Form;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.devrel.training.conference.Constants;
import com.google.devrel.training.conference.domain.Profile;
import com.google.devrel.training.conference.form.ProfileForm;
import com.google.devrel.training.conference.form.ProfileForm.TeeShirtSize;
import com.googlecode.objectify.Key;


/**
 * Defines conference APIs.
 */
@Api(name = "conference", version = "v1", scopes = { Constants.EMAIL_SCOPE }, clientIds = {
        Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID }, description = "API for the Conference Central Backend application.")
public class ConferenceApi {

    /*
     * Get the display name from the user's email. For example, if the email is
     * lemoncake@example.com, then the display name becomes "lemoncake."
     */
    private static String extractDefaultDisplayNameFromEmail(String email) {
        return email == null ? null : email.substring(0, email.indexOf("@"));
    }

    /**
     * Creates or updates a Profile object associated with the given user
     * object.
     *
     * @param user
     *            A User object injected by the cloud endpoints.
     * @param profileForm
     *            A ProfileForm object sent from the client form.
     * @return Profile object just created.
     * @throws UnauthorizedException
     *             when the User object is null.
     */

    // Declare this method as a method available externally through Endpoints
    @ApiMethod(name = "saveProfile", path = "profile", httpMethod = HttpMethod.POST)
    // The request that invokes this method should provide data that
    // conforms to the fields defined in ProfileForm

    // TODO 1 Pass the ProfileForm parameter
    // TODO 2 Pass the User parameter
    
	public Profile saveProfile(ProfileForm profileForm, User user) throws UnauthorizedException {
        // TODO 2
        // If the user is not logged in, throw an UnauthorizedException
    	if ( user == null || profileForm == null ){
    		throw new UnauthorizedException( "user == null");
    	}
    	
    	Profile result = null;
    	result = tryUpdate( user, profileForm) ;
    	
    	if ( result != null ) {
    		return result;
    	}
    	
        String userId = user.getUserId();
        String mainEmail = user.getEmail();
       
        TeeShirtSize teeShirtSize = profileForm.getTeeShirtSize();
        if ( teeShirtSize == null ){
        	teeShirtSize = TeeShirtSize.NOT_SPECIFIED;
        }
        
        String displayName = profileForm.getDisplayName();
        if ( displayName == null ){
        	displayName = extractDefaultDisplayNameFromEmail(user.getEmail());
        }
        
       
        Profile profile = new Profile(userId, displayName, mainEmail, teeShirtSize);

        // TODO 3 (In Lesson 3)
        // Save the Profile entity in the datastore

        ofy().save().entity(profile).now();
        
        // Return the profile
        
        return profile;
    }

    private Profile tryUpdate(final User user, ProfileForm form){
    	String userId = user.getUserId(); 
        Key<Profile> key = Key.create( Profile.class, userId); 
     
        Profile profile = null;
        
        try{
        	profile = ofy().load().key(key).now(); // TODO load the Profile entity
        }
        catch( Exception e){
        	// profile hasn't been saved
        	return null;
        }
        
        if (profile == null){
        	return null;
        }
        
        String displayName = form.getDisplayName() == null ? profile.getDisplayName() : form.getDisplayName();
        TeeShirtSize size = form.getTeeShirtSize() == null ? profile.getTeeShirtSize() : form.getTeeShirtSize();
        
        profile.update(displayName, size);
        
        ofy().save().entity(profile).now();
    	return profile;
    }
    
    /**
     * Returns a Profile object associated with the given user object. The cloud
     * endpoints system automatically inject the User object.
     *
     * @param user
     *            A User object injected by the cloud endpoints.
     * @return Profile object.
     * @throws UnauthorizedException
     *             when the User object is null.
     */
    @ApiMethod(name = "getProfile", path = "profile", httpMethod = HttpMethod.GET)
    public Profile getProfile(final User user) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }

        
        String userId = user.getUserId(); // TODO
        Key<Profile> key = Key.create( Profile.class, userId); // TODO
        Profile profile = ofy().load().key(key).now(); // TODO load the Profile entity
        return profile;
    }
}
