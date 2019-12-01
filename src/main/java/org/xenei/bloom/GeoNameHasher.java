package org.xenei.bloom;

import org.apache.commons.collections4.bloomfilter.BloomFilter.Shape;
import org.apache.commons.collections4.bloomfilter.Hasher;
import org.apache.commons.collections4.bloomfilter.hasher.DynamicHasher;
import org.apache.commons.collections4.bloomfilter.hasher.Murmur128;
import org.xenei.geoname.GeoName;

public class GeoNameHasher {

    public static int names = 0;
    public static int maxNames = 0;
    public final static int POPULATION = 300; // number of items in each filter
    public final static double PROBABILITY = 1.0/1000000;  //1 in 1 million

    public final static Shape shape = new Shape(Murmur128.NAME, POPULATION, PROBABILITY );

    public static Hasher createHasher( GeoName geoName ) {
        Hasher.Builder builder = DynamicHasher.Factory.DEFAULT.useFunction( Murmur128.NAME )
        .with( geoName.feature_code).with( geoName.country_code );
        String[] lst = geoName.alternatenames.split( ",");
        names += lst.length;
        maxNames = Integer.max(maxNames, lst.length);
        for (String s : lst)
        {
            builder.with( s.trim() );
        }
        return builder.build();
    }

}
