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

import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import java.io.*; //file

import org.w3c.dom.NodeList;

import javafx.scene.web.WebEngine;

import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.views.DocumentView;

import com.sun.jdi.connect.Connector.IntegerArgument;
import com.sun.webkit.dom.DOMWindowImpl;
import netscape.javascript.*;
import netscape.javascript.JSObject;

import java.util.logging.Logger;

import org.luwrain.core.*;

import java.lang.System;

import static org.luwrain.graphical.FxThread.*;
import static org.luwrain.web.WebKitGeom.*;

public final class WebKitGeomInfo {
	private final WebEngine engine;
	private final JSObject src, root;
	private final HTMLDocument doc;
	private final DOMWindowImpl window;

	// Logger logger = Logger.getLogger("MyLogInfo");
	// FileHandler fh;

	final Map<Node, Item> nodes = new HashMap<>();

	WebKitGeomInfo(WebEngine engine, JSObject src) throws IOException {

		// File file = new File("Test.txt");
		// PrintWriter test = new PrintWriter(new FileWriter("Geometry.txt"));
		// PrintWriter test = new PrintWriter("Geometry.txt");
		// FileOutputStream test = new FileOutputStream("Geometry.txt");
		// ObjectOutputStream ObjTest = new ObjectOutputStream(test);
		// logg.info("Injection data read start!");

		ensure();
		this.engine = engine;
		this.src = src;
		this.doc = (HTMLDocument) engine.getDocument();
		this.window = (DOMWindowImpl) ((DocumentView) doc).getDefaultView();
		this.root = (JSObject) src.getMember("dom");
		Object o;// = new Object();

		File file2 = new File("WebKitGeomInfo Test.txt");
		FileWriter writer2 = new FileWriter("WebKitGeomInfo Test.txt");
		writer2.write("Hello world! 1\n");

		// Map nodes = new Map(); initializing the nodes

		for (int i = 0; !(o = root.getSlot(i)).getClass().equals(String.class); i++) {

			final JSObject jsObj = (JSObject) o;
			final JSObject rect = (JSObject) jsObj.getMember("rect");
			final String text = (String) jsObj.getMember("text");
			final Node node = (Node) jsObj.getMember("node");
			int x = -1, y = -1, width = -1, height = -1;

			// Тело цикла, когда объект o является экземпляром класса String или его
			// подкласса

			if (o instanceof String) {
				writer2.write("testing the object");
				break;
			} else {

				if (rect != null) {
					x = toInt(rect.getMember("left"));
					y = toInt(rect.getMember("top"));
					width = toInt(rect.getMember("width"));
					height = toInt(rect.getMember("height"));

					try {
						// writer2.append()
						writer2.write("Handling the nodes" + String.valueOf(text) + " and x = " + x + " y = " + y
								+ " width = " + width + " height = " + height + "\n");

					} catch (IOException e) {
						e.printStackTrace(System.err);
					} catch (Exception e) {
						e.printStackTrace(System.err);
					}
				}

				// Taking the data
				if (text.equals("123") == false && text.isBlank() == false) {

					nodes.put(node, new Item(x, y, width, height, String.valueOf(text)));
					// logg.info("Got text = " + String.valueOf(text) + " and x = " + x + " y = " +
					// y + " width = " + width + " height = " + height);
				}
			}
		}
		writer2.write("\n\n\n");

		// this.nodes = new HashMap<>();
		// this.nodes = nodes;

		Map<Node, Item> retrieveNodes = getNodes();
		// retrieving the nodes of the Map

		// iterating with the properties of the nodes
		for (Map.Entry<Node, Item> entry : nodes.entrySet()) {
			Node nod = entry.getKey();
			WebKitGeomInfo.Item item = entry.getValue();

			// Access to properties of the nodes and item
			int x = item.x;
			int y = item.y;
			int width = item.width;
			int height = item.height;
			String text = item.text;

			try {
				writer2.write("Node: " + nod.getNodeName() + "\n");
				writer2.write("Geometry: x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "\n");
				writer2.write("Text: " + text + "\n");
				writer2.write("-------------------\n");
			} catch (IOException e) {
				e.printStackTrace(System.err);
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}

		Log.debug(LOG_COMPONENT, "geom scanning completed: " + nodes.size());

		writer2.close();
	}

	public Map<Node, Item> getNodes() {
		return nodes;
	}

	static int intValue(Object o) {
		if (o == null)
			return 0;
		if (o instanceof Number)
			return ((Number) o).intValue();
		return Double.valueOf(Double.parseDouble(o.toString())).intValue();
	}

	// converting the value to int
	public static int toInt(Object obj) {
		if (obj instanceof Integer) {
			return ((Integer) obj);
			// return ((Number) obj).intValue();
		} else if (obj instanceof String) {
			// else if (obj instanceof JSObject) {
			try {
				return Integer.parseInt((String) obj);
			} catch (NumberFormatException e) {
			}
		}
		return 0;
	}

	static public final class Item {
		public final int x, y, width, height;
		public final String text;

		Item(int x, int y, int width, int height, String text) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.text = text;
		}
	}

	public void traverseNodesDFS() {
		Set<Node> visited = new HashSet<>(); // creating a visiting set to keep track of visited nodes

		for (Node node : nodes.keySet()) {
			if (!visited.contains(node)) {
				dfs(node, visited);
			}
		}
	}

	private void dfs(Node node, Set<Node> visited) {
		visited.add(node); // marking nodes as visited adding them to a visited set

		NodeList children = node.getChildNodes();
		// Recursively traversing the child nodes
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (!visited.contains(child)) {
				dfs(child, visited);
			}
		}
	}
}
