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
import java.util.concurrent.atomic.*;

import javafx.scene.web.WebEngine;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSStyleDeclaration;
import com.sun.webkit.dom.TextImpl;
import com.sun.webkit.dom.ElementImpl;

import org.luwrain.core.*;

import static org.luwrain.graphical.FxThread.*;

public final class WebObject
{
    final WebKitTree tree;
    final Node node;
    final Element el;
    public String tagName;
    public String text;

    WebObject(WebKitTree tree, Node node)
    {
	NullCheck.notNull(tree, "tree");
	NullCheck.notNull(node, "node");
	ensure();
	this.tree = tree;
	this.node = node;
	if (node instanceof Element)
	{
	    this.el = (Element)node;
	    this.tagName = this.el.getTagName();
	    this.text = null;
	} else
	    if (node instanceof TextImpl)
	    {
		final TextImpl textObj = (TextImpl)node;
		this.el = null;
		this.tagName = null;
		this.text = textObj.getWholeText();
	    } else 
	{
	    this.el = null;
	    this.tagName = null;
	    this.text = node.getClass().getName();
	}

    }

    public WebObject[] getChildren()
    {
	final AtomicBoolean isNull = new AtomicBoolean(false);	
	final List<WebObject> res = new ArrayList<>();
	runSync(()->{
	    	final NodeList items =node.getChildNodes();
	if (items == null)
	{
	    isNull.set(true);
	    return;
	}
	for(int i = 0;i < items.getLength();i++)
	{
	    final WebObject newObj = new WebObject(tree, items.item(i));
	    if (newObj.text != null && newObj.text.trim().isEmpty())
		continue;
	    res.add(newObj);
	}
	});
    if (isNull.get())
	return null;
	return res.toArray(new WebObject[res.size()]);
    }

    public boolean hasChildren()
    {
	return node.hasChildNodes();
    }

    public CSSStyleDeclaration getStyle()
    {
	if (el == null)
	    return null;
	return tree.getStyle(el);
    }

    public String getStyleAsText()
    {
	final CSSStyleDeclaration style = getStyle();
	if (style == null)
	    return null;
	final WebKitGeomInfo.Item geom = tree.geom.nodes.get(node);
	final StringBuilder b = new StringBuilder();
	if (el != null && geom != null)
	{
	    	    final ElementImpl e = (ElementImpl)el;
	    b.append("lwr-geom: true;");
	    b.append("lwr-x: ").append(String.valueOf(geom.x)).append("px;");
	    b.append("lwr-x2: ").append(String.format("%.2f", e.getOffsetLeft())).append("px;");
	    b.append("lwr-y: ").append(String.valueOf(geom.y)).append("px;");
	    	    b.append("lwr-y2: ").append(String.format("%.2f", e.getOffsetTop())).append("px;");
	    b.append("lwr-width: ").append(String.valueOf(geom.width)).append("px;");
	    b.append("lwr-height: ").append(String.valueOf(geom.height)).append("px;");
	} else
	{
	    b.append("lwr-geom: false;");
	}
	return new String(b) + style.getCssText();
    }

    @Override public String toString()
    {
	if (tagName != null)
	    return tagName;
	return text;
    }
}
