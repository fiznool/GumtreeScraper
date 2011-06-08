package com.fiznool.gumtreescraper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class GumtreeFeedHandler extends DefaultHandler {

	private static final Logger log = Logger.getLogger(GumtreeFeedHandler.class.getName());

	private static final String LOOK_FOR = "Dishwasher";

	private boolean title = false;
	private boolean link = false;

	private String titleValue = null;
	private String linkValue = null;

	private List<GumtreeItem> matchingItemList = new ArrayList<GumtreeItem>();

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		log.config("Start Element: " + qName);

		if ("TITLE".equalsIgnoreCase(qName)) {
			title = true;
		} else if ("LINK".equalsIgnoreCase(qName)) {
			link = true;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
	throws SAXException {
		if (title) {
			titleValue = new String(ch, start, length);
			log.config("Title: " + titleValue);
			title = false;
		}

		if (link) {			
			linkValue = new String(ch, start, length);
			log.config("Link: " + linkValue);
			link = false;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	throws SAXException {
		log.config("End Element: " + qName);

		if ("ITEM".equalsIgnoreCase(qName)) {
			// The item is over. Make a new object if we have a match
			if (titleValue != null && titleValue.matches("(?i).*"+LOOK_FOR+".*")) {
				// We've got a matching title. Add to matching list
				GumtreeItem item = new GumtreeItem(titleValue, linkValue);
				matchingItemList.add(item);

				// Reset values
				titleValue = null;
				linkValue = null;

				log.info("Found the following item which matches our criteria: " + item.toString());

			}
		}

	}

	@Override
	public void endDocument() throws SAXException {
		if (log.isLoggable(Level.INFO)) {
			String fmtStr = "dd/MM/yyyy hh:mm:ss";
			Date now = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat(fmtStr);
			sdf.setTimeZone(TimeZone.getTimeZone("Europe/London"));
			log.info("Parsed Gumtree feed successfully at " + sdf.format(now));
		}

		// Now send out email and store values in database
		if (matchingItemList.size() > 0) {
			// We have an email to send.

			// For each item, check if it is already been sent.
			// If it has, don't send an email.
			StringBuilder sb = new StringBuilder();
			for (GumtreeItem itemToSend : matchingItemList) {
				// Check for item
				boolean itemAlreadySent = itemExists(itemToSend);
				if (!itemAlreadySent) {
					sb.append("<p>");
					sb.append("<a href='" + itemToSend.getUrl() + "'>");
					sb.append(itemToSend.getTitle());
					sb.append("</a>");
					sb.append("</p>");
					
					// Store item so it won't be sent again
					storeItem(itemToSend);
				}
				
			}
			
			if (sb.length() > 0) {
				// We have items to send.
				String emailMsg = "<p> The following item(s) matched your search criteria (" + LOOK_FOR + "):</p>" + sb.toString();
				sendEmail(emailMsg);
			}
		}

	}

	private boolean itemExists(GumtreeItem itemToSend) {
		// Query for item in datastore
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query q = new Query("ViewedItem");
		q.addFilter("link", Query.FilterOperator.EQUAL, itemToSend.getUrl());
		
		PreparedQuery pq = datastore.prepare(q);
		int count = pq.countEntities(FetchOptions.Builder.withDefaults());
		
		return count != 0;
	}
	
	private void storeItem(GumtreeItem itemToStore) {
		// Store the item in the datastore so we don't send it again.
		// Items will just be placed in the root

		// Create the entity under the parent key
		Entity viewedItem = new Entity("ViewedItem");

		// Set properties of the entity
		viewedItem.setProperty("title", itemToStore.getTitle());
		viewedItem.setProperty("link", itemToStore.getUrl());

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(viewedItem);


	}

	private void sendEmail(String msgBody) {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("spencer.ts@gmail.com", "Gumtree Scraper"));
			msg.addRecipient(Message.RecipientType.TO,
					new InternetAddress("spencer.ts@gmail.com", "Tom Spencer"));
			msg.setSubject("Gumtree Scraper: you've got mail!");
			//msg.setText(msgBody);
			msg.setContent(msgBody, "text/html");
			Transport.send(msg);

		} catch (Exception e) {
			log.warning("There was a problem sending the email.");
			e.printStackTrace();
		}
	}

	



}
