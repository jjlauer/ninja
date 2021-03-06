/**
 * Copyright (C) 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ninja.session;

import java.util.Map;

import ninja.Context;
import ninja.Result;

import com.google.inject.ImplementedBy;


@ImplementedBy(SessionImpl.class)
public interface Session {
    
    String AUTHENTICITY_KEY = "___AT";
    String ID_KEY = "___ID";
    String TIMESTAMP_KEY = "___TS";
    
	/**
     * Has to be called initially. => maybe in the future as assisted inject.
     * 
     * @param context The context of this session.
     */
	public void init(Context context);

	/**
	 * @return id of a session.
	 */
	public String getId();

	/**
	 * @return complete content of session as immutable copy.
	 */
	public Map<String, String> getData();

	/**
	 * @return a authenticity token (may generate a new one if the session
     *         currently does not contain the token). 
	 */
	public String getAuthenticityToken();

    /**
     * To finally send this session to the user this method has to be called.
     * It basically serializes the session into the header of the response.
     * 
     * @param context The context from where to deduct a potentially existing session.
     * @param result The result where to add the session.
     */
	public void save(Context context, Result result);

    /**
     * Puts key / value into the session. 
     * PLEASE NOTICE: If value == null the key will be removed!
     * 
     * @param key Name of the key to store in the session.
     * @param value The value to store in the session
     */
	public void put(String key, String value);

	/**
	 * Returns the value of the key or null.
	 * 
	 * @param key Name of the key to retrieve.
	 * @return The value of the key or null.
	 */
	public String get(String key);

	/**
	 * Removes the value of the key and returns the value or null.
	 * 
	 * @param key name of the key to remove
	 * @return original value of the key we just removed
	 */
	public String remove(String key);

    /**
     * Removes all values from the session.
     */
	public void clear();

	/**
	 * Returns true if the session is empty, e.g. does not contain anything else
	 * than the timestamp key.
     * 
     * @return true if session does not contain any values / false if it contains
     *         values.
	 */
	public boolean isEmpty();

}
