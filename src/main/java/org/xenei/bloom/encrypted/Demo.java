package org.xenei.bloom.encrypted;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.bloomfilter.Hasher;
import org.apache.commons.collections4.bloomfilter.Hasher.Factory;
import org.apache.commons.collections4.bloomfilter.hasher.DynamicHasher;
import org.apache.commons.collections4.bloomfilter.hasher.Murmur128;
import org.apache.commons.collections4.iterators.TransformIterator;
import org.xenei.bloom.GeoNameHasher;
import org.xenei.bloom.multidimensional.Container;
import org.xenei.bloom.multidimensional.Container.Index;
import org.xenei.bloom.multidimensional.Container.Storage;
import org.xenei.bloom.multidimensional.ContainerImpl;
import org.xenei.bloom.multidimensional.index.FlatBloofi;
import org.xenei.bloom.multidimensional.index.Linear;
import org.xenei.bloom.multidimensional.index.RangePacked;
import org.xenei.bloom.multidimensional.storage.InMemory;
import org.xenei.geoname.GeoName;
import org.xenei.geoname.GeoNameIterator;

public class Demo {

    private Container<byte[]> container;
    private List<GeoName> sample;
    private GeoName.Serde serde;
    private Ende ende;

    public static void main(String[] args) throws IOException, GeneralSecurityException
    {
        Demo demo = new Demo();
        System.out.println( String.format( "items: %s filters: %s", demo.container.getValueCount(), demo.container.getFilterCount()));
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in)))
        {
            System.out.println( "Enter criteria (enter to quit)");
            String s = reader.readLine();
            while ( ! s.isEmpty() )
            {
                Hasher.Builder builder = Factory.DEFAULT.useFunction( Murmur128.NAME )
                        .with(  s );
                System.out.println( "Enter additional criteria (enter to search)");
                s = reader.readLine();
                while ( ! s.isEmpty() )
                {
                    builder.with( s );
                    System.out.println( "Enter additional criteria (enter to search)");
                    s = reader.readLine();
                }

                System.out.println( "\nSearch Results:");
                demo.getGeoNames(builder.build()).forEachRemaining( gn -> { System.out.println( String.format( "%s%n%n", gn ));});
                System.out.println( "\nEnter criteria (enter to quit)");
                s = reader.readLine();
            }

        }
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(Demo.class.getResource("/words.txt").openStream()));
        String s = reader.readLine();
        while ( s != null )
        {
            Hasher hasher = Factory.DEFAULT.useFunction( Murmur128.NAME )
                    .with( s ).build();
            System.out.println( String.format("%nSearch Results for [%s]:", s ));
            demo.getGeoNames(hasher).forEachRemaining( gn -> { System.out.println( String.format( "%s%n%n", gn ));});
            s = reader.readLine();
        }


    }

    public Demo() throws IOException, GeneralSecurityException {
        Storage<byte[]> storage = new InMemory<byte[]>();
        Index index = new RangePacked( GeoNameHasher.shape );
        container = new ContainerImpl<byte[]>( GeoNameHasher.shape, storage, index );
        sample = new ArrayList<GeoName>();
        serde = new GeoName.Serde();
        SecretKeySpec secretKey = Ende_AES256.makeKey( "MySecretKey");
        ende = new Ende_AES256(secretKey);
        // populate the index.
        try (GeoNameIterator iter = new GeoNameIterator( GeoNameIterator.DEFAULT_INPUT ))
        {
            while (iter.hasNext())
            {
                GeoName geoName = iter.next();
                Hasher hasher = GeoNameHasher.createHasher(geoName);
                if (container.getValueCount() % 1000 == 0)
                {
                    System.out.println( container.getValueCount() );
                    sample.add( geoName );
                }
                container.put( hasher, encrypt( geoName ));
            }
        }


    }

    private byte[] encrypt( GeoName geoName ) throws GeneralSecurityException
    {
        return ende.encrypt( serde.serialize( geoName ).getBytes( StandardCharsets.UTF_8));
    }

    public Stream<GeoName> getSample() {
        return sample.stream();
    }

    public Iterator<GeoName> getGeoNames( Hasher hasher )
    {

        return new TransformIterator<byte[],GeoName>( container.search( hasher ),
                new Transformer<byte[],GeoName>(){

                    @Override
                    public GeoName transform(byte[] input)  {
                        try {
                            return serde.deserialize(new String( ende.decrypt(input )));
                        } catch (GeneralSecurityException e) {
                            throw new IllegalStateException( e );
                        }
                    }});
    }

    public Container<byte[]> getContainer() {
        return container;
    }
}
