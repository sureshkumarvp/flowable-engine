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
package org.activiti.conf;

import javax.sql.DataSource;

import org.flowable.engine.common.runtime.Clock;
import org.flowable.idm.api.IdmIdentityService;
import org.flowable.idm.api.IdmManagementService;
import org.flowable.idm.engine.IdmEngine;
import org.flowable.idm.engine.IdmEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class ActivitiIdmEngineConfiguration {

    @Autowired
    private DataSource dataSource;
    
    @Bean(name="idmEngine")
    public IdmEngine idmEngine() {
        return idmEngineConfiguration().buildIdmEngine();
    }
    
    @Bean(name="idmEngineConfiguration")
    public IdmEngineConfiguration idmEngineConfiguration() {
      IdmEngineConfiguration idmEngineConfiguration = new IdmEngineConfiguration();
      idmEngineConfiguration.setDataSource(dataSource);
      idmEngineConfiguration.setDatabaseSchemaUpdate(IdmEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
    	
    	return idmEngineConfiguration;
    }
    
    @Bean(name="clock")
    @DependsOn("idmEngine")
    public Clock getClock() {
    	return idmEngineConfiguration().getClock();
    }
    
    @Bean
    public IdmIdentityService idmIdentityService() {
    	return idmEngine().getIdmIdentityService();
    }
    
    @Bean
    public IdmManagementService idmManagementService() {
    	return idmEngine().getIdmManagementService();
    }
}
