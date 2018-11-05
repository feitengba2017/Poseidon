package model;

import java.util.List;

public class Category{
	private String dict_data_name;
	private String dict_data_value;
    private String image_url;
    private String memo;
    private List<Category> next_data;
    
	public String getDict_data_name() {
		return dict_data_name;
	}
	public void setDict_data_name(String dict_data_name) {
		this.dict_data_name = dict_data_name;
	}
	public String getDict_data_value() {
		return dict_data_value;
	}
	public void setDict_data_value(String dict_data_value) {
		this.dict_data_value = dict_data_value;
	}
	public String getImage_url() {
		return image_url;
	}
	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public List<Category> getNext_data() {
		return next_data;
	}
	public void setNext_data(List<Category> next_data) {
		this.next_data = next_data;
	}    
    	
}