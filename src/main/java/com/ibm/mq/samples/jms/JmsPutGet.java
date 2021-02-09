/*
* (c) Copyright IBM Corporation 2018
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.ibm.mq.samples.jms;


import javax.jms.*;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

/**
 * A minimal and simple application for Point-to-point messaging.
 *
 * Application makes use of fixed literals, any customisations will require
 * re-compilation of this source file. Application assumes that the named queue
 * is empty prior to a run.
 *
 * Notes:
 *
 * API type: JMS API (v2.0, simplified domain)
 *
 * Messaging domain: Point-to-point
 *
 * Provider type: IBM MQ
 *
 * Connection mode: Client connection
 *
 * JNDI in use: No
 *
 */
public class JmsPutGet {

	// System exit status value (assume unset value to be 1)
	private static int status = 1;

	// Create variables for the connection to MQ
	private static final String HOST = "localhost"; // Host name or IP address
	private static final int PORT = 1414; // Listener port for your queue manager
	private static final String CHANNEL = "DEV.APP.SVRCONN"; // Channel name
	private static final String QMGR = "QM1"; // Queue manager name
	private static final String APP_USER = "app"; // User name that application uses to connect to MQ
	private static final String APP_PASSWORD = "passw0rd"; // Password that the application uses to connect to MQ
	private static final String QUEUE_NAME = "DEV.QUEUE.1"; // Queue that the application uses to put and get messages to and from

	private static final String response9610 = "AL4ALM6945949705401110STIDQAL4R " +
			"STILIBRAMD10015205000000000000000000000000000050000100001 TR961069459497054011101231S0000Todo funciona correctamente gracias a esta prueba.         96101000000096100000010372569160192,168,127,1560202012011610011             110000001                     SS";
	private static final String response9369 = "AL4ALM6945949705401110STIDQAL4R " +
			"STILIBRAMD10015205000000000000000000000000000050000100001 TR961069459497054011101231S0000Todo funciona correctamente gracias a esta prueba.         96101000000096100000010372569160192,168,127,1560202012011610011             110000001                     2020120114151008521";


	/**
	 * Main method
	 *
	 * @param args
	 */


	public static void main(String[] args) throws InterruptedException {
		new JmsPutGet().main();
	}

	public void main() {
		// Variables
		JMSContext context = null;
		Destination origin = null;
		Destination destination = null;
		JMSConsumer consumer = null;


		try {
			// Create a connection factory
			JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
			JmsConnectionFactory cf = ff.createConnectionFactory();
			// Set the properties
			cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, HOST);
			cf.setIntProperty(WMQConstants.WMQ_PORT, PORT);
			cf.setStringProperty(WMQConstants.WMQ_CHANNEL, CHANNEL);
			cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
			cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, QMGR);
			cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JmsPutGet (JMS)");
			cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
			cf.setStringProperty(WMQConstants.USERID, APP_USER);
			cf.setStringProperty(WMQConstants.PASSWORD, APP_PASSWORD);

			// Create JMS objects
			context = cf.createContext();
			origin = context.createQueue("queue:///DEV.QUEUE.1");
			destination = context.createQueue("queue:///DEV.QUEUE.2");

			consumer(context, destination, origin, cf);

			//recordSuccess();
		} catch (JMSException jmsex) {
			recordFailure(jmsex);
		}
	}

	public void producer(JMSContext context, Destination destination, String msg, String correlationId)
			throws JMSException, InterruptedException {

		//String msgResponse = "0012520 LOGIN 200 TRANSACCION EXISTOSA               1525522525";
		String msgResponse = response9369;
		//Thread.sleep(500);
		System.out.println("Request message : " + msg);
		System.out.println("Response message : " + msgResponse);
		TextMessage message = context.createTextMessage(msgResponse);
		message.setJMSCorrelationID(correlationId);
		JMSProducer producer = context.createProducer();
		producer.send(destination, message);
	}

	public void consumer(JMSContext context, Destination destination,
						 Destination origin,JmsConnectionFactory cf)
			throws JMSException {

		Connection connection = cf.createConnection();
		connection.createSession()
				.createConsumer(origin)
				.setMessageListener( listener -> {
					try {
						producer(context, destination, listener.getBody(String.class), listener.getJMSMessageID());
					} catch (JMSException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				});
		connection.start();
	}
	/**
	 * Record this run as successful.
	 */
	private static void recordSuccess() {
		System.out.println("SUCCESS");
		status = 0;
		return;
	}

	/**
	 * Record this run as failure.
	 *
	 * @param ex
	 */
	private static void recordFailure(Exception ex) {
		if (ex != null) {
			if (ex instanceof JMSException) {
				processJMSException((JMSException) ex);
			} else {
				System.out.println(ex);
			}
		}
		System.out.println("FAILURE");
		status = -1;
		return;
	}

	/**
	 * Process a JMSException and any associated inner exceptions.
	 *
	 * @param jmsex
	 */
	private static void processJMSException(JMSException jmsex) {
		System.out.println(jmsex);
		Throwable innerException = jmsex.getLinkedException();
		if (innerException != null) {
			System.out.println("Inner exception(s):");
		}
		while (innerException != null) {
			System.out.println(innerException);
			innerException = innerException.getCause();
		}
		return;
	}

}
