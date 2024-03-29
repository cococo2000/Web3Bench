/******************************************************************************
 *  Copyright 2015 by OLTPBenchmark Project                                   *
 *                                                                            *
 *  Licensed under the Apache License, Version 2.0 (the "License");           *
 *  you may not use this file except in compliance with the License.          *
 *  You may obtain a copy of the License at                                   *
 *                                                                            *
 *    http://www.apache.org/licenses/LICENSE-2.0                              *
 *                                                                            *
 *  Unless required by applicable law or agreed to in writing, software       *
 *  distributed under the License is distributed on an "AS IS" BASIS,         *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 *  See the License for the specific language governing permissions and       *
 *  limitations under the License.                                            *
 ******************************************************************************/

package com.olxpbenchmark.util;

/**
 * EventObserver
 */
public abstract class EventObserver<T> {

    protected class InnerObserver {
        public void update(EventObservable<T> o, T arg) {
            EventObserver.this.update(o, arg);
        }

        public EventObserver<T> getEventObserver() {
            return EventObserver.this;
        }
    }

    private final InnerObserver observer;

    public EventObserver() {
        this.observer = new InnerObserver();
    }

    protected InnerObserver getObserver() {
        return this.observer;
    }

    public abstract void update(EventObservable<T> o, T arg);
}
