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

package com.questdb.ql.impl.aggregation;

import com.questdb.factory.configuration.RecordColumnMetadata;
import com.questdb.std.ObjList;
import com.questdb.std.ObjectFactory;
import com.questdb.std.ThreadLocal;
import com.questdb.store.ColumnType;

final class AggregationUtils {
    static final ThreadLocal<ObjList<RecordColumnMetadata>> TL_COLUMNS = new ThreadLocal<>(new ObjectFactory<ObjList<RecordColumnMetadata>>() {
        @Override
        public ObjList<RecordColumnMetadata> newInstance() {
            return new ObjList<>();
        }
    });

    static final ThreadLocal<ObjList<ColumnType>> TL_COLUMN_TYPES = new ThreadLocal<>(new ObjectFactory<ObjList<ColumnType>>() {
        @Override
        public ObjList<ColumnType> newInstance() {
            return new ObjList<>();
        }
    });

    private AggregationUtils() {
    }
}
