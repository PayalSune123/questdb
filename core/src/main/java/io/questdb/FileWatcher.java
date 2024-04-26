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

package io.questdb;

import io.questdb.log.Log;
import io.questdb.log.LogFactory;
import io.questdb.std.Misc;
import io.questdb.std.QuietCloseable;

public class FileWatcher implements QuietCloseable {
    private static final Log LOG = LogFactory.getLog(FileWatcher.class);
    private final FileEventCallback callback;
    private final FileEventNotifier notifier;
    private final Thread reloadThread;
    private boolean closed;

    public FileWatcher(
            FileEventCallback callback,
            FileEventNotifier notifier

    ) {
        this.callback = callback;
        this.notifier = notifier;
        reloadThread = new Thread(() -> {
            if (this.notifier == null || this.callback == null) {
                return;
            }

            do {
                if (closed) {
                    return;
                }
                try {
                    this.notifier.waitForChange(this.callback);
                } catch (FileEventNotifierException exc) {
                    LOG.error().$(exc).$();
                }
            } while (true);

        });
    }


    @Override
    public void close() {
        if (!closed) {
            reloadThread.interrupt();
            Misc.free(notifier);
        }
        closed = true;
    }

    public void watch() {
        reloadThread.start();
    }
}
