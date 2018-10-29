package com.crawling.domain;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.crawling.service.InterParkCrawler;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "INTERPART_CRAWLING_DATA")
@NoArgsConstructor
@ToString
@Getter
@EqualsAndHashCode
public class InterParkData {
	@Id
	@GeneratedValue
	@Column(name = "INTERPARK_ID")
	private Long id;
	private String name;
	private String location;
	@Embedded
	private Address address;
	@Column(unique = true)
	private String interparkCode;

	private String imageFilePath;

	@Enumerated(EnumType.STRING)
	private DeleteFlag deleteflag = DeleteFlag.N;

	@Enumerated(EnumType.STRING)
	private InterparkType dtype;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "interpark")
	private List<Price> price = new ArrayList<>();

	private LocalDateTime startDate;
	private LocalDateTime endDate;

	@Transient
	private String addressUrl;

	@Transient
	private String date;

	@Transient
	private String groupCode;

	public InterParkData(Long id, String name, String location, InterparkType dtype) {
		this.id = id;
		this.name = name;
		this.location = location;
		this.dtype = dtype;
	}

	public InterParkData(Long id, String name, String location, InterparkType dtype, String addressUrl, String date,
			String groupCode) {
		super();
		this.id = id;
		this.name = name;
		this.location = location;
		this.dtype = dtype;
		this.addressUrl = addressUrl;
		this.date = date;
		this.groupCode = groupCode;
	}

	public InterParkData(Long id, String name, String location, Address address, String interparkCode,
			String imageFilePath, InterparkType dtype, List<Price> price, LocalDateTime startDate,
			LocalDateTime endDate, String addressUrl, String date, String groupCode) {
		super();
		this.id = id;
		this.name = name;
		this.location = location;
		this.address = address;
		this.interparkCode = interparkCode;
		this.imageFilePath = imageFilePath;
		this.dtype = dtype;
		this.price = price;
		this.startDate = startDate;
		this.endDate = endDate;
		this.addressUrl = addressUrl;
		this.date = date;
		this.groupCode = groupCode;
	}

	public void addStartDateAndEndDate() {
		String[] tm = this.date.split("~");
		String start = tm[0].replace(".", "-");
		String end = tm[1].replace(".", "-");
		this.startDate = LocalDate.parse(start.trim()).atStartOfDay();
		this.endDate = LocalDate.parse(end.trim()).atStartOfDay();
	}

	public void setDeleteflag(DeleteFlag deleteflag) {
		this.deleteflag = deleteflag;
	}

	public void addInterparkCode(String groupCode) {
		String pattern = "^*.*=";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(groupCode);
		this.interparkCode = m.replaceAll("");
	}

	public void addAddress() throws IOException {
		this.address = InterParkCrawler.findAddressByUrl(this.addressUrl);
	}

	public void addImageFilePath() throws IOException {
		this.imageFilePath = InterParkCrawler.saveImgFile("http://ticket.interpark.com/" + this.groupCode);
		;
	}

	// public void addPrice() {
	// ChromeOptions chromeOptions = new ChromeOptions();
	// chromeOptions.addArguments("--headless");
	// System.setProperty("webdriver.chrome.driver",
	// "src/main/resources/chromedriver.exe");
	// WebDriver driver = new ChromeDriver(chromeOptions);
	//
	// if(dtype.equals(InterparkType.Ex)) {
	// price = InterParkCrawling.findPriceDtypeEx(driver, this);
	// }else {
	// price = InterParkCrawling.findPrice(driver, this);
	// }
	// }

	public static void interparkConsumer(InterParkData m) {
		try {
			m.addAddress();
			m.addStartDateAndEndDate();
			m.addImageFilePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addPrice(Price price) {
		if (this.price != null && !this.price.contains(price)) {
			this.price.add(price);
		}
		price.setInterpark(this);
	}

	public void addPrice(List<Price> prices) {
		if(!this.price.containsAll(prices)) {
			this.price = prices;
		}
	}
}