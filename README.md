# Gumtree Scraper

A simple scraper which polls the [Gumtree](http://www.gumtree.com) RSS feed to check for keywords. If a posting is found with the specified keyword in the title, an email is sent. As Gumtree is an online classifieds website, this service will tell you quickly if a new item goes on sale, allowing you to contact the seller pretty soon after an item is posted.

Currently the service is hard coded to check the Bristol Gumtree listings every 10 minutes for posts containing the word *dishwasher*. Once I get a bit of time an admin interface will be very useful to set up individual scrapers for different keywords, categories, cities, notification email addresses and polling intervals.

And no, I don't need a dishwasher any more - the service has already done its job. :)
