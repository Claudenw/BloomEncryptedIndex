package org.xenei.bloom.multidimensional;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.PrimitiveIterator;
import java.util.function.IntConsumer;

import org.apache.commons.collections4.bloomfilter.BloomFilter;
import org.apache.commons.collections4.bloomfilter.BloomFilter.Shape;
import org.apache.commons.collections4.bloomfilter.Hasher;
import com.googlecode.javaewah.datastructure.BitSet;

/**
 * This is what Daniel called Bloofi2. Basically, instead of using a tree
 * structure like Bloofi (see BloomFilterIndex), we "transpose" the BitSets.
 *
 * Originally from https://github.com/lemire/bloofi/blob/master/src/mvm/provenance/FlatBloomFilterIndex.java
 *
 * @author Daniel Lemire
 *
 * @param <E>
 */

public final class FlatBloofi {

    private final Shape shape;
    private final ArrayList<Integer> fromindextoId;

    private final Hashtable<Integer, Integer> idMap;

    private ArrayList<long[]> buffer;

    private BitSet busy;

    public FlatBloofi(Shape shape) {
        this.shape = shape;
        this.fromindextoId = new ArrayList<Integer>();
        this.idMap = new Hashtable<Integer, Integer>();
        this.buffer = new ArrayList<long[]>(0);
        this.busy = new BitSet(0);
    }


    public int deleteFromIndex(int id) {//, InsDelUpdateStatistics stat) {
        int index = idMap.remove(id);
        idMap.remove(id);
        busy.unset(index);

        if (busy.getWord(index / Long.SIZE) == 0) {
            for (int k = index / Long.SIZE * Long.SIZE; k < index / Long.SIZE * Long.SIZE + Long.SIZE; ++k)
                fromindextoId.remove(k);
            buffer.remove(index / Long.SIZE);
            busy.removeWord(index / Long.SIZE);
            for (Map.Entry<Integer, Integer> me : idMap.entrySet()) {
                if (me.getValue().intValue() / 64 >= index / 64) {
                    idMap.put(me.getKey(), me.getValue()
                            .intValue() - 64);
                }
            }
        } else {
            clearBloomAt(index);
        }
        return 0;
    }

    public Set<Integer> getIDs() {
        return idMap.keySet();
    }


    public int getSize() {
        return idMap.size();
    }


    public void insert(BloomFilter bf, int externalIdx) {
        verifyShape( bf );
        insert( bf.getHasher(), externalIdx );
    }

    public void insert(Hasher hasher, int externalIdx) {
        verifyHasher( hasher );

        int i = busy.nextUnsetBit(0);
        if (i < 0) {
            // extend the busy buffer
            i = busy.length();
            busy.resize(i + 64);
            int longCount = Double.valueOf(Math.ceil(shape.getNumberOfBits() / (double)Long.SIZE )).intValue();
            buffer.add(new long[longCount]);
        }
        if (i < fromindextoId.size()) {
            fromindextoId.set(i, externalIdx);
        } else { // if(i == fromindextoId.size()) {
            fromindextoId.add(externalIdx);
        }
        setBloomAt(i, hasher);
        idMap.put(externalIdx, i);
        busy.set(i);
    }

    public List<Integer> search(Hasher hasher) { //, SearchStatistics stat) {
        ArrayList<Integer> answer = new ArrayList<Integer>();
        for (int i = 0; i < buffer.size(); ++i) {
            long w = ~0l;
            PrimitiveIterator.OfInt iter = hasher.getBits(shape);
            while( iter.hasNext() ) {
                w &= buffer.get(i)[iter.nextInt()];
            }
            while (w != 0) {
                long t = w & -w;
                answer.add(fromindextoId.get(i * 64
                        + Long.bitCount(t - 1)));
                w ^= t;
            }
        }
        return answer;
    }


    // this assumes that the bloom filter only received new values
    public void updateIndex(BloomFilter newBloomFilter, int externalIdx ) {
        //InsDelUpdateStatistics stat) {
        verifyShape( newBloomFilter );
        updateIndex( newBloomFilter.getHasher(), externalIdx);
    }

    public void updateIndex(Hasher hasher, int externalIdx ) {
        setBloomAt(idMap.get(externalIdx), hasher);
    }

    // this is like updateIndex except that it does not
    // assume that the BloomFilter was only updated through the addition
    // of values.
    public void replaceIndex(BloomFilter newBloomFilter, int externalIdx) {
        verifyShape( newBloomFilter );
        replaceIndex( newBloomFilter.getHasher(), externalIdx);
    }

    public void replaceIndex(Hasher hasher, int externalIdx) {
        verifyHasher( hasher );
        replaceBloomAt(idMap.get(externalIdx), hasher );
    }

    private void clearBloomAt(int i) {
        final long[] mybuffer = buffer.get(i / 64);
        final long mask = ~(1l << i);
        for (int k = 0; k < mybuffer.length; ++k) {
            mybuffer[k] &= mask;
        }
    }

    private void setBloomAt(int i, Hasher hasher) {
        final long[] mybuffer = buffer.get(i / 64);
        final long mask = (1l << i);
        hasher.getBits(shape).forEachRemaining( (IntConsumer) idx -> mybuffer[idx] |= mask);
    }

    private void replaceBloomAt(int i, Hasher hasher) {
        long[] mybuffer = buffer.get(i / 64);
        final long mask = (1l << i);
        BitSet bs = new BitSet( shape.getNumberOfBits() );
        hasher.getBits(shape).forEachRemaining( (IntConsumer) bs::set );
        for (int k = 0; k < mybuffer.length; ++k) {
            if (bs.get(k))
                mybuffer[k] |= mask;
            else
                mybuffer[k] &= ~mask;
        }
    }

    /**
     * Verify the other Bloom filter has the same shape as this Bloom filter.
     *
     * @param other the other filter to check.
     * @throws IllegalArgumentException if the shapes are not the same.
     */
    protected final void verifyShape(BloomFilter other) {
        verifyShape(other.getShape());
    }

    /**
     * Verify the specified shape has the same shape as this Bloom filter.
     *
     * @param shape the other shape to check.
     * @throws IllegalArgumentException if the shapes are not the same.
     */
    protected final void verifyShape(Shape shape) {
        if (!this.shape.equals(shape)) {
            throw new IllegalArgumentException(String.format("Shape %s is not the same as %s", shape, this.shape));
        }
    }

    /**
     * Verifies that the hasher has the same name as the shape.
     *
     * @param hasher the Hasher to check
     */
    protected final void verifyHasher(Hasher hasher) {
        if (!shape.getHashFunctionName().equals(hasher.getName())) {
            throw new IllegalArgumentException(
                String.format("Hasher (%s) is not the hasher for shape (%s)", hasher.getName(), shape.toString()));
        }
    }

}
