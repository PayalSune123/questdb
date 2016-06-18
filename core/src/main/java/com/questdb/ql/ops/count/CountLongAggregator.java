/*******************************************************************************
 *    ___                  _   ____  ____
 *   / _ \ _   _  ___  ___| |_|  _ \| __ )
 *  | | | | | | |/ _ \/ __| __| | | |  _ \
 *  | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *   \__\_\\__,_|\___||___/\__|____/|____/
 *
 * Copyright (C) 2014-2016 Appsicle
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package com.questdb.ql.ops.count;

import com.questdb.misc.Numbers;
import com.questdb.ql.Record;
import com.questdb.ql.impl.map.DirectMapValues;
import com.questdb.ql.ops.AbstractUnaryAggregator;
import com.questdb.ql.ops.Function;
import com.questdb.std.ObjectFactory;
import com.questdb.store.ColumnType;

public final class CountLongAggregator extends AbstractUnaryAggregator {

    public static final ObjectFactory<Function> FACTORY = new ObjectFactory<Function>() {
        @Override
        public Function newInstance() {
            return new CountLongAggregator();
        }
    };

    private CountLongAggregator() {
        super(ColumnType.LONG);
    }

    @Override
    public void calculate(Record rec, DirectMapValues values) {
        long d = value.getLong(rec);
        if (values.isNew()) {
            values.putLong(valueIndex, d == Numbers.LONG_NaN ? 0 : 1);
        } else if (d != Numbers.LONG_NaN) {
            values.putLong(valueIndex, values.getLong(valueIndex) + 1);
        }
    }
}
