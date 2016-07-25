/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.lib.wsrs;

import org.apache.hadoop.classification.InterfaceAudience;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;

@InterfaceAudience.Private
public abstract class EnumSetParam<E extends Enum<E>> extends Param<EnumSet<E>> {
    Class<E> klass;

    public EnumSetParam(String name, Class<E> e, EnumSet<E> defaultValue) {
        super(name, defaultValue);
        klass = e;
    }

    /**
     * Convert an EnumSet to a string of comma separated values.
     */
    public static <E extends Enum<E>> String toString(EnumSet<E> set) {
        if (set == null || set.isEmpty()) {
            return "";
        } else {
            final StringBuilder b = new StringBuilder();
            final Iterator<E> i = set.iterator();
            b.append(i.next());
            while (i.hasNext()) {
                b.append(',').append(i.next());
            }
            return b.toString();
        }
    }

    @Override
    protected EnumSet<E> parse(String str) throws Exception {
        final EnumSet<E> set = EnumSet.noneOf(klass);
        if (!str.isEmpty()) {
            for (String sub : str.split(",")) {
                set.add(Enum.valueOf(klass, sub.trim().toUpperCase()));
            }
        }
        return set;
    }

    @Override
    protected String getDomain() {
        return Arrays.asList(klass.getEnumConstants()).toString();
    }

    @Override
    public String toString() {
        return getName() + "=" + toString(value);
    }
}
