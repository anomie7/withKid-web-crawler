package com.crawling.domain;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.*;

import com.crawling.service.InterParkCrawler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "INTERPART_CRAWLING_DATA")
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString
@Getter
@EqualsAndHashCode
public class InterParkContent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Builder.Default
    private DeleteFlag deleteflag = DeleteFlag.N;

    @Enumerated(EnumType.STRING)
    private InterparkType dtype;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "interpark")
    @Builder.Default
    private List<Price> price = new ArrayList<>();

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Transient
    private String addressUrl;

    @Transient
    private String date;

    @Transient
    private String groupCode;

    public InterParkContent(Long id, String name, String location, InterparkType dtype) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.dtype = dtype;
    }

    public InterParkContent(Long id, String name, String location, InterparkType dtype, String addressUrl, String date,
                            String groupCode) {
        super();
        this.deleteflag = DeleteFlag.N;
        this.id = id;
        this.name = name;
        this.location = location;
        this.dtype = dtype;
        this.addressUrl = addressUrl;
        this.date = date;
        this.groupCode = groupCode;
    }

    public static void addMembers(InterParkContent content) {
        try {
            content.addAddress();
            content.addStartDateAndEndDate();
            content.addImageFilePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addAddress() throws IOException {
        this.address = InterParkCrawler.findAddressByUrl(this.addressUrl);
    }

    public void addStartDateAndEndDate() {
        String[] tm = this.date.split("~");
        String start = tm[0].replace(".", "-");
        String end = tm[1].replace(".", "-");
        this.startDate = LocalDate.parse(start.trim()).atStartOfDay();
        this.endDate = LocalDate.parse(end.trim()).atStartOfDay();
    }

    public void addImageFilePath() throws IOException {
        String imageRootPath = "http://ticket.interpark.com/";
        this.imageFilePath = InterParkCrawler.saveImgFile(imageRootPath + this.groupCode);
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

    public void addPrice(Price price) {
        if (this.price == null) {
            this.price = new ArrayList<>();
        }

        if (this.price != null && !this.price.contains(price)) {
            this.price.add(price);
        }
        price.setInterpark(this);
    }

    public void addPrice(List<Price> prices) {
        if (!this.price.containsAll(prices)) {
            this.price = prices;
        }
    }
}
