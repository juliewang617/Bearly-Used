package edu.brown.cs.student.main.server.classes;

/**
 * A class representing an User object. Contains accessor methods to access the data stored in an
 * User object.
 */
public class User {

  public String clerkId;
  public String name;
  public String phoneNumber;
  public String school;

  public User(String clerkId, String name, String phoneNumber, String school) {
    this.clerkId = clerkId;
    this.name = name;
    this.phoneNumber = phoneNumber;
    this.school = school;
  }

  public String getClerkId() {
    return this.clerkId;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPhoneNumber() {
    return this.phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getSchool() {
    return this.school;
  }

  public void setSchool(String school) {
    this.school = school;
  }
}
