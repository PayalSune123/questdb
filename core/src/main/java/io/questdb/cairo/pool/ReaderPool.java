/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2024 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.cairo.pool;

import io.questdb.MessageBus;
import io.questdb.cairo.*;
import io.questdb.cairo.pool.ex.PoolClosedException;
import org.jetbrains.annotations.TestOnly;

public class ReaderPool extends AbstractMultiTenantPool<ReaderPool.R> {

    private final MessageBus messageBus;
    private ReaderListener readerListener;

    public ReaderPool(CairoConfiguration configuration, MessageBus messageBus) {
        super(configuration, configuration.getReaderPoolMaxSegments(), configuration.getInactiveReaderTTL());
        this.messageBus = messageBus;
    }

    @TestOnly
    public void setTableReaderListener(ReaderListener readerListener) {
        this.readerListener = readerListener;
    }

    @Override
    protected byte getListenerSrc() {
        return PoolListener.SRC_READER;
    }

    @Override
    protected R newTenant(TableToken tableToken, Entry<R> entry, int index) {
        return new R(this, entry, index, tableToken, messageBus, readerListener);
    }

    @TestOnly
    @FunctionalInterface
    public interface ReaderListener {
        void onOpenPartition(TableToken tableToken, int partitionIndex);
    }

    public boolean isPartitionVersionUsed(TableToken tableToken, long partitionTimestamp, long nameVersion) {
        if (isClosed()) {
            throw PoolClosedException.INSTANCE;
        }

        AbstractMultiTenantPool.Entry<ReaderPool.R> rEntry = entries().get(tableToken.getDirName());
        while (rEntry != null) {
            for (int i = 0; i < ReaderPool.ENTRY_SIZE; i++) {
                if (rEntry.isItemLocked(i)) {
                    ReaderPool.R reader = rEntry.getTenant(i);
                    if (reader == null) {
                        continue;
                    }
                    if (reader.isPartitionVersionUsed(partitionTimestamp, nameVersion)) {
                        return true;
                    }
                }
            }
            rEntry = rEntry.getNext();
        }
        return false;
    }

    public static class R extends TableReader implements PoolTenant<R> {
        private final int index;
        private final ReaderListener readerListener;
        private Entry<R> entry;
        private AbstractMultiTenantPool<R> pool;

        public R(
                AbstractMultiTenantPool<R> pool,
                Entry<R> entry,
                int index,
                TableToken tableToken,
                MessageBus messageBus,
                ReaderListener readerListener
        ) {
            super(pool.getConfiguration(), tableToken, messageBus);
            this.pool = pool;
            this.entry = entry;
            this.index = index;
            this.readerListener = readerListener;
        }

        @Override
        public void close() {
            if (isOpen()) {
                goPassive();
                final AbstractMultiTenantPool<R> pool = this.pool;
                if (pool == null || entry == null || !pool.returnToPool(this)) {
                    super.close();
                }
            }
        }

        @Override
        public Entry<R> getEntry() {
            return entry;
        }

        @Override
        public int getIndex() {
            return index;
        }

        public void goodbye() {
            entry = null;
            pool = null;
        }

        @Override
        public long openPartition(int partitionIndex) {
            if (readerListener != null) {
                readerListener.onOpenPartition(getTableToken(), partitionIndex);
            }
            return super.openPartition(partitionIndex);
        }

        @Override
        public void refresh() {
            try {
                goActive();
            } catch (Throwable ex) {
                close();
                throw ex;
            }
        }
    }
}
