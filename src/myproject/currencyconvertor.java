package myproject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.Date;

import org.json.*;

import java.sql.*;
import java.text.SimpleDateFormat;

public class currencyconvertor {	
	public static String fieldsTransactionString="(date, name, nationality, passportNo, currencyFrom, currencyTo, conversionRate, givenAmount, finalAmount)";
	
	public static String fieldsConverter="(date, conversionRate, currencyFrom, currencyTo)";
	
	public static void main(String[] args) {		
		Connection con=getConnection();
		
		Scanner s=new Scanner(System.in);
		System.out.println("Operation to perform?");
		System.out.println("1. View history");
		System.out.println("2. Insert data");

		System.out.println("Enter -1 to exit..");
		
		int input=s.nextInt();
		while(input!=-1) {
			if(input==1) {
				printTransaction(con);
				System.out.println();
				//printTable("conversionRates", con);
			}else if(input==2) {
				
				System.out.println("Enter name: ");
				String name=s.next();
				
				System.out.println("Enter nationality: ");
				String nationality=s.next();
				
				System.out.println("Enter Passport no: ");
				int passportNo=s.nextInt();
				
				System.out.println("Enter currency to convert from: ");
				String currencyFrom=s.next();
				
				System.out.println("Enter currency to convert to: ");
				String currencyTo=s.next();
				
				System.out.println("Enter amount to convert: ");
				BigDecimal amount=s.nextBigDecimal();
				currencyFrom=currencyFrom.toUpperCase();
				currencyTo=currencyTo.toUpperCase();
				
//				SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy"); 
//				String str = ft.format(new Date()); 
				
				try {
					Statement stmt = con.createStatement();
					String q="SELECT conversionRate FROM conversionRates WHERE date=curdate() and currencyFrom='"+currencyFrom+"' and currencyTo='"+currencyTo+"'";
					ResultSet rs=stmt.executeQuery(q);
						
					if(rs!=null && rs.getRow()!=0) 
						insertTransactionHistory(con, name, nationality, passportNo, currencyFrom, currencyTo, rs.getDouble(1), passportNo, rs.getDouble(1));
					else {
						int finalAmt=callAPI(currencyFrom, currencyTo, amount);
						insertTransactionHistory(con, name, nationality, passportNo, currencyFrom, currencyTo, finalAmt, passportNo, finalAmt);	
						insertConversionRates(con, finalAmt, currencyFrom, currencyTo);
					}
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			input=s.nextInt();
		}

		closeConnection(con);
	}
	
	public static int callAPI(String currencyFrom, String currencyTo, BigDecimal amount) {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://currency-convertor-api.p.rapidapi.com/convert/"+amount+"/"+currencyFrom.toUpperCase()+"/"+currencyTo.toUpperCase()))
				.header("x-rapidapi-key", "73e4c5f7f9mshbf966f4dff095e1p12bf8djsn7eea14496e66")
				.header("x-rapidapi-host", "currency-convertor-api.p.rapidapi.com")
				.method("GET", HttpRequest.BodyPublishers.noBody())
				.build();
		HttpResponse<String> response=null;
		try {
			response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(response.statusCode());
		System.out.println(response.body());
		String stringResponse=response.body().toString();
		
		JSONArray json=new JSONArray(stringResponse);		
		JSONObject jObj=(JSONObject)json.get(0);
		Object rate=jObj.get("rate");
		
		//BigDecimal rate=ratesObject.getBigDecimal(currencyTo.toUpperCase());
		
		//BigDecimal result=rate.multiply(amount);
		Integer rateValue=(Integer)rate;
		//return (Integer) rate;
		return rateValue;
	}
	
	public static Connection getConnection() {
		Connection con=null;
		Properties prop=new Properties();
		InputStream input=new FileInputStream("../../../global.properties");
		prop.load(input);

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");  
			con=(Connection)DriverManager.getConnection(prop.getProperty("DB_URL"),prop.getProperty("DB_USER"), prop.getProperty("DB_PASSWORD"));
			if(con!=null) System.out.println("database is connected");
			
		}catch (Exception e) {
			System.out.println("not connected");
		}
		return con;
	}
	
	public static void closeConnection(Connection con) {
		try {
			if (con!=null) con.close();
		}catch (Exception e) {
		}
	}
	
	public static boolean insertTransactionHistory(Connection con, String name, String nationality, int passportNo, String currencyFrom, String currencyTo, double conversionRate, double givenAmount, double finalAmount){
		try {
			
			Statement stmt=con.createStatement();   
			String q1="Insert into transactionHistory"+fieldsTransactionString+" VALUES(curdate(), '"+name+"','"+nationality+"',"+passportNo+",'"+currencyFrom+"','"+currencyTo+"',"+conversionRate+","+givenAmount+","+finalAmount+")";
			stmt.executeUpdate(q1);  
			//while(rs.next())  
			//System.out.println(rs.getInt(1)+"  "+rs.getString(2));  
			return true;

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Can't be inserted");
			return false;
		}
	}
	
	public static boolean insertConversionRates(Connection con, double conversionRate, String currencyFrom, String currencyTo) {
		try {
			Statement stmt=con.createStatement();
			String q="Insert into conversionRates"+fieldsConverter+" VALUES(curdate(), "+conversionRate+",'"+currencyFrom+"','"+currencyTo+"')";
			stmt.executeUpdate(q);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}
	
	public static void printTransaction(Connection con) {
		try {
			Statement stmt=con.createStatement();
			String q2="SELECT * FROM transactionHistory";
			ResultSet rSet=stmt.executeQuery(q2);
			while(rSet.next()) {
				System.out.println("Name: "+rSet.getString("name")+" Nationality: "+rSet.getString("nationality")+ " Passport No: "+rSet.getInt("passportNo")+" currencyFrom: "+rSet.getString("currencyFrom")+ " currencyTo: "+rSet.getString("currencyTo")+" givenAmount: "+rSet.getDouble("givenAmount")+" finalAmount: "+rSet.getDouble("finalAmount")+" conversionRate: "+rSet.getDouble("conversionRate"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public static void printConversion(Connection con) {
		try {
			Statement stmt=con.createStatement();
			String q2="SELECT * FROM conversionRates";
			ResultSet rSet=stmt.executeQuery(q2);
			while(rSet.next()) {
				System.out.println("Name: "+rSet.getString("name")+" Nationality: "+rSet.getString("nationality")+ " Passport No: "+rSet.getInt("passportNo")+" currencyFrom: "+rSet.getString("currencyFrom")+ " currencyTo: "+rSet.getString("currencyTo")+" givenAmount: "+rSet.getDouble("givenAmount")+" finalAmount: "+rSet.getDouble("finalAmount")+" conversionRate: "+rSet.getDouble("conversionRate"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
