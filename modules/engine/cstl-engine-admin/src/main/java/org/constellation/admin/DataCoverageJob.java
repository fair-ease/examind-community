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
package org.constellation.admin;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;

import org.opengis.metadata.Metadata;
import org.opengis.metadata.content.ContentInformation;
import org.opengis.metadata.content.CoverageDescription;

import org.apache.sis.storage.GridCoverageResource;
import org.apache.sis.storage.Resource;
import org.apache.sis.util.collection.BackingStoreException;
import org.apache.sis.util.logging.Logging;

import org.geotoolkit.storage.coverage.CoverageDescriptionAdapter;
import org.geotoolkit.storage.coverage.ImageStatistics;
import org.geotoolkit.storage.multires.MultiResolutionResource;

import org.constellation.api.DataType;
import org.constellation.business.IDataCoverageJob;
import org.constellation.business.IMetadataBusiness;
import org.constellation.dto.Data;
import org.constellation.exception.ConfigurationException;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.repository.DataRepository;
import org.constellation.repository.ProviderRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import static org.constellation.api.StatisticState.STATE_ERROR;
import static org.constellation.api.StatisticState.STATE_PARTIAL;
import static org.constellation.api.StatisticState.STATE_PENDING;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
@Component
@Primary
public class DataCoverageJob implements IDataCoverageJob {

    /**
     * Used for debugging purposes.
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.admin");

    /**
     * Injected data repository.
     */
    @Inject
    private DataRepository dataRepository;

    /**
     * Injected data repository.
     */
    @Inject
    private ProviderRepository providerRepository;

    @Inject
    private IMetadataBusiness metadataService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Async
    public void asyncUpdateDataStatistics(final int dataId) {

        Data data = dataRepository.findById(dataId);
        if (data == null) {
            LOGGER.log(Level.WARNING, "Can't compute coverage statistics on data id " + dataId +
                    " because data is not found in database.");
            return;
        }
        try {
            if (DataType.COVERAGE.name().equals(data.getType())
                    && (data.getRendered() == null || !data.getRendered())
                    && data.getStatsState() == null) {

                LOGGER.log(Level.INFO, "Start computing data " + dataId + " "+data.getName()+" coverage statistics.");

                data.setStatsState(STATE_PENDING);
                updateData(data);

                if (providerRepository.existById(data.getProviderId())) {
                    final DataProvider dataProvider = DataProviders.getProvider(data.getProviderId());
                    final org.constellation.provider.Data dataP = dataProvider.get(data.getNamespace(), data.getName());
                    final Resource res = dataP.getOrigin();

                    if (res instanceof MultiResolutionResource) {
                        //pyramid, too large to compute statistics
                        data.setStatsState(STATE_PARTIAL);
                        updateData(data);
                        return;
                    }

                    if (res instanceof GridCoverageResource) {
                        final Object result = dataP.computeStatistic(dataId, dataRepository);
                        if (result instanceof ImageStatistics) {
                            updateMetadata((ImageStatistics) result, data);
                        }
                    }

                } else {
                    throw new ConfigurationException("Provider has been removed before the end of statistic computation");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during coverage statistic update for data " + dataId + " "+data.getName() + " : " + e.getMessage(), e);

            //update data
            Data lastData = dataRepository.findById(dataId);
            if (lastData != null && !lastData.getStatsState().equals(STATE_ERROR)) {
                data.setStatsState(STATE_ERROR);
                //data.setStatsResult(Exceptions.formatStackTrace(e));
                updateData(data);
            }
        }
    }

    private void updateData(final Data data) {
        SpringHelper.executeInTransaction(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                dataRepository.updateStatistics(data.getId(), data.getStatsResult(), data.getStatsState());

                // forward original data statistic result and state to pyramid conform child.
                final List<Data> dataChildren = dataRepository.getDataLinkedData(data.getId());
                for (Data dataChild : dataChildren) {
                    if (dataChild.getSubtype().equalsIgnoreCase("pyramid") && !dataChild.getRendered()) {
                        dataRepository.updateStatistics(dataChild.getId(), data.getStatsResult(), data.getStatsState());
                    }
                }
            }
        });
    }

    private void updateMetadata(final ImageStatistics stats, final Data target) {
        SpringHelper.executeInTransaction(status -> {
            final Object md;
            try {
                md = metadataService.getIsoMetadataForData(target.getId());
                if (!(md instanceof Metadata))
                    throw new RuntimeException("Only GeoAPI metadata accepted for statistics update");
                final ContentInformation adapter = new CoverageDescriptionAdapter(stats);
                final Collection contentInfo = ((Metadata) md).getContentInfo();
                // TODO: should we try to merge instead ?
                contentInfo.removeIf(info -> info instanceof CoverageDescription);
                contentInfo.add(adapter);
                metadataService.updateMetadata(((Metadata) md).getFileIdentifier(), md, target.getId(), null, null, null, null, null);
                return null;
            } catch (ConfigurationException e) {
                throw new BackingStoreException(String.format("Cannot update metadata for data %d (%s)", target.getId(), target.getName()), e);
            }
        });
    }
}
