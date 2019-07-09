package com.hirrr.jobsnatcher.Test;

/*
 * 
 * Program to read from a xlsx file and enter the details into the database.
 */


import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.hirrr.jobsnatcher.DBConnection.DataSource;


public class ExcelToDb {
 	static int c = 0 ;
 	static String name = "";
 
	private XSSFWorkbook myWorkBook;
    public static void main( String [] args ) throws IOException, SQLException {

    	
   		 String fileName="/home/umn/Downloads/newcompaniestoscrap.xlsx";

        ExcelToDb obj = new ExcelToDb();
        obj.readXlsxFile(fileName);
    }
    /**
     * Reading Xlsx
     * @param fileName
     * @return
     * @throws IOException 
     * @throws SQLException 
     */
    
	public void readXlsxFile(String fileName) throws IOException, SQLException{
		ArrayList<String> cellHolderList = new ArrayList<String>();
            FileInputStream myInput = new FileInputStream(fileName);
			myWorkBook = new XSSFWorkbook(myInput);
			
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);
            Iterator<Row> rowIter = mySheet.iterator();
		

            while(rowIter.hasNext()){
            	 
                Row myRow = rowIter.next();
//               Iterator<Cell> cellIter = myRow.iterator();
                for(Integer i = 0; i< 7 ; i++){
                	cellHolderList.add(myRow.getCell(i) + "");
                }
                addDataToDB(cellHolderList);
                cellHolderList.clear();
            }
            
           
    }

	private void addDataToDB(ArrayList<String> list) throws SQLException, IOException{
		
		String sl_no = "";
		String companyname ="";
        String companyurl = "";
        String pagetype = "";
        String nxtpage = "";
        String email = "";
        String titlematch = "";
		
        
        
        PreparedStatement st = null;
   
        DataSource ds = null;
		Connection con = null;
		try {
			ds = DataSource.getInstance();
			con =  ds.getConnection();
		} catch (Exception e2) {
			e2.printStackTrace();
		}	
			sl_no = list.get(0).toString().trim();
			companyname  = list.get(1).toString().trim(); 
			companyurl  = list.get(2).toString().trim();
			pagetype = list.get(3).toString().trim();
			nxtpage = list.get(4).toString().trim();
			email = list.get(5).toString().trim();  
			titlematch = list.get(6).toString().trim();
		
			
            try {
            	String sql11 = " insert into Next_Page_Case (companyname, companyurl, pagetype , nxtpage, email , titlematch )"
            			+ " values ( ?, ?, ?, ? , ? ,? )";
            	  	
            	st=con.prepareStatement(sql11);
            	st.setString(1, sl_no);
            	st.setString(2, companyname);
            	st.setString(3, companyurl);
            	st.setString(4, pagetype);
            	st.setString(5, nxtpage);
            	st.setString(6, email);  
            	st.setString(7, titlematch);
                int a = st.executeUpdate();
                if(a > 0){
					System.out.println("Inserted to Database > " + companyname);
                }

            } catch (SQLException e) {
            	System.out.println("Exception in inserting values to Database ...." );
            	System.out.println(e.getMessage());
            	System.out.println(e.getCause());
		}
            st.close();
            con.close();
    }

    }