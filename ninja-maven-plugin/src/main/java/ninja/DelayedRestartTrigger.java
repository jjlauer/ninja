/**
 * Copyright (C) 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ninja;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class DelayedRestartTrigger extends Thread {

    //private volatile boolean restart;
    private final RunClassInSeparateJvmMachine runClassInSeparateJvmMachine;
    private final long settleDownMillis = 50;
    private final AtomicBoolean fileChanged;
    private final ReentrantLock restartLock;
    private final Condition restartRequested;
    
    public DelayedRestartTrigger(
            RunClassInSeparateJvmMachine runClassInSeparateJvmMachine) {
        
        this.restartLock = new ReentrantLock();
        this.restartRequested = this.restartLock.newCondition();
        this.fileChanged = new AtomicBoolean(false);
        this.runClassInSeparateJvmMachine = runClassInSeparateJvmMachine;

    }

    @Override
    public void run() {

        while (true) {
            
            try {
                
                // BUG: just a more efficient / atomic method of being signalled
                // wait for this thread to be signalled for a restart
                this.restartLock.lock();
                try {
                    this.restartRequested.await();
                } finally {
                    this.restartLock.unlock();
                }
                
                // BUG w/ previous -- many file system changes would trigger
                // a TON of restarts
                // wait for X millis while file changes are still occurring before
                // trying to trigger a restart
                while (this.fileChanged.compareAndSet(true, false)) {
                    System.out.println("Delaying restart for " + settleDownMillis + " ms to wait for file changes to settle");
                    Thread.sleep(settleDownMillis);
                }
                
                System.out.println("Restarting SuperDevMode");
                
                runClassInSeparateJvmMachine.restartNinjaJetty();
            } catch (InterruptedException e) {
                System.out.println("Interrupted while delayed restart..." + e.getMessage());
                e.printStackTrace();
            }
        }

    }
    
    public void triggerRestart() {
        // indicates a file changed and restart is requested
        this.fileChanged.set(true);
        
        // try to signal for a restart
        if (this.restartLock.tryLock()) {
            try {
                this.restartRequested.signal();
            } finally {
                this.restartLock.unlock();
            }
        }
    }
    
}
