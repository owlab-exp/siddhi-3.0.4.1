/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.function;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class UUIDFunctionTestCase {

    static final Logger log = Logger.getLogger(UUIDFunctionTestCase.class);
    private int count;
    private boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testFunctionQuery1() throws InterruptedException {

        log.info("UUIDFunction test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String planName = "@plan:name('UUIDFunction') ";
        String cseEventStream = "@config(async = 'true') define stream cseEventStream (symbol string, price double, volume long , quantity int);";
        String query = "@info(name = 'query1') " +
                "from cseEventStream " +
                "select symbol, price as price, quantity, UUID() as uniqueValue " +
                "insert into outputStream;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(planName + cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                Assert.assertEquals(1.56, inEvents[0].getData()[1]);
                System.out.println("Event : " + count + ",uniqueId : " + inEvents[0].getData(3));
                count = count + inEvents.length;
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"WSO2", 1.56d, 60l, 6});
        Thread.sleep(200);
        junit.framework.Assert.assertEquals(1, count);
        executionPlanRuntime.shutdown();
    }
}
