package model;

public class Building
{

	private String postcode, numName, street, locality, town, district, county;
	private int price;
	private String type, oldOrNew;
	private String dateOfSale;
	
	public Building(int price, String dateOfSale, String postcode, String type, String oldOrNew, String numName, String street, String locality, String town, String district, String county)
	{
		this.price = price;
		this.dateOfSale = dateOfSale;
		this.postcode = postcode;
		this.type = type;
		this.oldOrNew = oldOrNew;
		this.numName = numName;
		this.street = street;
		this.locality = locality;
		this.town = town;
		this.district = district;
		this.county = county;
	}

	public int getPrice() {
		return price;
	}

	public String getdateOfSale() {
		return dateOfSale;
	}

	public String getPostcode() {
		return postcode;
	}

	public String getType() {
		return type;
	}

	public String getOldOrNew() {
		return oldOrNew;
	}

	public String getnumName() {
		return numName;
	}

	public String getStreet() {
		return street;
	}

	public String getlocality() {
		return locality;
	}

	public String getTown() {
		return town;
	}

	public String getDistrict() {
		return district;
	}

	public String getCounty() {
		return county;
	}
	
	public String toString()
	{
		return Integer.toString(price) + ", " + dateOfSale + ", " + postcode + ", " + type + ", " + oldOrNew + ", " + numName + ", " + street + ", " + locality + ", " + town + ", " + district + ", " + county;
	}


}
