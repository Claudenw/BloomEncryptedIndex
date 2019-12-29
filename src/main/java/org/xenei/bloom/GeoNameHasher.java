package org.xenei.bloom;

import org.apache.commons.collections4.bloomfilter.BloomFilter;
import org.apache.commons.collections4.bloomfilter.Hasher;
import org.apache.commons.collections4.bloomfilter.hasher.DynamicHasher;
import org.apache.commons.collections4.bloomfilter.hasher.HashFunction;
import org.apache.commons.collections4.bloomfilter.hasher.function.Murmur128x86Cyclic;
import org.xenei.geoname.GeoName;

public class GeoNameHasher {

    public final static int POPULATION = 10; // number of items in each filter
    public final static double PROBABILITY = 1.0/2000000;  //1 in 2 million
    private final static HashFunction hashFunction = new Murmur128x86Cyclic();
    public final static BloomFilter.Shape shape = new BloomFilter.Shape( hashFunction, POPULATION, PROBABILITY );

    public static Hasher createHasher( GeoName geoName ) {
        Hasher.Builder builder = new DynamicHasher.Builder( hashFunction )
                .with( geoName.feature_code).with( geoName.country_code );
        String[] lst = geoName.alternatenames.split( ",");
        int limit = Integer.min(POPULATION, lst.length );
        for (int i=0;i<limit;i++)
        {
            builder.with( lst[i].trim() );
        }
        return builder.build();
    }

}
