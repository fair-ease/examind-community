/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
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

import org.constellation.dto.service.Service;
import org.constellation.repository.ServiceRepository;
import org.constellation.database.impl.AbstractJooqTestTestCase;
import org.constellation.database.impl.TestSamples;
import org.constellation.dto.CstlUser;
import org.constellation.repository.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class JooqServicesRepositoryTestCase extends AbstractJooqTestTestCase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;


    @Test
    public void all() {
        dump(serviceRepository.findAll());
    }

    @Test
    public void findByDataId() {
        List<Service> findByDataId = serviceRepository.findByDataId(3);
        dump(findByDataId);
    }

    @Test
    public void findByDataIdentierAndType() {
        Service service = serviceRepository.findByIdentifierAndType("test", "WMS");
        dump(service);
    }

    @Test
    public void findIdentifiersByType() {
        dump(serviceRepository.findIdentifiersByType("WMS"));
    }

    @Test
    @Transactional()
    public void crud() {

        // no removeAll method
        List<Service> all = serviceRepository.findAll();
        for (Service p : all) {
            serviceRepository.delete(p.getId());
        }
        all = serviceRepository.findAll();
        Assert.assertTrue(all.isEmpty());

        CstlUser owner = userRepository.create(TestSamples.newAdminUser());
        Assert.assertNotNull(owner);
        Assert.assertNotNull(owner.getId());

        Integer sid = serviceRepository.create(TestSamples.newService(owner.getId()));
        Assert.assertNotNull(sid);

        Service s = serviceRepository.findById(sid);
        Assert.assertNotNull(s);

        serviceRepository.delete(sid);

        s = serviceRepository.findById(sid);
        Assert.assertNull(s);
    }

}
