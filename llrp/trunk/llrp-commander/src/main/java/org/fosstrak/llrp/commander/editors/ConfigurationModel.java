/*
 * Copyright (C) 2007 ETH Zurich
 *
 * This file is part of Fosstrak (www.fosstrak.org).
 *
 * Fosstrak is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 *
 * Fosstrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Fosstrak; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package org.fosstrak.llrp.commander.editors;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.commander.util.*;
import org.llrp.ltkGenerator.generated.ParameterDefinition;

/**
* ...
* @author zhanghao
*
*/
public class ConfigurationModel {
	
	/**
	 * Log4j instance.
	 */
	private static Logger log = Logger.getLogger(ConfigurationModel.class);
	
	private static String baseProps[] = { "llrp", "<llrp:GET_READER_CONFIG>",
			"<llrp:AntennaID>", "<llrp:RequestedData>", "<llrp:GPIPortNum>",
			"<llrp:GPOPortNum>", "<llrp:ADD_ROSPEC>", "<llrp:ROSpec>",
			"<llrp:Priority>", "<llrp:CurrentState>" };

	private ArrayList<String> reservedWords;
	
	private Properties properties;
	private String rootLogger;
	private String[] appenders;
	
	public ConfigurationModel(String configuration) throws IOException {
		properties = new Properties();
		properties.load(new ByteArrayInputStream(configuration.getBytes()));
		rootLogger = properties.getProperty("log4j.rootLogger");
		appenders = parseCategory(rootLogger);

		reservedWords = new ArrayList<String>();
		
		List<Object> list = LLRP.getLlrpDefintion().getMessageDefinitionOrParameterDefinitionOrChoiceDefinition();
		for (Object o : list){
			if (o instanceof ParameterDefinition){
				ParameterDefinition pdef = (ParameterDefinition) o;
				reservedWords.add(pdef.getName());
				log.debug(pdef.getName());
			}
		}
		
	}

	public List getCompletions(String prefix) {
		
		log.debug("Here55555");
		
		List completions = new LinkedList();
	
		for (int i = 0; i < appenders.length; i++) {
			if (testCompletion(appenders[i], prefix))
				completions.add(appenders[i]);
		}
		
		/*
		Iterator<String> reserve = reservedWords.iterator();
		while (reserve.hasNext()) {
			String word = reserve.next();
			if (testCompletion(word, prefix))
				completions.add(word);
		}
		*/
		
		for (int i = 0; i < baseProps.length; i++) {
			if (testCompletion(baseProps[i], prefix))
				completions.add(baseProps[i]);
		}
		
		return completions;
	}

	private boolean testCompletion(String completion, String prefix) {
		return completion.toLowerCase().startsWith(prefix.toLowerCase())
				&& (completion.lastIndexOf(":") == prefix.lastIndexOf(":"));
	}

	private String[] parseCategory(String value) {
		List appenders = new LinkedList();

		if (value != null) {
			StringTokenizer st = new StringTokenizer(value, ",");

			if (!(value.startsWith(",") || value.equals(""))) {
				st.nextToken();
			}

			while (st.hasMoreTokens()) {
				String appenderName = st.nextToken().trim();
				if (appenderName == null || appenderName.equals(","))
					continue;
				appenders.add("log4j.appender." + appenderName);
			}
		}
		return (String[]) appenders.toArray(new String[0]);
	}
}
