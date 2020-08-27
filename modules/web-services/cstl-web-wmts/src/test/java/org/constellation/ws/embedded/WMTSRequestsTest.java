/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2020 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.constellation.ws.embedded;

import org.constellation.configuration.ConfigDirectory;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.TestRunner;
import static org.constellation.ws.embedded.AbstractGrizzlyServer.controllerConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(TestRunner.class)
public class WMTSRequestsTest extends AbstractGrizzlyServer {
    
    private static boolean initialized = false;

    @BeforeClass
    public static void startup() {
        ConfigDirectory.setupTestEnvironement("WMTSRequestTest");
        controllerConfiguration = WMTSControllerConfig.class;
    }
    
    @Test
    @Order(order=1)
    public void testGetCapabilities() throws Exception {
        // TODO
    }
}