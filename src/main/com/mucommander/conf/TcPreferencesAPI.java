/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.conf;

import java.util.List;

import com.mucommander.commons.conf.ValueList;

/**
 * 
 * @author Arik Hadas
 */
public interface TcPreferencesAPI {
	boolean setVariable(TcPreference preference, String value);
	boolean setVariable(TcPreference preference, int value);
	boolean setVariable(TcPreference preference, List<String> value, String separator);
	boolean setVariable(TcPreference preference, float value);
	boolean setVariable(TcPreference preference, boolean value);
	boolean setVariable(TcPreference preference, long value);
	boolean setVariable(TcPreference preference, double value);
	
	String getVariable(TcPreference preference);
	String getVariable(TcPreference preference, String value);
	int getVariable(TcPreference preference, int value);
	List<String> getVariable(TcPreference preference, List<String> value, String separator);
	float getVariable(TcPreference preference, float value);
	boolean getVariable(TcPreference preference, boolean value);
	long getVariable(TcPreference preference, long value);
	double getVariable(TcPreference preference, double value);
	ValueList getListVariable(TcPreference preference, String separator);
	
	// TODO: remove those methods
	boolean getBooleanVariable(String name);
	String  getVariable(String name);
	
	boolean isVariableSet(TcPreference preference);
	
	String removeVariable(String name);
}
