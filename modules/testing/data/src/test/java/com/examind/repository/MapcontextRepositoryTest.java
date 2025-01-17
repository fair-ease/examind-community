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
package com.examind.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.constellation.dto.CstlUser;
import org.constellation.dto.Data;
import org.constellation.dto.Layer;
import org.constellation.dto.MapContextDTO;
import org.constellation.dto.AbstractMCLayerDTO;
import org.constellation.dto.InternalServiceMCLayerDTO;
import org.constellation.repository.LayerRepository;
import org.constellation.repository.MapContextRepository;
import org.constellation.repository.DataRepository;
import org.constellation.repository.ProviderRepository;
import org.constellation.repository.SensorRepository;
import org.constellation.repository.ServiceRepository;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MapcontextRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private SensorRepository sensorRepository;
    
    @Autowired
    private MapContextRepository mapcontextRepository;

    @Autowired
    private LayerRepository layerRepository;

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    public void crude() {

        CstlUser owner = getOrCreateUser();
        Assert.assertNotNull(owner);
        Assert.assertNotNull(owner.getId());

        sensorRepository.deleteAll();
        layerRepository.deleteAll();
        dataRepository.deleteAll();
        providerRepository.deleteAll();
        serviceRepository.deleteAll();
        mapcontextRepository.deleteAll();

        List<MapContextDTO> all  = mapcontextRepository.findAll(false);
        Assert.assertTrue(all.isEmpty());

        int pid = providerRepository.create(TestSamples.newProvider2(owner.getId()));
        int did1 = dataRepository.create(TestSamples.newDataQuote(owner.getId(), pid, null));
        int sid1 = serviceRepository.create(TestSamples.newServiceQuote(owner.getId()));
        int lid1 = layerRepository.create(TestSamples.newLayer4(owner.getId(), did1, sid1));

        Layer l1 = layerRepository.findById(lid1);
        
        Data db = dataRepository.findById(did1);

        Integer mpid1 = mapcontextRepository.create(TestSamples.newMapcontext(owner, "mp", "desc"));
        Assert.assertNotNull(mpid1);

        MapContextDTO mp1 = mapcontextRepository.findById(mpid1, true);
        Assert.assertNotNull(mp1);

        /*
         * layers
         */
        AbstractMCLayerDTO mpl =  new InternalServiceMCLayerDTO(1, l1.getName(), 0, 100, true, l1.getId(), null, null, l1.getDate(), db.getType(), owner.getLogin(), l1.getDataId(), null, null, null);
        List<AbstractMCLayerDTO> layers = new ArrayList<>();
        layers.add(mpl);
        mapcontextRepository.setLinkedLayers(mpid1, layers);

        layers = mapcontextRepository.getLinkedLayers(mpid1, true);
        Assert.assertNotNull(layers);
        Assert.assertEquals(1, layers.size());

        AbstractMCLayerDTO resMpl = layers.get(0);
        mpl.setId(resMpl.getId());
        Assert.assertEquals(mpl.getName(), layers.get(0).getName());
        Assert.assertEquals(mpl, layers.get(0));

        Integer mpid2 = mapcontextRepository.create(TestSamples.newMapcontext(owner, "mp';", "'; delete * from admin.mp;'"));
        Assert.assertNotNull(mpid2);

        MapContextDTO mp2 = mapcontextRepository.findById(mpid2, true);
        Assert.assertNotNull(mp2);

        /**
         * search
         */

        final Map<String,Object> filterMap = new HashMap<>();
        final Map.Entry<String,String> sortEntry = null;

        filterMap.put("owner", owner.getId());
        Map.Entry<Integer, List<MapContextDTO>> results = mapcontextRepository.filterAndGet(filterMap, sortEntry, 1, 10);
        Assert.assertEquals(Integer.valueOf(2), results.getKey());
        Assert.assertTrue(results.getValue().contains(mp1));
        Assert.assertTrue(results.getValue().contains(mp2));

        filterMap.put("term", "mp");
        results = mapcontextRepository.filterAndGet(filterMap, sortEntry, 1, 10);
        Assert.assertEquals(Integer.valueOf(2), results.getKey());
        Assert.assertTrue(results.getValue().contains(mp1));
        Assert.assertTrue(results.getValue().contains(mp2));

        filterMap.put("term", "admin.mp");
        results = mapcontextRepository.filterAndGet(filterMap, sortEntry, 1, 10);
        Assert.assertEquals(Integer.valueOf(1), results.getKey());
        Assert.assertTrue(results.getValue().contains(mp2));

        /**
         * Deletion
         */
        mapcontextRepository.delete(mp1.getId());

        MapContextDTO mp = mapcontextRepository.findById(mp1.getId(), true);
        Assert.assertNull(mp);

        // cleanup
        layerRepository.deleteAll();
        dataRepository.deleteAll();
        providerRepository.deleteAll();
        serviceRepository.deleteAll();
        mapcontextRepository.deleteAll();
    }

}
