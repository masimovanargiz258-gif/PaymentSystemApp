package az.bank.paymentsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "ValCurs")
public class CbarRatesResponse {
    @JacksonXmlProperty(isAttribute = true,localName = "Date")
    private String date;
    @JacksonXmlProperty(isAttribute = true, localName = "Name")
    private String name;
    @JacksonXmlProperty(isAttribute = true, localName = "Description")
    private String description;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "ValType")
    private List<ValType> valTypes;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValType {
        @JacksonXmlProperty(isAttribute = true, localName = "Type")
        private String type;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "Valute")
        private List<Valute> valutes;
    }
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Valute {
        @JacksonXmlProperty(isAttribute = true, localName = "Code")
        private String code;

        @JacksonXmlProperty(localName = "Nominal")
        private String nominal;

        @JacksonXmlProperty(localName = "Name")
        private String name;

        @JacksonXmlProperty(localName = "Value")
        private BigDecimal value;
    }

}