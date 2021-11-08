package org.xenei.bloom;

import org.apache.commons.collections4.bloomfilter.hasher.HasherCollection;
import org.apache.commons.collections4.bloomfilter.hasher.SimpleHasher;
import org.apache.commons.collections4.bloomfilter.hasher.Hasher;
import org.apache.commons.collections4.bloomfilter.Shape;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.digest.MurmurHash3;
import org.xenei.geoname.GeoName;

public class GeoNameHasher {

    public final static int POPULATION = 10; // number of items in each filter
    public final static double PROBABILITY = 1.0/2000000;  //1 in 2 million
    public final static Shape shape = Shape.Factory.fromNP( POPULATION, PROBABILITY );

    public static Hasher createHasher( GeoName geoName ) {
        HasherCollection hashers = new HasherCollection();
        long[] longs = MurmurHash3.hash128( geoName.feature_code.getBytes( StandardCharsets.UTF_8 ));
        hashers.add( new SimpleHasher( longs[0], longs[1]));
        longs = MurmurHash3.hash128( geoName.country_code.getBytes( StandardCharsets.UTF_8 ));
        hashers.add( new SimpleHasher( longs[0], longs[1]));
        String[] lst = geoName.alternatenames.split( ",");
        int limit = Integer.min(POPULATION, lst.length );
        for (int i=0;i<limit;i++)
        {
            longs = MurmurHash3.hash128( lst[i].trim().getBytes( StandardCharsets.UTF_8 ));
            hashers.add( new SimpleHasher( longs[0], longs[1]));
        }
        return hashers;
    }

}
