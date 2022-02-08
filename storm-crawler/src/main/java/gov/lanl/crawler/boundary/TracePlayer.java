package gov.lanl.crawler.boundary;

import java.io.File;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.bogdanlivadariu.gifwebdriver.GifScreenshotWorker;
import com.github.bogdanlivadariu.gifwebdriver.GifWebDriver;

public class TracePlayer extends AbstractWebDriverEventListener {
	String allarea = "click all links in an area";
	TraceUtils bu = new TraceUtils();
	int clickcount = 0;
	String slowmode;
	String partialexit = null;
	static Logger statsloger = Logger.getLogger("stats");
	// statsloger.info(id+" "+srvdate+" "+logservice+" 0 0 1 0");
	WebDriver gdriver;
	TracePlayer(String slowmode,RemoteWebDriver driver) {
		this.slowmode = slowmode;
		this.gdriver = new GifWebDriver(driver);
		 //GifScreenshotWorker gifWorker = ((GifWebDriver) gdriver).getGifScreenshotWorker();
	        //gifWorker.setTimeBetweenFramesInMilliseconds(1000);
	       // gifWorker.setRootDir("/Users/Lyudmila/stormarchiver/gif/");
	       // gifWorker.setLoopContinuously(true);

		// driver.register(new HighLighterEventListener());

	}

	public void finish() {
		File createdGif = ((GifWebDriver) gdriver).getGifScreenshotWorker().createGif();
	}
	public static By getBy(String locatortype, String tn) {

		switch (locatortype) {
		case "XPath":
			return By.xpath(tn);
		case "CSSSelector":
			return By.cssSelector(tn);
		case "TagName":
			return By.tagName(tn);
		default:
			return null;
		}

	}

	public int getTotalClickcount() {
		return clickcount;
	}

	public String getPartialexit() {
		return partialexit;
	}

	public int getCount(By by, RemoteWebDriver driver) {
		WebElement w = getElementByLocator(by, driver);
		int result = 0;
		String a = "";
		if (w != null) {
			a = w.getText();
			try {
				result = Integer.parseInt(a);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("problem to download:" + a);
				e.printStackTrace();
			}
		}
		return result;

	}

	public Boolean isDisabled(ArrayList stopvalues, RemoteWebDriver driver, String rlocatortype) {

		Iterator it = stopvalues.iterator();
		List<WebElement> ls = null;
		while (it.hasNext()) {
			String tn = (String) it.next();
			ls = getElementsBy(rlocatortype, tn, driver);
			if (ls != null)
				break;
		}
		return ls == null ? false : true;

	}

	public static List<WebElement> getElementsBy(String locatortype, String tn, RemoteWebDriver driver) {
		By by = getBy(locatortype, tn);
		if (by != null) {
			List<WebElement> ls = getElementsByLocator(by, driver);
			return ls;
		} else {
			return null;
		}

	}

	public int getCountValuefromElement(JsonNode parentNode, RemoteWebDriver driver) {
		String s = parentNode.path("repeat").path("until").path("selectorValue").asText();
		String locator = parentNode.path("repeat").path("until").path("selectorType").asText();
		int how_many = getCount(getBy(locator, s), driver);
		return how_many;
	}

	Long getRelativeHeight(JavascriptExecutor js) {
		Long a = (Long) js.executeScript("return document.documentElement.scrollTop;");
		System.out.println("top" + a);
		Long b = (Long) js
				.executeScript("return document.documentElement.scrollHeight - document.documentElement.clientHeight;");
		System.out.println("height" + b);
		if (b.equals(0L)) {
			return -1L;
		}
		Long relativeHeight = a / b;
		System.out.println("rel" + relativeHeight);
		return relativeHeight;
	}

	public static List<WebElement> getElementsByLocator(final By locator, RemoteWebDriver driver) {
		if (locator == null) {
			return null;
		}
		// LOGGER.info( "Get element by locator: " + locator.toString() );
		final long startTime = System.currentTimeMillis();
		Wait<WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(8, TimeUnit.SECONDS)
				.pollingEvery(2, TimeUnit.SECONDS)
				.ignoring(StaleElementReferenceException.class, NoSuchElementException.class);
		int tries = 0;
		boolean found = false;
		WebElement we = null;
		List<WebElement> ls = null;
		while ((System.currentTimeMillis() - startTime) < 25000) {
			// LOGGER.info( "Searching for element. Try number " + (tries++) );
			System.out.println("Searching for element. Try number " + (tries++));
			try {

				ls = (List<WebElement>) wait.until(ExpectedConditions.
				// .elementToBeClickable(locator));
				// visibilityOfAllElementsLocatedBy(locator);
						presenceOfAllElementsLocatedBy(locator));
				found = true;
				break;
			} catch (StaleElementReferenceException | NoSuchElementException | TimeoutException e) {
				// LOGGER.info( "Stale element: \n" + e.getMessage() + "\n");
			}

		}
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		if (found) {
			System.out.println("Found element after waiting for " + totalTime + " milliseconds.");
			// LOGGER.info("Found element after waiting for " + totalTime + " milliseconds."
			// );
		} else {
			System.out.println("Failed to find element after waiting for " + totalTime + " milliseconds.");
			// LOGGER.info( "Failed to find element after " + totalTime + " milliseconds."
			// );
		}
		return ls;
	}

	public static WebElement getElementByLocator(final By locator, RemoteWebDriver driver) {
		// LOGGER.info( "Get element by locator: " + locator.toString() );
		final long startTime = System.currentTimeMillis();
		Wait<WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(4, TimeUnit.SECONDS)
				.pollingEvery(2, TimeUnit.SECONDS)
				.ignoring(StaleElementReferenceException.class, NoSuchElementException.class);

		int tries = 0;
		boolean found = false;
		WebElement we = null;

		while ((System.currentTimeMillis() - startTime) < 8000) {
			// LOGGER.info( "Searching for element. Try number " + (tries++) );
			System.out.println("Searching for element. Try number " + (tries++));
			try {
				we = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
				found = true;
				break;
			} catch (StaleElementReferenceException | NoSuchElementException | TimeoutException e) {
				// LOGGER.info( "Stale element: \n" + e.getMessage() + "\n");
			}
		}
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		if (found) {
			System.out.println("Found element after waiting for " + totalTime + " milliseconds.");
			// LOGGER.info("Found element after waiting for " + totalTime + " milliseconds."
			// );
		} else {
			System.out.println("Failed to find element after waiting for " + totalTime + " milliseconds.");
			// LOGGER.info( "Failed to find element after " + totalTime + " milliseconds."
			// );
		}
		return we;
	}

	public String print_tag(WebElement we) {
		String tagName = "";
		try {
			tagName = we.getTagName();
			if (tagName != null) {
				System.out.println("tagname:" + tagName);
			}
		} catch (StaleElementReferenceException e) {
			// we = getElementsByIndex(locatortype, tn, driver, i);
			// tagName = we.getTagName();
			// System.out.println("tagname_:" + tagName);
		}
		return tagName;
	}

	void sClick(WebElement we,RemoteWebDriver driver,By by, int index){
		
		new FluentWait<RemoteWebDriver>(driver).withTimeout(120, TimeUnit.SECONDS)
		.pollingEvery(5, TimeUnit.SECONDS).until(webDriver -> ((JavascriptExecutor) webDriver)
				.executeScript("return  document.readyState =='complete'"));

		try {
			Actions actions = new Actions(driver);
			actions.moveToElement(we);
			actions.perform();
			Actions builder = new Actions(driver);
			builder.moveToElement(we).click(we);
			builder.perform();
			/*
			 * JavascriptExecutor js = (JavascriptExecutor) driver; //
			 * js.executeScript("window.scrollTo(0, " + loc + ");"); //
			 * js.executeScript("arguments[0].scrollIntoViewIfNeeded(true);", we);
			 * js.executeScript("arguments[0].click();", we);
			 */
		} catch (StaleElementReferenceException e) {
	          //one more attemp to click  
			  new WebDriverWait(driver, 25) .ignoring(StaleElementReferenceException.class)
			  .until((WebDriver d ) -> { WebElement w = getElementsByLocatorIndex(by, 
			  d, index); Actions builder = new Actions(driver);
			  builder.moveToElement(w).click(w); builder.perform(); return true; });
			 
		}
	}
	public int doClick(RemoteWebDriver driver, By by, String url, Boolean goback, Map myoptions,
			List<SimpleEntry> dummyContent) {
		StringBuffer sblog = new StringBuffer();
		int status = 0;
		List<WebElement> ls = getElementsByLocator(by, driver);
		// String current = driver.getWindowHandle();
		String subtrace = null;
		int i = 0;
		int start = 0;
		if (ls != null) {
			System.out.println("size:" + ls.size());
			if (myoptions.containsKey("index")) {
				start = (int) myoptions.get("index");

			}
			for (i = start; i < ls.size(); i++) {
				System.out.println("element number:" + i);
				try {
					status = 2;
					WebElement we = ls.get(i);
					String tagName = print_tag(we);

					String hr = we.getAttribute("href");

					String estyle = we.getAttribute("style");
					System.out.println("style:" + estyle);
					if (slowmode.equals("true")) {
						highLighterMethod(driver, we);
						((GifWebDriver) gdriver).getGifScreenshotWorker().takeScreenshot();
					}

					int loc = we.getLocation().getY();
					System.out.println("element location" + loc);
					// System.out.println("options"+myoptions.size());
					if (!myoptions.containsKey("repeatedclick")) {
						if (!myoptions.containsKey("click")) {
						if (tagName.equals("a") && myoptions.containsKey("subtrace")) {
							subtrace = (String) myoptions.get("subtrace");
							status = 3;
						}
						System.out.println("status" + status);
						if (hr != null) {
							System.out.println("element url" + hr);
							dummyContent.add(new SimpleEntry(hr, subtrace));
							clickcount = clickcount + 1;
							continue;
						}
						}
					}

					boolean q = we.isEnabled();
					System.out.println("is_enabled element:" + q);

					// RetryingFindClick(we,driver);
					sClick( we, driver, by,i);
					if (slowmode.equals("true")) {
					((GifWebDriver) gdriver).getGifScreenshotWorker().takeScreenshot();
					}
					// js.notify();

					clickcount = clickcount + 1;
					System.out.println(" clicked element number:" + i);
				
					status = 1;
					     if (slowmode.equals("true")) {
						 try {Thread.sleep(500);} catch (InterruptedException ie) {}
						 change_style(we, driver, estyle);
					     }
				  }  catch (Exception e) {

					System.out.println(e.getMessage());
					System.out.println("element_number:" + i);
                   }
              
				if (start>0) break;
				
			} // for

		} // if
		return status;
	}

	public static WebElement getElementsByLocatorIndex(final By locator, WebDriver driver, int ind) {
		if (locator == null) {
			return null;
		}
		// LOGGER.info( "Get element by locator: " + locator.toString() );
		final long startTime = System.currentTimeMillis();
		Wait<WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(8, TimeUnit.SECONDS)
				.pollingEvery(2, TimeUnit.SECONDS)
				.ignoring(StaleElementReferenceException.class, NoSuchElementException.class);
		int tries = 0;
		boolean found = false;
		WebElement we = null;
		List<WebElement> ls = new ArrayList();
		while ((System.currentTimeMillis() - startTime) < 25000) {
			// LOGGER.info( "Searching for element. Try number " + (tries++) );
			System.out.println("Searching for element. Try number " + (tries++));
			try {

				ls = (List<WebElement>) wait.until(ExpectedConditions.
				// .elementToBeClickable(locator));
				// visibilityOfAllElementsLocatedBy(locator);
						presenceOfAllElementsLocatedBy(locator));
				found = true;
				break;
			} catch (StaleElementReferenceException | NoSuchElementException | TimeoutException e) {
				// LOGGER.info( "Stale element: \n" + e.getMessage() + "\n");
			}

		}
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		if (found) {
			System.out.println("Found element after waiting for " + totalTime + " milliseconds.");
			// LOGGER.info("Found element after waiting for " + totalTime + " milliseconds."
			// );
		} else {
			System.out.println("Failed to find element after waiting for " + totalTime + " milliseconds.");
			// LOGGER.info( "Failed to find element after " + totalTime + " milliseconds."
			// );
		}
		return ls.get(ind);
	}

	public HashMap<String, Object> getPreferredSelector(JsonNode node, Boolean prefer) {
		ArrayNode array = (ArrayNode) node.path("selectors");
		String selector = null;
		ObjectMapper mapperObj = new ObjectMapper();
		for (JsonNode element : array) {
			String jsonStr = element.toString();
			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			try {
				resultMap = mapperObj.readValue(jsonStr, new TypeReference<HashMap<String, Object>>() {
				});
				Boolean pr = (Boolean) resultMap.get("selectorPreferred");
				if (pr.equals(prefer)) {
					return resultMap;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public Map add_subtrace(JsonNode parentNode) {
		Map _options = new HashMap();
		Iterator<JsonNode> tmp = parentNode.get("children").iterator();
		if (tmp.hasNext()) {
			ObjectMapper mapperObj = new ObjectMapper();
			try {
				String subtrace = mapperObj.writeValueAsString(parentNode);
				System.out.println(subtrace);
				_options.put("subtrace", subtrace);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// stop_traversing = true;
		}
		return _options;
	}

	public void play_video(RemoteWebDriver driver, String urlValue, List<SimpleEntry> urls, int frameindex) {
		Map _options = new HashMap();

		// new WebDriverWait(driver, 10).until(
		// ExpectedConditions.elementToBeClickable(By.xpath("//button[@class='ytp-play-button
		// ytp-button']")))
		// .click();
		// List<WebElement> iframe = driver.findElements(By.tagName("iframe"));
		// driver.switchTo().frame(frameindex);
		// driver.switchTo().frame(iframe.get(0));
		// doClick(driver, "XPath","//button[@class='ytp-play-button ytp-button']" ,
		// urlValue, false, _options, urls);
		JavascriptExecutor js = driver;
		// document.getElementById('movie_player') ||||
		// document.getElementsByTagName('embed')[0];""
		Object player_status = js.executeScript("return document.getElementById('movie_player').getPlayerState()");
		System.out.println(player_status);
		while (!player_status.toString().equals("0")) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			player_status = js.executeScript("return document.getElementById('movie_player').getPlayerState()");
			System.out.println(player_status);
		}
		// driver.findElement(By.cssSelector("button[class='ytp-large-play-button
		// ytp-button']")).click();
		// doClick(driver, "XPath", "//button[@class='ytp-play-button ytp-button']",
		// urlValue, false, _options, urls);
		driver.switchTo().defaultContent();
	}

	public void traverseChildren(JsonNode parentNode, RemoteWebDriver driver, String urlValue, List<SimpleEntry> urls) {
		Iterator<JsonNode> eventElements = parentNode.get("children").iterator();

		while (eventElements.hasNext()) {
			System.out.println("traverse children");
			JsonNode node = eventElements.next();
			traverseTrace(node, driver, urlValue, urls);
		}
	}

	public void process(JsonNode parentNode) {
		Iterator<JsonNode> eventElements = parentNode.get("children").iterator();
		while (eventElements.hasNext()) {
			System.out.println("process children");
			JsonNode node = eventElements.next();
			 ObjectNode parentObjectNode = (ObjectNode) node;
			 String action=parentObjectNode.path("actionName").asText();
			 parentObjectNode.put("actionName", ":"+action);
			 process(node);
			
		}
	}
	// this is main entry point for the trace replay
	public void traverseTrace(JsonNode parentNode, RemoteWebDriver driver, String urlValue, List<SimpleEntry> urls) {
		// traverse all nodes that belong to the parent
	
		
		StringBuffer sb = new StringBuffer();
		System.out.println("nodename" + parentNode.path("actionName"));
		System.out.println("order" + parentNode.path("eventOrder"));
		String order = parentNode.path("eventOrder").asText();
		String action = parentNode.path("actionName").asText();
		String evname = parentNode.path("name").asText();
		sb.append(urlValue + "," + evname + "," + action + "," + order);
		System.out.println("action" + action);
		JsonNode repeatNode = parentNode.path("repeat").path("until");
		
		 System.out.println("repeat null"+repeatNode.isNull());
		 System.out.println("repeat missing"+repeatNode.isMissingNode());
		 boolean stop_traversing = false;
		 JsonNode wNode = parentNode.path("wait").path("until");
		
		// one click
		
		if (action.equals("click") && repeatNode.isMissingNode()) {
			if (wNode.isMissingNode()) {
				// simple click
				Map sel = getPreferredSelector(parentNode, true);
				// getBy((String) sel.get("selectorType"), (String) sel.get("selector"));
				bu.waitForJSandJQueryToLoad(driver);
				//bu.presClick(driver,getBy((String) sel.get("selectorType"), (String)
				//sel.get("selector")) );
				Map _options = add_subtrace(parentNode);
				//_options.put("click", true);
				int status = doClick(driver, getBy((String) sel.get("selectorType"), (String) sel.get("selector")),
						urlValue, true,_options, urls);
				if (status == 3) {
					stop_traversing = true;
				}
				statsloger.info(urlValue + "," + evname + "," + action + "," + order + "," + status);
			} else {
				// click video button play
				// testing not in production
				// get frameIndex
				play_video(driver, urlValue, urls, 0);

			}

		}
// this is old behavior
		if (action.equals("_allarea") && repeatNode.isMissingNode()) {
			if (wNode.isMissingNode()) {
				// all links in the area click
				Map sel = getPreferredSelector(parentNode, true);
				// getBy((String) sel.get("selectorType"), (String) sel.get("selector"));
				int status = doClick(driver, getBy((String) sel.get("selectorType"), (String) sel.get("selector")),
						urlValue, true, add_subtrace(parentNode), urls);
				if (status == 3) {
					stop_traversing = true;
				}
				statsloger.info(urlValue + "," + evname + "," + action + "," + order + "," + status);
			} 
		}
		
	   
     //  Boolean   sameclick = action.equals(allarea)||action.equals("click");
     //new behavior multiple click
		if (action.equals(allarea) && repeatNode.isMissingNode()) {
			if (wNode.isMissingNode()) {
				// all links in the area click
				Map sel = getPreferredSelector(parentNode, true);
				By by = null;
				String locatortype = null;
				String tn = null;
				if (sel != null) {
					locatortype = (String) sel.get("selectorType");
					tn = (String) sel.get("selector");
					by = getBy(locatortype, tn);
				}
				List<WebElement> ls = getElementsByLocator(by, driver);
				int howmany =0;
				 if (ls !=null) {
					 howmany = ls.size();
				 }
				System.out.println("howmany"+howmany);
				int status = 0;
				for (int i = 0; i < howmany; i++) {
					Map _options=add_subtrace(parentNode);
					_options.put("index", i);
					 status = doClick(driver, by, urlValue, false, _options, urls);	
					 traverseChildren(parentNode, driver, urlValue, urls);
						//JsonNode p = parentNode.get("children");
					
				}
				process(parentNode);
				statsloger.info(urlValue + "," + evname + "," + action + "," + order + "," + status);
				
			} 
		}
		
		
		if (action.equals("clickandback") && repeatNode.isMissingNode()) {
			if (wNode.isMissingNode()) {
				// all links in the area click
				Map sel = getPreferredSelector(parentNode, true);
				By by = null;
				String locatortype = null;
				String tn = null;
				if (sel != null) {
					locatortype = (String) sel.get("selectorType");
					tn = (String) sel.get("selector");
					by = getBy(locatortype, tn);
				}
				List<WebElement> ls = getElementsByLocator(by, driver);
				int howmany =0;
				 if (ls !=null) {
					 howmany = ls.size();
				 }
				System.out.println("howmany"+howmany);
				int status = 0;
				  String currentPageHandle = driver.getWindowHandle();
				for (int i = 0; i < howmany; i++) {
					Map _options=add_subtrace(parentNode);
					_options.put("index", i);
					  
					 status = doClick(driver, by, urlValue, false, _options, urls);	
					   String back_url = driver.getCurrentUrl().toString();
					   
						System.out.println("back_url:" + back_url);
						if (!back_url.equals(urlValue)){
						JavascriptExecutor js = (JavascriptExecutor) driver; 
						js.executeScript("window.history.go(-1)");
						//driver.switchTo().window(currentPageHandle);
						}
						
					
				}
				ArrayList<String> tabHandles = new ArrayList<String>(driver.getWindowHandles());

				for(String eachHandle : tabHandles)
				{
				    if (!eachHandle.equals(currentPageHandle)) {
					driver.switchTo().window(eachHandle);
					driver.close();
					driver.switchTo().window(currentPageHandle);
				    }
				}
				
				
				statsloger.info(urlValue + "," + evname + "," + action + "," + order + "," + status);
				
			} 
		}
		
		
		if (action.equals("scroll") && repeatNode.isMissingNode()) {

			///
		}
		if (action.equals("hover") && repeatNode.isMissingNode()) {
			Map sel = getPreferredSelector(parentNode, true);
			doHover(driver, getBy((String) sel.get("selectorType"), (String) sel.get("selector")));
			///
		}

		// looping multiple next clicks

		if (!repeatNode.isMissingNode()) {
			String selectorCondition = parentNode.path("repeat").path("until").path("selectorCondition").asText();
			By by = null;
			String locatortype = null;
			String tn = null;
			Map sel = getPreferredSelector(parentNode, true);
			if (sel != null) {
				locatortype = (String) sel.get("selectorType");
				tn = (String) sel.get("selector");
				by = getBy(locatortype, tn);
			}
			String name = (String) parentNode.path("name").asText();
			System.out.println("node name " + name);

			if (action.equals("scroll")) {
				boolean readyStateComplete = false;
				int scrollcount = 0;
				int max = -1;
				String s = parentNode.path("repeat").path("until").path("selectorValue").asText();
				if (!s.equals("all")) {
					max = Integer.parseInt(s);
				}
				if (s.equals("all")) {
					// infinite scroll

					JavascriptExecutor js = ((JavascriptExecutor) driver);
					while (true) {
						// js.executeScript("window.scrollBy(0,document.body.offsetHeight);");
						js.executeScript("window.scrollBy(0,1024);");
						Object height = js.executeScript("return document.body.offsetHeight");
						System.out.println("offset height" + height);
						scrollcount = scrollcount + 1;
						readyStateComplete = bu.waitForJSandJQueryToLoad(driver);
						Long relativeHeight = getRelativeHeight(js);

						traverseChildren(parentNode, driver, urlValue, urls);
						if (max > -1) {
							if (scrollcount == max)
								break;
						}
						if (relativeHeight.equals(1l)) {
							break;
						}
						if (relativeHeight.equals(-1l)) {
							break;
						}
					}

				}
			}

			if (action.equals("hover")) {

			}
			if (action.equals("click")) {

				Map _options = (Map) new HashMap();
				_options.put("repeatedclick", "repeatedclick");

				switch (selectorCondition) {
				// case "clickCount":
				case "value_matches":

					// new slideshare case
					int totalslides = getCountValuefromElement(parentNode, driver);
					System.out.println("slides" + totalslides);
					int clickcount_ = 0;
					looplbl: while (true) {
						if (clickcount_ > totalslides) {
							System.out.println("exiting resource count");
							break looplbl;
						}
						traverseChildren(parentNode, driver, urlValue, urls);
						bu.waitForJSandJQueryToLoad(driver);
						doClick(driver, by, urlValue, false, _options, urls);
						// bu.dependableClick(driver, by);
						bu.waitForJSandJQueryToLoad(driver);
						System.out.println("slide " + clickcount_);
						clickcount_ = clickcount_ + 1;
						clickcount = clickcount + 1;

					} // while
					//stop_traversing = true;
					process(parentNode);
					break;
				case "changes":
					// currently depricated former slideshare case where url of page changes
					String newurl = driver.getCurrentUrl().toString();
					System.out.println("newurl" + newurl);
					int slides = 0;
					looplbl: while (newurl.equals(urlValue)) {
						slides = slides + 1;
						System.out.println("slides" + slides);
						traverseChildren(parentNode, driver, urlValue, urls);
						bu.waitForJSandJQueryToLoad(driver);
						doClick(driver, by, urlValue, false, _options, urls);
						newurl = driver.getCurrentUrl().toString();
						if (slides > 200) {
							System.out.println("breaking loop 200 slides");
							partialexit = "200slides";
							break looplbl;
						}

					}
					stop_traversing = true;
					driver.navigate().back();
					newurl = driver.getCurrentUrl().toString();
					System.out.println("backurl" + newurl);
					break;
				// curently depricated , use default
				case "disabled":
					// case when button is disabled
					int crazycase = 0;
					// figshare case
					// should I change
					// boolean bnext =
					// driver.findElement(By.cssSelector("btn.btn-default.btn-next")).isEnabled();
					ArrayList stopvalues = (ArrayList) new ArrayList();
					String type = parentNode.path("repeat").path("until").path("selectorType").asText();
					Boolean dis = false;
					if (type.equals("selectorPreferred")) {
						if (locatortype.equals("CSSSelector")) {
							stopvalues.add(tn + "[disabled]");
							stopvalues.add(tn + ".disabled");
						} else {
							stopvalues.add(tn + "[@disabled]");
						}
						dis = isDisabled(stopvalues, driver, locatortype);
						System.out.println("f-disable:" + dis);
					}
					// int status = 0;
					looplbl: while (!(dis)) {
						if (crazycase > 75) {// && filter.contains("figshare")) {
							System.out.println("exiting figshare");
							partialexit = "75clicks";
							break looplbl;
						}
						traverseChildren(parentNode, driver, urlValue, urls);
						bu.waitForJSandJQueryToLoad(driver);
						bu.dependableClick(driver, by);

						dis = isDisabled(stopvalues, driver, locatortype);
						crazycase = crazycase + 1;
						System.out.println("clicked " + crazycase);
						System.out.println("disable:" + dis);
					}
					break;
				case "equals":
					// case one use supplyed manual count
					int usercount = 0;
					clickcount = 0 ;
					String typ = parentNode.path("repeat").path("until").path("selectorType").asText();
					if (typ.equals("new_resource_count")) {
						usercount = parentNode.path("repeat").path("until").path("selectorValue").intValue();
						System.out.println( usercount);
					}

					looplbl: while (true) {
						if (clickcount > usercount) {
							System.out.println("exiting resource count");
							break looplbl;
						}
						//traverseChildren(parentNode, driver, urlValue, urls);

						bu.waitForJSandJQueryToLoad(driver);
						doClick(driver, by, urlValue, false, _options, urls);
						traverseChildren(parentNode, driver, urlValue, urls);
						// bu.dependableClick(driver, by);
						bu.backscroll500(driver);

						String currentUrl = driver.getCurrentUrl();
						System.out.println("currenturl:" + currentUrl);
						System.out.println("clicked " + clickcount);
						clickcount = clickcount + 1;

					} // while
					//stop_traversing = true;
					process(parentNode);
					break;
				case "not_exists":
					// button not exists case
					Boolean isPresent_ = true;
					bu.backscroll0(driver);
					System.out.println(isPresent_);
					int status_ = 0;
					int nextcount = 1;
					while (isPresent_) {
						traverseChildren(parentNode, driver, urlValue, urls);
						status_ = doClick(driver, by, urlValue, false, _options, urls);
						System.out.println("nextclicked " + nextcount);
						if (status_ == 0 || nextcount == 100) {
							System.out.println("no next button");
							stop_traversing = true;
							isPresent_ = false;
							break;
						}
						nextcount = nextcount + 1;
						bu.backscroll0(driver);
						System.out.println(isPresent_);

					}
					process(parentNode);
					break;
				//experement	
				case "sameselector":
					List<WebElement> ls = getElementsByLocator(by, driver);
					int howmany = ls.size();
					for (int i = 0; i < howmany; i++) {

						_options.put("index", i);
						status_ = doClick(driver, by, urlValue, false, _options, urls);
						traverseChildren(parentNode, driver, urlValue, urls);
					}
					process(parentNode);
					break;
				case "default":
					
					if (locatortype.equals("CSSSelector")) {
						tn = tn + ":not([disabled])";
					}
					if (locatortype.equals("XPath")) {
						tn = tn + "[not(@disabled)]";
					}
					by = getBy(locatortype, tn);

					Boolean isExists = true;
					bu.backscroll0(driver);
					System.out.println(isExists);
					status_ = 0;
					int defcount = 1;
					while (isExists) {
						//traverseChildren(parentNode, driver, urlValue, urls);
						status_ = doClick(driver, by, urlValue, false, _options, urls);
						bu.waitForJSandJQueryToLoad(driver);
						traverseChildren(parentNode, driver, urlValue, urls);
						System.out.println("nextclicked " + defcount);
						if (status_ == 0 || defcount == 100) {
							System.out.println("no next button");
							stop_traversing = true;
							isExists = false;
							break;
						}
						defcount = defcount + 1;
						bu.backscroll0(driver);
						System.out.println(isExists);
					}
					process(parentNode);
					break;
				default:

				}
				// }

				// stop_traversing = true;
			} // if node

		}
       if ( action.startsWith(":")) {
    	     ObjectNode parentObjectNode = (ObjectNode) parentNode;
			 action= action.replace(":","");
			 parentObjectNode.put("actionName", action);
       }
		//if (!(stop_traversing)) {
			Iterator<JsonNode> eventElements = parentNode.get("children").iterator();
			           
			while (eventElements.hasNext()) {
				
				JsonNode node = eventElements.next();
				String ord = node.path("eventOrder").asText();
				String act = node.path("actionName").asText();
				String evn = node.path("name").asText();
				System.out.println(urlValue + "," + evn + "," + act + "," + ord);
			
				traverseTrace(node, driver, urlValue, urls);
			}
		//}
		
	}

	Predicate<RemoteWebDriver> pageLoaded = wd -> ((JavascriptExecutor) wd).executeScript("return document.readyState")
			.equals("complete");

	public void change_style(WebElement elem, RemoteWebDriver driver, String new_style) {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		// jse.executeScript("'arguments[0].{} = arguments[1]'.format('style')", elem,
		// new_style);
		// jse.executeScript("arguments[0].setAttribute('style', 'background:
		// arguments[1] ');", elem,new_style);
		jse.executeScript("arguments[0].setAttribute('style', arguments[1]);", elem, "");

	}

	@Override
	public void afterFindBy(By by, WebElement element, WebDriver driver) {
		((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid green'", element);
	}

	public void highLighterMethod(RemoteWebDriver driver, WebElement element) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].setAttribute('style', 'background: yellow; border: 3px solid red;');", element);
		try {
			Thread.sleep(300);
		} catch (InterruptedException ie) {
		}
	}

	public void doHover(RemoteWebDriver driver, By byhover) {
		// driver.manage().timeouts().implicitlyWait(10000, TimeUnit.MILLISECONDS);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		WebElement ele = driver.findElement(byhover);
		Actions builder = new Actions(driver);
		builder.moveToElement(ele).perform();
		System.out.println("Done Mouse hover on ");
		// By locator = By.id("clickElementID");
		// driver.click(locator);
	}

	public int _getCount(JsonNode parentNode, RemoteWebDriver driver) {
		String s = parentNode.path("repeat").path("until").path("selector").asText();
		String locator = parentNode.path("repeat").path("until").path("selectorType").asText();
		int how_many = getCount(getBy(locator, s), driver);
		return how_many;
	}

	public static void loadFrame(RemoteWebDriver driver, String frameName) {
		WebDriverWait wait = new WebDriverWait(driver, 10);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameName));
		// WebDriverWait(driver, 20).until(EC.element_to_be_clickable((By.XPATH,
		// "//button[@class='ytp-large-play-button ytp-button']"))).click();
		// new WebDriverWait(driver,
		// 10).until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@class='ytp-play-button
		// ytp-button']"))).click();
		JavascriptExecutor js = driver;
		Object player_status = js.executeScript("return document.getElementById('movie_player').getPlayerState()");

		// driver.switchTo().defaultContent();
		// youtubePlayer = driver.getElementById("movie_player");
		// youtubePlayer.getPlayerState();
	}
/*
	public void doBackNavigation(RemoteWebDriver driver, String url, String current) {

		String newurl = driver.getCurrentUrl().toString();
		System.out.println("new_url:" + newurl);
		if (!newurl.equals(url)) {
			driver.switchTo().window(current);
			String back_url = driver.getCurrentUrl().toString();
			System.out.println("back_url:" + back_url);
			if (!back_url.equals(url)) {
				driver.navigate().back();
				System.out.println("navigate_url:" + driver.getCurrentUrl().toString());
			}

		}
	}
*/
	public static void playHTML5(RemoteWebDriver driver, String urlValue, By by) {
		// {
		// "action_order":"2","value":"ytp-play-button","type":"CSSselector","action":"click"}]
		// List<WebElement> iframe = driver.findElements(By.tagName("iframe"));
		// driver.switchTo().frame(iframe.get(0));
		// System.out.println("iframe");
		System.out.println("video");
		// driver.execute_script('document.getElementsByTagName("video")[0].play()');
		// driver.get(urlValue);
		// driver.manage().timeouts().implicitlyWait(50,TimeUnit.SECONDS);
		// driver.getScreenshotAs("my_screenshot.png");
		// String length_str =
		// driver.findElementByClassName("ytp-time-duration").getText();
		// String current_time_str =
		// driver.findElementByClassName("ytp-time-current").getText();
		// if (length_str!=null) {
		// System.out.println("total length:"+length_str)
		// ;
		// }
		// WebElement video = getElementByLocator(By.tagName("video"), driver);

		WebElement video = null;

		List<WebElement> videos = getElementsByLocator(by, driver);
		if (videos != null) {
			video = videos.get(0);
		}

		String id = video.getAttribute("id");

		// WebElement video = driver.findElement(By.tagName("video"));
		JavascriptExecutor js = driver;

		js.executeScript("arguments[0].play();", video);
		// driver.manage().timeouts().implicitlyWait(120,TimeUnit.SECONDS);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// pausing the video
		js.executeScript("arguments[0].pause();", video);
		// driver.manage().timeouts().implicitlyWait(5,TimeUnit.SECONDS);

		js.executeScript("arguments[0].load();", video);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// playing the video
		// js.executeScript("arguments[0].play();", video);
		// driver.manage().timeouts().implicitlyWait(120,TimeUnit.SECONDS);

		// muting the video
		// js.executeScript("arguments[0].mute();", video);
		// try {
		// Thread.sleep(5000L);
		// } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// unmuting the video
		// js.executeScript("arguments[0].unMute();", video);
		// try {
		// Thread.sleep(5000L);
		// } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// Dragging the video
		js.executeScript("arguments[0].currentTime = 600;", video);
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		js.executeScript("alert(arguments[0].readyState);", video);
	}

	public static class MyEventListener extends AbstractWebDriverEventListener {

		@Override
		public void afterFindBy(By by, WebElement element, WebDriver driver) {
			((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid green'", element);
		}

	}
}
