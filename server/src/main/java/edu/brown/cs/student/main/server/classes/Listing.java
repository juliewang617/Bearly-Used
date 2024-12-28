package edu.brown.cs.student.main.server.classes;

import java.util.List;

/**
 * A class representing a Listing object. Contains accessor methods to access the data stored in a
 * Listing object.
 */
public class Listing {
  public Long id;
  public String seller_id;
  public String title;
  public String description;
  public Float price;
  public String category;
  public String condition;
  public String image_url;
  public List<String> tags;
  public boolean available;

  public Listing(
      Long id,
      String seller_id,
      String title,
      String description,
      Float price,
      String category,
      String condition,
      String img,
      List<String> tags,
      boolean avail) {
    this.id = id;
    this.seller_id = seller_id;
    this.title = title;
    this.description = description;
    this.price = price;
    this.category = category;
    this.condition = condition;
    this.image_url = img;
    this.tags = tags;
    this.available = avail;
  }

  public Long getId() {
    return id;
  }

  public Float getPrice() {
    return this.price;
  }

  public void setPrice(Float price) {
    this.price = price;
  }

  public String getSellerId() {
    return this.seller_id;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCategory() {
    return this.category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getCondition() {
    return this.condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public String getImageUrl() {
    return this.image_url;
  }

  public void setImageUrl(String image_url) {
    this.image_url = image_url;
  }

  public Boolean getAvailable() {
    return this.available;
  }

  public void setAvailable(boolean available) {
    this.available = available;
  }

  public List<String> getTags() {
    return this.tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }
}
