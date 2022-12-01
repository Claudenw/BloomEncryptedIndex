package org.xenei.bloom.encrypted;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.LongPredicate;
import java.util.stream.Stream;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.bloomfilter.BitMapProducer;
import org.apache.commons.collections4.bloomfilter.Shape;
import org.apache.commons.collections4.iterators.TransformIterator;
import org.xenei.bloom.GeoNameHasher;
import org.xenei.bloom.filter.HasherCollection;
import org.xenei.bloom.filter.HasherFactory;
import org.xenei.bloom.multidimensional.Container;
import org.xenei.bloom.multidimensional.Container.Index;
import org.xenei.bloom.multidimensional.Container.Storage;
import org.xenei.bloom.multidimensional.ContainerImpl;
import org.xenei.bloom.multidimensional.index.RangePacked;
import org.xenei.bloom.multidimensional.storage.InMemory;
import org.xenei.geoname.GeoName;
import org.xenei.geoname.GeoNameIterator;

public class Demo {

    private Container<byte[]> container;
    private List<GeoName> sample;
    private GeoName.Serde serde;
    private Ende ende;

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        Demo demo = new Demo();
        HasherCollection hashers = new HasherCollection();

        System.out.println(String.format("items: %s filters: %s", demo.container.getValueCount(),
                demo.container.getFilterCount()));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Enter criteria (enter to quit)");
            String s = reader.readLine();
            while (!s.isEmpty()) {
                hashers.add(HasherFactory.hasherFor(s));

                System.out.println("Enter additional criteria (enter to search)");
                s = reader.readLine();
                while (!s.isEmpty()) {
                    hashers.add(HasherFactory.hasherFor(s));
                    System.out.println("Enter additional criteria (enter to search)");
                    s = reader.readLine();
                }

                System.out.println("\nSearch Results:");

                demo.getGeoNames(hashers).forEachRemaining(gn -> {
                    System.out.println(String.format("%s%n%n", gn));
                });
                System.out.println("\nEnter criteria (enter to quit)");
                s = reader.readLine();
            }

        }
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(Demo.class.getResource("/words.txt").openStream()));
        String s = reader.readLine();
        while (s != null) {
            hashers.clear();
            hashers.add(HasherFactory.hasherFor(s));
            System.out.println(String.format("%nSearch Results for [%s]:", s));
            demo.getGeoNames(hashers).forEachRemaining(gn -> {
                System.out.println(String.format("%s%n%n", gn));
            });
            s = reader.readLine();
        }
    }

    public Demo() throws IOException, GeneralSecurityException {
        Storage<byte[], UUID> storage = new InMemory<byte[], UUID>();
        Index<UUID> index = new RangePacked<UUID>(new Func(GeoNameHasher.shape), GeoNameHasher.shape);
        container = new ContainerImpl<byte[], UUID>(1000000, GeoNameHasher.shape, storage, index);
        sample = new ArrayList<GeoName>();
        serde = new GeoName.Serde();
        SecretKeySpec secretKey = Ende_AES256.makeKey("MySecretKey");
        ende = new Ende_AES256(secretKey);
        // populate the index.
        int count = 0;
        try (GeoNameIterator iter = new GeoNameIterator(GeoNameIterator.DEFAULT_INPUT)) {
            while (iter.hasNext()) {
                GeoName geoName = iter.next();
                HasherCollection hashers = GeoNameHasher.createHasher(geoName);
                if (container.getValueCount() % 1000 == 0) {
                    System.out.println(container.getValueCount());
                    sample.add(geoName);
                }
                container.put(hashers, encrypt(geoName));
                count++;
            }
        }

        System.out.print(GeoNameHasher.shape.toString());
        System.out.println(String.format(" p=%s", GeoNameHasher.shape.getProbability(count)));

    }

    private byte[] encrypt(GeoName geoName) throws GeneralSecurityException {
        return ende.encrypt(serde.serialize(geoName).getBytes(StandardCharsets.UTF_8));
    }

    public Stream<GeoName> getSample() {
        return sample.stream();
    }

    public Iterator<GeoName> getGeoNames(HasherCollection hashers) {
        return new TransformIterator<byte[], GeoName>(container.search(hashers), new Transformer<byte[], GeoName>() {

            @Override
            public GeoName transform(byte[] input) {
                try {
                    return serde.deserialize(new String(ende.decrypt(input)));
                } catch (GeneralSecurityException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    public Container<byte[]> getContainer() {
        return container;
    }

    /**
     * A standard Func to use in testing where UUID creation is desired.
     *
     */
    public static class Func implements Function<BitMapProducer, UUID> {
        private int numberOfBytes;

        public Func(Shape shape) {
            numberOfBytes = shape.getNumberOfBits() / Byte.SIZE + ((shape.getNumberOfBits() % Byte.SIZE) > 0 ? 1 : 0);
        }

        private byte[] getBytes(BitMapProducer bitMapProducer) {
            byte[] buffer = new byte[numberOfBytes];

            bitMapProducer.forEachBitMap(new LongPredicate() {
                int idx = 0;

                @Override
                public boolean test(long word) {
                    for (int longOfs = 0; longOfs < Long.BYTES; longOfs++) {
                        buffer[idx++] = (byte) ((word >> (Byte.SIZE * longOfs)) & 0xFFL);
                        if (idx == numberOfBytes) {
                            return true;
                        }
                    }
                    return false;
                }

            });
            return buffer;
        }

        @Override
        public UUID apply(BitMapProducer bitMapProducer) {
            return UUID.nameUUIDFromBytes(getBytes(bitMapProducer));
        }

    }

}
