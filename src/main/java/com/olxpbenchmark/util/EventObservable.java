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

import java.util.ArrayList;
import java.util.List;

/**
 * EventObservable
 */
public class EventObservable<T> {

    private final List<EventObserver<T>> observers = new ArrayList<>();

    public synchronized void addObserver(EventObserver<T> observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    public synchronized void deleteObserver(EventObserver<T> observer) {
        observers.remove(observer);
    }

    public synchronized void deleteObservers() {
        observers.clear();
    }

    public int countObservers() {
        return observers.size();
    }

    /**
     * Notifies the Observers that a changed occurred
     * 
     * @param arg - the state that changed
     */
    public void notifyObservers(T arg) {
        for (EventObserver<T> observer : observers) {
            observer.update(this, arg);
        }
    }

    /**
     * Notifies the Observers that a changed occurred
     */
    public void notifyObservers() {
        notifyObservers(null);
    }
}
