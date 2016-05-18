package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.jsoup.select.Elements;

public class WikiPhilosophy {
	
	final static WikiFetcher wf = new WikiFetcher();
	private static List<String> visited = new ArrayList<String>();
	private static String currentUrl;
	private static int valid = 1;
	
	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 * 
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 * 
	 * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
        String url = "https://en.wikipedia.org/wiki/Greek_language";
		Elements paragraphs = wf.fetchWikipedia(url);
		
		int counter = 0;
		currentUrl = url;

        String nextPage = processPage(paragraphs);
        while (!nextPage.equals("https://en.wikipedia.org/wiki/Philosophy")) {
        	if (nextPage == null) {
        		exit(0);
        		return;
        	}
        	visited.add(nextPage);
        	currentUrl = nextPage;
        	Elements next = wf.fetchWikipedia(nextPage);
        	nextPage = processPage(next);
        }
        visited.add(nextPage);
        exit(1);

	}

	private static int countMatches(String text, String s) {
		int count = 0;
		int i = 0;
		while ((i = text.indexOf(s, i)) != -1) {
		    i++;
		    count++;
		}
		return count;
	}

	private static String processPage(Elements paragraphs) {
		for (Element paragraph : paragraphs) {
			int parentheses = 0;
			Iterable<Node> iter = new WikiNodeIterable(paragraph);
			for (Node node: iter) {
				int open = 0, close = 0;
				String nodeName = node.nodeName();
				if (node instanceof TextNode) {
					open = countMatches(node.toString(), "(");
					close = countMatches(node.toString(), ")");
					parentheses += open - close;
				}
				if (nodeName.equals("a")) {
					String linkUrl = node.attr("abs:href");
					if (visited.contains(linkUrl)) {
						return null;
					}
					if (linkUrl.startsWith(currentUrl) || !linkUrl.startsWith("https://en.wikipedia.org") || parentheses != 0) {
						continue;
					} else {
						Element nodeElement = (Element) node;
						Elements parents = nodeElement.parents();
						for (Element parent : parents) {
							if (parent.tagName().equals("i") || parent.tagName().equals("em")) {
								valid = 0;
								break;
							}
						}
						if (valid != 0) {
							return linkUrl;
						} else {
							valid = 1;
						}
					}
				}
	        }
		}
		return null;
	}

	private static void print(List<String> urls) {
		System.out.println("Visited pages:");
		for (String s : urls) {
			System.out.println(s);
		}
	}

	private static void exit(int success) {
		if (success == 0) {
			System.out.println("Failure!");
		} else {
			System.out.println("Success!");
		}
		print(visited);
	}
}
