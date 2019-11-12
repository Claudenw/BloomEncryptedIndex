package org.xenei.bloom.encrypted;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.collections4.bloomfilter.BloomFilter.Shape;
import org.apache.commons.collections4.bloomfilter.Hasher;
import org.apache.commons.collections4.bloomfilter.hasher.Murmur128;
import org.xenei.bloom.GeoNameHasher;
import org.xenei.bloom.multidimensional.Container;
import org.xenei.bloom.multidimensional.Container.Index;
import org.xenei.bloom.multidimensional.Container.Storage;
import org.xenei.bloom.multidimensional.ContainerImpl;
import org.xenei.bloom.multidimensional.index.FlatBloofi;
import org.xenei.bloom.multidimensional.storage.InMemory;
import org.xenei.geoname.GeoName;
import org.xenei.geoname.GeoNameIterator;

public class Demo {

    private Container<byte[]> container;
    private List<GeoName> sample;


    public Demo() throws IOException {
        Shape shape = new Shape( Murmur128.NAME, 5000, 1.0/10000 );
        Storage<byte[]> storage = new InMemory<byte[]>();
        Index index = new FlatBloofi( shape );
        container = new ContainerImpl<byte[]>( shape, storage, index );
        sample = new ArrayList<GeoName>();

        // populate the index.
        try (GeoNameIterator iter = new GeoNameIterator( GeoNameIterator.DEFAULT_INPUT ))
        {
            while (iter.hasNext())
            {
                GeoName geoName = iter.next();
                Hasher hasher = GeoNameHasher.createHasher(geoName);
                if (container.getValueCount() % 1000 == 0)
                {
                    sample.add( geoName );
                }
                container.put( hasher, encrypt( geoName ));
            }
        }
    }

    private byte[] encrypt( GeoName geoName )
    {
        return null;
    }

    public Stream<GeoName> getSample() {
        return sample.stream();
    }

    public Stream<byte[]> getGeoNames( Hasher hasher )
    {
        return container.get( hasher );
    }
}
