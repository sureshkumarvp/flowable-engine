/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti5.standalone.event;

import org.activiti5.engine.delegate.event.impl.ActivitiEventImpl;
import org.activiti5.engine.impl.test.ResourceActivitiTestCase;
import org.activiti5.engine.test.api.event.TestActivitiEventListener;
import org.flowable.engine.common.api.delegate.event.FlowableEvent;
import org.flowable.engine.delegate.event.FlowableEngineEventType;

/**
 * Test to verify event-listeners, which are configured in the cfg.xml, are notified.
 * 
 * @author Frederik Heremans
 */
public class EventListenersConfigurationTest extends ResourceActivitiTestCase {

  public EventListenersConfigurationTest() {
    super("org/activiti5/standalone/event/activiti-eventlistener.cfg.xml");
  }
  
  public void testEventListenerConfiguration() {
  	// Fetch the listener to check received events
  	TestActivitiEventListener listener = (TestActivitiEventListener) processEngineConfiguration.getBeans().get("eventListener");
  	assertNotNull(listener);
  	
  	// Clear any events received (eg. engine initialisation)
    listener.clearEventsReceived();
  	
  	// Dispath a custom event
  	FlowableEvent event = new ActivitiEventImpl(FlowableEngineEventType.CUSTOM);
  	processEngineConfiguration.getEventDispatcher().dispatchEvent(event);
  	
  	assertEquals(1, listener.getEventsReceived().size());
  	assertEquals(event, listener.getEventsReceived().get(0));
  }
}