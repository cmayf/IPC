package edu.cs300;

public class SearchRequest {

  int requestID;
  String prefix;


  public SearchRequest(int reqID, String reqPrefix){
    this.requestID=reqID;
    this.prefix=reqPrefix;
  }

  public SearchRequest() {
	  this.requestID = -1;
	  this.prefix = "---";
  }

  public String toString(){
    return this.requestID+" "+this.prefix;
  }

  public int getRequestID() {
	  return this.requestID;
  }

  public String getRequestPrefix() {
	  return this.prefix;
  }

}
