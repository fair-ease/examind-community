package org.constellation.converter;

import org.apache.sis.storage.Resource;
import org.apache.sis.util.UnconvertibleObjectException;
import org.constellation.admin.SpringHelper;
import org.constellation.business.IDataBusiness;
import org.constellation.dto.process.DataProcessReference;
import org.constellation.exception.ConstellationException;
import org.constellation.exception.TargetNotFoundException;
import org.constellation.provider.Data;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.geotoolkit.feature.util.converter.SimpleConverter;

public class DataProcessReferenceToResourceConverter extends SimpleConverter<DataProcessReference, Resource> {

    @Override
    public Class<DataProcessReference> getSourceClass() {
        return DataProcessReference.class;
    }

    @Override
    public Class<Resource> getTargetClass() {
        return Resource.class;
    }

    /**
     * Return Resource from a DataProcessReference.
     * @param ref DataProcessReference.
     * @return Resource.
     * @throws UnconvertibleObjectException if getProvider() or findResource() fails.
     */
    @Override
    public Resource apply(DataProcessReference ref) throws UnconvertibleObjectException {
        if (ref == null) return null;
        try {
            return getByDataId(ref);
        } catch (ConstellationException e) {
            // Maybe input reference does not give data id. We'll try with provider information instead
            try {
                return findData(ref.getProvider(), ref.getNamespace(), ref.getName());
            } catch (ConstellationException bis) {
                e.addSuppressed(bis);
                throw new UnconvertibleObjectException("Cannot find data for given information", e);
            }
        }
    }

    private static Resource getByDataId(final DataProcessReference ref) throws ConstellationException {
        final IDataBusiness dataBiz = SpringHelper.getBean(IDataBusiness.class);
        if (dataBiz == null) throw new UnconvertibleObjectException("Application context unavailable");
        final org.constellation.dto.Data data = dataBiz.getData(ref.getId());
        return findData(data.getProviderId(), data.getNamespace(), data.getName());
    }

    private static Resource findData(int providerId, String namespace, String name) throws ConstellationException {
        DataProvider dp = DataProviders.getProvider(providerId);
        Data d = dp.get(namespace, name);
        if (d == null) throw new TargetNotFoundException(String.format("No data found in provider %s for name %s:%s", providerId, namespace, name));
        return d.getOrigin();
    }
}