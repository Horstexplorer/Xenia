/*
 *     Copyright 2021 Horstexplorer @ https://www.netbeacon.de
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

package de.netbeacon.utils.statistics;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class SlidingTimeframeCounter {

    private final TimeUnit timeUnit;
    private final long timeUnits;

    private final long frameLength;
    private final long frames;

    private final List<Long> frameList = new LinkedList<>();

    private long head = System.nanoTime();
    private long tail = System.nanoTime();

    private final ReentrantLock reentrantLock = new ReentrantLock();

    public SlidingTimeframeCounter(TimeUnit timeUnit, long units, long frames){
        this.timeUnit = timeUnit;
        this.timeUnits = units;
        this.frames = frames;
        this.frameLength = timeUnit.toNanos(units) / frames;
    }

    public TimeUnit getTimeUnit(){
        return timeUnit;
    }

    public long getTimeUnits(){
        return timeUnits;
    }

    public long getFrameLength() {
        return frameLength;
    }

    public long getFrameCount() {
        return frames;
    }

    public void increment(){
        increment(1L);
    }

    public void decrement(){
        increment(-1L);
    }

    public void increment(long value){
        try{
            reentrantLock.lock();

            long now = System.nanoTime();
            long delay = now - head;
            long moves = delay / frameLength;
            if(moves > 0){
                head = now;
            }
            for(int i = 0; i < moves || frameList.isEmpty(); i++){
                frameList.add(0L);
            }
            frameList.add(frameList.remove(frameList.size() - 1) + value);
            while ((head - tail) / frameLength > frames && !frameList.isEmpty()){
                frameList.remove(0);
                tail += frameLength;
            }
            if(head < tail){
                head = tail;
            }
        }finally {
            reentrantLock.unlock();
        }
    }

    public long getCount(){
        try{
            reentrantLock.lock();

            long now = System.nanoTime() - (frameLength * frames);
            long delay = now - tail;
            long moves = delay / frameLength;
            if(moves > 0){
                tail = now;
            }
            for(int i = 0; i < moves && !frameList.isEmpty(); i++){
                frameList.remove(0);
            }
            if(head < tail){
                head = tail;
            }
            return frameList.stream().reduce(0L, Long::sum);
        }finally {
            reentrantLock.unlock();
        }
    }
}
