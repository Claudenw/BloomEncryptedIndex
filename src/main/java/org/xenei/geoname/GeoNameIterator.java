package org.xenei.geoname;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class GeoNameIterator implements Iterator<GeoName>, AutoCloseable {

    public final static URL DEFAULT_INPUT = GeoNameIterator.class.getResource("/allCountries.txt");

    private final BufferedReader bufferedReader;
    private GeoName next;

    public GeoNameIterator(URL inputFile) throws IOException {
        this(inputFile.openStream());
    }

    public GeoNameIterator(InputStream stream) {
        this(new InputStreamReader(stream));
    }

    public GeoNameIterator(Reader reader) {
        if (reader instanceof BufferedReader) {
            bufferedReader = (BufferedReader) reader;
        } else {
            bufferedReader = new BufferedReader(reader);
        }
        next = null;
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }

    @Override
    public boolean hasNext() {
        if (next == null) {
            try {
                next = GeoName.parse(bufferedReader.readLine());
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public GeoName next() {
        if (hasNext()) {
            try {
                return next;
            } finally {
                next = null;
            }
        } else {
            throw new NoSuchElementException();
        }
    }
}
