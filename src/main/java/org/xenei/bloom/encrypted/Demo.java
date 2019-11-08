package org.xenei.bloom.encrypted;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.collections4.bloomfilter.BloomFilter;
import org.apache.commons.collections4.bloomfilter.Hasher;
import org.xenei.bloom.GeoNameHasher;
import org.xenei.bloom.multidimensional.FlatBloofi;
import org.xenei.geoname.GeoName;
import org.xenei.geoname.GeoNameIterator;

public class Demo {

    private List<byte[]> encryptedGeoName;
    private FlatBloofi index;
    private List<GeoName> sample;


    public Demo() throws IOException {
        encryptedGeoName = new ArrayList<byte[]>();
        index = new FlatBloofi( GeoNameHasher.shape );
        sample = new ArrayList<GeoName>();

        // populate the index.
        GeoNameIterator iter = new GeoNameIterator( GeoNameIterator.DEFAULT_INPUT );
        while (iter.hasNext())
        {
            GeoName geoName = iter.next();
            encryptedGeoName.add( encrypt( geoName ));
            index.insert( GeoNameHasher.createHasher(geoName), encryptedGeoName.size() );
            if (index.getSize() % 1000 == 0)
            {
                sample.add( geoName );
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

    public List<Integer> getIds( BloomFilter bf )
    {
        return getIds( bf.getHasher() );
    }

    public List<Integer> getIds( Hasher hasher )
    {
        return index.search( hasher );
    }

    public Stream<byte[]> getGeoNames( BloomFilter bf )
    {
        return getGeoNames( bf.getHasher() );
    }

    public Stream<byte[]> getGeoNames( Hasher hasher )
    {
        return index.search( hasher ).stream().map( i -> encryptedGeoName.get(i) );
    }
}
