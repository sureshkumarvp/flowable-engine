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

package org.activiti.rest.service.api.form;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.runtime.process.ProcessInstanceResponse;
import org.flowable.engine.FormService;
import org.flowable.engine.common.api.FlowableException;
import org.flowable.engine.common.api.FlowableIllegalArgumentException;
import org.flowable.engine.common.api.FlowableObjectNotFoundException;
import org.flowable.engine.form.FormData;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tijs Rademakers
 */
@RestController
@Api(tags = { "Forms" }, description = "Manage Forms")
public class FormDataResource {

  @Autowired
  protected RestResponseFactory restResponseFactory;

  @Autowired
  protected FormService formService;


  @ApiOperation(value = "Get form data", tags = { "Forms" }, notes = "")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Indicates that form data could be queried."),
          @ApiResponse(code = 404, message = "Indicates that form data could not be found.") })
  @RequestMapping(value = "/form/form-data", method = RequestMethod.GET, produces = "application/json")
  public FormDataResponse getFormData(@RequestParam(value = "taskId", required = false) String taskId,
          @RequestParam(value = "processDefinitionId", required = false) String processDefinitionId, HttpServletRequest request) {

    if (taskId == null && processDefinitionId == null) {
      throw new FlowableIllegalArgumentException("The taskId or processDefinitionId parameter has to be provided");
    }

    if (taskId != null && processDefinitionId != null) {
      throw new FlowableIllegalArgumentException("Not both a taskId and a processDefinitionId parameter can be provided");
    }

    FormData formData = null;
    String id = null;
    if (taskId != null) {
      formData = formService.getTaskFormData(taskId);
      id = taskId;
    } else {
      formData = formService.getStartFormData(processDefinitionId);
      id = processDefinitionId;
    }

    if (formData == null) {
      throw new FlowableObjectNotFoundException("Could not find a form data with id '" + id + "'.", FormData.class);
    }

    return restResponseFactory.createFormDataResponse(formData);
  }

  @ApiOperation(value = "Submit task form data", tags = { "Forms" })
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Indicates request was successful and the form data was submitted"),
          @ApiResponse(code = 400, message = "Indicates an parameter was passed in the wrong format. The status-message contains additional information.") })
  @RequestMapping(value = "/form/form-data", method = RequestMethod.POST, produces = "application/json")
  public ProcessInstanceResponse submitForm(@RequestBody SubmitFormRequest submitRequest, HttpServletRequest request, HttpServletResponse response) {

    if (submitRequest == null) {
      throw new FlowableException("A request body was expected when executing the form submit.");
    }

    if (submitRequest.getTaskId() == null && submitRequest.getProcessDefinitionId() == null) {
      throw new FlowableIllegalArgumentException("The taskId or processDefinitionId property has to be provided");
    }

    Map<String, String> propertyMap = new HashMap<String, String>();
    if (submitRequest.getProperties() != null) {
      for (RestFormProperty formProperty : submitRequest.getProperties()) {
        propertyMap.put(formProperty.getId(), formProperty.getValue());
      }
    }

    if (submitRequest.getTaskId() != null) {
      formService.submitTaskFormData(submitRequest.getTaskId(), propertyMap);
      response.setStatus(HttpStatus.NO_CONTENT.value());
      return null;

    } else {
      ProcessInstance processInstance = null;
      if (submitRequest.getBusinessKey() != null) {
        processInstance = formService.submitStartFormData(submitRequest.getProcessDefinitionId(), submitRequest.getBusinessKey(), propertyMap);
      } else {
        processInstance = formService.submitStartFormData(submitRequest.getProcessDefinitionId(), propertyMap);
      }
      return restResponseFactory.createProcessInstanceResponse(processInstance);
    }
  }
}
