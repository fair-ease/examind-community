/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2019 Geomatys.
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
package org.constellation.database.impl.repository;

import java.util.List;
import org.constellation.database.impl.AbstractJooqTestTestCase;
import org.constellation.database.impl.TestSamples;
import org.constellation.dto.CstlUser;
import org.constellation.dto.Sensor;
import org.constellation.repository.SensorRepository;
import org.constellation.repository.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class JooqSensorRepositoryTestCase extends AbstractJooqTestTestCase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Test
    @Transactional()
    public void crude() {

        // no removeAll method
        sensorRepository.deleteAll();
        List<Sensor> all = sensorRepository.findAll();
        Assert.assertTrue(all.isEmpty());

        CstlUser owner = userRepository.create(TestSamples.newAdminUser());
        Assert.assertNotNull(owner);
        Assert.assertNotNull(owner.getId());

        Sensor s = sensorRepository.create(TestSamples.newSensor(owner.getId(), "ds1"));
        Assert.assertNotNull(s);

        s = sensorRepository.findById(s.getId());
        Assert.assertNotNull(s);

        sensorRepository.delete(s.getIdentifier());

        s = sensorRepository.findById(s.getId());
        Assert.assertNull(s);
    }

}