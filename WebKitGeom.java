/*
   Copyright 2012-2022 Michael Pozhidaev <msp@luwrain.org>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.web;

import java.io.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.scene.web.WebEngine;

import netscape.javascript.*;

import org.luwrain.core.*;

import static org.luwrain.graphical.FxThread.*;
import static org.luwrain.util.ResourceUtils.*;

public final class WebKitGeom {
	static final String LOG_COMPONENT = "web",
			INJECTION_NAME = "injection.js";

	static private String injection = null;
	final WebEngine engine;

	public WebKitGeom(WebEngine engine) {
		this.engine = engine;
		try {
			if (injection == null) {
				injection = getStringResource(getClass(), INJECTION_NAME);
				Log.debug(LOG_COMPONENT, "the web injection loaded");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public WebKitGeomInfo scan() throws IOException {

		ensure();
		final Object res = engine.executeScript(injection);
		if (res == null)
			throw new RuntimeException("The result of web scanning is null");

		if (!(res instanceof JSObject))
			throw new RuntimeException("The result of web scanning is not an instance of JSObject");

		Log.debug(LOG_COMPONENT, "Performing scanning of the web page");
		final JSObject jsRes = (JSObject)res;

		try {
			return new WebKitGeomInfo(engine, jsRes);
		} catch (Exception e) {
			return null;
		}
	}
}
