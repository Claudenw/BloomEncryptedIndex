package org.xenei.bloom;

import org.apache.commons.collections4.bloomfilter.BloomFilter.Shape;
import org.apache.commons.collections4.bloomfilter.Hasher;
import org.apache.commons.collections4.bloomfilter.hasher.DynamicHasher;
import org.apache.commons.collections4.bloomfilter.hasher.Murmur128;
import org.xenei.geoname.GeoName;

public class GeoNameHasher {

    public final static int POPULATION = 10000000; // 10 million
    public final static double PROBABILITY = 1.0/100000;  //1 in 100 thousand

    public final static Shape shape = new Shape(Murmur128.NAME, POPULATION, PROBABILITY );

    public static Hasher createHasher( GeoName geoName ) {
        return DynamicHasher.Factory.DEFAULT.useFunction( Murmur128.NAME )
        .with( geoName.name ).with( geoName.feature_code).with( geoName.geonameid ).build();
    }

}
