/*
 *     Copyright 2020 Horstexplorer @ https://www.netbeacon.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.netbeacon.utils.concurrent;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SuspendableBlockingQueue<E> {

    private final Queue<E> queue;
    private boolean isSuspended = false;

    /**
     * Creates a new instance of this class wrapping a LinkedBlockingQueue
     */
    public SuspendableBlockingQueue(){
        queue = new LinkedBlockingQueue<>();
    }

    /**
     * Creates a new instance of this class wrapping an ArrayBlockingQueue with a given capacity
     * @param size of the ArrayBlockingQueue
     */
    public SuspendableBlockingQueue(int size){
        queue = new ArrayBlockingQueue<>(size);
    }

    /**
     * Creates a new instance of this class wrapping the supplied BlockingQueue
     * @param queue Queue
     */
    public SuspendableBlockingQueue(Queue<E> queue){
        this.queue = queue;
    }

    /**
     * Used to toggle processing
     * @param state state
     */
    public void suspend(boolean state){
        synchronized (queue){
            isSuspended = state;
            if(!isSuspended){
                queue.notifyAll();
            }
        }
    }

    /**
     * Used to get the next object from the queue
     * @return Object from the queue
     * @throws InterruptedException when interrupted
     */
    public E get() throws InterruptedException {
        synchronized (queue) {
            while (isSuspended || queue.isEmpty()) {
                queue.wait();
            }
            return queue.poll();
        }
    }

    /**
     * Used to add another object to the queue
     * @param elem object
     */
    public void put(E elem) {
        synchronized (queue) {
            queue.offer(elem);
            if (!isSuspended) queue.notify();
        }
    }

    /**
     * Returns the number of elements within the queue
     * @return
     */
    public int size(){
        return queue.size();
    }
}
