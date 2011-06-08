package com.fiznool.gumtreescraper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class GumtreeScraperServlet extends HttpServlet {
	
	private static final Logger log = Logger.getLogger(GumtreeScraperServlet.class.getName());
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws IOException {
		
		// Get XML file from Gumtree
		try {
			URL url = new URL("http://bristol.gumtree.com/cgi-bin/list_postings.pl?feed=rss&posting_cat=2370");
			log.config("Loading URL: " + url.toString());
					
			/*BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			StringBuffer sb = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();*/

			// Parse XML file for Title / description containing the right word
			try {
				SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
				SAXParser saxParser = saxParserFactory.newSAXParser();
				saxParser.parse(url.openStream(), new GumtreeFeedHandler());
			} catch (SAXException e) {
				log.warning(e.getLocalizedMessage());
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				log.warning(e.getLocalizedMessage());
				e.printStackTrace();
			}
			
			
			// Store the results for next time?
		} catch (MalformedURLException e) {
			log.warning(e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IOException e) {
			log.warning(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
}
