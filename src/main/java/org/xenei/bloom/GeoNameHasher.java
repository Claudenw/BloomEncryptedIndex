package org.xenei.bloom;

import org.apache.commons.collections4.bloomfilter.Shape;
import org.xenei.bloom.filter.HasherCollection;
import org.xenei.bloom.filter.HasherFactory;
import org.xenei.geoname.GeoName;

public class GeoNameHasher {

    public final static int POPULATION = 10; // number of items in each filter
    public final static double PROBABILITY = 1.0 / 2000000; // 1 in 2 million
    public final static Shape shape = Shape.fromNP(POPULATION, PROBABILITY);

    public static HasherCollection createHasher(GeoName geoName) {
        HasherCollection hashers = new HasherCollection();
        hashers.add(HasherFactory.hasherFor(geoName.feature_code));
        hashers.add(HasherFactory.hasherFor(geoName.country_code));
        String[] lst = geoName.alternatenames.split(",");
        int limit = Integer.min(POPULATION, lst.length);
        for (int i = 0; i < limit; i++) {
            hashers.add(HasherFactory.hasherFor(lst[i]));
        }
        return hashers;
    }

}
