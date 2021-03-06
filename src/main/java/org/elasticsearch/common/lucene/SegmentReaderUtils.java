/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.common.lucene;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.FilterAtomicReader;
import org.apache.lucene.index.SegmentReader;
import org.elasticsearch.ElasticsearchIllegalStateException;
import org.elasticsearch.common.Nullable;

public class SegmentReaderUtils {

    /**
     * Tries to extract a segment reader from the given index reader.
     * If no SegmentReader can be extracted an {@link org.elasticsearch.ElasticsearchIllegalStateException} is thrown.
     */
    @Nullable
    public static SegmentReader segmentReader(AtomicReader reader) {
        return internalSegmentReader(reader, true);
    }

    /**
     * Tries to extract a segment reader from the given index reader and returns it, otherwise <code>null</code>
     * is returned
     */
    @Nullable
    public static SegmentReader segmentReaderOrNull(AtomicReader reader) {
        return internalSegmentReader(reader, false);
    }

    public static boolean registerCoreListener(AtomicReader reader, SegmentReader.CoreClosedListener listener) {
        SegmentReader segReader = SegmentReaderUtils.segmentReaderOrNull(reader);
        if (segReader != null) {
            segReader.addCoreClosedListener(listener);
            return true;
        }
        return false;
    }

    private static SegmentReader internalSegmentReader(AtomicReader reader, boolean fail) {
        if (reader == null) {
            return null;
        }
        if (reader instanceof SegmentReader) {
            return (SegmentReader) reader;
        } else if (reader instanceof FilterAtomicReader) {
            final FilterAtomicReader fReader = (FilterAtomicReader) reader;
            return segmentReader(FilterAtomicReader.unwrap(fReader));
        }
        if (fail) {
            // hard fail - we can't get a SegmentReader
            throw new ElasticsearchIllegalStateException("Can not extract segment reader from given index reader [" + reader + "]");
        }
        return null;
    }
}
