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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.views.DocumentView;
import org.w3c.dom.Element;

import java.math.*;
import java.util.regex.*;

import com.sun.jdi.connect.Connector.IntegerArgument;
import com.sun.webkit.dom.DOMWindowImpl;
import netscape.javascript.*;
import netscape.javascript.JSObject;

import java.util.logging.Logger;

import org.luwrain.core.*;

import java.lang.System;

import static org.luwrain.graphical.FxThread.*;
import static org.luwrain.web.WebKitGeom.*;

import java.util.concurrent.*;

//Инициализация WebEngine, JSObject для получения доступа к DOM в браузере внутри LUWRAIN

public final class WebKitGeomInfo {
	private final WebEngine engine;
	private final JSObject src, root;
	private final HTMLDocument doc;
	private final DOMWindowImpl window;

	final Map<Node, Item> nodes = new HashMap<>();
	Stack<Element> stack = new Stack<>();

	WebKitGeomInfo(WebEngine engine, JSObject src) throws IOException {

		ensure();
		this.engine = engine;
		this.src = src;
		this.doc = (HTMLDocument) engine.getDocument();
		this.window = (DOMWindowImpl) ((DocumentView) doc).getDefaultView();
		this.root = (JSObject) src.getMember("dom");
		Object o;// = new Object();

		// создание файла для посмотря результатов в виде txt
		File file2 = new File("WebKitGeomInfo Test.txt");
		FileWriter writer2 = new FileWriter("WebKitGeomInfo Test.txt");
		writer2.write("Hello world! 1\n");

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

				// получение данных
				if (text.equals("123") == false && text.isBlank() == false) {

					nodes.put(node, new Item(x, y, width, height, String.valueOf(text)));
					// logg.info("Got text = " + String.valueOf(text) + " and x = " + x + " y = " +
					// y + " width = " + width + " height = " + height);
				}
			}
		}

		Log.debug(LOG_COMPONENT, "geom scanning completed: " + nodes.size());
		writer2.close();

		// отработка нодов
		traverseNodesDFS();

		// this.nodes = new HashMap<>();
		// this.nodes = nodes;

		try (FileWriter writer3 = new FileWriter("WebKitGeomInfo Node Test.txt")) {

			// 3. Проверка корректности получения геометрии для выделенных тего
			Map<Node, Item> nodes = getNodes();
			// Map<Node, Item> retrieveNodes = getNodes();
			// получение узлов Map

			// итерация со свойствами узлов
			for (Map.Entry<Node, Item> entry : nodes.entrySet()) {
				Node node = entry.getKey();
				Item item = entry.getValue();

				// 4. Пересчет геометрии под новыми координатами
				// Доступ к свойствам узлов и элемента
				int x = item.x;
				int y = item.y;
				int width = item.width;
				int height = item.height;
				String text = item.text;

				// вывод в txt
				try {
					writer3.write("Node: " + NodeName(node) + "\n");
					writer3.write("Geometry: x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "\n");
					writer3.write("Text: " + text + "\n");
					writer3.write("-------------------\n");
				} catch (IOException e) {
					e.printStackTrace(System.err);
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}

			writer3.flush();
			writer3.close();

		} catch (IOException e) {
			e.printStackTrace(System.err);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	public Map<Node, Item> getNodes() {
		return nodes;
	}

	// Доставление нодов
	public String NodeName(Node node) {
		String nodeName = "";

		// Проверка тип узла и установление подходящего имена.
		switch (node.getNodeType()) {

			// получение названия тега
			case Node.ELEMENT_NODE:
				if (node instanceof Element) {
					nodeName = ((Element) node).getTagName(); // Проверка на Элемента
				}
				nodeName = NodeName(node);
				// nodeName = node.getNodeName();

				break;

			// Получение текста
			case Node.TEXT_NODE:
				Node parent = node.getParentNode();
				// получение нода для текстового узла, получив имя узла родительского элемента.

				if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
					// проверка если текст является дочери элемента

					nodeName = parent.getNodeName();
				} else {
					nodeName = "#text-Node";
					String nodeValue = node.getNodeValue();
					if (nodeValue != null) {
						nodeName += ": " + nodeValue.trim();
					}
				}
				break;

			// Получение коментарий
			case Node.COMMENT_NODE:
				nodeName = "#Сomment-Node";
				String commentValue = node.getNodeValue();
				if (commentValue != null) {
					nodeName += ": " + commentValue.trim();
				}
				break;

			default:
				nodeName = "Unknown";
				break;
		}

		// выделение тегов от контента
		/*
		 * if (nodeName.equals("div") || nodeName.equals("a") || nodeName.equals("span")
		 * ||
		 * nodeName.equals("h1") || nodeName.equals("h2") || nodeName.equals("h3")
		 * || nodeName.equals("h4") || nodeName.equals("p") || nodeName.equals("h5")
		 * || nodeName.equals("h6") || nodeName.equals("ul") || nodeName.equals("ol")
		 * || nodeName.equals("li") || nodeName.equals("table") || nodeName.equals("tr")
		 * || nodeName.equals("th") || nodeName.equals("td") || nodeName.equals("form")
		 * || nodeName.equals("input") || nodeName.equals("textarea") ||
		 * nodeName.equals("select")
		 * || nodeName.equals("button") || nodeName.equals("script")) {
		 * return nodeName;
		 * } else {
		 * return "Unknown"; // для незнакомых тегов
		 * }
		 */

		// просто возращает название тега
		return nodeName;
	}

	// Конвертация значения геометрия на number
	static int intValue(Object o) {
		if (o == null)
			return 0;
		if (o instanceof Number)
			return ((Number) o).intValue();
		return Double.valueOf(Double.parseDouble(o.toString())).intValue();
	}

	// Конвертация значения геометрия на int
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

	// Геомерия тега
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

	// Вызов метода, который выполняет обход DOM-модели в глубину и позволяет
	// обработать каждый узел
	public void traverseNodesDFS() {
		Set<Node> visited = new HashSet<>(); // создание набора посещений для отслеживания посещенных узлов

		for (Node node : nodes.keySet()) {
			if (!visited.contains(node)) {
				dfs(node, visited);
			}
		}
	}

	private void dfs(Node node, Set<Node> visited) {
		visited.add(node); // маркировка узлов как посещенные, добавление их в посещенный набор

		NodeList children = node.getChildNodes();

		// Рекурсивный обход дочерних узлов.
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (!visited.contains(child)) {
				dfs(child, visited);
			}
		}

		if (node instanceof HTMLElement) {
			HTMLElement element = (HTMLElement) node; // Проверка узел на HTMLElement
			String NodeTagName = element.getTagName(); // получение доступа к свойвам тега

			// Выделение тега с Контентом
			if (NodeTagName.equals("a") || NodeTagName.equals("span")
					|| NodeTagName.equals("p") || NodeTagName.equals("div") ||
					NodeTagName.equals("h1") || NodeTagName.equals("h2") ||
					NodeTagName.equals("h3") || NodeTagName.equals("h4") ||
					NodeTagName.equals("h5") || NodeTagName.equals("h6") ||
					NodeTagName.equals("ul") || NodeTagName.equals("ol") ||
					NodeTagName.equals("li") || NodeTagName.equals("table") ||
					NodeTagName.equals("tr") || NodeTagName.equals("th") ||
					NodeTagName.equals("td") || NodeTagName.equals("form") ||
					NodeTagName.equals("input") || NodeTagName.equals("textarea") ||
					NodeTagName.equals("select") || NodeTagName.equals("button")) {
				stack.push(element); // Получение тега в стек для форматирования
			}

			// System.out.println("Visited Node: " + NodeTagName);
			// Результат в txt
			try (FileWriter writer4 = new FileWriter("VisitedNodes.txt", true)) {
				writer4.write("Visited Node: " + NodeTagName + "\n");

				writer4.flush();
				writer4.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
